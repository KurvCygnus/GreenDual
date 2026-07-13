package com.wenhua.tcpservice.tcp;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.mapper.MonitorRecordMapper;
import com.wenhua.tcpservice.pojo.MonitorRecord;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.utils.StringFormatter;
import com.wenhua.tcpservice.websocket.WebSocketSessionManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.atomic.AtomicInteger;

@Component @Slf4j
public final class TCPServer implements CommandLineRunner
{
    private final EnvMapper envMapper;
    private final MonitorRecordMapper monitorRecordMapper;

    private static final Map<String, Socket> DEVICE_SOCKETS = new ConcurrentHashMap<>();
    private static final Map<String, PrintWriter> DEVICE_WRITERS = new ConcurrentHashMap<>();
    private static final Map<String, Long> DEVICE_LAST_ACTIVITY = new ConcurrentHashMap<>();

    @Value("${tcp.heartbeat.timeout}")
    private long heartbeatTimeoutSeconds;

    @Value("${tcp.heartbeat.interval}")
    private long heartbeatIntervalSeconds;

    private final ExecutorService executorService;
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(
        r ->
        {
            final var thread = new Thread(r, "tcp-heartbeat");
            thread.setDaemon(true);
            return thread;
        }
    );

    private volatile ServerSocket serverSocket;
    private volatile boolean running = false;
    
    /**
     * @implNote 该构造方法不可被重构或优化.<br>
     * 由于被 <u>{@link Value @Value}</u> 注解的字段是 <b>类本身先被构造, 字段再被注入</b> 的生命周期设计, 将 {@code coreSize}, {@code maxSize}, {@code keepAlive} 和
     * {@code queueCapacity} 设计成字段会导致 <span style="color: f84b4b">这些字段全部在未被初始化时被使用, 程序崩溃</span>, 而将这些字段设计成构造参数, 使用 <u>{@link Value @Value}</u>
     * 的注入生命周期是更早, 更稳定的, 因此这种实现是安全的.
     */
    public TCPServer(
        @NonNull EnvMapper envMapper,
        @NonNull MonitorRecordMapper monitorRecordMapper,
        @Value("${tcp.thread.pool.core-size}") int coreSize,
        @Value("${tcp.thread.pool.max-size}") int maxSize,
        @Value("${tcp.thread.pool.keep-alive}") int keepAlive,
        @Value("${tcp.thread.pool.queue-capacity}") int queueCapacity
    )
    {
        this.envMapper = envMapper;
        this.monitorRecordMapper = monitorRecordMapper;
        this.executorService = new ThreadPoolExecutor(
            coreSize,
            maxSize,
            keepAlive,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            new TCPThreadFactory(),
            new CallerRunsPolicy()
        );
    }

    @Override public void run(String... args)
    {
        final var acceptThread = new Thread(
            () ->
            {
                try
                {
                    serverSocket = new ServerSocket(GlobalConfiguration.TCP_PORT);
                    running = true;
                    log.info("TCP服务端启动，端口: {}", GlobalConfiguration.TCP_PORT);

                    // 启动心跳检测
                    heartbeatExecutor.scheduleAtFixedRate(
                        this::checkDeviceHeartbeats,
                        heartbeatIntervalSeconds,
                        heartbeatIntervalSeconds,
                        TimeUnit.SECONDS
                    );

                    while(running)
                        try
                        {
                            final var clientSocket = serverSocket.accept();
                            log.debug("新的设备连接: {}", clientSocket.getInetAddress().getHostAddress());
                            executorService.submit(() -> this.handleClient(clientSocket));
                        }
                        catch(IOException e) { if(running) log.error("接受设备连接失败: {}", e.getMessage()); }
                }
                catch (IOException e) { log.error("TCP服务端启动失败: {}", e.getMessage()); }
            },
            "tcp-accept"
        );

        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    private void handleClient(@NonNull Socket socket)
    {
        final var deviceIp = socket.getInetAddress().getHostAddress();
        String deviceId = null;

        try(
            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final var out = new PrintWriter(socket.getOutputStream(), true)
        )
        {
            socket.setSoTimeout(0);
            String inputLine;

            while((inputLine = in.readLine()) != null)
            {
                // 跳过空行
                if(inputLine.isBlank())
                    continue;

                log.debug("收到设备数据: {}", inputLine);

                try
                {
                    final var json = new JSONObject(inputLine);
                    final var returnIntent = json.optString("returnIntent", "");
                    deviceId = json.optString("id", "");

                    if(!deviceId.isEmpty())
                    {
                        DEVICE_SOCKETS.put(deviceId, socket);
                        DEVICE_WRITERS.put(deviceId, out);
                        DEVICE_LAST_ACTIVITY.put(deviceId, System.currentTimeMillis());
                    }

                    switch(returnIntent)
                    {
                        case "online" -> handleDeviceOnline(json, deviceIp, out);
                        case "REPORT" -> handleDeviceReport(json, deviceIp);
                        case "SET" -> handleDeviceSetResponse(json);
                        default -> sendCommand(out, "cmd", "REPORT");
                    }
                }
                catch (Exception e)
                {
                    log.error("解析JSON失败: {}", e.getMessage());
                    out.println("{\"status\":\"error\",\"message\":\"Invalid JSON\"}");
                }
            }
        }
        catch(IOException e) { log.debug("设备断开连接: {}", deviceIp); }
        finally
        {
            if(deviceId != null)
            {
                //noinspection resource
                DEVICE_SOCKETS.remove(deviceId);
                //noinspection resource
                DEVICE_WRITERS.remove(deviceId);
                DEVICE_LAST_ACTIVITY.remove(deviceId);
                updateDeviceOffline(deviceId);
            }
        }
    }

    /**
     * 心跳检测：检查所有设备是否在超时时间内有数据活动，
     * 超时设备强制关闭连接并清理资源。
     */
    private void checkDeviceHeartbeats()
    {
        final var now = System.currentTimeMillis();
        final var timeoutMs = heartbeatTimeoutSeconds * 1000;

        DEVICE_LAST_ACTIVITY.forEach(
            (deviceId, lastTime) ->
            {
                if(now - lastTime > timeoutMs)
                {
                    log.warn("设备心跳超时，准备清理: {}", deviceId);
                    cleanupStaleDevice(deviceId);
                }
            }
        );
    }

    private void cleanupStaleDevice(String deviceId)
    {
        DEVICE_LAST_ACTIVITY.remove(deviceId);

        final var socket = DEVICE_SOCKETS.remove(deviceId);
        if(socket != null)
            try { socket.close(); }
            catch(IOException ignored) {}

        DEVICE_WRITERS.remove(deviceId);
        updateDeviceOffline(deviceId);
    }

    private void handleDeviceOnline(JSONObject json, String ip, PrintWriter out)
    {
        final var deviceId = json.optString("id", "");
        log.debug("设备上线: {}, IP: {}", deviceId, ip);

        var device = envMapper.selectDeviceById(deviceId);
        if(device == null)
        {
            device = new Device();
            device.setId(deviceId);
            device.setName(StringFormatter.format("环境监测设备-{}", deviceId));
            device.setIp(ip);
            device.setOnLine(1);
            device.setDeviceType("环境监测");
            device.setTemperature(25F);
            device.setHumidity(60);
            device.setLightIntensity(10000);
            //初始化路灯相关字段
            device.setLampStatus(json.optInt("lampStatus", 0));
            device.setBrightness(json.optInt("brightness", 80));
            device.setFaultStatus(0);
            device.setPowerConsumption(0F);
            envMapper.insertDevice(device);
        }
        else
        {
            device.setIp(ip);
            device.setOnLine(1);
            //更新路灯相关字段
            if(json.has("lampStatus"))
                device.setLampStatus(json.optInt("lampStatus", device.getLampStatus()));
            if(json.has("brightness"))
                device.setBrightness(json.optInt("brightness", device.getBrightness()));
            envMapper.updateDevice(device);
        }

        sendCommand(out, "cmd", "REPORT");
    }

    private void handleDeviceReport(JSONObject json, String ip)
    {
        final var deviceId = json.optString("id", "");

        var device = envMapper.selectDeviceById(deviceId);
        if(device == null)
        {
            device = new Device();
            device.setId(deviceId);
            device.setName(StringFormatter.format("环境监测设备-{}", deviceId));
            device.setDeviceType("环境监测");
        }

        device.setIp(json.optString("ip", ip));
        device.setOnLine(json.optInt("onlineStatus", 1));
        
        if(json.has("temperature"))
            device.setTemperature((float) json.optDouble("temperature", 25.));
        if(json.has("humidity"))
            device.setHumidity(json.optInt("humidity", 60));
        if(json.has("lightIntensity"))
            device.setLightIntensity(json.optInt("lightIntensity", 10000));
        
        //处理路灯相关字段
        if(json.has("lampStatus"))
            device.setLampStatus(json.optInt("lampStatus", 0));
        if(json.has("brightness"))
            device.setBrightness(json.optInt("brightness", 80));
        if(json.has("faultStatus"))
            device.setFaultStatus(json.optInt("faultStatus", 0));
        if(json.has("powerConsumption"))
            device.setPowerConsumption((float) json.optDouble("powerConsumption", 0.));
        
        if(envMapper.selectDeviceById(deviceId) == null)
            envMapper.insertDevice(device);
        else
            envMapper.updateDevice(device);

        // 写入监测记录
        final var now = LocalDateTime.now();
        final var deviceName = device.getName() != null ? device.getName() : deviceId;
        if(json.has("temperature"))
            monitorRecordMapper.insertRecord(new MonitorRecord(
                null, deviceId, deviceName, now, "temperature",
                json.optDouble("temperature", 25.), "normal", null, null
            ));
        if(json.has("humidity"))
            monitorRecordMapper.insertRecord(new MonitorRecord(
                null, deviceId, deviceName, now, "humidity",
                (double) json.optInt("humidity", 60), "normal", null, null
            ));
        if(json.has("lightIntensity"))
            monitorRecordMapper.insertRecord(new MonitorRecord(
                null, deviceId, deviceName, now, "lightIntensity",
                (double) json.optInt("lightIntensity", 10000), "normal", null, null
            ));

        // 通过 WebSocket 广播实时数据
        final var broadcastJson = new JSONObject();
        broadcastJson.put("type", "deviceReport");
        broadcastJson.put("deviceId", deviceId);
        broadcastJson.put("deviceName", deviceName);
        broadcastJson.put("timestamp", now.toString());
        if(json.has("temperature")) broadcastJson.put("temperature", json.optDouble("temperature", 25.));
        if(json.has("humidity")) broadcastJson.put("humidity", json.optInt("humidity", 60));
        if(json.has("lightIntensity")) broadcastJson.put("lightIntensity", json.optInt("lightIntensity", 10000));
        if(json.has("lampStatus")) broadcastJson.put("lampStatus", json.optInt("lampStatus", 0));
        if(json.has("brightness")) broadcastJson.put("brightness", json.optInt("brightness", 80));
        WebSocketSessionManager.broadcast(broadcastJson.toString());

        log.debug(
            "设备数据更新: {}, 温度: {}, 湿度: {}, 光照: {}, 路灯状态: {}, 亮度: {}",
            deviceId,
            device.getTemperature(),
            device.getHumidity(),
            device.getLightIntensity(),
            device.getLampStatus(),
            device.getBrightness()
        );
    }

    private void handleDeviceSetResponse(JSONObject json)
    {
        final var deviceId = json.optString("id", "");
        log.debug("设备设置响应: {}", deviceId);
    }

    private void updateDeviceOffline(String deviceId)
    {
        final var device = envMapper.selectDeviceById(deviceId);
        if(device != null)
        {
            device.setOnLine(0);
            envMapper.updateDevice(device);
            log.debug("设备离线: {}", deviceId);
        }
    }

    @PreDestroy public void destroy()
    {
        log.info("TCP服务正在关闭...");
        running = false;

        // 关闭 ServerSocket，中断 accept 循环
        if(serverSocket != null && !serverSocket.isClosed())
            try { serverSocket.close(); }
            catch(IOException e) { log.warn("关闭ServerSocket异常: {}", e.getMessage()); }

        // 关闭所有设备连接
        DEVICE_SOCKETS.forEach(
            (deviceId, socket) ->
            {
                try { socket.close(); }
                catch (IOException ignored) {}
            }
        );
        DEVICE_SOCKETS.clear();
        DEVICE_WRITERS.clear();
        DEVICE_LAST_ACTIVITY.clear();

        // 关闭线程池
        shutdownExecutor(executorService, "handler");
        shutdownExecutor(heartbeatExecutor, "heartbeat");

        log.info("TCP服务已关闭");
    }

    private void shutdownExecutor(ExecutorService executor, String name)
    {
        executor.shutdown();
        try
        {
            if(!executor.awaitTermination(5, TimeUnit.SECONDS))
            {
                log.warn("{}线程池未在5秒内关闭，强制终止", name);
                executor.shutdownNow();
            }
        }
        catch (InterruptedException e)
        {
            log.warn("{}线程池关闭被中断", name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void sendCommand(PrintWriter out, String key, String value)
    {
        final var cmd = new JSONObject();
        cmd.put(key, value);
        out.println(cmd);
    }

    public static boolean sendCommandToDevice(@Nonnull String deviceId, @Nonnull String command)
    {
        final var writer = DEVICE_WRITERS.get(deviceId);
        if(writer != null)
        {
            writer.println(command);
            log.debug("发送命令到设备 {}: {}", deviceId, command);
            return true;
        }
        log.error("设备 {} 未连接", deviceId);
        return false;
    }

    public static boolean isDeviceConnected(@Nonnull String deviceId) { return DEVICE_SOCKETS.containsKey(deviceId); }

    public static void setDeviceAutoMonitor(@Nonnull String deviceId, boolean autoOn)
    {
        final var cmd = new JSONObject();
        cmd.put("cmd", autoOn ? "AUTO_ON" : "AUTO_OFF");
        sendCommandToDevice(deviceId, cmd.toString());
    }

    public static void setDeviceReportInterval(@Nonnull String deviceId, int interval)
    {
        final var cmd = new JSONObject();
        cmd.put("cmd", "SET");
        cmd.put("interval", interval);
        sendCommandToDevice(deviceId, cmd.toString());
    }

    public static void requestDeviceReport(@Nonnull String deviceId)
    {
        final var cmd = new JSONObject();
        cmd.put("cmd", "REPORT");
        sendCommandToDevice(deviceId, cmd.toString());
    }

    public static boolean turnOnLamp(@Nonnull String deviceId)
    {
        final var cmd = new JSONObject();
        cmd.put("cmd", "LAMP_ON");
        return sendCommandToDevice(deviceId, cmd.toString());
    }

    public static boolean turnOffLamp(@Nonnull String deviceId)
    {
        final var cmd = new JSONObject();
        cmd.put("cmd", "LAMP_OFF");
        return sendCommandToDevice(deviceId, cmd.toString());
    }

    public static boolean setLampBrightness(@Nonnull String deviceId, int brightness)
    {
        final var cmd = new JSONObject();
        cmd.put("cmd", "LAMP_BRIGHTNESS");
        cmd.put("brightness", brightness);
        return sendCommandToDevice(deviceId, cmd.toString());
    }
}

final class TCPThreadFactory implements ThreadFactory
{
    private final AtomicInteger counter = new AtomicInteger(1);
    
    @Override public @NonNull Thread newThread(@NonNull Runnable r)
    {
        final var thread = new Thread(r, "tcp-handler-" + counter.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
