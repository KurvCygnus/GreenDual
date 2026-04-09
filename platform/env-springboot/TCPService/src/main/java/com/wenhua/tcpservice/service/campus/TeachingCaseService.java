package com.wenhua.tcpservice.service.campus;

import com.wenhua.tcpservice.mapper.EnvMapper;
import com.wenhua.tcpservice.pojo.campus.TeachingCase;
import com.wenhua.tcpservice.pojo.dev.Device;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class TeachingCaseService
{
    private EnvMapper envMapper;
    
    public TeachingCase createTeachingCase(Device device, String caseType)
    {
        TeachingCase teachingCase = new TeachingCase();
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
            case "lighting_control":
                teachingCase.setDescription("智能路灯控制实验案例");
                teachingCase.setTeachingContent(generateLightingTeachingContent(device));
                teachingCase.setExperimentGuide(generateLightingExperimentGuide());
                teachingCase.setRelatedCourse("物联网技术基础、嵌入式系统设计");
                break;
            case "environment_monitor":
                teachingCase.setDescription("环境监测数据采集实验案例");
                teachingCase.setTeachingContent(generateEnvironmentTeachingContent(device));
                teachingCase.setExperimentGuide(generateEnvironmentExperimentGuide());
                teachingCase.setRelatedCourse("传感器技术、数据采集与处理");
                break;
            case "energy_saving":
                teachingCase.setDescription("智能节能控制实验案例");
                teachingCase.setTeachingContent(generateEnergySavingTeachingContent(device));
                teachingCase.setExperimentGuide(generateEnergySavingExperimentGuide());
                teachingCase.setRelatedCourse("智能控制、能源管理");
                break;
            default:
                teachingCase.setDescription("物联网综合应用案例");
                teachingCase.setTeachingContent(generateGeneralTeachingContent(device));
                teachingCase.setExperimentGuide(generateGeneralExperimentGuide());
                teachingCase.setRelatedCourse("物联网工程导论");
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
        return device.getName() + "-" + typeName + "-实验案例";
    }
    
    private String generateLightingTeachingContent(Device device)
    {
        StringBuilder content = new StringBuilder();
        content.append("【教学目标】\n");
        content.append("1. 理解智能路灯控制系统的工作原理\n");
        content.append("2. 掌握基于光照强度的自动调光算法\n");
        content.append("3. 学习物联网设备的远程控制方法\n\n");
        
        content.append("【知识点】\n");
        content.append("• 光照传感器原理与应用\n");
        content.append("• PWM调光技术\n");
        content.append("• TCP/IP通信协议\n");
        content.append("• 嵌入式系统编程\n\n");
        
        content.append("【设备参数】\n");
        content.append("设备ID: ").append(device.getId()).append("\n");
        content.append("设备名称: ").append(device.getName()).append("\n");
        content.append("当前亮度: ").append(device.getBrightness()).append("%\n");
        content.append("光照强度: ").append(device.getLightIntensity()).append(" Lux\n");
        content.append("路灯状态: ").append(device.getLampStatus() == 1 ? "开启" : "关闭").append("\n");
        
        return content.toString();
    }
    
    private String generateLightingExperimentGuide()
    {
        StringBuilder guide = new StringBuilder();
        guide.append("【实验步骤】\n\n");
        guide.append("实验一：基础控制实验\n");
        guide.append("1. 连接设备，查看当前路灯状态\n");
        guide.append("2. 发送开关指令，观察路灯响应\n");
        guide.append("3. 调节亮度（0-100%），记录响应时间\n\n");
        
        guide.append("实验二：自动调光实验\n");
        guide.append("1. 设置光照阈值参数\n");
        guide.append("2. 模拟不同光照环境\n");
        guide.append("3. 观察系统自动调光效果\n");
        guide.append("4. 记录节能数据\n\n");
        
        guide.append("实验三：综合应用实验\n");
        guide.append("1. 设计分时段调光策略\n");
        guide.append("2. 实现基于光照的自动控制\n");
        guide.append("3. 计算节能效果\n");
        guide.append("4. 撰写实验报告\n");
        
        return guide.toString();
    }
    
    private String generateEnvironmentTeachingContent(Device device)
    {
        StringBuilder content = new StringBuilder();
        content.append("【教学目标】\n");
        content.append("1. 掌握环境监测传感器的使用方法\n");
        content.append("2. 学习数据采集与处理技术\n");
        content.append("3. 理解环境监测系统的整体架构\n\n");
        
        content.append("【知识点】\n");
        content.append("• 温湿度传感器原理\n");
        content.append("• 光照传感器原理\n");
        content.append("• 模数转换技术\n");
        content.append("• 数据滤波与处理\n\n");
        
        content.append("【实时数据】\n");
        content.append("温度: ").append(device.getTemperature()).append("°C\n");
        content.append("湿度: ").append(device.getHumidity()).append("%\n");
        content.append("光照强度: ").append(device.getLightIntensity()).append(" Lux\n");
        
        return content.toString();
    }
    
    private String generateEnvironmentExperimentGuide()
    {
        StringBuilder guide = new StringBuilder();
        guide.append("【实验步骤】\n\n");
        guide.append("实验一：传感器校准\n");
        guide.append("1. 连接传感器模块\n");
        guide.append("2. 读取原始数据\n");
        guide.append("3. 进行校准操作\n\n");
        
        guide.append("实验二：数据采集\n");
        guide.append("1. 设置采样频率\n");
        guide.append("2. 采集环境数据\n");
        guide.append("3. 存储到数据库\n\n");
        
        guide.append("实验三：数据分析\n");
        guide.append("1. 导出历史数据\n");
        guide.append("2. 绘制趋势图表\n");
        guide.append("3. 分析环境变化规律\n");
        
        return guide.toString();
    }
    
    private String generateEnergySavingTeachingContent(Device device)
    {
        StringBuilder content = new StringBuilder();
        content.append("【教学目标】\n");
        content.append("1. 理解智能节能控制原理\n");
        content.append("2. 掌握能耗计算方法\n");
        content.append("3. 学习节能策略设计\n\n");
        
        content.append("【知识点】\n");
        content.append("• 能耗计算模型\n");
        content.append("• 节能算法设计\n");
        content.append("• 成本效益分析\n");
        content.append("• 碳排放计算\n\n");
        
        content.append("【节能数据】\n");
        content.append("当前亮度: ").append(device.getBrightness()).append("%\n");
        content.append("节能率: ").append(100 - device.getBrightness()).append("%\n");
        content.append("预计月省电费: ").append(String.format("%.2f", calculateMonthlySaving(device))).append("元\n");
        
        return content.toString();
    }
    
    private String generateEnergySavingExperimentGuide()
    {
        StringBuilder guide = new StringBuilder();
        guide.append("【实验步骤】\n\n");
        guide.append("实验一：能耗测量\n");
        guide.append("1. 测量不同亮度下的功耗\n");
        guide.append("2. 计算能耗差异\n");
        guide.append("3. 绘制能耗曲线\n\n");
        
        guide.append("实验二：节能策略设计\n");
        guide.append("1. 分析使用场景\n");
        guide.append("2. 设计调光策略\n");
        guide.append("3. 模拟节能效果\n\n");
        
        guide.append("实验三：效益评估\n");
        guide.append("1. 计算节能收益\n");
        guide.append("2. 评估投资回报\n");
        guide.append("3. 撰写节能报告\n");
        
        return guide.toString();
    }
    
    private String generateGeneralTeachingContent(Device device)
    {
        StringBuilder content = new StringBuilder();
        content.append("【教学目标】\n");
        content.append("1. 了解物联网系统整体架构\n");
        content.append("2. 学习设备接入与管理\n");
        content.append("3. 掌握数据可视化方法\n\n");
        
        content.append("【设备信息】\n");
        content.append("设备ID: ").append(device.getId()).append("\n");
        content.append("设备名称: ").append(device.getName()).append("\n");
        content.append("设备类型: ").append(device.getDeviceType()).append("\n");
        content.append("在线状态: ").append(device.getOnLine() == 1 ? "在线" : "离线").append("\n");
        
        return content.toString();
    }
    
    private String generateGeneralExperimentGuide()
    {
        StringBuilder guide = new StringBuilder();
        guide.append("【实验步骤】\n\n");
        guide.append("1. 设备注册与接入\n");
        guide.append("2. 数据采集与展示\n");
        guide.append("3. 远程控制操作\n");
        guide.append("4. 数据分析与报告\n");
        
        return guide.toString();
    }
    
    private String generateDataPreview(Device device)
    {
        StringBuilder preview = new StringBuilder();
        preview.append("{\n");
        preview.append("  \"deviceId\": \"").append(device.getId()).append("\",\n");
        preview.append("  \"name\": \"").append(device.getName()).append("\",\n");
        preview.append("  \"temperature\": ").append(device.getTemperature()).append(",\n");
        preview.append("  \"humidity\": ").append(device.getHumidity()).append(",\n");
        preview.append("  \"lightIntensity\": ").append(device.getLightIntensity()).append(",\n");
        preview.append("  \"brightness\": ").append(device.getBrightness()).append(",\n");
        preview.append("  \"lampStatus\": ").append(device.getLampStatus()).append(",\n");
        preview.append("  \"timestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\"\n");
        preview.append("}");
        return preview.toString();
    }
    
    private String generateAnalysisResult(Device device, String caseType)
    {
        StringBuilder result = new StringBuilder();
        result.append("【数据分析结果】\n\n");
        
        if("lighting_control".equals(caseType) || "energy_saving".equals(caseType))
        {
            result.append("当前亮度: ").append(device.getBrightness()).append("%\n");
            result.append("光照强度: ").append(device.getLightIntensity()).append(" Lux\n");
            result.append("节能潜力: ").append(100 - device.getBrightness()).append("%\n");
            result.append("建议亮度: ").append(calculateSuggestedBrightness(device)).append("%\n");
        }
        
        if("environment_monitor".equals(caseType))
        {
            result.append("温度状态: ").append(device.getTemperature() > 30 ? "偏高" : "正常").append("\n");
            result.append("湿度状态: ").append(device.getHumidity() > 70 ? "偏高" : "正常").append("\n");
            result.append("光照状态: ").append(device.getLightIntensity() > 500 ? "充足" : "不足").append("\n");
        }
        
        return result.toString();
    }
    
    private int calculateSuggestedBrightness(Device device)
    {
        if(device.getLightIntensity() != null)
        {
            if(device.getLightIntensity() > 500) return 30;
            if(device.getLightIntensity() > 300) return 50;
            if(device.getLightIntensity() > 100) return 70;
        }
        return 80;
    }
    
    private double calculateMonthlySaving(Device device)
    {
        int brightness = device.getBrightness() != null ? device.getBrightness() : 100;
        double savingRate = (100 - brightness) / 100.0;
        return 150 * 10 * 30 * savingRate * 0.6 / 1000;
    }
    
    public List<TeachingCase> getTeachingCasesByCourse(String courseName)
    {
        List<TeachingCase> cases = new ArrayList<>();
        List<Device> devices = envMapper.selectAllDevice();
        
        for(Device device: devices)
        {
            if(courseName.contains("物联网") || courseName.contains("嵌入式"))
            {
                cases.add(createTeachingCase(device, "lighting_control"));
            }
            if(courseName.contains("传感器") || courseName.contains("数据采集"))
            {
                cases.add(createTeachingCase(device, "environment_monitor"));
            }
            if(courseName.contains("控制") || courseName.contains("能源"))
            {
                cases.add(createTeachingCase(device, "energy_saving"));
            }
        }
        
        return cases;
    }
    
    public Map<String, Object> getTeachingStatistics()
    {
        Map<String, Object> stats = new HashMap<>();
        
        List<Device> devices = envMapper.selectAllDevice();
        
        stats.put("totalCases", devices.size() * 3);
        stats.put("lightingCases", devices.size());
        stats.put("environmentCases", devices.size());
        stats.put("energyCases", devices.size());
        stats.put("relatedCourses", Arrays.asList(
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
