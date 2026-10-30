package com.bidvelocity.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InitializePaymentRequest {

    @NotNull(message = "auctionId is required")
    private Long auctionId;

    @NotNull(message = "winnerId is required")
    private Long winnerId;

    private int paymentRound = 1;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    public InitializePaymentRequest() {
    }

    public InitializePaymentRequest(Long auctionId, Long winnerId, int paymentRound, BigDecimal amount) {
        this.auctionId = auctionId;
        this.winnerId = winnerId;
        this.paymentRound = paymentRound;
        this.amount = amount;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public int getPaymentRound() {
        return paymentRound;
    }

    public void setPaymentRound(int paymentRound) {
        this.paymentRound = paymentRound;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
