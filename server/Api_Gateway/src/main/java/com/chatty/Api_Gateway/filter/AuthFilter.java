package com.chatty.Api_Gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();


        if (exchange.getRequest().getMethod().name().equalsIgnoreCase("OPTIONS")) {
            return chain.filter(exchange);
        }

        // List of public routes that don't require authentication
        if (isPublicRoute(path)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();

        List<String> authHeaders = request.getHeaders().getOrEmpty("Authorization");
        if (authHeaders.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeaders.get(0).replace("Bearer ", "");

        return webClientBuilder.build().post()
                .uri("lb://authservice/auth/validate") // ✅ Eureka + LoadBalancer URI
                .bodyValue(new TokenRequest(token))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .flatMap(response -> {
                    if (!response.isValid()) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Name", response.getUsername())
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(ex -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/auth/login") ||
                path.startsWith("/auth/register") ||
                path.startsWith("/auth/validate")
                || path.startsWith("/sse")||
                path.startsWith("/presence");

    }

    @Override
    public int getOrder() {
        return -1;
    }

    // Internal DTOs (should ideally match your HTTP model used in Auth service)
    static class TokenRequest {
        private String token;

        public TokenRequest() {} // Required for serialization
        public TokenRequest(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    static class TokenResponse {
        private boolean valid;
        private String username;

        public TokenResponse() {} // Required for deserialization

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
