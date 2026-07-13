package com.wenhua.tcpservice.websocket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component @Slf4j @RequiredArgsConstructor
public final class DataWebSocketHandler extends TextWebSocketHandler
{
    private final WebSocketSessionManager sessionManager;

    @Override public void afterConnectionEstablished(@NonNull WebSocketSession session) { sessionManager.addSession(session); }

    @Override public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status)
        { sessionManager.removeSession(session); }

    @Override protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message)
    {
        // 当前为单向推送模式, 忽略客户端上行消息.
    }
}
