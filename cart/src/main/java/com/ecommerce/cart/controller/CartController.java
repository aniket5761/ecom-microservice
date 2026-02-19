package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.security.JwtService;
import com.ecommerce.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtService jwtService;

    @GetMapping
    public CartResponse getCart(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        UUID userId = jwtService.extractUserId(token);

        return cartService.getCart(userId);
    }

    @PostMapping("/add")
    public void addToCart(@RequestHeader("Authorization") String header,
                          @RequestBody AddToCartRequest request) {

        String token = header.substring(7);
        UUID userId = jwtService.extractUserId(token);

        cartService.addToCart(userId, request);
    }

    @DeleteMapping("/{productId}")
    public void remove(@RequestHeader("Authorization") String header,
                       @PathVariable UUID productId) {

        String token = header.substring(7);
        UUID userId = jwtService.extractUserId(token);

        cartService.removeFromCart(userId, productId);
    }

    @PostMapping("/checkout")
    public void checkout(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);
        UUID userId = jwtService.extractUserId(token);

        cartService.checkout(userId, token);
    }
}
