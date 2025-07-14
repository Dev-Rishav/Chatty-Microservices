package com.chatty.chatservice.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to track which users have which chat windows open
 */
@Service
public class ActiveChatSessionService {
    
    // Map: userId -> Set of chatPartnerIds (who they're actively chatting with)
    private final ConcurrentHashMap<String, Set<String>> activeChatSessions = new ConcurrentHashMap<>();
    
    /**
     * Mark that a user has opened a chat window with another user
     */
    public void openChatSession(String userId, String chatPartnerId) {
        activeChatSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(chatPartnerId);
        System.out.println("📱 User " + userId + " opened chat with " + chatPartnerId);
    }
    
    /**
     * Mark that a user has closed a chat window with another user
     */
    public void closeChatSession(String userId, String chatPartnerId) {
        Set<String> userSessions = activeChatSessions.get(userId);
        if (userSessions != null) {
            userSessions.remove(chatPartnerId);
            if (userSessions.isEmpty()) {
                activeChatSessions.remove(userId);
            }
        }
        System.out.println("📱 User " + userId + " closed chat with " + chatPartnerId);
    }
    
    /**
     * Check if a user has a specific chat window open
     */
    public boolean hasChatWindowOpen(String userId, String chatPartnerId) {
        Set<String> userSessions = activeChatSessions.get(userId);
        return userSessions != null && userSessions.contains(chatPartnerId);
    }
    
    /**
     * Close all chat sessions for a user (when they go offline)
     */
    public void closeAllChatSessions(String userId) {
        activeChatSessions.remove(userId);
        System.out.println("📱 Closed all chat sessions for user: " + userId);
    }
    
    /**
     * Get all active chat sessions for a user
     */
    public Set<String> getActiveChatSessions(String userId) {
        return activeChatSessions.getOrDefault(userId, Set.of());
    }
    
    /**
     * Check if user is actively chatting with anyone
     */
    public boolean hasAnyActiveSessions(String userId) {
        Set<String> sessions = activeChatSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
    
    /**
     * Get all users who currently have chat windows open
     */
    public Set<String> getAllActiveUsers() {
        return activeChatSessions.keySet();
    }
    
    /**
     * Get count of active chat sessions
     */
    public int getActiveChatSessionCount() {
        return activeChatSessions.values().stream().mapToInt(Set::size).sum();
    }
}
