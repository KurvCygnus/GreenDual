package com.wenhua.tcpservice.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 监测记录 —— 对外不可变（无公开 setter），通过 {@link JsonCreator} 构造。
 * <p>包级私有 setter 供 MyBatis 反射调用。</p>
 */
@Getter @ToString
public final class MonitorRecord
{
    private Integer id;
    private String deviceId;
    private String deviceName;
    private LocalDateTime recordTime;
    private String dataType;
    private Double value;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    
    @JsonCreator public MonitorRecord(
        @JsonProperty("id") Integer id,
        @JsonProperty("deviceId") String deviceId,
        @JsonProperty("deviceName") String deviceName,
        @JsonProperty("recordTime") LocalDateTime recordTime,
        @JsonProperty("dataType") String dataType,
        @JsonProperty("value") Double value,
        @JsonProperty("status") String status,
        @JsonProperty("remark") String remark,
        @JsonProperty("createdAt") LocalDateTime createdAt
    )
    {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.recordTime = recordTime;
        this.dataType = dataType;
        this.value = value;
        this.status = status;
        this.remark = remark;
        this.createdAt = createdAt;
    }
    
    /**
     * MyBatis 反序列化用（框架通过反射调用）。
     */
    @SuppressWarnings("unused") MonitorRecord() { }
    
    @SuppressWarnings("unused") void setId(Integer id) { this.id = id; }
    
    @SuppressWarnings("unused") void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    @SuppressWarnings("unused") void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    @SuppressWarnings("unused") void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }
    
    @SuppressWarnings("unused") void setDataType(String dataType) { this.dataType = dataType; }
    
    @SuppressWarnings("unused") void setValue(Double value) { this.value = value; }
    
    @SuppressWarnings("unused") void setStatus(String status) { this.status = status; }
    
    @SuppressWarnings("unused") void setRemark(String remark) { this.remark = remark; }
    
    @SuppressWarnings("unused") void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof MonitorRecord that)) return false;
        return Objects.equals(id, that.id)
            && Objects.equals(deviceId, that.deviceId)
            && Objects.equals(recordTime, that.recordTime);
    }
    
    @Override public int hashCode() { return Objects.hash(id, deviceId, recordTime); }
}