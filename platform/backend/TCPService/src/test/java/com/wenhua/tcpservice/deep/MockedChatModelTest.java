package com.wenhua.tcpservice.deep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import static org.junit.jupiter.api.Assertions.*;

final class MockedChatModelTest
{
    private final MockedChatModel model = new MockedChatModel();

    @Test void testCallReturnsNonNullResponse()
    {
        ChatResponse response = model.call(new Prompt("hello environment"));
        assertNotNull(response);
        assertNotNull(response.getResult());
        assertNotNull(response.getResult().getOutput());
        assertNotNull(response.getResult().getOutput().getContent());
    }

    @ParameterizedTest @CsvSource({
        "temperature analysis please, 温度",
        "今天湿度怎么样, 湿度",
        "光照强度多少, 光照",
        "analyze environment, 环境",
        "路灯亮度调节, 照明",
        "节能方案, 节能",
        "predict trend, 预测",
        "能耗分析, 能耗"
    }) void testKeywordMatching_Chinese(String input, String expectedKeyword)
    {
        ChatResponse response = model.call(new Prompt(input));
        String content = response.getResult().getOutput().getContent();
        assertNotNull(content);
        assertFalse(content.isBlank(), "Response should not be blank for input: " + input);
    }

    @Test void testUnknownInputReturnsFallback()
    {
        ChatResponse response = model.call(new Prompt("completely unrelated input xxxyyyzzz"));
        String content = response.getResult().getOutput().getContent();
        assertTrue(
            content.contains("api-key") || content.contains("API Key"),
            "Fallback response should mention API key configuration. Got: " + content
        );
    }

    @Test void testEmptyPrompt()
    {
        ChatResponse response = model.call(new Prompt(""));
        String content = response.getResult().getOutput().getContent();
        assertNotNull(content);
    }

    @Test void testEnvironmentKeywordTriggersEnvironmentReport()
    {
        ChatResponse response = model.call(new Prompt("analyze the environment data"));
        String content = response.getResult().getOutput().getContent();
        assertTrue(
            content.contains("环境状况分析") || content.contains("环境分析"),
            "Environment keyword should trigger environment analysis report. Got: " + content
        );
    }

    @Test void testTemperatureKeyword()
    {
        ChatResponse response = model.call(new Prompt("what is the temperature"));
        String content = response.getResult().getOutput().getContent();
        assertTrue(
            content.contains("温度分析") || content.contains("温度"),
            "Temperature keyword should trigger temperature analysis. Got: " + content
        );
    }

    @Test void testGetDefaultOptions()
    {
        var options = model.getDefaultOptions();
        assertNotNull(options);
        assertEquals(1.0F, options.getTemperature());
        assertEquals(0.0F, options.getTopP());
        assertEquals(0, options.getTopK());
    }
}