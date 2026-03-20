package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.service.EnvService;
import com.wenhua.tcpservice.utils.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@RequestMapping("/lamp")
public class LampController {

    @Autowired
    private EnvService envService;

    @Autowired
    private EnvMapper envMapper;

    /**
     * 获取所有路灯列表
     */
    @GetMapping("/list")
    public Result getLampList() {
        Log.d("获取路灯列表请求");
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
        
        Log.d("路灯控制请求: deviceId=" + deviceId + ", status=" + status);
        
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
            Log.d("路灯控制成功: " + deviceId);
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
        
        Log.d("调节亮度请求: deviceId=" + deviceId + ", brightness=" + brightness);
        
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
        
        Log.d("批量控制路灯请求: deviceIds=" + deviceIds + ", status=" + status);
        
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
        Log.d("获取路灯统计请求");
        List<Device> lamps = envMapper.selectLamps();
        
        int totalCount = lamps.size();
        int onlineCount = 0;
        int onCount = 0;
        int faultCount = 0;
        double totalPower = 0;
        
        for (Device lamp : lamps) {
            if (lamp.getOnLine() != null && lamp.getOnLine() == 1) {
                onlineCount++;
            }
            if (lamp.getLampStatus() != null && lamp.getLampStatus() == 1) {
                onCount++;
            }
            if (lamp.getFaultStatus() != null && lamp.getFaultStatus() == 1) {
                faultCount++;
            }
            if (lamp.getPowerConsumption() != null) {
                totalPower += lamp.getPowerConsumption();
            }
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("onlineCount", onlineCount);
        stats.put("offlineCount", totalCount - onlineCount);
        stats.put("onCount", onCount);
        stats.put("offCount", totalCount - onCount);
        stats.put("faultCount", faultCount);
        stats.put("totalPower", String.format("%.1f", totalPower));
        
        return Result.success(stats);
    }

    /**
     * 获取故障路灯列表
     */
    @GetMapping("/faults")
    public Result getFaultLamps() {
        Log.d("获取故障路灯请求");
        List<Device> faultLamps = envMapper.selectFaultLamps();
        return Result.success(faultLamps);
    }

    /**
     * 智能调光 - 根据环境光照自动调节
     */
    @PostMapping("/smartAdjust")
    public Result smartAdjust(@RequestBody Map<String, Object> params) {
        String deviceId = (String) params.get("deviceId");
        Integer ambientLight = (Integer) params.get("ambientLight"); // 环境光照强度
        
        Log.d("智能调光请求: deviceId=" + deviceId + ", ambientLight=" + ambientLight);
        
        if (deviceId == null || ambientLight == null) {
            return Result.error("参数错误");
        }

        Device device = envMapper.selectDeviceById(deviceId);
        if (device == null) {
            return Result.error("设备不存在");
        }

        // 智能调光算法：根据环境光照计算亮度
        // 环境光照越低，路灯亮度越高
        int brightness;
        if (ambientLight < 1000) {
            brightness = 100; // 很暗，最大亮度
        } else if (ambientLight < 5000) {
            brightness = 80; // 较暗，高亮度
        } else if (ambientLight < 10000) {
            brightness = 60; // 一般，中等亮度
        } else if (ambientLight < 20000) {
            brightness = 40; // 较亮，低亮度
        } else {
            brightness = 20; // 很亮，最低亮度
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
