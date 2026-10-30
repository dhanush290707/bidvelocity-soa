package com.bidvelocity.bidding.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PlaceBidRequest {

    @NotNull(message = "auctionId is required")
    private Long auctionId;

    @NotNull(message = "bidAmount is required")
    @DecimalMin(value = "0.01", message = "bidAmount must be greater than 0")
    private BigDecimal bidAmount;

    /**
     * Unique client idempotency key to prevent duplicate bids during network retries.
     * If omitted, a server UUID will be generated.
     */
    private String bidRequestId;

    public PlaceBidRequest() {
    }

    public PlaceBidRequest(Long auctionId, BigDecimal bidAmount, String bidRequestId) {
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
        this.bidRequestId = bidRequestId;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getBidRequestId() {
        return bidRequestId;
    }

    public void setBidRequestId(String bidRequestId) {
        this.bidRequestId = bidRequestId;
    }
}
