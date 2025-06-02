package com.chatty.AuthService.controller;




import com.chatty.AuthService.entity.Users;
import com.chatty.AuthService.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
public class AuthController {

    @Autowired
    private AuthService userService;



    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@RequestBody Users user) {
        System.out.println("the incoming object is "+user);
        return userService.verify(user);
    }



    @PostMapping("/register")
    @ResponseBody
    public Map<String,String> register(@RequestBody  Users user)
    {
        System.out.println("teh incoming object is= "+user);
        return  userService.addUser(user);
    }

    @GetMapping("/ping")
    public String hearBeat(){
        return "hearBeat";
    }


}
