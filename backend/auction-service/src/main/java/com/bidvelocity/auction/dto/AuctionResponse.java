package com.bidvelocity.auction.dto;

import com.bidvelocity.auction.entity.Auction;
import com.bidvelocity.auction.entity.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionResponse {

    private Long auctionId;
    private String itemName;
    private String description;
    private String category;
    private String imageUrl;
    private String conditionReport;
    private String provenance;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentHighestBid;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long sellerId;
    private Long winnerId;
    private AuctionStatus status;
    private boolean secondChanceOffered;
    private Long version;

    public AuctionResponse() {
    }

    public static AuctionResponse fromEntity(Auction auction) {
        AuctionResponse response = new AuctionResponse();
        response.setAuctionId(auction.getAuctionId());
        response.setItemName(auction.getItemName());
        response.setDescription(auction.getDescription());
        response.setCategory(auction.getCategory() != null ? auction.getCategory() : "Collectibles");
        response.setImageUrl(auction.getImageUrl());
        response.setConditionReport(auction.getConditionReport());
        response.setProvenance(auction.getProvenance());
        response.setStartingPrice(auction.getStartingPrice());
        response.setReservePrice(auction.getReservePrice());
        response.setCurrentHighestBid(auction.getCurrentHighestBid());
        response.setStartTime(auction.getStartTime());
        response.setEndTime(auction.getEndTime());
        response.setSellerId(auction.getSellerId());
        response.setWinnerId(auction.getWinnerId());
        response.setStatus(auction.getStatus());
        response.setSecondChanceOffered(auction.isSecondChanceOffered());
        response.setVersion(auction.getVersion());
        return response;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getConditionReport() {
        return conditionReport;
    }

    public void setConditionReport(String conditionReport) {
        this.conditionReport = conditionReport;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
    }

    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public boolean isSecondChanceOffered() {
        return secondChanceOffered;
    }

    public void setSecondChanceOffered(boolean secondChanceOffered) {
        this.secondChanceOffered = secondChanceOffered;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
