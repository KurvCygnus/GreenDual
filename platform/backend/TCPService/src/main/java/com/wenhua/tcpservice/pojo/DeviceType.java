package com.wenhua.tcpservice.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 设备类型 —— 对外不可变，通过 {@link JsonCreator} 构造。
 * <p>包级私有 setter 供 MyBatis 反射调用。</p>
 */
@Getter @ToString public final class DeviceType
{
    private Integer id;
    private String name;
    private String type;
    private String sensors;
    private Integer deviceCount;
    private String status;
    private String color;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @JsonCreator
    public DeviceType(
        @JsonProperty("id") Integer id,
        @JsonProperty("name") String name,
        @JsonProperty("type") String type,
        @JsonProperty("sensors") String sensors,
        @JsonProperty("deviceCount") Integer deviceCount,
        @JsonProperty("status") String status,
        @JsonProperty("color") String color,
        @JsonProperty("description") String description,
        @JsonProperty("createdAt") LocalDateTime createdAt,
        @JsonProperty("updatedAt") LocalDateTime updatedAt
    )
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.sensors = sensors;
        this.deviceCount = deviceCount;
        this.status = status;
        this.color = color;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * MyBatis 反序列化用。
     */
    @SuppressWarnings("unused") DeviceType() { }
    
    @SuppressWarnings("unused") void setId(Integer id) { this.id = id; }
    
    @SuppressWarnings("unused") void setName(String name) { this.name = name; }
    
    @SuppressWarnings("unused") void setType(String type) { this.type = type; }
    
    @SuppressWarnings("unused") void setSensors(String sensors) { this.sensors = sensors; }
    
    @SuppressWarnings("unused") void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }
    
    @SuppressWarnings("unused") void setStatus(String status) { this.status = status; }
    
    @SuppressWarnings("unused") void setColor(String color) { this.color = color; }
    
    @SuppressWarnings("unused") void setDescription(String description) { this.description = description; }
    
    @SuppressWarnings("unused") void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @SuppressWarnings("unused") void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof DeviceType that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }
    
    @Override public int hashCode() { return Objects.hash(id, name); }
}