package com.bidvelocity.payment.dto;

import com.bidvelocity.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CheckoutResponse {

    private Long paymentId;
    private Long auctionId;
    private Long winnerId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String checkoutToken;
    private LocalDateTime tokenExpiresAt;

    /**
     * Complete simulated checkout URL reachable from browser or scanned via phone on local network
     */
    private String checkoutUrl;

    /**
     * Visual SVG representation of the QR code for seamless frontend rendering
     */
    private String qrCodeSvg;

    /**
     * Formatted UPI URI parameters for demo display
     */
    private String upiVpa;
    private String upiPayUri;

    public CheckoutResponse() {
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getCheckoutToken() {
        return checkoutToken;
    }

    public void setCheckoutToken(String checkoutToken) {
        this.checkoutToken = checkoutToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getQrCodeSvg() {
        return qrCodeSvg;
    }

    public void setQrCodeSvg(String qrCodeSvg) {
        this.qrCodeSvg = qrCodeSvg;
    }

    public String getUpiVpa() {
        return upiVpa;
    }

    public void setUpiVpa(String upiVpa) {
        this.upiVpa = upiVpa;
    }

    public String getUpiPayUri() {
        return upiPayUri;
    }

    public void setUpiPayUri(String upiPayUri) {
        this.upiPayUri = upiPayUri;
    }
}
