package com.wenhua.tcpservice.mapper;

import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.dev.Device;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EnvMapper {

    //使目标id的设备上线
    @Update("update Device set onLine = 1 where id = #{id}")
    void setOnline(String id);

    //使目标id的设备下线
    @Update("update Device set onLine = 0 where id = #{id}")
    void setOffline(String id);

    //设置目标id设备繁忙
    @Update("update Device set isBusy = 1 where id = #{id}")
    void setBusyId(String id);

    //设置目标id设备空闲
    @Update("update Device set isBusy = 0 where id = #{id}")
    void setUnBusyId(String id);

    //查询目标设备是否繁忙
    @Select("select isBusy from Device where id = #{id}")
    Integer getIsBusy(String id);

    @Select("select isBusy from Device where ip = #{ip}")
    Integer getIsBusyByIp(String ip);

    //通过id获取ip
    @Select("select ip from Device where id = #{id}")
    String getIp(String id);

    //设置设备ip
    @Update("update Device set ip = #{ip} where id = #{id}")
    void setIp(String id, String ip);

    //查询(写xml里面了)
    List<Device> selectDevice(QueryParameter queryParameter);

    //查询符合条件的设备数量
    int selectDeviceCount(QueryParameter queryParameter);

    //插入设备
    @Insert("insert into Device(id, name, ip, port, deviceType, temperature, humidity, lightIntensity, onLine, lampStatus, brightness, faultStatus, powerConsumption) " +
            "values(#{id}, #{name}, #{ip}, #{port}, #{deviceType}, #{temperature}, #{humidity}, #{lightIntensity}, #{onLine}, #{lampStatus}, #{brightness}, #{faultStatus}, #{powerConsumption})")
    int insertDevice(Device device);

    //查询设备是否存在
    @Select("select count(*) from Device where id = #{id}")
    int isDeviceExist(String id);

    //删除设备
    @Update("delete from Device where id = #{id}")
    int deleteDevice(String id);

    //查询所有设备
    @Select("select * from device")
    List<Device> selectAllDevice();

    //查询一个设备
    @Select("select * from Device where id = #{id}")
    Device selectDeviceById(String id);

    //设置设备是否损坏
    @Update("update Device set isBroken = #{isBroken} where id = #{id}")
    void setIsBroken(String id, Integer isBroken);

    //查询所有设备id
    @Select("select id from Device")
    List<String> selectAllDeviceId();

    //更新设备
    @Update("update Device set " +
            "name = #{name}, " +
            "ip = #{ip}, " +
            "port = #{port}, " +
            "deviceType = #{deviceType}, " +
            "temperature = #{temperature}, " +
            "humidity = #{humidity}, " +
            "lightIntensity = #{lightIntensity}, " +
            "onLine = #{onLine}, " +
            "lampStatus = #{lampStatus}, " +
            "brightness = #{brightness}, " +
            "faultStatus = #{faultStatus}, " +
            "powerConsumption = #{powerConsumption} " +
            "where id = #{id}")
    int updateDevice(Device device);

    //更新设备状态
    @Update("update Device set onLine = #{onLine} where id = #{id}")
    int updateDeviceStatus(String id, Integer onLine);

    // ========== 路灯相关方法 ==========

    //查询所有路灯
    @Select("select * from device where deviceType = '路灯'")
    List<Device> selectLamps();

    //查询故障路灯
    @Select("select * from device where deviceType = '路灯' and (faultStatus = 1 or isBroken = 1)")
    List<Device> selectFaultLamps();

    //更新路灯状态（包含路灯特有字段）
    @Update("update Device set " +
            "name = #{name}, " +
            "ip = #{ip}, " +
            "port = #{port}, " +
            "deviceType = #{deviceType}, " +
            "temperature = #{temperature}, " +
            "humidity = #{humidity}, " +
            "lightIntensity = #{lightIntensity}, " +
            "onLine = #{onLine}, " +
            "isBusy = #{isBusy}, " +
            "isBroken = #{isBroken}, " +
            "lampStatus = #{lampStatus}, " +
            "brightness = #{brightness}, " +
            "longitude = #{longitude}, " +
            "latitude = #{latitude}, " +
            "powerConsumption = #{powerConsumption}, " +
            "lastMaintenance = #{lastMaintenance}, " +
            "faultStatus = #{faultStatus} " +
            "where id = #{id}")
    int updateLamp(Device device);
}
