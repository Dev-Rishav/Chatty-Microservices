package com.chatty.chatservice.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class PresenceUpdateDTO {
    private String email;
    private boolean online;

    public PresenceUpdateDTO() {}

    public PresenceUpdateDTO(String email, boolean online) {
        this.email = email;
        this.online = online;
    }

}


