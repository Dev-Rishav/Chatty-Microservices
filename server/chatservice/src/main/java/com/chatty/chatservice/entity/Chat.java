package com.chatty.chatservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Chat {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int chatId;

    private String user1Id;
    private String user2Id;
    private LocalDateTime createdAt;


}