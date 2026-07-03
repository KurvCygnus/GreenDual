package com.wenhua.tcpservice.mapper;

import com.wenhua.tcpservice.pojo.DeviceType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DeviceTypeMapper {

    @Select("SELECT * FROM device_type ORDER BY id")
    List<DeviceType> selectAll();

    @Select("SELECT * FROM device_type WHERE id = #{id}")
    DeviceType selectById(@Param("id") Integer id);

    @Select("SELECT * FROM device_type WHERE type = #{type}")
    DeviceType selectByType(@Param("type") String type);

    @Insert("INSERT INTO device_type (name, type, sensors, device_count, status, color, description) " +
            "VALUES (#{name}, #{type}, #{sensors}, #{deviceCount}, #{status}, #{color}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DeviceType deviceType);

    @Update("UPDATE device_type SET name=#{name}, type=#{type}, sensors=#{sensors}, " +
            "device_count=#{deviceCount}, status=#{status}, color=#{color}, description=#{description} " +
            "WHERE id=#{id}")
    void update(DeviceType deviceType);

    @Update("UPDATE device_type SET status=#{status} WHERE id=#{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Delete("DELETE FROM device_type WHERE id = #{id}")
    void deleteById(@Param("id") Integer id);
}
