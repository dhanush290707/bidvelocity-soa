package com.bidvelocity.bidding.controller;

import com.bidvelocity.bidding.service.BiddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/internal/bids")
public class InternalBiddingController {

    private final BiddingService biddingService;

    @Value("${bidvelocity.security.internal-secret:bidvelocity-internal-secure-secret-2026}")
    private String internalSecret;

    public InternalBiddingController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    private void verifyInternalSecret(String secretHeader) {
        if (secretHeader == null || !MessageDigest.isEqual(secretHeader.getBytes(), internalSecret.getBytes())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: invalid internal service secret");
        }
    }

    @GetMapping("/auction/{auctionId}/runner-up")
    public ResponseEntity<Map<String, Object>> getRunnerUp(
            @PathVariable Long auctionId,
            @RequestParam(value = "excludeUserId", required = false) Long excludeUserId,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secretHeader) {
        verifyInternalSecret(secretHeader);
        Map<String, Object> runnerUp = biddingService.getRunnerUpBid(auctionId, excludeUserId);
        return ResponseEntity.ok(runnerUp);
    }
}
