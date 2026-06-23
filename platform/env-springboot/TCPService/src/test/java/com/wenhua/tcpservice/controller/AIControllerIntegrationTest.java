package com.wenhua.tcpservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wenhua.tcpservice.interceptor.LoginCheckInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AIControllerIntegrationTest
{
    @MockBean
    private LoginCheckInterceptor loginCheckInterceptor;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception
    {
        when(loginCheckInterceptor.preHandle(
            any(HttpServletRequest.class),
            any(HttpServletResponse.class),
            any(Object.class)
        )).thenReturn(true);
    }

    @Test
    void testChatEndpoint() throws Exception
    {
        mockMvc.perform(post("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "analyze environment")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.data.response").exists());
    }

    @Test
    void testChatEndpoint_EmptyMessage() throws Exception
    {
        mockMvc.perform(post("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void testChatEndpoint_MissingMessage() throws Exception
    {
        mockMvc.perform(post("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of()))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void testAnalyzeEndpoint() throws Exception
    {
        mockMvc.perform(post("/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "temperature", 25,
                    "humidity", 60,
                    "lightIntensity", 500
                )))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.data.analysis").exists());
    }

    @Test
    void testAnalyzeEndpoint_MissingParams() throws Exception
    {
        mockMvc.perform(post("/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("foo", "bar")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void testHistoryEndpoint() throws Exception
    {
        mockMvc.perform(post("/ai/history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("sessionId", "test-session")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testClearHistoryEndpoint() throws Exception
    {
        mockMvc.perform(post("/ai/clear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("sessionId", "test-session")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void testPredictEndpoint() throws Exception
    {
        mockMvc.perform(post("/ai/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "predictType", "温度",
                    "sessionId", "predict-test"
                )))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1))
            .andExpect(jsonPath("$.data.prediction").exists());
    }

    @Test
    void testChatWithSession() throws Exception
    {
        mockMvc.perform(post("/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "message", "analyze environment",
                    "sessionId", "session-1"
                )))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/ai/history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("sessionId", "session-1")))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1));
    }
}