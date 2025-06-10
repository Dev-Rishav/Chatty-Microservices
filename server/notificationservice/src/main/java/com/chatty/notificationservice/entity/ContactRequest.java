package com.chatty.notificationservice.entity;



import com.chatty.notificationservice.dto.Users;
import com.chatty.notificationservice.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ContactRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    private String receiver;

    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

}


