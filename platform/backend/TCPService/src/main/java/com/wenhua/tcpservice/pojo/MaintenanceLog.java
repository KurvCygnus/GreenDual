package com.wenhua.tcpservice.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * 维修日志 —— 构造即合法：若未传入 time 则自动设为当前时间。
 */
public record MaintenanceLog(Integer id, Timestamp time, String subject, String content, Integer userId)
{
    /**
     * JSON 反序列化 + 手动构造。time 为 null 时自动取当前时间。
     */
    @JsonCreator
    public MaintenanceLog(
        @JsonProperty("id") Integer id,
        @JsonProperty("time") Timestamp time,
        @JsonProperty("subject") String subject,
        @JsonProperty("content") String content,
        @JsonProperty("userId") Integer userId
    )
    {
        this.id = id;
        this.time = time != null ? time : new Timestamp(System.currentTimeMillis());
        this.subject = subject;
        this.content = content;
        this.userId = userId;
    }
    
    /**
     * MyBatis 反序列化用（框架通过反射设值）。
     */
    @SuppressWarnings("unused")
    MaintenanceLog() { this(null, new Timestamp(System.currentTimeMillis()), null, null, null); }
}