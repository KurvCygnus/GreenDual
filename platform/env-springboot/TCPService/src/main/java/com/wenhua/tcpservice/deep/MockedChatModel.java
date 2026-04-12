package com.wenhua.tcpservice.deep;

import com.wenhua.tcpservice.utils.FunctionalUtils;
import lombok.NonNull;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

public final class MockedChatModel implements ChatModel
{
    private static final Map<String, String> MOCK_RESPONSE_MATCHER = FunctionalUtils.buildImmutableMap(
        map ->
        {
            final String[][] buildLookup =
                {
                    {"environment", "环境", "analyze"},
                    {"temperature", "温度"},
                    {"humidity", "湿度"},
                    {"lightintensity", "光照"},
                    {"路灯", "照明", "亮度"},
                    {"节能", "省电", "能耗"},
                    {"predict", "趋势", "预测"}
                };
            
            final String[] mockResponse =
                {
                    """
                    📊 **环境状况分析报告**
                    
                    🌡️ **温度分析**：当前温度 25°C
                    • 温度适宜，环境舒适
                    
                    💧 **湿度分析**：当前湿度 45%
                    • 湿度适中，体感舒适
                    
                    ☀️ **光照分析**：当前光照 500 lux
                    • 光照适中
                    
                    💡 **改善建议**
                    1. 继续保持环境监测频率
                    2. 根据光照变化自动调节路灯亮度
                    3. 定期检查传感器设备状态
                    4. 建议设置温湿度告警阈值
                    
                    ✅ 预计可节省能源消耗约25%！
                    """,
                    """
                    📊 **环境状况分析报告**
                    
                    🌡️ **温度分析**：当前温度 35°C
                    • 温度偏高，建议加强通风
                    
                    💡 **改善建议**
                    1. 检查散热系统运行状态
                    2. 增加环境巡检频次
                    
                    ✅ 系统已进入高温预警模式。
                    """,
                    """
                    📊 **环境状况分析报告**
                    
                    💧 **湿度分析**：当前湿度 85%
                    • 湿度偏高，注意防潮
                    
                    💡 **改善建议**
                    1. 对路灯控制箱进行除湿处理
                    2. 检查线路绝缘状态
                    
                    ✅ 预警：当前湿度可能影响电子元件寿命。
                    """,
                    """
                    📊 **环境状况分析报告**
                    
                    ☀️ **光照分析**：当前光照 50 lux
                    • 光照较弱，建议提高路灯亮度
                    
                    💡 **改善建议**
                    1. 立即补光，确保路面通行安全
                    2. 检查是否有遮挡物覆盖传感器
                    
                    ✅ 状态：路灯已自动调至 100% 亮度。
                    """,
                    """
                    💡 **智能路灯控制建议**
                    
                    根据当前环境数据分析：
                    • 当前光照充足，建议将路灯亮度调至60%
                    • 预计可节省电力消耗30%
                    
                    🎯 **优化方案**
                    1. 启用自动感光调节模式
                    2. 设置分时段亮度策略
                    """,
                    """
                    📈 **节能分析报告**
                    
                    ✅ **当前优化效果**
                    • 自适应调光策略已节省28%电力
                    • 设备运行效率提升35%
                    
                    🚀 **进一步优化建议**
                    1. 根据人流密度动态调整照明
                    2. 预测性维护减少设备故障
                    """,
                    """
                    📈 **趋势预测报告**
                    
                    根据输入的数据分析，未来该指标将呈现以下变化：
                    • 整体趋势：波动向好，峰值预计出现在午间
                    • 变化幅度：在合理区间内运行
                    
                    💡 **优化方案**
                    1. 建议开启自动调节模式以应对变化
                    2. 定期维护检查灯具状态
                    """
                };
            
            for(int index = 0; index < buildLookup.length; index++)
                for(final String keyword: buildLookup[index])
                    map.put(keyword.toLowerCase(), mockResponse[index]);
        }
    );
    
    @Override public @NonNull ChatResponse call(@NonNull Prompt prompt)
    {
        String userMessage = prompt.getContents();
        String mockContent = generateMockResponse(userMessage);
        
        return new ChatResponse(List.of(new Generation(mockContent)));
    }
    
    private @NonNull String generateMockResponse(@NonNull String message)
    {
        return MOCK_RESPONSE_MATCHER.entrySet().stream().
            filter(entry -> message.toLowerCase().contains(entry.getKey())).
            map(Map.Entry::getValue).
            findFirst().
            orElse("当前回复并未包含调试关键词. 如需全面的测试, 请去`application.properties`中配置`spring.ai.openai.api-key`.");
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
