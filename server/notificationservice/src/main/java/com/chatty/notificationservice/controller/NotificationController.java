package com.chatty.notificationservice.controller;



import com.chatty.notificationservice.entity.Notification;
import com.chatty.notificationservice.service.NotificationService;
import com.chatty.protos.auth.AuthServiceGrpc;
import com.chatty.protos.auth.TokenRequest;
import com.chatty.protos.auth.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notf")
public class NotificationController {

    @Autowired
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;
    @Autowired
    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotificationHistory(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        System.out.println("token: " + token);
        TokenRequest    tokenRequest = TokenRequest.newBuilder().setToken(token).build();
        TokenResponse res=authServiceBlockingStub.validateToken(tokenRequest);
        String email = res.getEmail();
        List<Notification> notifications = notificationService.getNotificationsForUser(email);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}

