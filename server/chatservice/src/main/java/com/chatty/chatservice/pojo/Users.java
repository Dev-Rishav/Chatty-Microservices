package com.chatty.chatservice.pojo;

/*
 * pojo is an old java object which have no set of rules, here this was used as the user object for auth stubs
 */

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Users {
    private int userId;
    private String username;
    private String email;
    private String profilePic;
}
