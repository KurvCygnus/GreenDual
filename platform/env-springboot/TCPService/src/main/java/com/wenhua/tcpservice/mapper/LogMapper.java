package com.wenhua.tcpservice.mapper;

import com.wenhua.tcpservice.pojo.MaintenanceLog;
import com.wenhua.tcpservice.pojo.QueryParameter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

//日志
@Mapper
public interface LogMapper {

    //添加日志
    // 添加日志
    @Insert("INSERT INTO MaintenanceLog (time, subject, content, userId) VALUES (#{time}, #{subject}, #{content}, #{userId})")
    void addLog(MaintenanceLog maintenanceLog);

    //条件查询(写xml里)
    List<MaintenanceLog> select(QueryParameter queryParameter);

    //查询符合要求日志的数量
    int selectCount(QueryParameter queryParameter);

    //删除
    @Delete("delete from MaintenanceLog where id = #{id}")
    void deleteLog(int id);
}
