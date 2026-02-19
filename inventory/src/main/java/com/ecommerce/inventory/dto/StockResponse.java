package com.ecommerce.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockResponse {

    private boolean success;
    private String message;
}

