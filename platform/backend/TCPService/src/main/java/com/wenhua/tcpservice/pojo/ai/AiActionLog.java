package com.wenhua.tcpservice.pojo.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AI 行动记录 —— 对外不可变，通过 {@link JsonCreator} 构造。
 * <p>包级私有 setter 供 MyBatis 反射调用。</p>
 */
@Getter @ToString public final class AiActionLog
{
    private Long actionId;
    private Long aiId;
    private String actionDescription;
    private LocalDateTime actionTime;
    
    @JsonCreator
    public AiActionLog(
        @JsonProperty("actionId") Long actionId,
        @JsonProperty("aiId") Long aiId,
        @JsonProperty("actionDescription") String actionDescription,
        @JsonProperty("actionTime") LocalDateTime actionTime
    )
    {
        this.actionId = actionId;
        this.aiId = aiId;
        this.actionDescription = actionDescription;
        this.actionTime = actionTime;
    }
    
    /**
     * MyBatis 反序列化用。
     */
    AiActionLog() { }
    
    void setActionId(Long actionId) { this.actionId = actionId; }
    
    void setAiId(Long aiId) { this.aiId = aiId; }
    
    void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }
    
    void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof AiActionLog that)) return false;
        return Objects.equals(actionId, that.actionId);
    }
    
    @Override public int hashCode() { return Objects.hash(actionId); }
}