package com.wenhua.tcpservice.service.impl;

import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.service.EnvService;
import com.wenhua.tcpservice.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnvServiceImpl implements EnvService {

    @Autowired
    private EnvMapper envMapper;

    @Override
    public List<Device> queryDevices(QueryParameter queryParameter) {
        Log.d("查询设备列表，参数: " + queryParameter.toString());
        return envMapper.selectDevice(queryParameter);
    }

    @Override
    public boolean addDevice(Device device) {
        Log.d("添加设备: " + device.toString());
        try {
            int result = envMapper.insertDevice(device);
            return result > 0;
        } catch (Exception e) {
            Log.c("添加设备失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateDevice(Device device) {
        Log.d("更新设备: " + device.toString());
        try {
            // 如果是路灯类型，使用updateLamp更新所有字段
            if ("路灯".equals(device.getDeviceType())) {
                int result = envMapper.updateLamp(device);
                return result > 0;
            } else {
                int result = envMapper.updateDevice(device);
                return result > 0;
            }
        } catch (Exception e) {
            Log.c("更新设备失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteDevices(List<String> ids) {
        Log.d("删除设备，ID列表: " + ids.toString());
        try {
            int result = 0;
            for (String id : ids) {
                result += envMapper.deleteDevice(id);
            }
            return result > 0;
        } catch (Exception e) {
            Log.c("删除设备失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateDeviceStatus(String id, Integer onLine) {
        Log.d("更新设备状态，ID: " + id + ", 状态: " + onLine);
        try {
            int result = envMapper.updateDeviceStatus(id, onLine);
            return result > 0;
        } catch (Exception e) {
            Log.c("更新设备状态失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean batchUpdateDeviceStatus(List<String> ids, Integer onLine) {
        Log.d("批量更新设备状态，ID列表: " + ids.toString() + ", 状态: " + onLine);
        try {
            int result = 0;
            for (String id : ids) {
                result += envMapper.updateDeviceStatus(id, onLine);
            }
            return result > 0;
        } catch (Exception e) {
            Log.c("批量更新设备状态失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Device getDeviceDetail(String id) {
        Log.d("获取设备详情，ID: " + id);
        return envMapper.selectDeviceById(id);
    }
}
