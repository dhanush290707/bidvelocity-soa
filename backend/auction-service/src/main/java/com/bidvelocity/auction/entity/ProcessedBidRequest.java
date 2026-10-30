package com.bidvelocity.auction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Persists processed bid request IDs in auction-service DB within the same transaction
 * as the highest-bid update to ensure strict dual-service idempotency.
 */
@Entity
@Table(name = "processed_bid_requests")
public class ProcessedBidRequest {

    @Id
    @Column(name = "bid_request_id", length = 100, nullable = false)
    private String bidRequestId;

    @Column(name = "auction_id", nullable = false)
    private Long auctionId;

    @Column(name = "applied_bid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal appliedBidAmount;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedBidRequest() {
    }

    public ProcessedBidRequest(String bidRequestId, Long auctionId, BigDecimal appliedBidAmount) {
        this.bidRequestId = bidRequestId;
        this.auctionId = auctionId;
        this.appliedBidAmount = appliedBidAmount;
        this.processedAt = LocalDateTime.now();
    }

    public String getBidRequestId() {
        return bidRequestId;
    }

    public void setBidRequestId(String bidRequestId) {
        this.bidRequestId = bidRequestId;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public BigDecimal getAppliedBidAmount() {
        return appliedBidAmount;
    }

    public void setAppliedBidAmount(BigDecimal appliedBidAmount) {
        this.appliedBidAmount = appliedBidAmount;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
