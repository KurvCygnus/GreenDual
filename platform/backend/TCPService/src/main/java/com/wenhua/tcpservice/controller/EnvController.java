package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.service.EnvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@AllArgsConstructor
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@Tag(name = "环境监测", description = "环境监测设备CRUD、状态查询接口")
public final class EnvController
{
    
    private final EnvService envService;
    private final EnvMapper envMapper;
    
    @Operation(summary = "查询设备列表", description = "分页查询环境监测设备")
    @PostMapping(GlobalConfiguration.DEVICE_REQUEST_PREFIX + "/select")
    public Result queryDevices(@RequestBody QueryParameter queryParameter)
    {
        log.debug("接受到了设备查询请求: {}", queryParameter);
        final var devices = envService.queryDevices(queryParameter);
        //还要查询目标数量
        final var i = envMapper.selectDeviceCount(queryParameter);
        log.debug("查询结果为: {}", devices.toString());
        final var result = Result.success(devices);
        return result.totalPages(i);
    }
    
    @Operation(summary = "添加设备")
    @PostMapping(GlobalConfiguration.DEVICE_REQUEST_PREFIX + "/add")
    public Result addDevice(@RequestBody Device device)
    {
        log.debug("接受到了添加设备请求");
        final var b = envService.addDevice(device);
        return Result.of(b);
    }
    
    @Operation(summary = "修改设备")
    @PostMapping(GlobalConfiguration.DEVICE_REQUEST_PREFIX + "/update")
    public Result updateDevice(@RequestBody Device device)
    {
        log.debug("接受到了修改设备请求: {}", device.toString());
        final var b = envService.updateDevice(device);
        return Result.of(b);
    }
    
    @Operation(summary = "删除设备")
    @PostMapping(GlobalConfiguration.DEVICE_REQUEST_PREFIX + "/delete")
    public Result deleteDevice(@RequestBody List<String> ids)
    {
        log.debug("接受到了删除设备请求: {}", ids.toString());
        final var b = envService.deleteDevices(ids);
        return Result.of(b);
    }
    
    @Operation(summary = "获取设备统计数据")
    @GetMapping(GlobalConfiguration.DEVICE_REQUEST_PREFIX + "/stats")
    public Result getDeviceStats()
    {
        log.debug("接受到了获取设备统计数据请求");
        final Map<String, Object> stats = new HashMap<>();
        
        //获取所有设备
        final var allDevices = envMapper.selectAllDevice();
        
        //计算统计数据
        final var deviceCount = allDevices.size();
        
        final var avgTemperature = allDevices.stream()
            .filter(d -> d.getTemperature() != null)
            .mapToDouble(Device::getTemperature)
            .average()
            .orElse(.0);
        final var avgHumidity = allDevices.stream()
            .filter(d -> d.getHumidity() != null)
            .mapToInt(Device::getHumidity)
            .average()
            .orElse(.0);
        final var avgLight = allDevices.stream()
            .filter(d -> d.getLightIntensity() != null)
            .mapToInt(Device::getLightIntensity)
            .average()
            .orElse(.0);
        
        stats.put("deviceCount", deviceCount);
        stats.put("avgTemperature", String.format("%.1f", avgTemperature));
        stats.put("avgHumidity", String.format("%.0f", avgHumidity));
        stats.put("avgLight", String.format("%.0f", avgLight));
        
        return Result.success(stats);
    }
    
    @Operation(summary = "获取设备详情")
    @GetMapping(GlobalConfiguration.DEVICE_REQUEST_PREFIX + "/detail/{id}")
    public Result getDeviceDetail(@PathVariable String id)
    {
        log.debug("接受到了获取设备详情请求: {}", id);
        final var device = envMapper.selectDeviceById(id);
        if(device != null)
            return Result.success(device);
        return Result.error("设备不存在");
    }
    
    @Operation(summary = "更新设备状态")
    @PostMapping(GlobalConfiguration.DEVICE_REQUEST_PREFIX + "/status")
    public Result updateDeviceStatus(@RequestBody Map<String, Object> params)
    {
        log.debug("接受到了更新设备状态请求");
        final var id = (String) params.get("id");
        final var onLine = (Integer) params.get("onLine");
        final var b = envService.updateDeviceStatus(id, onLine);
        return Result.of(b);
    }
    
    @Operation(summary = "批量更新设备状态")
    @PostMapping(GlobalConfiguration.DEVICE_REQUEST_PREFIX + "/status/batch")
    public Result batchUpdateDeviceStatus(@RequestBody Map<String, Object> params)
    {
        log.debug("接受到了批量更新设备状态请求");
        final var ids = (List<String>) params.get("ids");
        final var onLine = (Integer) params.get("onLine");
        final var b = envService.batchUpdateDeviceStatus(ids, onLine);
        return Result.of(b);
    }
}
