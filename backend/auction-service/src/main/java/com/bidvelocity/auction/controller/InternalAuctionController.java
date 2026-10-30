package com.bidvelocity.auction.controller;

import com.bidvelocity.auction.dto.ApplyBidInternalRequest;
import com.bidvelocity.auction.dto.ApplyBidInternalResponse;
import com.bidvelocity.auction.dto.AuctionResponse;
import com.bidvelocity.auction.entity.Auction;
import com.bidvelocity.auction.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/internal/auctions")
public class InternalAuctionController {

    private final AuctionService auctionService;

    @Value("${bidvelocity.security.internal-secret:bidvelocity-internal-secure-secret-2026}")
    private String internalSecret;

    public InternalAuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    private void verifyInternalSecret(String secretHeader) {
        if (secretHeader == null || !MessageDigest.isEqual(secretHeader.getBytes(), internalSecret.getBytes())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: invalid internal service secret");
        }
    }

    @PutMapping("/{id}/apply-bid")
    public ResponseEntity<ApplyBidInternalResponse> applyBid(
            @PathVariable Long id,
            @Valid @RequestBody ApplyBidInternalRequest request,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secretHeader) {
        verifyInternalSecret(secretHeader);
        ApplyBidInternalResponse response = auctionService.applyBid(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/clear")
    public ResponseEntity<Map<String, Object>> clearAuction(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secretHeader) {
        verifyInternalSecret(secretHeader);
        auctionService.markAuctionCleared(id);
        return ResponseEntity.ok(Map.of("auctionId", id, "status", "CLEARED"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuctionInternal(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secretHeader) {
        verifyInternalSecret(secretHeader);
        Auction auction = auctionService.getAuctionById(id);
        return ResponseEntity.ok(AuctionResponse.fromEntity(auction));
    }
}
