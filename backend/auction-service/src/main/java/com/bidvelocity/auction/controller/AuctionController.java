package com.bidvelocity.auction.controller;

import com.bidvelocity.auction.dto.AuctionResponse;
import com.bidvelocity.auction.dto.CreateAuctionRequest;
import com.bidvelocity.auction.entity.Auction;
import com.bidvelocity.auction.entity.AuctionStatus;
import com.bidvelocity.auction.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "*")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody CreateAuctionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String userRoleHeader) {

        Long sellerId = 1L; // default fallback
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            sellerId = Long.parseLong(userIdHeader);
        }

        Auction auction = auctionService.createAuction(request, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuctionResponse.fromEntity(auction));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<java.util.Map<String, String>> uploadImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only JPEG, PNG, and WebP are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds 5MB limit");
        }

        try {
            // Encode as base64 data URI for portable, self-contained persistence in academic DB
            String base64 = java.util.Base64.getEncoder().encodeToString(file.getBytes());
            String dataUri = "data:" + contentType + ";base64," + base64;
            return ResponseEntity.ok(java.util.Map.of("imageUrl", dataUri));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> listAuctions(@RequestParam(value = "status", required = false) AuctionStatus status) {
        List<Auction> auctions = auctionService.getAllAuctions(status);
        List<AuctionResponse> responses = auctions.stream().map(AuctionResponse::fromEntity).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getAuction(@PathVariable Long id) {
        Auction auction = auctionService.getAuctionById(id);
        return ResponseEntity.ok(AuctionResponse.fromEntity(auction));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<AuctionResponse> closeAuction(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        Auction closed = auctionService.closeAuction(id);
        return ResponseEntity.ok(AuctionResponse.fromEntity(closed));
    }

    @PostMapping("/{id}/second-chance")
    public ResponseEntity<AuctionResponse> offerSecondChance(@PathVariable Long id) {
        Auction updated = auctionService.offerSecondChance(id);
        return ResponseEntity.ok(AuctionResponse.fromEntity(updated));
    }
}
