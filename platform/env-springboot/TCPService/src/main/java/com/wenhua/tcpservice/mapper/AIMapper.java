package com.wenhua.tcpservice.mapper;

import com.wenhua.tcpservice.pojo.QueryParameter;
import com.wenhua.tcpservice.pojo.ai.AiActionLog;
import com.wenhua.tcpservice.pojo.ai.AiEmployee;
import org.apache.ibatis.annotations.*;

import java.util.List;

//AI相关
@Mapper
public interface AIMapper {
    //向数据库中添加一个AI
    void addAI(AiEmployee aiEmployee);

    //向数据库中删除一个AI
    void deleteAI(String id);
    //向数据库中更新一个AI
    void updateAI(AiEmployee aiEmployee);

    //根据查询参数进行查询
    List<AiEmployee> selectAI(QueryParameter qp);

    int selectAICount(QueryParameter qp);

    // 设置目标ai员工为正在工作
    @Update("UPDATE ai_employee SET is_working = 1 WHERE ai_id = #{id}")
    void setAIWorking(@Param("id") String id);

    // 设置目标ai员工为停止工作
    @Update("UPDATE ai_employee SET is_working = 0 WHERE ai_id = #{id}")
    void setAIStopWorking(@Param("id") String id);

    //设置所有AI员工停止工作
    @Update("UPDATE ai_employee SET is_working = 0")
    void setAllAIStopWorking();

    //通过id查询AI员工
    @Select("select * from ai_employee where ai_id = #{id}")
    AiEmployee selectAIById(String id);

    // 设置ai员工的工作总结
    @Update("UPDATE ai_employee SET daily_summary = #{dailySummary} WHERE ai_id = #{aiId}")
    void setDailySummary(@Param("aiId") String aiId, @Param("dailySummary") String dailySummary);


    //添加日志
    @Insert("INSERT INTO ai_action_log (ai_id, action_description, action_time) " +
            "VALUES (#{aiId}, #{actionDescription}, NOW())")
    void insertAILog(@Param("aiId") String aiId, @Param("actionDescription") String actionDescription);

    //查询日志
    List<AiActionLog> selectAILog(QueryParameter qp);
    // 查询符合要求的日志数量
    int countAILog(QueryParameter qp);

    //删除AI日志
    void deleteAILog(@Param("ids") List<Integer> ids);
}
