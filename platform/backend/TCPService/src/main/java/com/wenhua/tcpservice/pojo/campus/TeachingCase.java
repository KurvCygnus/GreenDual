package com.wenhua.tcpservice.pojo.campus;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeachingCase {
    private Integer id;
    private String caseName;
    private String caseType;
    private String description;
    private String deviceId;
    private String deviceName;
    private String dataSource;
    private String teachingContent;
    private String experimentGuide;
    private String relatedCourse;
    private Integer useCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private String dataPreview;
    private String analysisResult;
}
