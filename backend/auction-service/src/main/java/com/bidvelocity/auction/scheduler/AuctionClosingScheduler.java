package com.bidvelocity.auction.scheduler;

import com.bidvelocity.auction.entity.Auction;
import com.bidvelocity.auction.entity.AuctionStatus;
import com.bidvelocity.auction.repository.AuctionRepository;
import com.bidvelocity.auction.service.AuctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuctionClosingScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuctionClosingScheduler.class);

    private final AuctionRepository auctionRepository;
    private final AuctionService auctionService;

    public AuctionClosingScheduler(AuctionRepository auctionRepository, AuctionService auctionService) {
        this.auctionRepository = auctionRepository;
        this.auctionService = auctionService;
    }

    /**
     * Checks every 10 seconds for auctions whose startTime has arrived and activates them.
     */
    @Scheduled(fixedDelay = 10000)
    public void activatePendingAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> pending = auctionRepository.findPendingAuctionsToActivate(now);
        for (Auction auction : pending) {
            auction.setStatus(AuctionStatus.ACTIVE);
            auctionRepository.save(auction);
            log.info("Auto-activated auction id: {} - {}", auction.getAuctionId(), auction.getItemName());
        }
    }

    /**
     * Checks every 5 seconds for active auctions whose endTime has expired and closes them.
     */
    @Scheduled(fixedDelay = 5000)
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expired = auctionRepository.findActiveAuctionsToClose(now);
        for (Auction auction : expired) {
            log.info("Closing expired auction id: {} - {}", auction.getAuctionId(), auction.getItemName());
            try {
                auctionService.closeAuction(auction.getAuctionId());
            } catch (Exception e) {
                log.error("Failed to auto-close auction {}: {}", auction.getAuctionId(), e.getMessage());
            }
        }
    }
}
