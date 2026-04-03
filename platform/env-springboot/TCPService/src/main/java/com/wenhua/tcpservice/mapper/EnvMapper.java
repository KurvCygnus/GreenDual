package com.wenhua.tcpservice.mapper;

import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.dev.Device;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EnvMapper {

    //使目标id的设备上线
    @Update("update device set onLine = 1 where id = #{id}")
    void setOnline(String id);

    //使目标id的设备下线
    @Update("update device set onLine = 0 where id = #{id}")
    void setOffline(String id);

    //设置目标id设备繁忙
    @Update("update device set isBusy = 1 where id = #{id}")
    void setBusyId(String id);

    //设置目标id设备空闲
    @Update("update device set isBusy = 0 where id = #{id}")
    void setUnBusyId(String id);

    //查询目标设备是否繁忙
    @Select("select isBusy from device where id = #{id}")
    Integer getIsBusy(String id);

    @Select("select isBusy from device where ip = #{ip}")
    Integer getIsBusyByIp(String ip);

    //通过id获取ip
    @Select("select ip from device where id = #{id}")
    String getIp(String id);

    //设置设备ip
    @Update("update device set ip = #{ip} where id = #{id}")
    void setIp(String id, String ip);

    //查询(写xml里面了)
    List<Device> selectDevice(QueryParameter queryParameter);

    //查询符合条件的设备数量
    int selectDeviceCount(QueryParameter queryParameter);

    //插入设备
    @Insert("insert into device(id, name, ip, port, deviceType, temperature, humidity, lightIntensity, onLine, isBusy, isBroken) " +
            "values(#{id}, #{name}, #{ip}, #{port}, #{deviceType}, #{temperature}, #{humidity}, #{lightIntensity}, #{onLine}, #{isBusy}, #{isBroken})")
    int insertDevice(Device device);

    //查询设备是否存在
    @Select("select count(*) from device where id = #{id}")
    int isDeviceExist(String id);

    //删除设备
    @Update("delete from device where id = #{id}")
    int deleteDevice(String id);

    //查询所有设备
    @Select("select * from device")
    List<Device> selectAllDevice();

    //查询一个设备
    @Select("select * from device where id = #{id}")
    Device selectDeviceById(String id);

    //查询所有设备id
    @Select("select id from device")
    List<String> selectAllDeviceId();

    //更新设备
    @Update("update device set " +
            "name = #{name}, " +
            "ip = #{ip}, " +
            "port = #{port}, " +
            "deviceType = #{deviceType}, " +
            "temperature = #{temperature}, " +
            "humidity = #{humidity}, " +
            "lightIntensity = #{lightIntensity}, " +
            "onLine = #{onLine}, " +
            "isBusy = #{isBusy}, " +
            "isBroken = #{isBroken} " +
            "where id = #{id}")
    int updateDevice(Device device);

    //更新设备状态
    @Update("update device set onLine = #{onLine} where id = #{id}")
    int updateDeviceStatus(String id, Integer onLine);

    // ========== 路灯相关方法 ==========

    //查询所有路灯
    @Select("select * from device where deviceType = '路灯'")
    List<Device> selectLamps();

    //查询故障路灯
    @Select("select * from device where deviceType = '路灯' and (faultStatus = 1 or isBroken = 1)")
    List<Device> selectFaultLamps();

    //更新路灯状态（包含路灯特有字段）
    @Update("update device set " +
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
            "brightness = #{brightness} " +
            "where id = #{id}")
    int updateLamp(Device device);

    //更新路灯亮度
    @Update("update device set brightness = #{brightness}, lampStatus = #{lampStatus} where id = #{id}")
    int updateLampBrightness(@Param("id") String id, @Param("brightness") Integer brightness, @Param("lampStatus") Integer lampStatus);

    //查询指定区域的设备
    @Select("select * from device where name like concat('%', #{areaName}, '%')")
    List<Device> selectDevicesByArea(String areaName);
}
