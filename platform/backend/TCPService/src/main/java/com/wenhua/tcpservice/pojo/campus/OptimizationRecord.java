package com.wenhua.tcpservice.pojo.campus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 照明优化记录 —— 对外不可变，通过 {@link JsonCreator} 构造。
 * <p>包级私有 setter 供 MyBatis 反射调用。</p>
 */
@Getter @ToString public final class OptimizationRecord
{
    private Integer id;
    private Integer areaId;
    private Integer totalDevices;
    private Integer adjustedDevices;
    private Double averageSavingRate;
    private Double estimatedMonthlySaving;
    private Integer status;
    private LocalDateTime createTime;

    @JsonCreator
    public OptimizationRecord(
        @JsonProperty("id") Integer id,
        @JsonProperty("areaId") Integer areaId,
        @JsonProperty("totalDevices") Integer totalDevices,
        @JsonProperty("adjustedDevices") Integer adjustedDevices,
        @JsonProperty("averageSavingRate") Double averageSavingRate,
        @JsonProperty("estimatedMonthlySaving") Double estimatedMonthlySaving,
        @JsonProperty("status") Integer status,
        @JsonProperty("createTime") LocalDateTime createTime
    )
    {
        this.id = id;
        this.areaId = areaId;
        this.totalDevices = totalDevices;
        this.adjustedDevices = adjustedDevices;
        this.averageSavingRate = averageSavingRate;
        this.estimatedMonthlySaving = estimatedMonthlySaving;
        this.status = status;
        this.createTime = createTime;
    }

    /**
     * MyBatis 反序列化用。
     */
    OptimizationRecord() { }

    void setId(Integer id) { this.id = id; }
    void setAreaId(Integer areaId) { this.areaId = areaId; }
    void setTotalDevices(Integer totalDevices) { this.totalDevices = totalDevices; }
    void setAdjustedDevices(Integer adjustedDevices) { this.adjustedDevices = adjustedDevices; }
    void setAverageSavingRate(Double averageSavingRate) { this.averageSavingRate = averageSavingRate; }
    void setEstimatedMonthlySaving(Double estimatedMonthlySaving) { this.estimatedMonthlySaving = estimatedMonthlySaving; }
    void setStatus(Integer status) { this.status = status; }
    void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof OptimizationRecord that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }
}
