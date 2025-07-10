package com.chatty.notificationservice.config;

import com.chatty.notificationservice.service.JWTService;
import com.chatty.notificationservice.service.MyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

@Configuration
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomHandshakeHandler.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        try {
            String token = extractTokenFromQuery(request.getURI());

            if (token != null) {
                String username = jwtService.extractUsername(token);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.validateToken(token, userDetails)) {
                        attributes.put("username", username); // Optional: store for future use
                        logger.info("✅ WebSocket authenticated for user: {}", username);
                        return () -> username;
                    }
                }
            }

            logger.warn("❌ WebSocket token invalid or user not found");
            throw new IllegalArgumentException("Unauthorized WebSocket connection: invalid token");

        } catch (Exception e) {
            logger.error("❌ WebSocket handshake failed: {}", e.getMessage());
            throw new IllegalArgumentException("Unauthorized WebSocket connection: " + e.getMessage(), e);
        }
    }

    private String extractTokenFromQuery(URI uri) {
        String query = uri.getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] parts = param.split("=");
            if (parts.length == 2 && parts[0].equals("token")) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
