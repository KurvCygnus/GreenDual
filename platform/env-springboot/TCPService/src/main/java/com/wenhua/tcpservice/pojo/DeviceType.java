package com.wenhua.tcpservice.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceType {
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
}
