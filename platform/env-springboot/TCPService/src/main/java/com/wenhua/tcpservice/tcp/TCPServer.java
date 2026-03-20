package com.wenhua.tcpservice.tcp;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.utils.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TCPServer implements CommandLineRunner {

    @Autowired
    private EnvMapper envMapper;

    private static final Map<String, Socket> deviceSockets = new ConcurrentHashMap<>();
    private static final Map<String, PrintWriter> deviceWriters = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void run(String... args) throws Exception {
        startServer();
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(GlobalConfiguration.TCP_PORT)) {
                Log.d("TCP服务端启动，端口: " + GlobalConfiguration.TCP_PORT);
                
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    Log.d("新的设备连接: " + clientSocket.getInetAddress().getHostAddress());
                    executorService.submit(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
                Log.e("TCP服务端启动失败: " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        String deviceIp = socket.getInetAddress().getHostAddress();
        String deviceId = null;
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            socket.setSoTimeout(0);
            String inputLine;
            
            while ((inputLine = in.readLine()) != null) {
                Log.d("收到设备数据: " + inputLine);
                
                try {
                    JSONObject json = new JSONObject(inputLine);
                    String returnIntent = json.optString("returnIntent", "");
                    deviceId = json.optString("id", "");
                    
                    if (!deviceId.isEmpty()) {
                        deviceSockets.put(deviceId, socket);
                        deviceWriters.put(deviceId, out);
                    }
                    
                    switch (returnIntent) {
                        case "online":
                            handleDeviceOnline(json, deviceIp, out);
                            break;
                        case "REPORT":
                            handleDeviceReport(json, deviceIp);
                            break;
                        case "SET":
                            handleDeviceSetResponse(json);
                            break;
                        default:
                            sendCommand(out, "cmd", "REPORT");
                    }
                } catch (Exception e) {
                    Log.e("解析JSON失败: " + e.getMessage());
                    out.println("{\"status\":\"error\",\"message\":\"Invalid JSON\"}");
                }
            }
        } catch (IOException e) {
            Log.d("设备断开连接: " + deviceIp);
        } finally {
            if (deviceId != null) {
                deviceSockets.remove(deviceId);
                deviceWriters.remove(deviceId);
                updateDeviceOffline(deviceId);
            }
        }
    }

    private void handleDeviceOnline(JSONObject json, String ip, PrintWriter out) {
        String deviceId = json.optString("id", "");
        Log.d("设备上线: " + deviceId + ", IP: " + ip);
        
        Device device = envMapper.selectDeviceById(deviceId);
        if (device == null) {
            device = new Device();
            device.setId(deviceId);
            device.setName("环境监测设备-" + deviceId);
            device.setIp(ip);
            device.setOnLine(1);
            device.setDeviceType("环境监测");
            device.setTemperature(25.0f);
            device.setHumidity(60);
            device.setLightIntensity(10000);
            //初始化路灯相关字段
            device.setLampStatus(json.optInt("lampStatus", 0));
            device.setBrightness(json.optInt("brightness", 80));
            device.setFaultStatus(0);
            device.setPowerConsumption(0.0f);
            envMapper.insertDevice(device);
        } else {
            device.setIp(ip);
            device.setOnLine(1);
            //更新路灯相关字段
            if (json.has("lampStatus")) {
                device.setLampStatus(json.optInt("lampStatus", device.getLampStatus()));
            }
            if (json.has("brightness")) {
                device.setBrightness(json.optInt("brightness", device.getBrightness()));
            }
            envMapper.updateDevice(device);
        }
        
        sendCommand(out, "cmd", "REPORT");
    }

    private void handleDeviceReport(JSONObject json, String ip) {
        String deviceId = json.optString("id", "");
        
        Device device = envMapper.selectDeviceById(deviceId);
        if (device == null) {
            device = new Device();
            device.setId(deviceId);
            device.setName("环境监测设备-" + deviceId);
            device.setDeviceType("环境监测");
        }
        
        device.setIp(json.optString("ip", ip));
        device.setOnLine(json.optInt("onlineStatus", 1));
        
        if (json.has("temperature")) {
            device.setTemperature((float) json.optDouble("temperature", 25.0));
        }
        if (json.has("humidity")) {
            device.setHumidity(json.optInt("humidity", 60));
        }
        if (json.has("lightIntensity")) {
            device.setLightIntensity(json.optInt("lightIntensity", 10000));
        }
        
        //处理路灯相关字段
        if (json.has("lampStatus")) {
            device.setLampStatus(json.optInt("lampStatus", 0));
        }
        if (json.has("brightness")) {
            device.setBrightness(json.optInt("brightness", 80));
        }
        if (json.has("faultStatus")) {
            device.setFaultStatus(json.optInt("faultStatus", 0));
        }
        if (json.has("powerConsumption")) {
            device.setPowerConsumption((float) json.optDouble("powerConsumption", 0.0));
        }
        
        if (envMapper.selectDeviceById(deviceId) == null) {
            envMapper.insertDevice(device);
        } else {
            envMapper.updateDevice(device);
        }
        
        Log.d("设备数据更新: " + deviceId + ", 温度: " + device.getTemperature() + 
              ", 湿度: " + device.getHumidity() + ", 光照: " + device.getLightIntensity() +
              ", 路灯状态: " + device.getLampStatus() + ", 亮度: " + device.getBrightness());
    }

    private void handleDeviceSetResponse(JSONObject json) {
        String deviceId = json.optString("id", "");
        Log.d("设备设置响应: " + deviceId);
    }

    private void updateDeviceOffline(String deviceId) {
        Device device = envMapper.selectDeviceById(deviceId);
        if (device != null) {
            device.setOnLine(0);
            envMapper.updateDevice(device);
            Log.d("设备离线: " + deviceId);
        }
    }

    private void sendCommand(PrintWriter out, String key, String value) {
        JSONObject cmd = new JSONObject();
        cmd.put(key, value);
        out.println(cmd.toString());
    }

    public static boolean sendCommandToDevice(String deviceId, String command) {
        PrintWriter writer = deviceWriters.get(deviceId);
        if (writer != null) {
            writer.println(command);
            Log.d("发送命令到设备 " + deviceId + ": " + command);
            return true;
        }
        Log.e("设备 " + deviceId + " 未连接");
        return false;
    }

    public static boolean isDeviceConnected(String deviceId) {
        return deviceSockets.containsKey(deviceId);
    }

    public static void setDeviceAutoMonitor(String deviceId, boolean autoOn) {
        JSONObject cmd = new JSONObject();
        cmd.put("cmd", autoOn ? "AUTO_ON" : "AUTO_OFF");
        sendCommandToDevice(deviceId, cmd.toString());
    }

    public static void setDeviceReportInterval(String deviceId, int interval) {
        JSONObject cmd = new JSONObject();
        cmd.put("cmd", "SET");
        cmd.put("interval", interval);
        sendCommandToDevice(deviceId, cmd.toString());
    }

    public static void requestDeviceReport(String deviceId) {
        JSONObject cmd = new JSONObject();
        cmd.put("cmd", "REPORT");
        sendCommandToDevice(deviceId, cmd.toString());
    }

    public static boolean turnOnLamp(String deviceId) {
        JSONObject cmd = new JSONObject();
        cmd.put("cmd", "LAMP_ON");
        return sendCommandToDevice(deviceId, cmd.toString());
    }

    public static boolean turnOffLamp(String deviceId) {
        JSONObject cmd = new JSONObject();
        cmd.put("cmd", "LAMP_OFF");
        return sendCommandToDevice(deviceId, cmd.toString());
    }

    public static boolean setLampBrightness(String deviceId, int brightness) {
        JSONObject cmd = new JSONObject();
        cmd.put("cmd", "LAMP_BRIGHTNESS");
        cmd.put("brightness", brightness);
        return sendCommandToDevice(deviceId, cmd.toString());
    }
}
