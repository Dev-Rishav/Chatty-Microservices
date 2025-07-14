package com.chatty.chatservice.listener;

import com.chatty.chatservice.service.ActiveChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens to WebSocket connection events to automatically manage chat sessions
 * This implements the optimized approach where WebSocket connection state indicates chat window state
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private ActiveChatSessionService activeChatSessionService;

    // Track which users are connected via WebSocket (indicating they have chat windows open)
    private final ConcurrentHashMap<String, String> webSocketSessions = new ConcurrentHashMap<>();

    /**
     * Handle WebSocket connection - signals that user opened a chat window
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        if (username != null) {
            webSocketSessions.put(sessionId, username);
            
            // WebSocket connection means user has opened a chat window
            // For now, we'll track the connection but wait for actual message activity to determine chat partner
            System.out.println("🔌 WebSocket CONNECTED - User: " + username + " (Session: " + sessionId + ")");
            System.out.println("📱 Chat window implicitly opened for user: " + username);
        }
    }

    /**
     * Handle WebSocket disconnection - signals that user closed chat window
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = webSocketSessions.remove(sessionId);

        if (username != null) {
            // WebSocket disconnection means user closed chat window
            // Close all active chat sessions for this user
            activeChatSessionService.closeAllChatSessions(username);
            
            System.out.println("🔌 WebSocket DISCONNECTED - User: " + username + " (Session: " + sessionId + ")");
            System.out.println("📱 All chat windows implicitly closed for user: " + username);
        }
    }

    /**
     * Get currently connected WebSocket users (those with chat windows open)
     */
    public java.util.Set<String> getConnectedUsers() {
        return new java.util.HashSet<>(webSocketSessions.values());
    }

    /**
     * Check if user has active WebSocket connection (chat window open)
     */
    public boolean isUserConnectedViaWebSocket(String username) {
        return webSocketSessions.containsValue(username);
    }

    /**
     * Get total WebSocket connections count
     */
    public int getActiveWebSocketConnections() {
        return webSocketSessions.size();
    }
}
