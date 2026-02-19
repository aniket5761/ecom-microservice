package com.ecommerce.order.client.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ReserveStockRequest {

    private UUID productId;
    private int quantity;
}
