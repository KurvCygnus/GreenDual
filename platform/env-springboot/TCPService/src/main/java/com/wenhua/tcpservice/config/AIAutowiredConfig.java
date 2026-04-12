package com.wenhua.tcpservice.config;

import com.wenhua.tcpservice.deep.MockedChatModel;
import lombok.NonNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIAutowiredConfig
{
    @Value("${spring.ai.openai.base-url}") private String aiBaseUrl;
    
    @Bean @ConditionalOnProperty("spring.ai.openai.api-key")
    public @NonNull ChatModel intializeChatModel(@NonNull @Value("${spring.ai.openai.api-key}") String apiKey)
        { return new OpenAiChatModel(new OpenAiApi(aiBaseUrl, apiKey)); }
    
    @Bean @ConditionalOnMissingBean(ChatModel.class)
    public @NonNull ChatModel mockChatModel() { return new MockedChatModel(); }
    
    @Bean public @NonNull ChatMemory chatMemory() { return new InMemoryChatMemory(); }
}
