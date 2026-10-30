package com.bidvelocity.bidding.service;

import com.bidvelocity.bidding.dto.BidResponse;
import com.bidvelocity.bidding.dto.PlaceBidRequest;
import com.bidvelocity.bidding.entity.Bid;
import com.bidvelocity.bidding.entity.BidStatus;
import com.bidvelocity.bidding.repository.BidRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class BiddingService {

    private static final Logger log = LoggerFactory.getLogger(BiddingService.class);

    private final BidRepository bidRepository;
    private final RestTemplate restTemplate;

    @Value("${bidvelocity.security.internal-secret:bidvelocity-internal-secure-secret-2026}")
    private String internalSecret;

    public BiddingService(BidRepository bidRepository, RestTemplate restTemplate) {
        this.bidRepository = bidRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public BidResponse placeBid(PlaceBidRequest request, Long userId) {
        String bidRequestId = request.getBidRequestId();
        if (bidRequestId == null || bidRequestId.isBlank()) {
            bidRequestId = UUID.randomUUID().toString();
        } else {
            bidRequestId = bidRequestId.trim();
        }

        // 1. Idempotency Check in bidding-service DB
        Optional<Bid> existingBid = bidRepository.findByBidRequestId(bidRequestId);
        if (existingBid.isPresent()) {
            log.info("Idempotent replay in Bidding Service for bidRequestId: {}", bidRequestId);
            return BidResponse.fromEntity(existingBid.get(), true);
        }

        // 2. Delegate atomic validation & highest-bid update to Auction Service
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", internalSecret);

            Map<String, Object> body = Map.of(
                    "bidRequestId", bidRequestId,
                    "bidderId", userId,
                    "bidAmount", request.getBidAmount()
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String url = "http://AUCTION-SERVICE/internal/auctions/" + request.getAuctionId() + "/apply-bid";

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // 3. Persist accepted bid in bidding-service DB
                Bid bid = new Bid(bidRequestId, request.getAuctionId(), userId, request.getBidAmount(), BidStatus.ACCEPTED);
                Bid saved = bidRepository.save(bid);

                // Mark previous bids on this auction as OUTBID
                bidRepository.markPreviousBidsOutbid(request.getAuctionId(), saved.getBidId());

                log.info("Bid accepted: ID {} for auction {} amount {}", saved.getBidId(), request.getAuctionId(), request.getBidAmount());
                return BidResponse.fromEntity(saved, false);
            } else {
                throw new ResponseStatusException(response.getStatusCode(), "Auction service rejected bid");
            }

        } catch (HttpStatusCodeException ex) {
            log.warn("Auction service rejected bid with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Error communicating with Auction Service: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to communicate with Auction Service: " + ex.getMessage());
        }
    }

    public List<BidResponse> getBidsForAuction(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByBidAmountDesc(auctionId)
                .stream()
                .map(b -> BidResponse.fromEntity(b, false))
                .toList();
    }

    public List<BidResponse> getBidsForUser(Long userId) {
        return bidRepository.findByUserIdOrderByBidTimeDesc(userId)
                .stream()
                .map(b -> BidResponse.fromEntity(b, false))
                .toList();
    }

    public Map<String, Object> getRunnerUpBid(Long auctionId, Long excludeUserId) {
        List<Bid> bids = bidRepository.findRunnerUpBids(auctionId, excludeUserId != null ? excludeUserId : 0L);
        if (bids.isEmpty()) {
            return Collections.emptyMap();
        }
        Bid runnerUp = bids.get(0);
        return Map.of(
                "bidId", runnerUp.getBidId(),
                "auctionId", runnerUp.getAuctionId(),
                "userId", runnerUp.getUserId(),
                "bidAmount", runnerUp.getBidAmount()
        );
    }
}
