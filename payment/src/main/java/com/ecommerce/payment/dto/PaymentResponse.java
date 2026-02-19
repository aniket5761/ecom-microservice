package com.ecommerce.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentResponse {

    private UUID paymentId;
    private String status;   // SUCCESS / FAILED
    private String message;
}
