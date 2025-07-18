package com.chatty.AuthService.service;


import com.chatty.AuthService.dto.TokenHttpRequest;
import com.chatty.AuthService.dto.TokenHttpResponse;
import com.chatty.AuthService.dto.UserDTO;
import com.chatty.AuthService.entity.Users;
import com.chatty.AuthService.repository.UserRepository;
import com.chatty.AuthService.security.JWTSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTSecurity jwtSecurity;

    @Autowired
    private UserRepository repo;

    private final int saltRounds=12;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(saltRounds);
    @Autowired
    private UserDetailsService userDetailsService;

    public Map<String, String> addUser(Users user) {
        Map<String, String> response = new HashMap<>();
        try {
            // Validate email
            String email = user.getEmail();
            if (email == null || !email.contains("@")) {
                response.put("status", "error");
                response.put("message", "Invalid email address");
                return response;
            }

            // Check if user already exists
            Users existingUser = repo.findByEmail(email);
            if (existingUser != null) {
                response.put("status", "error");
                response.put("message", "User with this email already exists");
                return response;
            }

            // Generate username from email
            String username = email.substring(0, email.indexOf('@'));
            user.setUsername(username);

            // Encrypt password
            user.setPassword(encoder.encode(user.getPassword()));

            // Save user
            repo.save(user);
            response.put("status", "success");
            response.put("message", "User registered successfully");
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return response;
        }
    }

    public Map<String,Object> verify(Users user) {
        Authentication authentication=
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword())
                );
        if(authentication.isAuthenticated()) {
//            return jwtService.generateToken(user.getUsername());
            String token= jwtSecurity.generateToken(user.getEmail(),user.getUser_id());
            Users authenticatedUser=repo.findByEmail(user.getEmail());
            System.out.println("token= "+token);
            UserDTO userDTO=new UserDTO();
            userDTO.setUsername(authenticatedUser.getUsername());
            userDTO.setEmail(authenticatedUser.getEmail());
            userDTO.setUser_id(authenticatedUser.getUser_id());

            Map<String, Object> res=new HashMap<>();
            res.put("token",token);
            res.put("userDTO",userDTO);
            return res;
        }
        else
            return Collections.singletonMap("Message","failure");
    }


    public ResponseEntity<?> validate(TokenHttpRequest request) {
        try {
            String username = jwtSecurity.extractUsername(request.getToken());
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtSecurity.validateToken(request.getToken(), userDetails)) {
                return ResponseEntity.ok(new TokenHttpResponse(true,username));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new TokenHttpResponse(false,null));
        }
        return ResponseEntity.status(401).body(new TokenHttpResponse(false,null));
    }

}
