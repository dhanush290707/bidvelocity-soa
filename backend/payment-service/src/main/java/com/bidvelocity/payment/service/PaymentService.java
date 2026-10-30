package com.bidvelocity.payment.service;

import com.bidvelocity.payment.dto.CheckoutResponse;
import com.bidvelocity.payment.dto.InitializePaymentRequest;
import com.bidvelocity.payment.dto.WebhookPayload;
import com.bidvelocity.payment.entity.Payment;
import com.bidvelocity.payment.entity.PaymentStatus;
import com.bidvelocity.payment.entity.SyncStatus;
import com.bidvelocity.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final long MAX_TIMESTAMP_DRIFT_MS = 300_000L; // 5 minutes

    private final PaymentRepository paymentRepository;
    private final MockGatewayService mockGatewayService;
    private final RestTemplate restTemplate;

    @Value("${payment.checkout.base-url:http://localhost:8080}")
    private String checkoutBaseUrl;

    @Value("${payment.checkout.token-validity-minutes:15}")
    private int tokenValidityMinutes;

    @Value("${bidvelocity.security.internal-secret:bidvelocity-internal-secure-secret-2026}")
    private String internalSecret;

    public PaymentService(PaymentRepository paymentRepository,
                          MockGatewayService mockGatewayService,
                          RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.mockGatewayService = mockGatewayService;
        this.restTemplate = restTemplate;
    }

    /**
     * Idempotently initializes payment for an auction winner.
     */
    @Transactional
    public Payment initializePayment(InitializePaymentRequest request) {
        Optional<Payment> existing = paymentRepository.findByAuctionIdAndWinnerIdAndPaymentRound(
                request.getAuctionId(), request.getWinnerId(), request.getPaymentRound());

        if (existing.isPresent()) {
            log.info("Payment already initialized for auction {} winner {} round {}",
                    request.getAuctionId(), request.getWinnerId(), request.getPaymentRound());
            return existing.get();
        }

        String checkoutToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenValidityMinutes);

        Payment payment = new Payment(
                request.getAuctionId(),
                request.getWinnerId(),
                request.getPaymentRound(),
                request.getAmount(),
                checkoutToken,
                expiresAt
        );

        return paymentRepository.save(payment);
    }

    @Transactional
    public CheckoutResponse getCheckoutDetails(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .or(() -> paymentRepository.findTopByAuctionIdOrderByCreatedAtDesc(paymentId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found with id: " + paymentId));

        // Check if token expired
        if (payment.getStatus() == PaymentStatus.PENDING && LocalDateTime.now().isAfter(payment.getTokenExpiresAt())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            payment = paymentRepository.save(payment);
        }

        String checkoutUrl = checkoutBaseUrl + "/api/payments/mock-gateway/pay?token=" + payment.getCheckoutToken();
        String qrSvg = mockGatewayService.generateQrCodeSvg(checkoutUrl, 240);

        String upiVpa = "bidvelocity@upi";
        String upiPayUri = String.format(Locale.US, "upi://pay?pa=%s&pn=BidVelocity&am=%.2f&cu=INR&tn=Auction_%d",
                upiVpa, payment.getAmount(), payment.getAuctionId());

        CheckoutResponse response = new CheckoutResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setAuctionId(payment.getAuctionId());
        response.setWinnerId(payment.getWinnerId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setCheckoutToken(payment.getCheckoutToken());
        response.setTokenExpiresAt(payment.getTokenExpiresAt());
        response.setCheckoutUrl(checkoutUrl);
        response.setQrCodeSvg(qrSvg);
        response.setUpiVpa(upiVpa);
        response.setUpiPayUri(upiPayUri);

        return response;
    }

    @Transactional
    public CheckoutResponse getCheckoutByAuctionId(Long auctionId) {
        Payment payment = paymentRepository.findTopByAuctionIdOrderByCreatedAtDesc(auctionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No payment order found for auction: " + auctionId));
        return getCheckoutDetails(payment.getPaymentId());
    }

    public Payment getPaymentByToken(String token) {
        return paymentRepository.findByCheckoutToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid checkout token"));
    }

    /**
     * Webhook clearance with timestamp validation, constant-time HMAC check,
     * exact amount matching, and duplicate callback idempotency.
     */
    @Transactional
    public Payment processWebhook(WebhookPayload payload, String signature) {
        // 1. Timestamp validation (reject drift > 5 mins)
        long currentMs = System.currentTimeMillis();
        if (Math.abs(currentMs - payload.getTimestamp()) > MAX_TIMESTAMP_DRIFT_MS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook rejected: timestamp drift exceeds 5 minutes");
        }

        // 2. Constant-time HMAC signature verification
        String canonical = payload.getPaymentId() + "|" +
                payload.getAmount().setScale(2).toString() + "|" +
                payload.getIdempotencyKey() + "|" +
                payload.getStatus() + "|" +
                payload.getTimestamp();

        if (!mockGatewayService.verifySignature(canonical, signature)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Webhook rejected: invalid HMAC signature");
        }

        // 3. Find payment record
        Payment payment = paymentRepository.findById(payload.getPaymentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment record not found"));

        // 4. Idempotency Check: if this exact idempotencyKey was already processed
        if (payment.getIdempotencyKey() != null && payment.getIdempotencyKey().equals(payload.getIdempotencyKey())) {
            log.info("Idempotent webhook replay for payment {} with key {}", payment.getPaymentId(), payload.getIdempotencyKey());
            return payment;
        }

        // 5. Amount validation
        if (payload.getAmount().compareTo(payment.getAmount()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount mismatch: expected " + payment.getAmount());
        }

        // 6. Token expiration check
        if (payment.getStatus() == PaymentStatus.PENDING && LocalDateTime.now().isAfter(payment.getTokenExpiresAt())) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment checkout has expired");
        }

        // 7. Update status
        payment.setIdempotencyKey(payload.getIdempotencyKey());
        payment.setTransactionRef(payload.getTransactionReference());
        payment.setWebhookSignature(signature);

        if ("SUCCESS".equalsIgnoreCase(payload.getStatus())) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setClearedAt(LocalDateTime.now());
            payment.setSyncStatus(SyncStatus.RETRY_PENDING);
            payment = paymentRepository.save(payment);

            // Notify Auction Service to clear auction
            syncAuctionClearance(payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment = paymentRepository.save(payment);
        }

        return payment;
    }

    /**
     * Inter-service notification to Auction Service with fallback retry tracking.
     */
    @Transactional
    public void syncAuctionClearance(Payment payment) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-Secret", internalSecret);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = "http://AUCTION-SERVICE/internal/auctions/" + payment.getAuctionId() + "/clear";

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                payment.setSyncStatus(SyncStatus.SYNCED);
                payment.setLastSyncError(null);
                paymentRepository.save(payment);
                log.info("Auction {} clearance successfully synchronized", payment.getAuctionId());
            } else {
                recordSyncFailure(payment, "Auction Service responded with " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("Auction clearance sync failed for payment {}: {}", payment.getPaymentId(), e.getMessage());
            recordSyncFailure(payment, e.getMessage());
        }
    }

    private void recordSyncFailure(Payment payment, String error) {
        int nextRetry = payment.getRetryCount() + 1;
        payment.setRetryCount(nextRetry);
        payment.setLastRetryAt(LocalDateTime.now());
        payment.setLastSyncError(error);

        if (nextRetry >= 5) {
            payment.setSyncStatus(SyncStatus.SYNC_FAILED);
            payment.setNextRetryAt(null);
            log.error("Payment {} sync reached MAX retries (5). Marked as SYNC_FAILED.", payment.getPaymentId());
        } else {
            long delaySeconds = (long) Math.pow(2, nextRetry) * 15L;
            payment.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
            payment.setSyncStatus(SyncStatus.RETRY_PENDING);
        }
        paymentRepository.save(payment);
    }
}
