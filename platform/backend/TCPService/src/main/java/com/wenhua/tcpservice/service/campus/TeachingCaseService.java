package com.wenhua.tcpservice.service.campus;

import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.campus.TeachingCase;
import com.wenhua.tcpservice.pojo.dev.Device;
import com.wenhua.tcpservice.utils.StringFormatter;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public final class TeachingCaseService
{
    private final EnvMapper envMapper;

    @Nonnull
    public TeachingCase createTeachingCase(@Nonnull Device device, @Nonnull String caseType)
    {
        //? FIX: 缺少字段`id`
        final TeachingCase teachingCase = new TeachingCase();
        teachingCase.setCaseName(generateCaseName(device, caseType));
        teachingCase.setCaseType(caseType);
        teachingCase.setDeviceId(device.getId());
        teachingCase.setDeviceName(device.getName());
        teachingCase.setCreateTime(LocalDateTime.now());
        teachingCase.setUpdateTime(LocalDateTime.now());
        teachingCase.setStatus(1);
        teachingCase.setUseCount(0);
        
        switch(caseType)
        {
            case "lighting_control" ->
            {
                teachingCase.setDescription("智能路灯控制实验案例");
                teachingCase.setTeachingContent(generateLightingTeachingContent(device));
                teachingCase.setExperimentGuide(generateLightingExperimentGuide());
                teachingCase.setRelatedCourse("物联网技术基础、嵌入式系统设计");
            }
            case "environment_monitor" ->
            {
                teachingCase.setDescription("环境监测数据采集实验案例");
                teachingCase.setTeachingContent(generateEnvironmentTeachingContent(device));
                teachingCase.setExperimentGuide(generateEnvironmentExperimentGuide());
                teachingCase.setRelatedCourse("传感器技术、数据采集与处理");
            }
            case "energy_saving" ->
            {
                teachingCase.setDescription("智能节能控制实验案例");
                teachingCase.setTeachingContent(generateEnergySavingTeachingContent(device));
                teachingCase.setExperimentGuide(generateEnergySavingExperimentGuide());
                teachingCase.setRelatedCourse("智能控制、能源管理");
            }
            default ->
            {
                teachingCase.setDescription("物联网综合应用案例");
                teachingCase.setTeachingContent(generateGeneralTeachingContent(device));
                teachingCase.setExperimentGuide(generateGeneralExperimentGuide());
                teachingCase.setRelatedCourse("物联网工程导论");
            }
        }
        
        teachingCase.setDataPreview(generateDataPreview(device));
        teachingCase.setAnalysisResult(generateAnalysisResult(device, caseType));
        teachingCase.setDataSource("实时设备数据");
        
        log.debug("创建教学案例: {}", teachingCase.getCaseName());
        return teachingCase;
    }
    
    private String generateCaseName(Device device, String caseType)
    {
        String typeName = switch(caseType)
        {
            case "lighting_control" -> "智能路灯控制";
            case "environment_monitor" -> "环境监测";
            case "energy_saving" -> "节能控制";
            default -> "综合应用";
        };
        return StringFormatter.format("{}-{}-实验案例", device.getName(), typeName);
    }
    
    private @NonNull String generateLightingTeachingContent(@NonNull Device device)
    {
        return StringFormatter.format(
            """
            【教学目标】
            1. 理解智能路灯控制系统的工作原理
            2. 掌握基于光照强度的自动调光算法
            3. 学习物联网设备的远程控制方法
            
            【知识点】
            • 光照传感器原理与应用
            • PWM调光技术
            • TCP/IP通信协议
            • 嵌入式系统编程
            
            【设备参数】
            设备ID: {}
            设备名称: {}
            当前亮度: {}
            光照强度: {} Lux
            路灯状态: {}
            """,
            device.getId(),
            device.getName(),
            device.getBrightness(),
            device.getLightIntensity(),
            device.getLampStatus() == 1 ? "开启" : "关闭"
        );
    }
    
    private @NonNull String generateLightingExperimentGuide()
    {
        return """
            【实验步骤】
            
            实验一：基础控制实验
            1. 连接设备，查看当前路灯状态
            2. 发送开关指令，观察路灯响应
            3. 调节亮度（0-100%），记录响应时间
            
            实验二：自动调光实验
            1. 设置光照阈值参数
            2. 模拟不同光照环境
            3. 观察系统自动调光效果
            4. 记录节能数据
            
            实验三：综合应用实验
            1. 设计分时段调光策略
            2. 实现基于光照的自动控制
            3. 计算节能效果
            4. 撰写实验报告
            """;
    }
    
    private @NonNull String generateEnvironmentTeachingContent(Device device)
    {
        return StringFormatter.format(
            """
            【教学目标】
            1. 掌握环境监测传感器的使用方法
            2. 学习数据采集与处理技术
            3. 理解环境监测系统的整体架构
            
            【知识点】
            • 温湿度传感器原理
            • 光照传感器原理
            • 模数转换技术
            • 数据滤波与处理
            
            【实时数据】
            温度: {}°C
            湿度: {}
            光照强度: {} Lux
            """,
            device.getTemperature(),
            device.getHumidity(),
            device.getLightIntensity()
        );
    }
    
    private @NonNull String generateEnvironmentExperimentGuide()
    {
        return """
            【实验步骤】
            
            实验一：传感器校准
            1. 连接传感器模块
            2. 读取原始数据
            3. 进行校准操作
            
            实验二：数据采集
            1. 设置采样频率
            2. 采集环境数据
            3. 存储到数据库
            
            实验三：数据分析
            1. 导出历史数据
            2. 绘制趋势图表
            3. 分析环境变化规律
            """;
    }
    
    private @NonNull String generateEnergySavingTeachingContent(Device device)
    {
        return StringFormatter.format(
            """
            【教学目标】
            1. 理解智能节能控制原理
            2. 掌握能耗计算方法
            3. 学习节能策略设计
            
            【知识点】
            • 能耗计算模型
            • 节能算法设计
            • 成本效益分析
            • 碳排放计算
            
            【节能数据】
            当前亮度: {}
            节能率: {}
            预计月省电费: {}元
            """,
            device.getBrightness(),
            100 - device.getBrightness(),
            String.format("%.2f", calculateMonthlySaving(device))
        );
    }
    
    private @NonNull String generateEnergySavingExperimentGuide()
    {
        return """
            【实验步骤】
            
            实验一：能耗测量
            1. 测量不同亮度下的功耗
            2. 计算能耗差异
            3. 绘制能耗曲线
            
            实验二：节能策略设计
            1. 分析使用场景
            2. 设计调光策略
            3. 模拟节能效果
            
            实验三：效益评估
            1. 计算节能收益
            2. 评估投资回报
            3. 撰写节能报告
            """;
    }
    
    private @NonNull String generateGeneralTeachingContent(Device device)
    {
        return StringFormatter.format(
            """
            【教学目标】
            1. 了解物联网系统整体架构
            2. 学习设备接入与管理
            3. 掌握数据可视化方法
            
            【设备信息】
            设备ID: {}
            设备名称: {}
            设备类型: {}
            在线状态: {}
            """,
            device.getId(),
            device.getName(),
            device.getDeviceType(),
            device.getOnLine() == 1 ? "在线" : "离线"
        );
    }
    
    private @NonNull String generateGeneralExperimentGuide()
    {
        return """
            【实验步骤】
            
            1. 设备注册与接入
            2. 数据采集与展示
            3. 远程控制操作
            4. 数据分析与报告
            """;
    }
    
    private @NonNull String generateDataPreview(Device device)
    {
        return StringFormatter.format(
            """
            {
              "deviceId": "{}",
              "name": "{}",
              "temperature": {},
              "humidity": {},
              "lightIntensity": {},
              "brightness": {},
              "lampStatus": {},
              "timestamp": "{}"
            }
            """,
            device.getId(),
            device.getName(),
            device.getTemperature(),
            device.getHumidity(),
            device.getLightIntensity(),
            device.getBrightness(),
            device.getLampStatus(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
    
    private @NonNull String generateAnalysisResult(Device device, String caseType)
    {
        final StringBuilder result = new StringBuilder();
        result.append("【数据分析结果】\n\n");
        
        switch(caseType)
        {
            case "lighting_control", "energy_saving" ->
            {
                result.append("当前亮度: ").append(device.getBrightness()).append("%\n");
                result.append("光照强度: ").append(device.getLightIntensity()).append(" Lux\n");
                result.append("节能潜力: ").append(100 - device.getBrightness()).append("%\n");
                result.append("建议亮度: ").append(calculateSuggestedBrightness(device)).append("%\n");
            }
            case "environment_monitor" ->
            {
                result.append("温度状态: ").append(device.getTemperature() > 30 ? "偏高" : "正常").append("\n");
                result.append("湿度状态: ").append(device.getHumidity() > 70 ? "偏高" : "正常").append("\n");
                result.append("光照状态: ").append(device.getLightIntensity() > 500 ? "充足" : "不足").append("\n");
            }
        }
        
        return result.toString();
    }
    
    private int calculateSuggestedBrightness(@NonNull Device device)
    {
        if(device.getLightIntensity() != null)
        {
            if(device.getLightIntensity() > 500)
                return 30;
            if(device.getLightIntensity() > 300)
                return 50;
            if(device.getLightIntensity() > 100)
                return 70;
        }
        
        return 80;
    }
    
    private double calculateMonthlySaving(@NonNull Device device)
    {
        int brightness = device.getBrightness() != null ? device.getBrightness() : 100;
        double savingRate = (100 - brightness) / 100.;
        return 150 * 10 * 30 * savingRate * .6 / 1000;
    }
    
    @Nonnull
    public List<TeachingCase> getTeachingCasesByCourse(@Nonnull String courseName)
    {
        final List<TeachingCase> cases = new ArrayList<>();
        final List<Device> devices = envMapper.selectAllDevice();
        
        for(final Device device: devices)
        {
            if(courseName.contains("物联网") || courseName.contains("嵌入式"))
                cases.add(createTeachingCase(device, "lighting_control"));
            if(courseName.contains("传感器") || courseName.contains("数据采集"))
                cases.add(createTeachingCase(device, "environment_monitor"));
            if(courseName.contains("控制") || courseName.contains("能源"))
                cases.add(createTeachingCase(device, "energy_saving"));
        }
        
        return cases;
    }
    
    @Nonnull
    public Map<String, Object> getTeachingStatistics()
    {
        Map<String, Object> stats = new HashMap<>();
        
        List<Device> devices = envMapper.selectAllDevice();
        
        stats.put("totalCases", devices.size() * 3);
        stats.put("lightingCases", devices.size());
        stats.put("environmentCases", devices.size());
        stats.put("energyCases", devices.size());
        stats.put(
            "relatedCourses",
            Arrays.asList(
                "物联网技术基础",
                "嵌入式系统设计",
                "传感器技术",
                "数据采集与处理",
                "智能控制",
                "能源管理"
            )
        );
        
        return stats;
    }
}
