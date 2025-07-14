package com.chatty.chatservice.service;

import com.chatty.chatservice.events.PresenceUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    private Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    
    public void addUser(String userId) {
        // Add to local cache for fast access
        onlineUsers.add(userId);
        
        // Only notify SSE clients - Redis is handled by AuthService via Kafka
        eventPublisher.publishEvent(new PresenceUpdateEvent(onlineUsers));
        
        System.out.println("✅ User " + userId + " added to online users (Local cache only)");
    }
    
    public void removeUser(String userId) {
        // Remove from local cache
        onlineUsers.remove(userId);
        
        // Only notify SSE clients - Redis cleanup is handled by AuthService via Kafka
        eventPublisher.publishEvent(new PresenceUpdateEvent(onlineUsers));
        
        System.out.println("❌ User " + userId + " removed from online users (Local cache only)");
    }
    
    public boolean isUserOnline(String userId) {
        // Check local cache first (faster)
        if (onlineUsers.contains(userId)) {
            return true;
        }
        
        // Fallback to Redis for accuracy (in case of cache miss)
        return Boolean.TRUE.equals(redisTemplate.hasKey("user:" + userId + ":status"));
    }
    
    public Set<String> getOnlineUsers() {
        return Set.copyOf(onlineUsers);
    }
    
    public void updateOnlineUsers(Set<String> users) {
        onlineUsers.clear();
        onlineUsers.addAll(users);
        
        // Optional: Sync with Redis for consistency
        System.out.println("🔄 Updated online users cache: " + users.size() + " users");
    }
    
    /**
     * Sync local cache with Redis data on startup or periodic refresh
     */
    public void syncWithRedis() {
        Set<String> redisKeys = redisTemplate.keys("user:*:status");
        onlineUsers.clear();
        
        if (redisKeys != null) {
            for (String key : redisKeys) {
                // Extract userId from "user:userId:status" pattern
                String userId = key.split(":")[1];
                onlineUsers.add(userId);
            }
        }
        
        System.out.println("🔄 Synced with Redis: " + onlineUsers.size() + " online users");
    }
    
    /**
     * Called by Kafka consumer to update local cache when Redis changes
     */
    public void onUserStatusChange(String userId, boolean isOnline) {
        if (isOnline) {
            onlineUsers.add(userId);
            System.out.println("🟢 Kafka Event: User " + userId + " came online");
        } else {
            onlineUsers.remove(userId);
            System.out.println("🔴 Kafka Event: User " + userId + " went offline");
        }
        
        // Notify SSE clients of presence change via event
        eventPublisher.publishEvent(new PresenceUpdateEvent(onlineUsers));
    }
}
