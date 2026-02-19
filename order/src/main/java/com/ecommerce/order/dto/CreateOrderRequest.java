package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull
    private List<Item> items;

    @Data
    public static class Item {
        private UUID productId;
        private int quantity;
        private BigDecimal price;
    }
}
