package com.chatty.chatservice.filter;

import com.chatty.protos.auth.AuthServiceGrpc;
import com.chatty.protos.auth.TokenRequest;
import com.chatty.protos.auth.TokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthServiceGrpc.AuthServiceBlockingStub authStub;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromHeader(request);

        if (token != null) {
            try {
                TokenResponse tokenResponse = validateTokenWithAuthService(token);

                if (tokenResponse.getValid()) {
                    String email = tokenResponse.getEmail();
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (Exception ex) {
                System.err.println("Token validation failed: " + ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private TokenResponse validateTokenWithAuthService(String token) {
        TokenRequest tokenRequest = TokenRequest.newBuilder()
                .setToken(token)
                .build();
        return authStub.validateToken(tokenRequest);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
