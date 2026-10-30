package com.bidvelocity.auction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ApplyBidInternalRequest {

    @NotBlank(message = "bidRequestId is required")
    private String bidRequestId;

    @NotNull(message = "bidderId is required")
    private Long bidderId;

    @NotNull(message = "bidAmount is required")
    @DecimalMin(value = "0.01", message = "bidAmount must be greater than 0")
    private BigDecimal bidAmount;

    public ApplyBidInternalRequest() {
    }

    public ApplyBidInternalRequest(String bidRequestId, Long bidderId, BigDecimal bidAmount) {
        this.bidRequestId = bidRequestId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
    }

    public String getBidRequestId() {
        return bidRequestId;
    }

    public void setBidRequestId(String bidRequestId) {
        this.bidRequestId = bidRequestId;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }
}
