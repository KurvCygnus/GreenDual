package com.wenhua.tcpservice.controller;

import com.wenhua.tcpservice.config.GlobalConfiguration;
import com.wenhua.tcpservice.pojo.Result;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController @Slf4j
public class AIController
{
    /**
     * @implNote <span style="color: red">
     *     这里不建议, 也不应该使用 <u>{@link org.springframework.beans.factory.annotation.Autowired Autowired}</u> 或者
     *      <u>{@link lombok.AllArgsConstructor AllArgsConstructor}</u> 来注入字段.
     * </span><br>
     * Spring AI 将 <u>{@link org.springframework.ai.chat.model.ChatModel ChatModel}</u>
     * 设计为单例, 这代表着其是<b>有状态的, 可定制的, 是AI后端的连接池, 因此Spring Bean不提供默认的Bean来注入.</b>
     * @see AIController#AIController(ChatClient.Builder, ChatMemory)
     */
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    public AIController(ChatClient.@NonNull Builder builder, @NonNull ChatMemory chatMemory)
    {
        this.chatClient = builder.
            defaultSystem(GlobalConfiguration.AI_INITIALIZATION_SYSTEM_INFO).
            defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory)).
            build();
        this.chatMemory = chatMemory;
    }

    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/chat")
    public Result chat(@RequestBody Map<String, Object> params) {
        log.debug("接收到AI对话请求");
        try {
            String tempId = (String) params.get("sessionId");
            final String sessionId = tempId == null || tempId.isEmpty() ? "default" : tempId;
            String message = (String) params.get("message");
            
            if (message == null || message.isEmpty()) {
                return Result.error("消息不能为空");
            }
            
            String response = chatClient.prompt().
                user(message).
                advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId)).
                call().
                content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("sessionId", tempId);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage(), e);
            return Result.error("AI对话失败: %s".formatted(e.getMessage()));
        }
    }
    
    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/clear")
    public Result clearHistory(@RequestBody Map<String, Object> params)
    {
        try
        {
            String sessionId = (String) params.getOrDefault("sessionId", "default");
            chatMemory.clear(sessionId);
            return Result.success("会话 [%s] 历史已清空".formatted(sessionId));
        }
        catch(Exception e) { return Result.error("清空失败"); }
    }
    
    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/history")
    public Result getHistory(@RequestBody Map<String, Object> params)
    {
        try
        {
            String sessionId = (String) params.getOrDefault("sessionId", "default");
            return Result.success(chatMemory.get(sessionId, 100));
        }
        catch(Exception e) { return Result.error("获取历史失败"); }
    }
    
    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/analyze")
    public Result analyzeEnvironment(@RequestBody Map<String, Object> params)
    {
        log.debug("接收到环境分析请求");
        try
        {
            String dataContext = params.entrySet().stream().
                filter(entry -> List.of("temperature", "humidity", "lightIntensity").contains(entry.getKey())).
                map(entry -> "%s:%s".formatted(entry.getKey(), entry.getValue())).
                collect(Collectors.joining(", "));
            
            if(dataContext.isEmpty())
                return Result.error("缺少环境参数");
            
            String response = chatClient.prompt().
                system("你是一个智能路灯专家，请分析环境数据并提供Markdown格式的报告。").
                user("分析数据：%s".formatted(dataContext)).
                call().
                content();
            
            return Result.success(
                Map.of(
                    "analysis", response,
                    "sessionId", params.getOrDefault("sessionId", "analyze")
                )
            );
        }
        catch(Exception e)
        {
            log.error("分析失败", e);
            return Result.error("分析失败: %s".formatted(e.getMessage()));
        }
    }
    
    @PostMapping(GlobalConfiguration.AI_REQUEST_PREFIX + "/predict")
    public Result predictTrend(@RequestBody Map<String, Object> params)
    {
        log.debug("接收到趋势预测请求");
        try
        {
            String sessionId = params.getOrDefault("sessionId", "predict").toString();
            String predictType = (String) params.getOrDefault("predictType", "综合");
            Object hours = params.getOrDefault("hours", 24);
            
            @SuppressWarnings("unchecked") List<Map<String, Object>> historicalData = (List<Map<String, Object>>) params.get("historicalData");
            String historySummary = (historicalData != null) ? historicalData.toString() : "暂无历史数据";
            
            String response = chatClient.prompt().
                system("你是一个环境数据分析专家，请根据历史数据预测未来趋势并提供Markdown报告。").
                user("预测类型：%s, 预测时长：%s小时, 历史参考数据：%s".
                    formatted(predictType, hours, historySummary)).
                call().
                content();
            
            return Result.success(
                Map.of(
                    "prediction", response,
                    "sessionId", sessionId
                )
            );
        }
        catch(Exception e)
        {
            log.error("趋势预测失败", e);
            return Result.error("趋势预测失败: %s".formatted(e.getMessage()));
        }
    }
}
