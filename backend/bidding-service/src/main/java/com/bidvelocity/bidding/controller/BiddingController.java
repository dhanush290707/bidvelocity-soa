package com.bidvelocity.bidding.controller;

import com.bidvelocity.bidding.dto.BidResponse;
import com.bidvelocity.bidding.dto.PlaceBidRequest;
import com.bidvelocity.bidding.service.BiddingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "*")
public class BiddingController {

    private final BiddingService biddingService;

    public BiddingController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @PostMapping
    public ResponseEntity<BidResponse> placeBid(
            @Valid @RequestBody PlaceBidRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        Long userId = 2L; // default fallback if unauthenticated demo
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            userId = Long.parseLong(userIdHeader);
        }

        BidResponse response = biddingService.placeBid(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidResponse>> getBidsForAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(biddingService.getBidsForAuction(auctionId));
    }

    @GetMapping("/my-bids")
    public ResponseEntity<List<BidResponse>> getMyBids(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Long userId = (userIdHeader != null && !userIdHeader.isBlank()) ? Long.parseLong(userIdHeader) : 1L;
        return ResponseEntity.ok(biddingService.getBidsForUser(userId));
    }
}
