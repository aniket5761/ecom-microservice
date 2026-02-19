package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.entity.*;
import com.ecommerce.cart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;

    public CartResponse getCart(UUID userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items.stream()
                        .map(i -> CartResponse.Item.builder()
                                .productId(i.getProductId())
                                .quantity(i.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void addToCart(UUID userId, AddToCartRequest request) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(
                        cartRepository.save(
                                Cart.builder()
                                        .userId(userId)
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        )
                );

        CartItem item = CartItem.builder()
                .cartId(cart.getId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();

        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(UUID userId, UUID productId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .forEach(cartItemRepository::delete);
    }

    @Transactional
    public void checkout(UUID userId, String userToken) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Build order request dynamically
        var orderRequest = new java.util.HashMap<String, Object>();
        var orderItems = items.stream().map(item -> {
            var map = new java.util.HashMap<String, Object>();
            map.put("productId", item.getProductId());
            map.put("quantity", item.getQuantity());
            map.put("price", java.math.BigDecimal.valueOf(100)); // mock price
            return map;
        }).collect(Collectors.toList());

        orderRequest.put("items", orderItems);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(orderRequest, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "http://order-service:8084/api/orders",
                HttpMethod.POST,
                entity,
                Object.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            cartItemRepository.deleteByCartId(cart.getId());
        }
    }
}

