package com.ecommerce.cart.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponse {

    private UUID cartId;
    private List<Item> items;

    @Data
    @Builder
    public static class Item {
        private UUID productId;
        private int quantity;
    }
}
