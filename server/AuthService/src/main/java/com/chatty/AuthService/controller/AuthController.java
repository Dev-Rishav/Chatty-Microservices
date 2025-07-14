package com.chatty.AuthService.controller;




import com.chatty.AuthService.dto.TokenHttpRequest;
import com.chatty.AuthService.dto.UserDTO;
import com.chatty.AuthService.entity.Users;
import com.chatty.AuthService.security.JWTSecurity;
import com.chatty.AuthService.service.AuthService;
import com.chatty.AuthService.service.UserStatusPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin()
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JWTSecurity  jwtSecurity;

    @Autowired
    private UserStatusPublisher userStatusPublisher;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@RequestBody Users user) {
        System.out.println("the incoming object is "+user);
        Map<String,Object> res= authService.verify(user);
        if(res.get("token")!=null){
            UserDTO userDTO = (UserDTO) res.get("userDTO");
            String userId=userDTO.getEmail();
            //update redis to include this user
            redisTemplate.opsForValue().set("user:"+userId+":status","online");
            //publish event to kafka
            userStatusPublisher.publishUserStatus(userId, true);
            System.out.println("User " +userId+" marked as online in Redis and event published to Kafka.");
        }
        else
            System.out.println("Login failed or incomplete response for user: "+ user.getEmail());
        return res;
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtSecurity.extractUsername(token); // implement token parsing

        if(userId==null){
            return ResponseEntity.badRequest().build();
        }
        try {
            // Remove user from Redis
//            redisTemplate.delete("user:" + userId + ":status");
            redisTemplate.opsForValue().set("user:" + userId + ":status", "offline");

            // Publish logout event to Kafka
            userStatusPublisher.publishUserStatus(userId, false);
            System.out.println(userId + " set to offline and event published to Kafka.");

            return ResponseEntity.ok("Logged out successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error: "+e);
        }
    }

}
