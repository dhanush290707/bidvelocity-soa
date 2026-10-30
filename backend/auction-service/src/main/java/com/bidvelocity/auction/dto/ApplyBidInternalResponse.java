package com.bidvelocity.auction.dto;

import java.math.BigDecimal;

public class ApplyBidInternalResponse {

    private boolean accepted;
    private String message;
    private Long auctionId;
    private BigDecimal currentHighestBid;
    private Long winnerId;
    private boolean idempotentReplay;

    public ApplyBidInternalResponse() {
    }

    public ApplyBidInternalResponse(boolean accepted, String message, Long auctionId, BigDecimal currentHighestBid, Long winnerId, boolean idempotentReplay) {
        this.accepted = accepted;
        this.message = message;
        this.auctionId = auctionId;
        this.currentHighestBid = currentHighestBid;
        this.winnerId = winnerId;
        this.idempotentReplay = idempotentReplay;
    }

    public static ApplyBidInternalResponse success(Long auctionId, BigDecimal currentHighestBid, Long winnerId, boolean idempotentReplay) {
        return new ApplyBidInternalResponse(true, "Bid accepted successfully", auctionId, currentHighestBid, winnerId, idempotentReplay);
    }

    public static ApplyBidInternalResponse conflict(String message, Long auctionId, BigDecimal currentHighestBid) {
        return new ApplyBidInternalResponse(false, message, auctionId, currentHighestBid, null, false);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public boolean isIdempotentReplay() {
        return idempotentReplay;
    }

    public void setIdempotentReplay(boolean idempotentReplay) {
        this.idempotentReplay = idempotentReplay;
    }
}
