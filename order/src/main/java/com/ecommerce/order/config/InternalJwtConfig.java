package com.ecommerce.order.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.security.Key;
import java.util.Date;

@Configuration
public class InternalJwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    public String generateOrderServiceToken() {

        Key key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .claim("role", "ORDER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}

