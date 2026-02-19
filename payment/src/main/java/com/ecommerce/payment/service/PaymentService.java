package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final Random random = new Random();

    public PaymentResponse process(PaymentRequest request) {

        boolean success = random.nextBoolean(); // randomly

        if (success) {
            return PaymentResponse.builder()
                    .paymentId(UUID.randomUUID())
                    .status("SUCCESS")
                    .message("Payment processed successfully")
                    .build();
        } else {
            return PaymentResponse.builder()
                    .paymentId(UUID.randomUUID())
                    .status("FAILED")
                    .message("Payment failed")
                    .build();
        }
    }
}

