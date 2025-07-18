package com.chatty.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.security.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatId")
    private Chat chat;


    private String content;
    private LocalDateTime timestamp;
    private String file_url;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}

