package com.bidvelocity.payment.controller;

import com.bidvelocity.payment.dto.CheckoutResponse;
import com.bidvelocity.payment.dto.WebhookPayload;
import com.bidvelocity.payment.entity.Payment;
import com.bidvelocity.payment.repository.PaymentRepository;
import com.bidvelocity.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService, PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/{id}/checkout")
    public ResponseEntity<CheckoutResponse> getCheckoutDetails(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getCheckoutDetails(id));
    }

    @GetMapping("/auction/{auctionId}/checkout")
    public ResponseEntity<CheckoutResponse> getCheckoutByAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(paymentService.getCheckoutByAuctionId(auctionId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @Valid @RequestBody WebhookPayload payload,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature) {

        Payment processed = paymentService.processWebhook(payload, signature);
        return ResponseEntity.ok(Map.of(
                "paymentId", processed.getPaymentId(),
                "status", processed.getStatus().name(),
                "syncStatus", processed.getSyncStatus().name(),
                "clearedAt", processed.getClearedAt() != null ? processed.getClearedAt().toString() : "",
                "message", "Webhook verified and processed successfully"
        ));
    }

    @PostMapping("/{id}/reconcile-manual")
    public ResponseEntity<Map<String, Object>> manualReconcile(@PathVariable Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow();
        paymentService.syncAuctionClearance(payment);
        return ResponseEntity.ok(Map.of(
                "paymentId", payment.getPaymentId(),
                "syncStatus", payment.getSyncStatus().name(),
                "retryCount", payment.getRetryCount()
        ));
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<Payment>> getMyPayments(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long winnerId = (userIdHeader != null && !userIdHeader.isBlank()) ? Long.parseLong(userIdHeader) : 1L;
        return ResponseEntity.ok(paymentRepository.findByWinnerIdOrderByCreatedAtDesc(winnerId));
    }
}
