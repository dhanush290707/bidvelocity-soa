package com.bidvelocity.auction.service;

import com.bidvelocity.auction.dto.ApplyBidInternalRequest;
import com.bidvelocity.auction.dto.ApplyBidInternalResponse;
import com.bidvelocity.auction.dto.CreateAuctionRequest;
import com.bidvelocity.auction.entity.Auction;
import com.bidvelocity.auction.entity.AuctionStatus;
import com.bidvelocity.auction.entity.ProcessedBidRequest;
import com.bidvelocity.auction.repository.AuctionRepository;
import com.bidvelocity.auction.repository.ProcessedBidRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuctionService {

    private static final Logger log = LoggerFactory.getLogger(AuctionService.class);
    private static final int MAX_OPTIMISTIC_LOCK_RETRIES = 3;

    private final AuctionRepository auctionRepository;
    private final ProcessedBidRequestRepository processedBidRequestRepository;
    private final RestTemplate restTemplate;

    @Value("${bidvelocity.security.internal-secret:bidvelocity-internal-secure-secret-2026}")
    private String internalSecret;

    public AuctionService(AuctionRepository auctionRepository,
                          ProcessedBidRequestRepository processedBidRequestRepository,
                          RestTemplate restTemplate) {
        this.auctionRepository = auctionRepository;
        this.processedBidRequestRepository = processedBidRequestRepository;
        this.restTemplate = restTemplate;
    }

    public Auction createAuction(CreateAuctionRequest request, Long sellerId) {
        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now();
        Auction auction = new Auction(
                request.getItemName().trim(),
                request.getDescription(),
                request.getCategory() != null ? request.getCategory() : "Collectibles",
                request.getImageUrl(),
                request.getConditionReport(),
                request.getProvenance(),
                request.getStartingPrice(),
                request.getReservePrice(),
                startTime,
                request.getEndTime(),
                sellerId
        );
        return auctionRepository.save(auction);
    }

    public List<Auction> getAllAuctions(AuctionStatus status) {
        if (status != null) {
            return auctionRepository.findByStatus(status);
        }
        return auctionRepository.findAll();
    }

    public Auction getAuctionById(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction not found with id: " + id));
    }

    /**
     * Atomic internal bid application called exclusively by bidding-service.
     * Enforces dual-table idempotency and bounded optimistic locking retry in a fresh transaction.
     */
    public ApplyBidInternalResponse applyBid(Long auctionId, ApplyBidInternalRequest request) {
        // 1. Dual-service idempotency check
        Optional<ProcessedBidRequest> existing = processedBidRequestRepository.findByBidRequestId(request.getBidRequestId());
        if (existing.isPresent()) {
            Auction auction = getAuctionById(auctionId);
            log.info("Idempotent replay detected for bidRequestId: {}", request.getBidRequestId());
            return ApplyBidInternalResponse.success(auctionId, existing.get().getAppliedBidAmount(), auction.getWinnerId(), true);
        }

        // 2. Bounded retry loop for optimistic locking
        for (int attempt = 1; attempt <= MAX_OPTIMISTIC_LOCK_RETRIES; attempt++) {
            try {
                return executeAtomicBidUpdate(auctionId, request);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Optimistic locking collision on auction {} (attempt {}/{})", auctionId, attempt, MAX_OPTIMISTIC_LOCK_RETRIES);
                if (attempt == MAX_OPTIMISTIC_LOCK_RETRIES) {
                    Auction current = getAuctionById(auctionId);
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "High concurrency collision: current highest bid is " + current.getCurrentHighestBid());
                }
                try {
                    Thread.sleep(30L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to apply bid due to concurrent bids");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ApplyBidInternalResponse executeAtomicBidUpdate(Long auctionId, ApplyBidInternalRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction not found"));

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auction is not active. Status: " + auction.getStatus());
        }

        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auction has already ended");
        }

        if (auction.getSellerId().equals(request.getBidderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sellers cannot bid on their own auctions");
        }

        if (request.getBidAmount().compareTo(auction.getCurrentHighestBid()) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bid amount must be strictly greater than current highest bid: " + auction.getCurrentHighestBid());
        }

        // Apply update
        auction.setCurrentHighestBid(request.getBidAmount());
        auction.setWinnerId(request.getBidderId());
        auctionRepository.save(auction);

        // Record processed bid request in the same transaction
        ProcessedBidRequest processed = new ProcessedBidRequest(
                request.getBidRequestId(),
                auctionId,
                request.getBidAmount()
        );
        processedBidRequestRepository.save(processed);

        return ApplyBidInternalResponse.success(auctionId, request.getBidAmount(), request.getBidderId(), false);
    }

    /**
     * Closes an auction atomically and notifies Payment Service if reserve price was met.
     */
    @Transactional
    public Auction closeAuction(Long auctionId) {
        Auction auction = getAuctionById(auctionId);

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ACTIVE auctions can be closed. Current: " + auction.getStatus());
        }

        auction.setStatus(AuctionStatus.PAYMENT_PENDING);
        Auction saved = auctionRepository.save(auction);

        // If reserve price met and there is a winner, initialize payment in payment-service
        if (saved.getWinnerId() != null && saved.getCurrentHighestBid().compareTo(saved.getReservePrice()) >= 0) {
            initializePaymentOrder(saved.getAuctionId(), saved.getWinnerId(), 1, saved.getCurrentHighestBid());
        } else {
            saved.setStatus(AuctionStatus.PAYMENT_FAILED);
            saved = auctionRepository.save(saved);
        }

        return saved;
    }

    /**
     * Marks auction as CLEARED upon verified payment webhook.
     */
    @Transactional
    public void markAuctionCleared(Long auctionId) {
        Auction auction = getAuctionById(auctionId);
        if (auction.getStatus() != AuctionStatus.PAYMENT_PENDING) {
            log.warn("Auction {} clearance requested but status is {}", auctionId, auction.getStatus());
        }
        auction.setStatus(AuctionStatus.CLEARED);
        auctionRepository.save(auction);
        log.info("Auction {} successfully marked as CLEARED", auctionId);
    }

    /**
     * Second-chance offer: if winner payment failed/expired, offer to runner-up bidder.
     */
    @Transactional
    public Auction offerSecondChance(Long auctionId) {
        Auction auction = getAuctionById(auctionId);

        if (auction.isSecondChanceOffered()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Second chance has already been offered for this auction");
        }
        if (auction.getStatus() != AuctionStatus.PAYMENT_FAILED && auction.getStatus() != AuctionStatus.PAYMENT_PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Second chance only eligible for failed/unpaid auctions");
        }

        // Query runner-up bidder from bidding-service
        Long excludedWinner = auction.getWinnerId();
        Map<String, Object> runnerUp = fetchRunnerUpBid(auctionId, excludedWinner);

        if (runnerUp == null || !runnerUp.containsKey("userId")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No eligible runner-up bidder found");
        }

        Long runnerUpUserId = Long.valueOf(runnerUp.get("userId").toString());
        BigDecimal runnerUpBidAmount = new BigDecimal(runnerUp.get("bidAmount").toString());

        if (runnerUpBidAmount.compareTo(auction.getReservePrice()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Runner up bid does not meet the reserve price");
        }

        auction.setWinnerId(runnerUpUserId);
        auction.setCurrentHighestBid(runnerUpBidAmount);
        auction.setSecondChanceOffered(true);
        auction.setStatus(AuctionStatus.PAYMENT_PENDING);
        Auction updated = auctionRepository.save(auction);

        // Initialize payment round 2 for runner-up
        initializePaymentOrder(auctionId, runnerUpUserId, 2, runnerUpBidAmount);
        return updated;
    }

    private void initializePaymentOrder(Long auctionId, Long winnerId, int round, BigDecimal amount) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", internalSecret);

            Map<String, Object> body = Map.of(
                    "auctionId", auctionId,
                    "winnerId", winnerId,
                    "paymentRound", round,
                    "amount", amount
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("http://PAYMENT-SERVICE/internal/payments/initialize", entity, Map.class);
            log.info("Initialized payment for auction {} winner {} round {}", auctionId, winnerId, round);
        } catch (Exception e) {
            log.error("Failed to initialize payment order for auction {}: {}", auctionId, e.getMessage());
        }
    }

    private Map<String, Object> fetchRunnerUpBid(Long auctionId, Long excludeUserId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecret);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = "http://BIDDING-SERVICE/internal/bids/auction/" + auctionId + "/runner-up?excludeUserId=" + (excludeUserId != null ? excludeUserId : 0);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("Could not fetch runner-up from bidding-service: {}", e.getMessage());
            return null;
        }
    }
}
