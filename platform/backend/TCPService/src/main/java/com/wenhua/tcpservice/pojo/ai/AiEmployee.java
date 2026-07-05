package com.wenhua.tcpservice.pojo.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalTime;
import java.util.Objects;

/**
 * AI 员工实体 —— 对外不可变，通过 {@link JsonCreator} 构造。
 * <p>包级私有 setter 供 MyBatis 反射调用。</p>
 */
@Getter @ToString public final class AiEmployee
{
    private Long aiId;
    private String name;
    private String jobDescription;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private String dailySummary;
    private Long userId;
    private int isWorking;
    
    @JsonCreator
    public AiEmployee(
        @JsonProperty("aiId") Long aiId,
        @JsonProperty("name") String name,
        @JsonProperty("jobDescription") String jobDescription,
        @JsonProperty("workStartTime") LocalTime workStartTime,
        @JsonProperty("workEndTime") LocalTime workEndTime,
        @JsonProperty("dailySummary") String dailySummary,
        @JsonProperty("userId") Long userId,
        @JsonProperty("isWorking") Integer isWorking
    )
    {
        this.aiId = aiId;
        this.name = name;
        this.jobDescription = jobDescription;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.dailySummary = dailySummary;
        this.userId = userId;
        this.isWorking = isWorking != null ? isWorking : 0;
    }
    
    /**
     * MyBatis 反序列化用。
     */
    AiEmployee() { }
    
    void setAiId(Long aiId) { this.aiId = aiId; }
    
    void setName(String name) { this.name = name; }
    
    void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    
    void setWorkStartTime(LocalTime workStartTime) { this.workStartTime = workStartTime; }
    
    void setWorkEndTime(LocalTime workEndTime) { this.workEndTime = workEndTime; }
    
    void setDailySummary(String dailySummary) { this.dailySummary = dailySummary; }
    
    void setUserId(Long userId) { this.userId = userId; }
    
    void setIsWorking(int isWorking) { this.isWorking = isWorking; }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof AiEmployee that)) return false;
        return Objects.equals(aiId, that.aiId);
    }
    
    @Override public int hashCode() { return Objects.hash(aiId); }
}