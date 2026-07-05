package com.wenhua.tcpservice.pojo.campus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 校园区域 —— 对外不可变，通过 {@link JsonCreator} 构造。
 * <p>包级私有 setter 供 MyBatis 反射调用。</p>
 */
@ToString
public final class CampusArea
{
    @Getter private Integer id;
    @Getter private String areaName;
    @Getter private String areaType;
    @Getter private Double areaSize;
    @Getter private Integer lampCount;
    @Getter private Integer deviceCount;
    @Getter private String managerName;
    @Getter private String managerPhone;
    @Getter private Integer status;
    @Getter private LocalDateTime createTime;
    @Getter private LocalDateTime updateTime;
    private Double monthlyEnergyConsumption;
    private Double monthlyEnergySaved;
    private Double savingRate;
    
    @JsonCreator
    public CampusArea(
        @JsonProperty("id") Integer id,
        @JsonProperty("areaName") String areaName,
        @JsonProperty("areaType") String areaType,
        @JsonProperty("areaSize") Double areaSize,
        @JsonProperty("lampCount") Integer lampCount,
        @JsonProperty("deviceCount") Integer deviceCount,
        @JsonProperty("managerName") String managerName,
        @JsonProperty("managerPhone") String managerPhone,
        @JsonProperty("status") Integer status,
        @JsonProperty("createTime") LocalDateTime createTime,
        @JsonProperty("updateTime") LocalDateTime updateTime,
        @JsonProperty("monthlyEnergyConsumption") Double monthlyEnergyConsumption,
        @JsonProperty("monthlyEnergySaved") Double monthlyEnergySaved,
        @JsonProperty("savingRate") Double savingRate
    )
    {
        this.id = id;
        this.areaName = areaName;
        this.areaType = areaType;
        this.areaSize = areaSize;
        this.lampCount = lampCount;
        this.deviceCount = deviceCount;
        this.managerName = managerName;
        this.managerPhone = managerPhone;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.monthlyEnergyConsumption = monthlyEnergyConsumption;
        this.monthlyEnergySaved = monthlyEnergySaved;
        this.savingRate = savingRate;
    }
    
    public @NonNull Optional<Double> getMonthlyEnergyConsumption() { return Optional.ofNullable(monthlyEnergyConsumption); }
    
    public @NonNull Optional<Double> getMonthlyEnergySaved() { return Optional.ofNullable(monthlyEnergySaved); }
    
    public @NonNull Optional<Double> getSavingRate() { return Optional.ofNullable(savingRate); }
    
    /**
     * MyBatis 反序列化用。
     */
    @SuppressWarnings("unused") CampusArea() { }
    
    @SuppressWarnings("unused") void setId(Integer id) { this.id = id; }
    
    @SuppressWarnings("unused") void setAreaName(String areaName) { this.areaName = areaName; }
    
    @SuppressWarnings("unused") void setAreaType(String areaType) { this.areaType = areaType; }
    
    @SuppressWarnings("unused") void setAreaSize(Double areaSize) { this.areaSize = areaSize; }
    
    @SuppressWarnings("unused") void setLampCount(Integer lampCount) { this.lampCount = lampCount; }
    
    @SuppressWarnings("unused") void setDeviceCount(Integer deviceCount) { this.deviceCount = deviceCount; }
    
    @SuppressWarnings("unused") void setManagerName(String managerName) { this.managerName = managerName; }
    
    @SuppressWarnings("unused") void setManagerPhone(String managerPhone) { this.managerPhone = managerPhone; }
    
    @SuppressWarnings("unused") void setStatus(Integer status) { this.status = status; }
    
    @SuppressWarnings("unused") void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    @SuppressWarnings("unused") void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    @SuppressWarnings("unused") void setMonthlyEnergyConsumption(Double monthlyEnergyConsumption) { this.monthlyEnergyConsumption = monthlyEnergyConsumption; }
    
    @SuppressWarnings("unused") void setMonthlyEnergySaved(Double monthlyEnergySaved) { this.monthlyEnergySaved = monthlyEnergySaved; }
    
    @SuppressWarnings("unused") void setSavingRate(Double savingRate) { this.savingRate = savingRate; }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof CampusArea that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override public int hashCode() { return Objects.hash(id); }
}