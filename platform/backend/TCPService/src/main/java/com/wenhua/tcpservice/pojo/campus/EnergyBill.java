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
 * 能耗账单 —— 对外不可变，通过 {@link JsonCreator} 构造。
 * <p>包级私有 setter 供 MyBatis 反射调用。</p>
 */
@ToString
public final class EnergyBill
{
    @Getter private Integer id;
    @Getter private Integer areaId;
    @Getter private String areaName;
    @Getter private String billMonth;
    @Getter private Double totalConsumption;
    @Getter private Double totalCost;
    @Getter private Double savedConsumption;
    @Getter private Double savedCost;
    @Getter private Double savingRate;
    @Getter private Double peakConsumption;
    @Getter private Double offPeakConsumption;
    @Getter private Double averageDailyConsumption;
    private String reportUrl;
    @Getter private Integer status;
    private LocalDateTime createTime;
    private String wasteAnalysis;
    private String optimizationSuggestions;
    
    @JsonCreator
    public EnergyBill(
        @JsonProperty("id") Integer id,
        @JsonProperty("areaId") Integer areaId,
        @JsonProperty("areaName") String areaName,
        @JsonProperty("billMonth") String billMonth,
        @JsonProperty("totalConsumption") Double totalConsumption,
        @JsonProperty("totalCost") Double totalCost,
        @JsonProperty("savedConsumption") Double savedConsumption,
        @JsonProperty("savedCost") Double savedCost,
        @JsonProperty("savingRate") Double savingRate,
        @JsonProperty("peakConsumption") Double peakConsumption,
        @JsonProperty("offPeakConsumption") Double offPeakConsumption,
        @JsonProperty("averageDailyConsumption") Double averageDailyConsumption,
        @JsonProperty("reportUrl") String reportUrl,
        @JsonProperty("status") Integer status,
        @JsonProperty("createTime") LocalDateTime createTime,
        @JsonProperty("wasteAnalysis") String wasteAnalysis,
        @JsonProperty("optimizationSuggestions") String optimizationSuggestions
    )
    {
        this.id = id;
        this.areaId = areaId;
        this.areaName = areaName;
        this.billMonth = billMonth;
        this.totalConsumption = totalConsumption;
        this.totalCost = totalCost;
        this.savedConsumption = savedConsumption;
        this.savedCost = savedCost;
        this.savingRate = savingRate;
        this.peakConsumption = peakConsumption;
        this.offPeakConsumption = offPeakConsumption;
        this.averageDailyConsumption = averageDailyConsumption;
        this.reportUrl = reportUrl;
        this.status = status;
        this.createTime = createTime;
        this.wasteAnalysis = wasteAnalysis;
        this.optimizationSuggestions = optimizationSuggestions;
    }
    
    public @NonNull Optional<String> getReportUrl() { return Optional.ofNullable(reportUrl); }
    
    public @NonNull Optional<LocalDateTime> getCreateTime() { return Optional.ofNullable(createTime); }
    
    public @NonNull Optional<String> wasteAnalysis() { return Optional.ofNullable(wasteAnalysis); }
    
    public @NonNull Optional<String> optimizationSuggestions() { return Optional.ofNullable(optimizationSuggestions); }
    
    /**
     * MyBatis 反序列化用。
     */
    EnergyBill() { }
    
    /**
     * @deprecated 仅被Mybatis使用, 因为这个傻逼框架连 <u>{@link Optional}</u> 都不知道怎么处理, 杂鱼
     */
    @Deprecated String getWasteAnalysis() { return wasteAnalysis; }
    
    /**
     * @deprecated 仅被Mybatis使用, 因为这个傻逼框架连 <u>{@link Optional}</u> 都不知道怎么处理, 杂鱼
     */
    @Deprecated String getOptimizationSuggestions() { return optimizationSuggestions; }
    
    void setId(Integer id) { this.id = id; }
    
    void setAreaId(Integer areaId) { this.areaId = areaId; }
    
    void setAreaName(String areaName) { this.areaName = areaName; }
    
    void setBillMonth(String billMonth) { this.billMonth = billMonth; }
    
    void setTotalConsumption(Double totalConsumption) { this.totalConsumption = totalConsumption; }
    
    void setTotalCost(Double totalCost) { this.totalCost = totalCost; }
    
    void setSavedConsumption(Double savedConsumption) { this.savedConsumption = savedConsumption; }
    
    void setSavedCost(Double savedCost) { this.savedCost = savedCost; }
    
    void setSavingRate(Double savingRate) { this.savingRate = savingRate; }
    
    void setPeakConsumption(Double peakConsumption) { this.peakConsumption = peakConsumption; }
    
    void setOffPeakConsumption(Double offPeakConsumption) { this.offPeakConsumption = offPeakConsumption; }
    
    void setAverageDailyConsumption(Double averageDailyConsumption) { this.averageDailyConsumption = averageDailyConsumption; }
    
    void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }
    
    void setStatus(Integer status) { this.status = status; }
    
    void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    void setWasteAnalysis(String wasteAnalysis) { this.wasteAnalysis = wasteAnalysis; }
    
    void setOptimizationSuggestions(String optimizationSuggestions) { this.optimizationSuggestions = optimizationSuggestions; }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof EnergyBill that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override public int hashCode() { return Objects.hash(id); }
}