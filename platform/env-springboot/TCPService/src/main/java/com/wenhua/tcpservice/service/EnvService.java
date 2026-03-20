package com.wenhua.tcpservice.service;

import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.dev.Device;

import java.util.List;

//环境监测服务-接口
public interface EnvService {
    //查询设备列表
    List<Device> queryDevices(QueryParameter queryParameter);

    //添加设备
    boolean addDevice(Device device);

    //更新设备
    boolean updateDevice(Device device);

    //批量删除设备
    boolean deleteDevices(List<String> ids);

    //更新设备状态
    boolean updateDeviceStatus(String id, Integer onLine);

    //批量更新设备状态
    boolean batchUpdateDeviceStatus(List<String> ids, Integer onLine);

    //获取设备详情
    Device getDeviceDetail(String id);
}
