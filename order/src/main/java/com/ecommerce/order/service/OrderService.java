package com.ecommerce.order.service;

import com.ecommerce.order.client.dto.PaymentRequest;
import com.ecommerce.order.client.dto.PaymentResponse;
import com.ecommerce.order.client.dto.StockResponse;
import com.ecommerce.order.config.InternalJwtConfig;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.repository.*;
import com.ecommerce.order.client.dto.ReserveStockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;
    private final InternalJwtConfig internalJwtConfig;


    @Transactional
    public OrderResponse placeOrder(UUID userId, CreateOrderRequest request) {

        BigDecimal total = request.getItems()
                .stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(total)
                .status(OrderStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);

        try {

            // STEP  Reserve stock
            for (var item : request.getItems()) {

                ReserveStockRequest reserveRequest = new ReserveStockRequest();
                reserveRequest.setProductId(item.getProductId());
                reserveRequest.setQuantity(item.getQuantity());

                StockResponse stockResponse = restTemplate.postForObject(
                        "http://inventory-service:8083/api/inventory/reserve",
                        reserveRequest,
                        StockResponse.class
                );

                if (stockResponse == null || !stockResponse.isSuccess()) {
                    order.setStatus(OrderStatus.FAILED);
                    orderRepository.save(order);
                    throw new RuntimeException("Stock reservation failed");
                }
            }

            order.setStatus(OrderStatus.RESERVED);
            orderRepository.save(order);

            // STEP 2 Payment Service
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(order.getId());
            paymentRequest.setAmount(order.getTotalAmount());

            String orderServiceToken = internalJwtConfig.generateOrderServiceToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + orderServiceToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PaymentRequest> entity =
                    new HttpEntity<>(paymentRequest, headers);

            PaymentResponse paymentResponse = restTemplate.postForObject(
                    "http://payment-service:8085/api/payment/process",
                    entity,
                    PaymentResponse.class
            );


            if (paymentResponse != null && "SUCCESS".equals(paymentResponse.getStatus())) {

                // Confirm stock
                for (var item : request.getItems()) {

                    ReserveStockRequest confirmRequest = new ReserveStockRequest();
                    confirmRequest.setProductId(item.getProductId());
                    confirmRequest.setQuantity(item.getQuantity());

                    restTemplate.postForObject(
                            "http://inventory-service:8083/api/inventory/confirm",
                            confirmRequest,
                            Void.class
                    );
                }

                order.setStatus(OrderStatus.PAID);

            } else {

                // Release stock
                for (var item : request.getItems()) {

                    ReserveStockRequest releaseRequest = new ReserveStockRequest();
                    releaseRequest.setProductId(item.getProductId());
                    releaseRequest.setQuantity(item.getQuantity());

                    restTemplate.postForObject(
                            "http://inventory-service:8083/api/inventory/release",
                            releaseRequest,
                            Void.class
                    );
                }

                order.setStatus(OrderStatus.FAILED);
            }

            orderRepository.save(order);

        } catch (Exception e) {

            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);

            throw new RuntimeException("Order processing failed: " + e.getMessage());
        }

        return OrderResponse.builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }



}

