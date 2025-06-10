package com.chatty.Api_Gateway.config;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.DedupeResponseHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class CorsGlobalConfiguration {

//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("*"));
//        config.setAllowedMethods(List.of("*"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setExposedHeaders(List.of("Authorization"));
//        config.setAllowCredentials(true);
//        config.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return new CorsWebFilter(source);
//    }

    @Bean
    public GlobalFilter corsDebugFilter() {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            System.out.println("==== Response Headers ====");
            exchange.getResponse().getHeaders().forEach((k, v) ->
                    System.out.println(k + ": " + v));
        }));
    }
//    @Bean
//    public RouteLocator getRouteLocator(RouteLocatorBuilder builder) {
//        String strategy = DedupeResponseHeaderGatewayFilterFactory.Strategy.RETAIN_LAST.name();
//        return builder.routes()
//                .route(p -> p
//                        .path("/**")
//                        .filters(f -> f
//                                .dedupeResponseHeader("Access-Control-Allow-Origin", strategy)
//                        )
//                        .uri("http://localhost:5173")
//                ).build();
//    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("notification_ws_route", r -> r
                        .path("/ws/**")
                        .uri("ws://localhost:8085")  // ✅ Must be `ws://` not `http://`
                )
                .build();
    }





}

