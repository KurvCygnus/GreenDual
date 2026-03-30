package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.mapper.DeviceTypeMapper;
import com.wenhua.tcpservice.pojo.DeviceType;
import com.wenhua.tcpservice.pojo.Result;
import com.wenhua.tcpservice.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = GlobalConfiguration.ORIGINS)
@RequestMapping(GlobalConfiguration.USER_REQUEST_PREFIX + "/deviceType")
public class DeviceTypeController {

    private DeviceTypeMapper deviceTypeMapper;

    private boolean isAdmin(HttpServletRequest request) {
        Integer userId = JwtUtils.getUserId(request);
        return userId != null;
    }

    @PostMapping("/list")
    public Result getDeviceTypeList(HttpServletRequest request) {
        try {
            List<DeviceType> types = deviceTypeMapper.selectAll();
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取设备类型列表失败: {}", e.getMessage());
            return Result.error("获取设备类型列表失败");
        }
    }

    @PostMapping("/add")
    public Result addDeviceType(@RequestBody DeviceType deviceType, HttpServletRequest request) {
        try {
            DeviceType existing = deviceTypeMapper.selectByType(deviceType.getType());
            if (existing != null) {
                return Result.error("类型标识已存在");
            }
            if (deviceType.getStatus() == null) {
                deviceType.setStatus("active");
            }
            if (deviceType.getDeviceCount() == null) {
                deviceType.setDeviceCount(0);
            }
            deviceTypeMapper.insert(deviceType);
            return Result.success("添加成功");
        } catch (Exception e) {
            log.error("添加设备类型失败: {}", e.getMessage());
            return Result.error("添加设备类型失败");
        }
    }

    @PostMapping("/update")
    public Result updateDeviceType(@RequestBody DeviceType deviceType, HttpServletRequest request) {
        try {
            DeviceType existing = deviceTypeMapper.selectById(deviceType.getId());
            if (existing == null) {
                return Result.error("设备类型不存在");
            }
            deviceTypeMapper.update(deviceType);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新设备类型失败: {}", e.getMessage());
            return Result.error("更新设备类型失败");
        }
    }

    @PostMapping("/toggleStatus")
    public Result toggleStatus(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Integer id = (Integer) params.get("id");
            DeviceType deviceType = deviceTypeMapper.selectById(id);
            if (deviceType == null) {
                return Result.error("设备类型不存在");
            }
            String newStatus = "active".equals(deviceType.getStatus()) ? "inactive" : "active";
            deviceTypeMapper.updateStatus(id, newStatus);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新状态失败: {}", e.getMessage());
            return Result.error("更新状态失败");
        }
    }

    @PostMapping("/delete")
    public Result deleteDeviceType(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Integer id = (Integer) params.get("id");
            deviceTypeMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除设备类型失败: {}", e.getMessage());
            return Result.error("删除设备类型失败");
        }
    }
}
