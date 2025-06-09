package com.chatty.chatservice.config;




import com.chatty.chatservice.service.JWTService;
import com.chatty.chatservice.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URI;
import java.security.Principal;
import java.util.Map;
@Configuration
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        try {
            String token = extractTokenFromQuery(request.getURI().getQuery());

            if (token != null) {
                String username = jwtService.extractUsername(token);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.validateToken(token, userDetails)) {
                        attributes.put("username", username); // Optional
                        return () -> username;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
        }

        return null; // Reject handshake
    }

    private String extractTokenFromQuery(String query) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] parts = param.split("=");
            if (parts.length == 2 && parts[0].equals("token")) {
                return parts[1];
            }
        }
        return null;
    }
}
