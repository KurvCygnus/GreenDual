package com.wenhua.tcpservice.deep;

import lombok.NonNull;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public final class MockedChatModel implements ChatModel
{
    @Override public @NonNull ChatResponse call(@NonNull Prompt prompt)
    {
        String userMessage = prompt.getContents();
        String mockContent = generateMockResponse(userMessage);
        
        return new ChatResponse(List.of(new Generation(mockContent)));
    }
    
    private @NonNull String generateMockResponse(@NonNull String message)
    {
        String lowerMsg = message.toLowerCase();
        
        if(lowerMsg.contains("环境") || lowerMsg.contains("温度") || lowerMsg.contains("湿度"))
        {
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
        }
        else if(lowerMsg.contains("路灯") || lowerMsg.contains("照明") || lowerMsg.contains("亮度"))
        {
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
        }
        else if(lowerMsg.contains("节能") || lowerMsg.contains("省电") || lowerMsg.contains("能耗"))
        {
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
        }
        // 为了保证Mock和真实配置的AI可区分, 应当禁用这个结果输出.
//        else
//        {
//            return """
//                🤖 **智绿云控AI助手**
//
//                您好！我是智绿云控平台的AI智能助手，可以为您提供以下服务：
//
//                📊 **环境分析** - 分析温湿度、光照等环境数据
//                💡 **路灯控制** - 提供照明优化建议
//                📈 **节能方案** - 生成能耗优化报告
//                🔧 **故障诊断** - 设备异常分析与处理建议
//
//                请告诉我您想了解哪方面的信息？例如：
//                • "分析当前环境情况"
//                • "路灯亮度建议"
//                • "如何进一步节能"
//                """;
//        }
        
        return "当前回复并未包含调试关键词. 如需全面的测试, 请去`application.properties`中配置`spring.ai.openai.api-key`.";
    }
    
    @Override public ChatOptions getDefaultOptions() { return MockedChatOptions.INST; }
    
    private enum MockedChatOptions implements ChatOptions
    {
        INST;
        
        @Override public @NonNull Float getTemperature() { return 1F; }
        
        @Override public @NonNull Float getTopP() { return 0F; }
        
        @Override public @NonNull Integer getTopK() { return 0; }
    }
}
