package com.chatty.chatservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserStatusConsumer {

    @Autowired
    private OnlineUserService onlineUserService;

    @KafkaListener(topics = "user-status-topic", groupId = "chatservice")
    public void handleUserStatus(String message) {
        String[] parts = message.split(":");
        String userId = parts[0];
        String status = parts[1];
        boolean isOnline = "online".equals(status);

        // Only Kafka-based presence management - Redis is handled by AuthService
        onlineUserService.onUserStatusChange(userId, isOnline);

        System.out.println("📨 Kafka: Updated OnlineUserService for user " + userId + " to " + status + " (Single source of truth)");
    }
}

