package com.chatty.AuthService.controller;




import com.chatty.AuthService.dto.TokenHttpRequest;
import com.chatty.AuthService.entity.Users;
import com.chatty.AuthService.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;



    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@RequestBody Users user) {
        System.out.println("the incoming object is "+user);
        return authService.verify(user);
    }



    @PostMapping("/register")
    @ResponseBody
    public Map<String,String> register(@RequestBody  Users user)
    {
        System.out.println("teh incoming object is= "+user);
        return  authService.addUser(user);
    }

    @GetMapping("/ping")
    public String hearBeat(){
        return "hearBeat";
    }


    @PostMapping("/validate")
    @ResponseBody
    public ResponseEntity<?> validate(@RequestBody TokenHttpRequest request){
        return authService.validate(request);
    }

}
