package com.wenhua.tcpservice.pojo.ai;

import lombok.Data;

import java.time.LocalTime;

// AI员工实体
@Data
public class AiEmployee {
    private Long aiId;          // 自增主键
    private String name;
    private String jobDescription;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private String dailySummary;
    private Long userId;
    //是否正在工作
    private int isWorking;


    // getters/setters...
}
