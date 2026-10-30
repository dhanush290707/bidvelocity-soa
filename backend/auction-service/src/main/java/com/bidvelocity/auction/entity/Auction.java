package com.bidvelocity.auction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long auctionId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "condition_report", columnDefinition = "TEXT")
    private String conditionReport;

    @Column(columnDefinition = "TEXT")
    private String provenance;

    @Column(name = "starting_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal startingPrice;

    @Column(name = "reserve_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal reservePrice;

    @Column(name = "current_highest_bid", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentHighestBid;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "winner_id")
    private Long winnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuctionStatus status = AuctionStatus.DRAFT;

    @Column(name = "second_chance_offered", nullable = false)
    private boolean secondChanceOffered = false;

    /**
     * Optimistic locking version field to prevent concurrent overwrites
     * and ensure atomic highest bid increments.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public Auction() {
    }

    public Auction(String itemName, String description, String category, String imageUrl,
                   String conditionReport, String provenance, BigDecimal startingPrice,
                   BigDecimal reservePrice, LocalDateTime startTime, LocalDateTime endTime, Long sellerId) {
        this.itemName = itemName;
        this.description = description;
        this.category = category != null ? category : "Collectibles";
        this.imageUrl = imageUrl;
        this.conditionReport = conditionReport;
        this.provenance = provenance;
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.currentHighestBid = startingPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sellerId = sellerId;
        this.status = (startTime != null && startTime.isAfter(LocalDateTime.now())) ? AuctionStatus.DRAFT : AuctionStatus.ACTIVE;
        this.secondChanceOffered = false;
        this.version = 0L;
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
