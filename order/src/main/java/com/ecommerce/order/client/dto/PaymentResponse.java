package com.ecommerce.order.client.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentResponse {

    private UUID paymentId;
    private String status;  // SUCCESS / FAILED
    private String message;
}
