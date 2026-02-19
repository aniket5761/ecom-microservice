package com.ecommerce.order.dto;

import com.ecommerce.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {

    private UUID orderId;
    private BigDecimal totalAmount;
    private OrderStatus status;
}
