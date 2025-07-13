package com.chatty.chatservice.controller;

import com.chatty.chatservice.dto.ChatMessageDTO;
import com.chatty.chatservice.service.ChatService;
import com.chatty.chatservice.service.SSEService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private SSEService sseService;


    @MessageMapping("/private-message")
    public void sendPrivateMessage(@Payload ChatMessageDTO message, Principal principal) {
        String senderEmail = principal.getName();  // Get the sender's email from Principal
        System.out.println("Received message: " + message.toString());

        // Call the service to handle the message sending logic
        ChatMessageDTO savedMessage = chatService.sendPrivateMessage(message, senderEmail);
        
        // Notify SSE subscribers about the new message
        if (savedMessage != null) {
            sseService.notifyNewMessage(savedMessage.getFrom(), savedMessage.getTo(), savedMessage);
        }
    }
    
    @MessageMapping("/typing")
    public void handleTypingStatus(@Payload TypingMessage typingMessage, Principal principal) {
        String senderEmail = principal.getName();
        System.out.println("Typing status: " + typingMessage.toString());
        
        // Notify SSE subscribers about typing status
        sseService.notifyTypingStatus(senderEmail, typingMessage.getReceiverId(), typingMessage.isTyping());
    }
    
    /**
     * DTO for typing status messages
     */
    public static class TypingMessage {
        private String receiverId;
        private boolean isTyping;
        
        public TypingMessage() {}
        
        public TypingMessage(String receiverId, boolean isTyping) {
            this.receiverId = receiverId;
            this.isTyping = isTyping;
        }
        
        public String getReceiverId() { return receiverId; }
        public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
        public boolean isTyping() { return isTyping; }
        public void setTyping(boolean typing) { isTyping = typing; }
        
        @Override
        public String toString() {
            return "TypingMessage{receiverId='" + receiverId + "', isTyping=" + isTyping + "}";
        }
    }
}
