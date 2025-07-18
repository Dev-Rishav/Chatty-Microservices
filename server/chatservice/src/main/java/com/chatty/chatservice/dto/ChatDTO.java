package com.chatty.chatservice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(JsonInclude.Include.NON_NULL)
//this DTO is ued for sending chat data to the frontend /allChats
public class ChatDTO {
    private Integer id; // Changed from int to Integer to allow null values
    private String email;
    private String username;
    private String lastMessage;
    private String fileUrl;
    private LocalDateTime timestamp;

    //nullable
    private Integer unread;
    private Boolean isGroup;
    private String profilePic;



}
