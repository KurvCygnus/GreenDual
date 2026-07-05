package com.wenhua.tcpservice.pojo.campus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 教学案例 —— 对外不可变，通过 {@link JsonCreator} 构造。
 * <p>包级私有 setter 供 MyBatis 反射调用。</p>
 */
@Getter @ToString public final class TeachingCase
{
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
    
    @JsonCreator
    public TeachingCase(
        @JsonProperty("id") Integer id,
        @JsonProperty("caseName") String caseName,
        @JsonProperty("caseType") String caseType,
        @JsonProperty("description") String description,
        @JsonProperty("deviceId") String deviceId,
        @JsonProperty("deviceName") String deviceName,
        @JsonProperty("dataSource") String dataSource,
        @JsonProperty("teachingContent") String teachingContent,
        @JsonProperty("experimentGuide") String experimentGuide,
        @JsonProperty("relatedCourse") String relatedCourse,
        @JsonProperty("useCount") Integer useCount,
        @JsonProperty("status") Integer status,
        @JsonProperty("createTime") LocalDateTime createTime,
        @JsonProperty("updateTime") LocalDateTime updateTime,
        @JsonProperty("dataPreview") String dataPreview,
        @JsonProperty("analysisResult") String analysisResult
    )
    {
        this.id = id;
        this.caseName = caseName;
        this.caseType = caseType;
        this.description = description;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.dataSource = dataSource;
        this.teachingContent = teachingContent;
        this.experimentGuide = experimentGuide;
        this.relatedCourse = relatedCourse;
        this.useCount = useCount;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.dataPreview = dataPreview;
        this.analysisResult = analysisResult;
    }
    
    /**
     * MyBatis 反序列化用。
     */
    TeachingCase() { }
    
    void setId(Integer id) { this.id = id; }
    
    void setCaseName(String caseName) { this.caseName = caseName; }
    
    void setCaseType(String caseType) { this.caseType = caseType; }
    
    void setDescription(String description) { this.description = description; }
    
    void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    void setDataSource(String dataSource) { this.dataSource = dataSource; }
    
    void setTeachingContent(String teachingContent) { this.teachingContent = teachingContent; }
    
    void setExperimentGuide(String experimentGuide) { this.experimentGuide = experimentGuide; }
    
    void setRelatedCourse(String relatedCourse) { this.relatedCourse = relatedCourse; }
    
    void setUseCount(Integer useCount) { this.useCount = useCount; }
    
    void setStatus(Integer status) { this.status = status; }
    
    void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    void setDataPreview(String dataPreview) { this.dataPreview = dataPreview; }
    
    void setAnalysisResult(String analysisResult) { this.analysisResult = analysisResult; }
    
    @Override public boolean equals(Object o)
    {
        if(this == o) return true;
        if(!(o instanceof TeachingCase that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override public int hashCode() { return Objects.hash(id); }
}