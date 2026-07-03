package com.wenhua.tcpservice.pojo.ai;

import lombok.Data;

import java.time.LocalDateTime;

// AI行动记录实体
@Data

public class AiActionLog {
    private Long actionId;      // 自增主键
    private Long aiId;          // 关联AI员工
    private String actionDescription;
    private LocalDateTime actionTime;

    // getters/setters...
}