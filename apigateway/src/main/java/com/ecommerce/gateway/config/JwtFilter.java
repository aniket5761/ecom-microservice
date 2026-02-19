package com.ecommerce.gateway.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import java.security.Key;

@Configuration
public class JwtFilter {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Bean
    public GlobalFilter jwtGlobalFilter() {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // Allow register & login without token
            if (path.contains("/api/users/register") ||
                    path.contains("/api/users/login")) {
                return chain.filter(exchange);
            }

            String header = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = header.substring(7);

            try {
                Jwts.parserBuilder()
                        .setSigningKey(getSigningKey())
                        .build()
                        .parseClaimsJws(token);
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }
}
