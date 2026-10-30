package com.bidvelocity.bidding.dto;

import com.bidvelocity.bidding.entity.Bid;
import com.bidvelocity.bidding.entity.BidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidResponse {

    private Long bidId;
    private String bidRequestId;
    private Long auctionId;
    private Long userId;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;
    private BidStatus status;
    private boolean idempotentReplay;

    public BidResponse() {
    }

    public static BidResponse fromEntity(Bid bid, boolean idempotentReplay) {
        BidResponse response = new BidResponse();
        response.setBidId(bid.getBidId());
        response.setBidRequestId(bid.getBidRequestId());
        response.setAuctionId(bid.getAuctionId());
        response.setUserId(bid.getUserId());
        response.setBidAmount(bid.getBidAmount());
        response.setBidTime(bid.getBidTime());
        response.setStatus(bid.getStatus());
        response.setIdempotentReplay(idempotentReplay);
        return response;
    }

    public Long getBidId() {
        return bidId;
    }

    public void setBidId(Long bidId) {
        this.bidId = bidId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public BidStatus getStatus() {
        return status;
    }

    public void setStatus(BidStatus status) {
        this.status = status;
    }

    public boolean isIdempotentReplay() {
        return idempotentReplay;
    }

    public void setIdempotentReplay(boolean idempotentReplay) {
        this.idempotentReplay = idempotentReplay;
    }
}
