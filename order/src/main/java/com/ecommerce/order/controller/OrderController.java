package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.security.JwtService;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtService jwtService;

    @PostMapping
    public OrderResponse create(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateOrderRequest request) {

        String token = authHeader.substring(7);
        UUID userId = jwtService.extractUserId(token);

        return orderService.placeOrder(userId, request);
    }
}
