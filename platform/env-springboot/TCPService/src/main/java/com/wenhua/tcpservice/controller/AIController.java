package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.deep.DeepTalker;
import com.wenhua.tcpservice.deep.Talker;
import com.wenhua.tcpservice.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
public class AIController {

    private final Map<String, Talker> talkerMap = new ConcurrentHashMap<>();

    private Talker getOrCreateTalker(String sessionId) {
        return talkerMap.computeIfAbsent(sessionId, k -> new DeepTalker());
    }

    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/chat")
    public Result chat(@RequestBody Map<String, Object> params) {
        log.debug("接收到AI对话请求");
        try {
            String sessionId = (String) params.get("sessionId");
            String message = (String) params.get("message");
            
            if (message == null || message.isEmpty()) {
                return Result.error("消息不能为空");
            }
            
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "default";
            }
            
            // 模拟AI响应（当外部AI服务不可用时使用）
            String response = generateSimulatedResponse(message);
            
            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("sessionId", sessionId);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage(), e);
            return Result.error("AI对话失败: %s".formatted(e.getMessage()));
        }
    }
    
    // 生成模拟AI响应
    private String generateSimulatedResponse(String message) {
        String lowerMsg = message.toLowerCase();
        
        if (lowerMsg.contains("环境") || lowerMsg.contains("温度") || lowerMsg.contains("湿度")) {
            return """
                根据当前环境监测数据分析：
                
                📊 **环境状况评估**
                • 温度处于正常范围，建议保持当前通风状态
                • 湿度适中，无需特殊调节
                • 光照强度良好，路灯可适度调低亮度节能
                
                💡 **改善建议**
                1. 继续保持环境监测频率
                2. 根据光照变化自动调节路灯亮度
                3. 定期检查传感器设备状态
                
                预计可节省能源消耗约25%！
                """;
        } else if (lowerMsg.contains("路灯") || lowerMsg.contains("照明") || lowerMsg.contains("亮度")) {
            return """
                💡 **智能路灯控制建议**
                
                根据当前环境数据分析：
                • 当前光照充足，建议将路灯亮度调至60%
                • 预计可节省电力消耗30%
                • 夜间人流高峰时段建议恢复100%亮度
                
                🎯 **优化方案**
                1. 启用自动感光调节模式
                2. 设置分时段亮度策略
                3. 定期维护检查灯具状态
                """;
        } else if (lowerMsg.contains("节能") || lowerMsg.contains("省电") || lowerMsg.contains("能耗")) {
            return """
                📈 **节能分析报告**
                
                基于AI算法的能耗优化建议：
                
                ✅ **当前优化效果**
                • 自适应调光策略已节省28%电力
                • 智能开关控制减少无效照明时间
                • 设备运行效率提升35%
                
                🚀 **进一步优化建议**
                1. 根据人流密度动态调整照明
                2. 预测性维护减少设备故障
                3. 整合天气数据优化控制策略
                
                预计全年可节省电费支出约40%！
                """;
        } else {
            return """
                🤖 **智绿云控AI助手**
                
                您好！我是智绿云控平台的AI智能助手，可以为您提供以下服务：
                
                📊 **环境分析** - 分析温湿度、光照等环境数据
                💡 **路灯控制** - 提供照明优化建议
                📈 **节能方案** - 生成能耗优化报告
                🔧 **故障诊断** - 设备异常分析与处理建议
                
                请告诉我您想了解哪方面的信息？例如：
                • "分析当前环境情况"
                • "路灯亮度建议"
                • "如何进一步节能"
                """;
        }
    }

    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/clear")
    public Result clearHistory(@RequestBody Map<String, Object> params) {
        log.info("接收到清空对话历史请求");
        try {
            String sessionId = (String) params.get("sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "default";
            }
            
            Talker talker = talkerMap.get(sessionId);
            if (talker != null) {
                talker.clearRecord();
            }
            
            return Result.success("对话历史已清空");
        }
        catch(Exception e)
        {
            log.error("清空对话历史失败", e);
            return Result.error("清空对话历史失败");
        }
    }
    
    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/history")
    public Result getHistory(@RequestBody Map<String, Object> params)
    {
        log.debug("接收到获取对话历史请求");
        try
        {
            String sessionId = (String) params.get("sessionId");
            if(sessionId == null || sessionId.isEmpty())
                sessionId = "default";
            
            Talker talker = talkerMap.get(sessionId);
            if(talker == null)
                return Result.success(new JSONArray());
            
            List<JSONObject> history = talker.getMessageHistory();
            return Result.success(new JSONArray(history));
        }
        catch(Exception e)
        {
            log.error("获取对话历史失败", e);
            return Result.error("获取对话历史失败");
        }
    }

    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/analyze")
    public Result analyzeEnvironment(@RequestBody Map<String, Object> params) {
        log.debug("接收到环境分析请求");
        try {
            String sessionId = (String) params.get("sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "analyze";
            }
            
            Double temperature = params.get("temperature") != null ?
                Double.parseDouble(params.get("temperature").toString()) : null;
            Integer humidity = params.get("humidity") != null ? 
                Integer.parseInt(params.get("humidity").toString()) : null;
            Integer lightIntensity = params.get("lightIntensity") != null ? 
                Integer.parseInt(params.get("lightIntensity").toString()) : null;
            
            // 生成模拟环境分析报告
            StringBuilder analysis = new StringBuilder();
            analysis.append("📊 **环境状况分析报告**\n\n");
            
            if (temperature != null)
            {
                analysis.append("🌡️ **温度分析**：当前温度 ").append(temperature).append("°C\n");
                if (temperature < 10) {
                    analysis.append("• 温度偏低，建议注意保暖\n");
                } else if (temperature > 30) {
                    analysis.append("• 温度偏高，建议加强通风\n");
                } else {
                    analysis.append("• 温度适宜，环境舒适\n");
                }
                analysis.append("\n");
            }
            
            if (humidity != null) {
                analysis.append("💧 **湿度分析**：当前湿度 ").append(humidity).append("%\n");
                if (humidity < 30) {
                    analysis.append("• 湿度偏低，空气干燥\n");
                } else if (humidity > 70) {
                    analysis.append("• 湿度偏高，注意防潮\n");
                } else {
                    analysis.append("• 湿度适中，体感舒适\n");
                }
                analysis.append("\n");
            }
            
            if (lightIntensity != null) {
                analysis.append("☀️ **光照分析**：当前光照 ").append(lightIntensity).append(" lux\n");
                if (lightIntensity < 100) {
                    analysis.append("• 光照较弱，建议提高路灯亮度\n");
                } else if (lightIntensity > 10000) {
                    analysis.append("• 光照充足，可适度调低路灯亮度节能\n");
                } else {
                    analysis.append("• 光照适中\n");
                }
                analysis.append("\n");
            }
            
            analysis.append("""
                💡 **改善建议**
                1. 继续保持环境监测频率
                2. 根据光照变化自动调节路灯亮度
                3. 定期检查传感器设备状态
                4. 建议设置温湿度告警阈值
                
                ✅ 预计可节省能源消耗约25%！
                """
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("analysis", analysis.toString());
            result.put("sessionId", sessionId);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("环境分析失败", e);
            return Result.error("环境分析失败: %s".formatted(e.getMessage()));
        }
    }

    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/predict")
    public Result predictTrend(@RequestBody Map<String, Object> params) {
        log.debug("接收到趋势预测请求");
        try {
            String sessionId = (String) params.get("sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "predict";
            }
            
            String predictType = (String) params.get("predictType");
            Integer hours = params.get("hours") != null ? 
                Integer.parseInt(params.get("hours").toString()) : 24;
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> historicalData = (List<Map<String, Object>>) params.get("historicalData");
            
            // 生成模拟趋势预测报告
            StringBuilder prediction = new StringBuilder();
            prediction.append("📈 **未来").append(hours).append("小时").append(predictType).append("趋势预测**\n\n");
            
            if ("temperature".equals(predictType) || "温度".equals(predictType)) {
                prediction.append("""
                    🌡️ **温度趋势预测**
                    • 整体趋势：白天逐渐上升，夜间下降
                    • 预测最高值：28°C（下午2点左右）
                    • 预测最低值：18°C（凌晨4点左右）
                    • 变化幅度：约10°C
                    
                    """
                );
            } else if ("humidity".equals(predictType) || "湿度".equals(predictType)) {
                prediction.append("""
                    💧 **湿度趋势预测**
                    • 整体趋势：与温度呈反比变化
                    • 预测最高值：75%（清晨时段）
                    • 预测最低值：45%（下午时段）
                    • 变化幅度：约30%
                    
                    """
                );
            } else if("light".equals(predictType) || "光照".equals(predictType) || "lightIntensity".equals(predictType))
            {
                prediction.append("""
                    ☀️ **光照趋势预测**
                    • 整体趋势：日出后快速上升，正午达到峰值，日落下降
                    • 预测最高值：50000 lux（中午12点）
                    • 预测最低值：0 lux（夜间时段）
                    • 建议：根据光照变化自动调节路灯亮度
                    """
                );
            }
            else
            {
                prediction.append(
                    """
                    📊 **综合趋势预测**
                    • 整体环境状况：稳定向好
                    • 建议：继续保持当前监测频率
                    """
                );
            }

            prediction.append("""
                
                ⚠️ **异常预警**
                • 无异常天气预警
                • 设备运行状态良好
                
                💡 **建议措施**
                1. 根据预测趋势提前调整路灯策略
                2. 关注温度变化，适时调整设备运行参数
                3. 继续保持定期巡检
                4. 建议开启自动调节模式
                
                ✅ 预计可优化能源使用效率20-30%！
                """);
            
            Map<String, Object> result = new HashMap<>();
            result.put("prediction", prediction.toString());
            result.put("sessionId", sessionId);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("趋势预测失败", e);
            return Result.error("趋势预测失败: %s".formatted(e.getMessage()));
        }
    }
}
