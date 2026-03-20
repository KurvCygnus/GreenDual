package com.wenhua.tcpservice.mapper;

import com.wenhua.tcpservice.pojo.MonitorRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface MonitorRecordMapper {

    @Select("<script>" +
            "SELECT * FROM monitor_record WHERE 1=1 " +
            "<if test='deviceId != null and deviceId != \"\"'>" +
            "AND device_id = #{deviceId} " +
            "</if>" +
            "<if test='dataType != null and dataType != \"\"'>" +
            "AND data_type = #{dataType} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND record_time &gt;= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND record_time &lt;= #{endTime} " +
            "</if>" +
            "ORDER BY record_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<MonitorRecord> selectRecords(Map<String, Object> params);

    @Select("<script>" +
            "SELECT COUNT(*) FROM monitor_record WHERE 1=1 " +
            "<if test='deviceId != null and deviceId != \"\"'>" +
            "AND device_id = #{deviceId} " +
            "</if>" +
            "<if test='dataType != null and dataType != \"\"'>" +
            "AND data_type = #{dataType} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND record_time &gt;= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND record_time &lt;= #{endTime} " +
            "</if>" +
            "</script>")
    int countRecords(Map<String, Object> params);

    @Select("SELECT COUNT(*) FROM monitor_record WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    @Insert("INSERT INTO monitor_record (device_id, device_name, record_time, data_type, value, status, remark) " +
            "VALUES (#{deviceId}, #{deviceName}, #{recordTime}, #{dataType}, #{value}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRecord(MonitorRecord record);

    @Delete("DELETE FROM monitor_record WHERE id = #{id}")
    void deleteById(@Param("id") Integer id);

    @Select("SELECT * FROM monitor_record WHERE id = #{id}")
    MonitorRecord selectById(@Param("id") Integer id);
}
