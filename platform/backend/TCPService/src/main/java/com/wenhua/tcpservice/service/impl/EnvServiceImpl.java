package com.wenhua.tcpservice.service.impl;

import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.service.EnvService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @Slf4j
@AllArgsConstructor
public class EnvServiceImpl implements EnvService {

    private EnvMapper envMapper;

    @Override
    public List<Device> queryDevices(QueryParameter queryParameter) {
        log.debug("查询设备列表，参数: {}", queryParameter.toString());
        return envMapper.selectDevice(queryParameter);
    }

    @Override
    public boolean addDevice(Device device) {
        log.debug("添加设备: {}", device.toString());
        try {
            int result = envMapper.insertDevice(device);
            return result > 0;
        } catch (Exception e) {
            log.error("添加设备失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateDevice(Device device) {
        log.debug("更新设备: {}", device.toString());
        try {
            // 如果是路灯类型，使用updateLamp更新所有字段
            final int result;
            if ("路灯".equals(device.getDeviceType()) || "智慧路灯".equals(device.getDeviceType())) {
                result = envMapper.updateLamp(device);
            } else {
                result = envMapper.updateDevice(device);
            }
            return result > 0;
        } catch (Exception e) {
            log.error("更新设备失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteDevices(List<String> ids) {
        log.debug("删除设备，ID列表: {}", ids.toString());
        try {
            int result = 0;
            for (String id : ids) {
                result += envMapper.deleteDevice(id);
            }
            return result > 0;
        } catch (Exception e) {
            log.error("删除设备失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateDeviceStatus(String id, Integer onLine) {
        log.debug("更新设备状态，ID: {}, 状态: {}", id, onLine);
        try {
            int result = envMapper.updateDeviceStatus(id, onLine);
            return result > 0;
        } catch (Exception e) {
            log.error("更新设备状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean batchUpdateDeviceStatus(List<String> ids, Integer onLine) {
        log.debug("批量更新设备状态，ID列表: {}, 状态: {}", ids.toString(), onLine);
        try {
            int result = 0;
            for (String id : ids) {
                result += envMapper.updateDeviceStatus(id, onLine);
            }
            return result > 0;
        } catch (Exception e) {
            log.error("批量更新设备状态失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Device getDeviceDetail(String id) {
        log.error("获取设备详情，ID: {}", id);
        return envMapper.selectDeviceById(id);
    }
}
