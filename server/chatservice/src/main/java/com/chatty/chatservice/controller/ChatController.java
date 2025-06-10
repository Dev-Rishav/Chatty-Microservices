package com.chatty.chatservice.controller;


import com.chatty.chatservice.dto.ChatDTO;
import com.chatty.chatservice.dto.ChatMessageDTO;
import com.chatty.chatservice.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    //this api returns the list of convo the current user has with other users
    @GetMapping("/allChats")
    public ResponseEntity<List<ChatDTO>> getAllChats(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        System.out.println("Extracted Token: " + token);
        List<ChatDTO> chats=chatService.getAllChats(token);
        return  ResponseEntity.ok(chats);
    }
}

