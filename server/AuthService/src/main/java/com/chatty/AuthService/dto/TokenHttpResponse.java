package com.chatty.AuthService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TokenHttpResponse {
    private boolean valid;
    private String username;

}
