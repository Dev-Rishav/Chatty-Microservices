package com.chatty.AuthService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserStatusPublisher {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publishUserStatus(String userId, boolean online) {
        String message = userId + ":" + (online ? "online" : "offline");
        kafkaTemplate.send("user-status-topic", message);
    }
}

