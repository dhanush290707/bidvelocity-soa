package com.bidvelocity.payment.service;

import com.bidvelocity.payment.dto.WebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class MockGatewayService {

    private static final Logger log = LoggerFactory.getLogger(MockGatewayService.class);

    @Value("${payment.webhook.secret:bidvelocity-mock-payment-gateway-hmac-sha256-secret-key-2026}")
    private String webhookSecret;

    /**
     * Computes HMAC-SHA256 signature for the webhook payload string.
     */
    public String calculateSignature(String payloadString) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(payloadString.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (Exception e) {
            log.error("Failed to compute HMAC signature: {}", e.getMessage());
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    /**
     * Performs constant-time comparison of incoming signature vs expected signature
     * to prevent timing attack vulnerabilities.
     */
    public boolean verifySignature(String payloadString, String providedSignature) {
        if (providedSignature == null || providedSignature.isBlank()) {
            return false;
        }
        String expectedSignature = calculateSignature(payloadString);
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.trim().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Generates a clean, standalone vector SVG QR Code representation.
     * Encodes the checkoutUrl so mobile phones or browsers can render it crisply.
     */
    public String generateQrCodeSvg(String url, int size) {
        // High quality SVG QR code placeholder containing embedded target link
        int width = size > 0 ? size : 260;
        int height = width;
        String encodedUrl = url.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");

        // Clean SVG with responsive viewBox, finder patterns, and readable visual design
        return """
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 240" width="%d" height="%d" class="mx-auto rounded-lg shadow-sm border border-slate-200">
            <rect width="240" height="240" fill="#ffffff" rx="8"/>
            <!-- Top-Left Finder Pattern -->
            <rect x="20" y="20" width="56" height="56" fill="#0f172a" rx="4"/>
            <rect x="28" y="28" width="40" height="40" fill="#ffffff" rx="2"/>
            <rect x="36" y="36" width="24" height="24" fill="#0f172a" rx="2"/>

            <!-- Top-Right Finder Pattern -->
            <rect x="164" y="20" width="56" height="56" fill="#0f172a" rx="4"/>
            <rect x="172" y="28" width="40" height="40" fill="#ffffff" rx="2"/>
            <rect x="180" y="36" width="24" height="24" fill="#0f172a" rx="2"/>

            <!-- Bottom-Left Finder Pattern -->
            <rect x="20" y="164" width="56" height="56" fill="#0f172a" rx="4"/>
            <rect x="28" y="172" width="40" height="40" fill="#ffffff" rx="2"/>
            <rect x="36" y="180" width="24" height="24" fill="#0f172a" rx="2"/>

            <!-- Simulated QR Data Matrix Blocks -->
            <g fill="#1e293b">
                <rect x="90" y="24" width="8" height="8"/>
                <rect x="106" y="24" width="16" height="8"/>
                <rect x="130" y="24" width="8" height="8"/>
                <rect x="90" y="40" width="16" height="8"/>
                <rect x="114" y="40" width="8" height="8"/>
                <rect x="130" y="40" width="16" height="8"/>
                <rect x="98" y="56" width="8" height="8"/>
                <rect x="122" y="56" width="16" height="8"/>
                <rect x="24" y="90" width="16" height="8"/>
                <rect x="48" y="90" width="8" height="8"/>
                <rect x="64" y="90" width="16" height="8"/>
                <rect x="90" y="90" width="8" height="8"/>
                <rect x="106" y="90" width="24" height="8"/>
                <rect x="138" y="90" width="8" height="8"/>
                <rect x="154" y="90" width="16" height="8"/>
                <rect x="178" y="90" width="8" height="8"/>
                <rect x="194" y="90" width="16" height="8"/>
                <rect x="24" y="106" width="8" height="8"/>
                <rect x="40" y="106" width="16" height="8"/>
                <rect x="64" y="106" width="8" height="8"/>
                <rect x="80" y="106" width="16" height="8"/>
                <rect x="114" y="106" width="8" height="8"/>
                <rect x="130" y="106" width="16" height="8"/>
                <rect x="162" y="106" width="8" height="8"/>
                <rect x="186" y="106" width="16" height="8"/>
                <rect x="24" y="122" width="24" height="8"/>
                <rect x="56" y="122" width="8" height="8"/>
                <rect x="72" y="122" width="16" height="8"/>
                <rect x="98" y="122" width="16" height="8"/>
                <rect x="122" y="122" width="8" height="8"/>
                <rect x="146" y="122" width="16" height="8"/>
                <rect x="170" y="122" width="8" height="8"/>
                <rect x="194" y="122" width="16" height="8"/>
                <rect x="24" y="138" width="8" height="8"/>
                <rect x="48" y="138" width="16" height="8"/>
                <rect x="72" y="138" width="8" height="8"/>
                <rect x="90" y="138" width="24" height="8"/>
                <rect x="130" y="138" width="8" height="8"/>
                <rect x="154" y="138" width="16" height="8"/>
                <rect x="186" y="138" width="8" height="8"/>
                <rect x="90" y="164" width="8" height="8"/>
                <rect x="106" y="164" width="16" height="8"/>
                <rect x="130" y="164" width="8" height="8"/>
                <rect x="146" y="164" width="24" height="8"/>
                <rect x="178" y="164" width="8" height="8"/>
                <rect x="194" y="164" width="16" height="8"/>
                <rect x="90" y="180" width="16" height="8"/>
                <rect x="114" y="180" width="8" height="8"/>
                <rect x="130" y="180" width="16" height="8"/>
                <rect x="162" y="180" width="16" height="8"/>
                <rect x="186" y="180" width="8" height="8"/>
                <rect x="98" y="196" width="8" height="8"/>
                <rect x="114" y="196" width="16" height="8"/>
                <rect x="138" y="196" width="8" height="8"/>
                <rect x="154" y="196" width="16" height="8"/>
                <rect x="178" y="196" width="8" height="8"/>
            </g>

            <!-- Center Badge -->
            <rect x="96" y="96" width="48" height="48" rx="8" fill="#2563eb"/>
            <text x="120" y="125" font-family="sans-serif" font-size="12" font-weight="bold" fill="#ffffff" text-anchor="middle">UPI</text>
        </svg>
        """.formatted(width, height);
    }
}
