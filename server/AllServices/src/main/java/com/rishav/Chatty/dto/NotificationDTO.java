package com.rishav.Chatty.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private boolean isRead;
    private String message;
    private LocalDateTime createdAt;
    private Long requestId; // Contact request ID for contact request notifications
    private String senderEmail; // Email of the person who sent the request
    private String senderUsername; // Username of the person who sent the request

    public NotificationDTO(String message) {
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

}
