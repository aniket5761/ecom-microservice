package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.*;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public PaymentResponse process(@RequestBody PaymentRequest request) {
        return paymentService.process(request);
    }
}

