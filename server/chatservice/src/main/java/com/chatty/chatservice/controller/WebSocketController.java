package com.chatty.chatservice.controller;

import com.chatty.chatservice.dto.ChatMessageDTO;
import com.chatty.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/ws")
public class WebSocketController {
    private final ChatService chatService;


    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload ChatMessageDTO message, Principal principal) {
        String senderEmail = principal.getName();  // Get the sender's email from Principal
        System.out.println("Received message: " + message.toString());

        // Call the service to handle the message sending logic
        chatService.sendPrivateMessage(message, senderEmail);
    }
}
