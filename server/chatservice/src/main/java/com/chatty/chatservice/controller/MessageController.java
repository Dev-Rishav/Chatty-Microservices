package com.chatty.chatservice.controller;


import com.chatty.chatservice.dto.ChatMessageDTO;
import com.chatty.chatservice.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/between")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesBetweenUsers(
            @RequestParam String user) {
        System.out.println(" user2= "+user);
        List<ChatMessageDTO> messages = messageService.getMessagesBetweenUsers(user);
        System.out.println("messages= "+messages);
        return ResponseEntity.ok(messages);
    }
}
