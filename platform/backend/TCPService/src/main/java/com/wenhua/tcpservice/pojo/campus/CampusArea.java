package com.wenhua.tcpservice.pojo.campus;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CampusArea {
    private Integer id;
    private String areaName;
    private String areaType;
    private Double areaSize;
    private Integer lampCount;
    private Integer deviceCount;
    private String managerName;
    private String managerPhone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private Double monthlyEnergyConsumption;
    private Double monthlyEnergySaved;
    private Double savingRate;
}
