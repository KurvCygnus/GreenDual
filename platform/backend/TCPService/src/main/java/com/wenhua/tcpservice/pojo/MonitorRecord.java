package com.wenhua.tcpservice.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MonitorRecord {
    private Integer id;
    private String deviceId;
    private String deviceName;
    private LocalDateTime recordTime;
    private String dataType;
    private Double value;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
}
