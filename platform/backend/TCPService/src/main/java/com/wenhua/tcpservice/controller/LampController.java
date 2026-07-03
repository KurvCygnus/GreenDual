package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.service.EnvService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@RequestMapping("/lamp")
@AllArgsConstructor
public class LampController {

    private EnvService envService;
    private EnvMapper envMapper;

    /**
     * 获取所有路灯列表
     */
    @GetMapping("/list")
    public Result getLampList() {
        log.debug("获取路灯列表请求");
        List<Device> lamps = envMapper.selectLamps();
        return Result.success(lamps);
    }

    /**
     * 控制路灯开关
     */
    @PostMapping("/control")
    public Result controlLamp(@RequestBody Map<String, Object> params) {
        String deviceId = (String) params.get("deviceId");
        Integer status = (Integer) params.get("status");
        
        log.debug("路灯控制请求: deviceId = {}, status = {}", deviceId, status);
        
        if (deviceId == null || status == null) {
            return Result.error("参数错误");
        }

        Device device = envMapper.selectDeviceById(deviceId);
        if (device == null) {
            return Result.error("设备不存在");
        }

        // 更新路灯状态
        device.setLampStatus(status);
        device.setBrightness(status == 1 ? 80 : 0); // 开灯默认亮度80
        
        boolean success = envService.updateDevice(device);
        if (success) {
            log.debug("路灯控制成功: {}", deviceId);
            return Result.success("操作成功");
        } else {
            return Result.error("操作失败");
        }
    }

    /**
     * 调节路灯亮度
     */
    @PostMapping("/brightness")
    public Result adjustBrightness(@RequestBody Map<String, Object> params) {
        String deviceId = (String) params.get("deviceId");
        Integer brightness = (Integer) params.get("brightness");
        
        log.debug("调节亮度请求: deviceId = {}, brightness = {}", deviceId, brightness);
        
        if (deviceId == null || brightness == null) {
            return Result.error("参数错误");
        }

        if (brightness < 0 || brightness > 100) {
            return Result.error("亮度范围应为0-100");
        }

        Device device = envMapper.selectDeviceById(deviceId);
        if (device == null) {
            return Result.error("设备不存在");
        }

        device.setBrightness(brightness);
        if (brightness > 0) {
            device.setLampStatus(1);
        } else {
            device.setLampStatus(0);
        }
        
        boolean success = envService.updateDevice(device);
        if (success) {
            return Result.success("亮度调节成功");
        } else {
            return Result.error("亮度调节失败");
        }
    }

    /**
     * 批量控制路灯
     */
    @PostMapping("/batchControl")
    public Result batchControl(@RequestBody Map<String, Object> params) {
        List<String> deviceIds = (List<String>) params.get("deviceIds");
        Integer status = (Integer) params.get("status");
        
        log.debug("批量控制路灯请求: deviceIds = {}, status = {}", deviceIds, status);
        
        if (deviceIds == null || status == null) {
            return Result.error("参数错误");
        }

        int successCount = 0;
        for (String deviceId : deviceIds) {
            Device device = envMapper.selectDeviceById(deviceId);
            if (device != null) {
                device.setLampStatus(status);
                device.setBrightness(status == 1 ? 80 : 0);
                if (envService.updateDevice(device)) {
                    successCount++;
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("totalCount", deviceIds.size());
        
        return Result.success(result);
    }

    /**
     * 获取路灯统计信息
     */
    @GetMapping("/stats")
    public Result getLampStats() {
        log.debug("获取路灯统计请求");
        List<Device> lamps = envMapper.selectLamps();
        
        int totalCount = lamps.size();
        int onlineCount = 0;
        int onCount = 0;
        int offCount = 0;
        int faultCount = 0;
        double totalPower = 0;
        
        for (Device lamp : lamps) {
            if (lamp.getOnLine() != null && lamp.getOnLine() == 1) {
                onlineCount++;
            }
            
            // 检查是否为故障路灯（同时检查faultStatus和isBroken字段）
            boolean isFault = (lamp.getFaultStatus() != null && lamp.getFaultStatus() == 1) || (lamp.getIsBroken() == 1);
            if (isFault) {
                faultCount++;
            } else {
                // 只有非故障路灯才统计开启/关闭状态
                if (lamp.getLampStatus() != null && lamp.getLampStatus() == 1) {
                    onCount++;
                    // 只计算开启状态的路灯功耗
                    if (lamp.getPowerConsumption() != null) {
                        totalPower += lamp.getPowerConsumption();
                    }
                } else {
                    offCount++;
                }
            }
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("onlineCount", onlineCount);
        stats.put("offlineCount", totalCount - onlineCount);
        stats.put("onCount", onCount);
        stats.put("offCount", offCount);
        stats.put("faultCount", faultCount);
        stats.put("totalPower", String.format("%.1f", totalPower));
        
        return Result.success(stats);
    }

    /**
     * 获取故障路灯列表
     */
    @GetMapping("/faults")
    public Result getFaultLamps() {
        log.debug("获取故障路灯请求");
        List<Device> faultLamps = envMapper.selectFaultLamps();
        return Result.success(faultLamps);
    }

    /**
     * 智能调光 - 根据环境光照自动调节
     * 支持单个路灯调光（deviceId）和批量调光（deviceIds）
     */
    @PostMapping("/smartAdjust")
    public Result smartAdjust(@RequestBody Map<String, Object> params) {
        String deviceId = (String) params.get("deviceId");
        List<String> deviceIds = (List<String>) params.get("deviceIds");
        Integer ambientLight = (Integer) params.get("ambientLight");
        
        log.debug("智能调光请求: deviceId = {}, deviceIds = {}, ambientLight = {}", deviceId, deviceIds, ambientLight);
        
        if (ambientLight == null) {
            return Result.error("缺少环境光照参数");
        }

        // 智能调光算法：根据环境光照计算亮度
        int brightness;
        if (ambientLight < 1000) {
            brightness = 100;
        } else if (ambientLight < 5000) {
            brightness = 80;
        } else if (ambientLight < 10000) {
            brightness = 60;
        } else if (ambientLight < 20000) {
            brightness = 40;
        } else {
            brightness = 20;
        }

        // 批量调光
        if (deviceIds != null && !deviceIds.isEmpty()) {
            int successCount = 0;
            for (String id : deviceIds) {
                Device device = envMapper.selectDeviceById(id);
                if (device != null && device.getOnLine() == 1) {
                    device.setBrightness(brightness);
                    device.setLampStatus(brightness > 0 ? 1 : 0);
                    if (envService.updateDevice(device)) {
                        successCount++;
                    }
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("brightness", brightness);
            result.put("ambientLight", ambientLight);
            result.put("successCount", successCount);
            result.put("totalCount", deviceIds.size());
            return Result.success(result);
        }

        // 单个调光
        if (deviceId == null) {
            return Result.error("缺少设备ID参数");
        }

        Device device = envMapper.selectDeviceById(deviceId);
        if (device == null) {
            return Result.error("设备不存在");
        }

        device.setBrightness(brightness);
        device.setLampStatus(brightness > 0 ? 1 : 0);
        
        boolean success = envService.updateDevice(device);
        
        Map<String, Object> result = new HashMap<>();
        result.put("brightness", brightness);
        result.put("ambientLight", ambientLight);
        
        if (success) {
            return Result.success(result);
        } else {
            return Result.error("调光失败");
        }
    }
}
