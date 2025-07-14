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
import org.springframework.context.event.EventListener;
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

    @Autowired
    private ActiveChatSessionService activeChatSessionService;

    @Autowired
    private OnlineUserService onlineUserService;

    @Value("${jwt.secret}")
    private String jwtSecret;
    

    /**
     * Notify presence change to all users
     */
    public void notifyPresenceChange(Set<String> onlineUsers) {
        eventPublisher.publishEvent(new PresenceUpdateEvent(onlineUsers));
    }

    /**
     * Notify new message to relevant users - only if they don't have the chat window open
     */
    public void notifyNewMessage(String senderId, String receiverId, Object messageData) {
        // Check if receiver is online (has SSE connection)
        boolean receiverIsOnline = onlineUserService.isUserOnline(receiverId);
        
        if (receiverIsOnline) {
            // Check if receiver has the chat window open with the sender
            boolean receiverHasChatOpen = activeChatSessionService.hasChatWindowOpen(receiverId, senderId);
            
            if (!receiverHasChatOpen) {
                // Only notify if receiver doesn't have the chat window open
                System.out.println("📨 Sending SSE notification: " + senderId + " -> " + receiverId + " (chat window not open)");
                eventPublisher.publishEvent(new MessageUpdateEvent(senderId, receiverId, messageData));
                
                // Update chat list for receiver (to show new message indicator)
                updateChatListForUser(receiverId);
            } else {
                System.out.println("🔇 Skipping SSE notification: " + receiverId + " has chat window open with " + senderId);
            }
        } else {
            System.out.println("🔇 Skipping SSE notification: " + receiverId + " is offline");
        }
        
        // Always update chat list for sender (they sent the message)
        updateChatListForUser(senderId);
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
    
    /**
     * Mark that a user opened a chat window with another user
     */
    public void markChatWindowOpened(String userId, String chatPartnerId) {
        activeChatSessionService.openChatSession(userId, chatPartnerId);
    }
    
    /**
     * Mark that a user closed a chat window with another user
     */
    public void markChatWindowClosed(String userId, String chatPartnerId) {
        activeChatSessionService.closeChatSession(userId, chatPartnerId);
    }
    
    /**
     * Close all chat windows for a user (when they go offline)
     */
    public void closeAllChatWindows(String userId) {
        activeChatSessionService.closeAllChatSessions(userId);
    }
}
