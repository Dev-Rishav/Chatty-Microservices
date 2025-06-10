package com.chatty.chatservice.controller;


import com.chatty.chatservice.dto.ChatDTO;
import com.chatty.chatservice.dto.ChatMessageDTO;
import com.chatty.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload ChatMessageDTO message, Principal principal) {
        String senderEmail = principal.getName();  // Get the sender's email from Principal
        System.out.println("Received message: " + message.toString());

        // Call the service to handle the message sending logic
        chatService.sendPrivateMessage(message, senderEmail);
    }

    //this api returns the list of convo the current user has with other users
    @GetMapping("/allChats")
    public ResponseEntity<List<ChatDTO>> getAllChats(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        System.out.println("Extracted Token: " + token);
        List<ChatDTO> chats=chatService.getAllChats(token);
        return  ResponseEntity.ok(chats);
    }
}

