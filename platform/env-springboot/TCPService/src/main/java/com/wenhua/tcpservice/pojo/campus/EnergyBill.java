package com.wenhua.tcpservice.pojo.campus;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnergyBill {
    private Integer id;
    private Integer areaId;
    private String areaName;
    private String billMonth;
    private Double totalConsumption;
    private Double totalCost;
    private Double savedConsumption;
    private Double savedCost;
    private Double savingRate;
    private Double peakConsumption;
    private Double offPeakConsumption;
    private Double averageDailyConsumption;
    private String reportUrl;
    private Integer status;
    private LocalDateTime createTime;
    
    private String wasteAnalysis;
    private String optimizationSuggestions;
}
