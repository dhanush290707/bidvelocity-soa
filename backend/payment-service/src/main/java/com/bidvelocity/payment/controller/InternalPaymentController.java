package com.bidvelocity.payment.controller;

import com.bidvelocity.payment.dto.InitializePaymentRequest;
import com.bidvelocity.payment.entity.Payment;
import com.bidvelocity.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;

@RestController
@RequestMapping("/internal/payments")
public class InternalPaymentController {

    private final PaymentService paymentService;

    @Value("${bidvelocity.security.internal-secret:bidvelocity-internal-secure-secret-2026}")
    private String internalSecret;

    public InternalPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    private void verifyInternalSecret(String secretHeader) {
        if (secretHeader == null || !MessageDigest.isEqual(secretHeader.getBytes(), internalSecret.getBytes())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: invalid internal service secret");
        }
    }

    @PostMapping("/initialize")
    public ResponseEntity<Payment> initializePayment(
            @Valid @RequestBody InitializePaymentRequest request,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secretHeader) {
        verifyInternalSecret(secretHeader);
        Payment payment = paymentService.initializePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
