package com.bidvelocity.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class MockPaymentSubmitRequest {

    @NotBlank(message = "token is required")
    private String token;

    @NotBlank(message = "action is required (SUCCESS, FAILED)")
    private String action;

    private String transactionRef;

    private String idempotencyKey;

    public MockPaymentSubmitRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
