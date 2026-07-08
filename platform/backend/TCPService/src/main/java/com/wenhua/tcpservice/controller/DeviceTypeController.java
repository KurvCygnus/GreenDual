package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.DeviceTypeMapper;
import com.wenhua.tcpservice.mapper.UserMapper;
import com.wenhua.tcpservice.pojo.DeviceType;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController @AllArgsConstructor @Slf4j
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@RequestMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/deviceType")
@Tag(name = "设备类型", description = "设备类型CRUD接口")
public final class DeviceTypeController
{
    
    private final DeviceTypeMapper deviceTypeMapper;
    private final UserMapper userMapper;
    
    @Operation(summary = "获取设备类型列表")
    @PostMapping("/list")
    public Result getDeviceTypeList(HttpServletRequest request)
    {
        try
        {
            final var types = deviceTypeMapper.selectAll();
            return Result.success(types);
        }
        catch(Exception e)
        {
            log.error("获取设备类型列表失败: {}", e.getMessage());
            return Result.error("获取设备类型列表失败");
        }
    }
    
    @Operation(summary = "添加设备类型")
    @PostMapping("/add")
    public Result addDeviceType(@RequestBody DeviceType deviceType, HttpServletRequest request)
    {
        if(!JwtUtils.isAdmin(request, userMapper::selectUserById)) return Result.error("权限不足");
        try
        {
            final var existing = deviceTypeMapper.selectByType(deviceType.getType());
            if(existing != null)
                return Result.error("类型标识已存在");
            deviceType = new DeviceType(
                deviceType.getId(),
                deviceType.getName(),
                deviceType.getType(),
                deviceType.getSensors(),
                Objects.requireNonNullElse(deviceType.getDeviceCount(), 0),
                Objects.requireNonNullElse(deviceType.getStatus(), "active"),
                deviceType.getColor(),
                deviceType.getDescription(),
                deviceType.getCreatedAt(),
                deviceType.getUpdatedAt()
            );
            deviceTypeMapper.insert(deviceType);
            return Result.success("添加成功");
        }
        catch(Exception e)
        {
            log.error("添加设备类型失败: {}", e.getMessage());
            return Result.error("添加设备类型失败");
        }
    }
    
    @Operation(summary = "更新设备类型")
    @PostMapping("/update")
    public Result updateDeviceType(@RequestBody DeviceType deviceType, HttpServletRequest request)
    {
        if(!JwtUtils.isAdmin(request, userMapper::selectUserById))
            return Result.error("权限不足");
        try
        {
            final var existing = deviceTypeMapper.selectById(deviceType.getId());
            if(existing == null)
                return Result.error("设备类型不存在");
            deviceTypeMapper.update(deviceType);
            return Result.success("更新成功");
        }
        catch(Exception e)
        {
            log.error("更新设备类型失败: {}", e.getMessage());
            return Result.error("更新设备类型失败");
        }
    }
    
    @Operation(summary = "切换设备类型状态")
    @PostMapping("/toggleStatus")
    public Result toggleStatus(@RequestBody Map<String, Object> params, HttpServletRequest request)
    {
        if(!JwtUtils.isAdmin(request, userMapper::selectUserById))
            return Result.error("权限不足");
        try
        {
            final var id = (Integer) params.get("id");
            final var deviceType = deviceTypeMapper.selectById(id);
            if(deviceType == null)
                return Result.error("设备类型不存在");
            final var newStatus = "active".equals(deviceType.getStatus()) ? "inactive" : "active";
            deviceTypeMapper.updateStatus(id, newStatus);
            return Result.success("状态更新成功");
        }
        catch(Exception e)
        {
            log.error("更新状态失败: {}", e.getMessage());
            return Result.error("更新状态失败");
        }
    }
    
    @Operation(summary = "删除设备类型")
    @PostMapping("/delete")
    public Result deleteDeviceType(@RequestBody Map<String, Object> params, HttpServletRequest request)
    {
        if(!JwtUtils.isAdmin(request, userMapper::selectUserById))
            return Result.error("权限不足");
        try
        {
            final var id = (Integer) params.get("id");
            deviceTypeMapper.deleteById(id);
            return Result.success("删除成功");
        }
        catch(Exception e)
        {
            log.error("删除设备类型失败: {}", e.getMessage());
            return Result.error("删除设备类型失败");
        }
    }
}
