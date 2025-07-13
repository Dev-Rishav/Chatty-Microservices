package com.chatty.chatservice.service;

import com.chatty.chatservice.dto.ChatDTO;
import com.chatty.chatservice.events.MessageUpdateEvent;
import com.chatty.chatservice.events.PresenceUpdateEvent;
import com.chatty.chatservice.events.TypingStatusEvent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Service
public class SSEService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ChatService chatService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Extract user ID from JWT token
     */
//    public String getUserIdFromToken(String token) {
//        try {
//            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//            return claims.getSubject();
//        } catch (Exception e) {
//            System.err.println("Failed to extract user ID from token: " + e.getMessage());
//            return null;
//        }
//    }

    /**
     * Notify presence change to all users
     */
    public void notifyPresenceChange(Set<String> onlineUsers) {
        eventPublisher.publishEvent(new PresenceUpdateEvent(onlineUsers));
    }

    /**
     * Notify new message to relevant users
     */
    public void notifyNewMessage(String senderId, String receiverId, Object messageData) {
        eventPublisher.publishEvent(new MessageUpdateEvent(senderId, receiverId, messageData));
        
        // Also update chat lists for both users
        updateChatListForUser(senderId);
        updateChatListForUser(receiverId);
    }

    /**
     * Update chat list for a specific user
     */
    public void updateChatListForUser(String userId) {
        try {
            // This would need the user's token, which we might need to store differently
            // For now, we'll trigger this from the WebSocket message handler
            System.out.println("Chat list update triggered for user: " + userId);
        } catch (Exception e) {
            System.err.println("Failed to update chat list for user " + userId + ": " + e.getMessage());
        }
    }

    /**
     * Notify typing status
     */
    public void notifyTypingStatus(String senderId, String receiverId, boolean isTyping) {
        eventPublisher.publishEvent(new TypingStatusEvent(senderId, receiverId, isTyping));
    }
}
