package com.bidvelocity.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class WebhookPayload {

    @NotNull(message = "paymentId is required")
    private Long paymentId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "idempotencyKey is required")
    private String idempotencyKey;

    @NotBlank(message = "status is required")
    private String status; // SUCCESS or FAILED

    @NotBlank(message = "transactionReference is required")
    private String transactionReference;

    @NotNull(message = "timestamp is required")
    private Long timestamp;

    public WebhookPayload() {
    }

    public WebhookPayload(Long paymentId, BigDecimal amount, String idempotencyKey, String status, String transactionReference, Long timestamp) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.transactionReference = transactionReference;
        this.timestamp = timestamp;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
