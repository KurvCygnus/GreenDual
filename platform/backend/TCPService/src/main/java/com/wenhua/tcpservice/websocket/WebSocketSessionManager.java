package com.wenhua.tcpservice.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component @Slf4j
public final class WebSocketSessionManager
{
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public void addSession(WebSocketSession session)
    {
        sessions.add(session);
        log.debug("WebSocket 客户端已连接, 当前连接数: {}", sessions.size());
    }

    public void removeSession(WebSocketSession session)
    {
        sessions.remove(session);
        log.debug("WebSocket 客户端已断开, 当前连接数: {}", sessions.size());
    }

    public static void broadcast(String message)
    {
        if(sessions.isEmpty()) return;

        for(final var session: sessions)
            if(session.isOpen())
                try { session.sendMessage(new TextMessage(message)); }
                catch(IOException e)
                {
                    log.warn("WebSocket 发送消息失败: {}", e.getMessage());
                    sessions.remove(session);
                }
    }
}
