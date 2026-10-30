package com.bidvelocity.payment.controller;

import com.bidvelocity.payment.dto.MockPaymentSubmitRequest;
import com.bidvelocity.payment.dto.WebhookPayload;
import com.bidvelocity.payment.entity.Payment;
import com.bidvelocity.payment.service.MockGatewayService;
import com.bidvelocity.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/mock-gateway")
@CrossOrigin(origins = "*")
public class MockGatewayWebController {

    private final PaymentService paymentService;
    private final MockGatewayService mockGatewayService;

    public MockGatewayWebController(PaymentService paymentService, MockGatewayService mockGatewayService) {
        this.paymentService = paymentService;
        this.mockGatewayService = mockGatewayService;
    }

    /**
     * Renders a responsive, simulated UPI Payment Gateway checkout page.
     * When scanned from a mobile phone or opened in a desktop browser, this UI
     * lets the user review the auction clearance amount and simulate payment outcomes.
     */
    @GetMapping(value = "/pay", produces = MediaType.TEXT_HTML_VALUE)
    public String renderMockGatewayPage(@RequestParam("token") String token) {
        Payment payment;
        try {
            payment = paymentService.getPaymentByToken(token);
        } catch (Exception e) {
            return """
                <!DOCTYPE html>
                <html><head><title>Invalid Payment Session</title>
                <script src="https://cdn.tailwindcss.com"></script></head>
                <body class="bg-slate-100 flex items-center justify-center min-h-screen p-4">
                    <div class="bg-white p-8 rounded-2xl shadow-lg max-w-md w-full text-center">
                        <div class="w-16 h-16 bg-red-100 text-red-600 rounded-full flex items-center justify-center mx-auto mb-4 text-2xl font-bold">!</div>
                        <h1 class="text-xl font-bold text-slate-900 mb-2">Invalid or Expired Payment Link</h1>
                        <p class="text-sm text-slate-600 mb-4">This checkout session does not exist or has already expired.</p>
                    </div>
                </body></html>
            """;
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>BidVelocity UPI Checkout (Simulation)</title>
                <script src="https://cdn.tailwindcss.com"></script>
            </head>
            <body class="bg-slate-900 text-slate-100 min-h-screen flex items-center justify-center p-4">
                <div class="bg-slate-800 border border-slate-700 max-w-lg w-full rounded-2xl p-6 shadow-2xl">
                    <div class="flex items-center justify-between border-b border-slate-700 pb-4 mb-4">
                        <div class="flex items-center space-x-3">
                            <div class="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center font-bold text-white text-lg">BV</div>
                            <div>
                                <h1 class="font-bold text-lg text-white">BidVelocity Mock UPI</h1>
                                <p class="text-xs text-slate-400">Secure Auction Clearance Portal</p>
                            </div>
                        </div>
                        <span class="px-3 py-1 bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-full text-xs font-semibold">Demo Gateway</span>
                    </div>

                    <div class="bg-slate-900/80 rounded-xl p-4 border border-slate-700/60 mb-6 space-y-2">
                        <div class="flex justify-between text-sm">
                            <span class="text-slate-400">Auction Reference:</span>
                            <span class="font-semibold text-white">#%d</span>
                        </div>
                        <div class="flex justify-between text-sm">
                            <span class="text-slate-400">Winner User ID:</span>
                            <span class="font-semibold text-white">#%d</span>
                        </div>
                        <div class="flex justify-between text-sm">
                            <span class="text-slate-400">Payment Status:</span>
                            <span class="font-semibold text-blue-400 uppercase tracking-wider">%s</span>
                        </div>
                        <div class="flex justify-between text-sm pt-2 border-t border-slate-700/80">
                            <span class="text-slate-300 font-medium">Total Winning Amount:</span>
                            <span class="text-2xl font-black text-emerald-400">₹ %s</span>
                        </div>
                    </div>

                    <div class="mb-6">
                        <h2 class="text-xs uppercase font-bold text-slate-400 mb-2">Simulated UPI Apps</h2>
                        <div class="grid grid-cols-4 gap-2 text-center text-xs">
                            <div class="p-2.5 bg-slate-700/50 hover:bg-slate-700 rounded-xl border border-slate-600/50 cursor-pointer">
                                <div class="w-8 h-8 mx-auto mb-1 bg-emerald-600 rounded-lg flex items-center justify-center font-bold text-white text-sm">G</div>
                                <span>GPay</span>
                            </div>
                            <div class="p-2.5 bg-slate-700/50 hover:bg-slate-700 rounded-xl border border-slate-600/50 cursor-pointer">
                                <div class="w-8 h-8 mx-auto mb-1 bg-purple-600 rounded-lg flex items-center justify-center font-bold text-white text-sm">Pe</div>
                                <span>PhonePe</span>
                            </div>
                            <div class="p-2.5 bg-slate-700/50 hover:bg-slate-700 rounded-xl border border-slate-600/50 cursor-pointer">
                                <div class="w-8 h-8 mx-auto mb-1 bg-sky-500 rounded-lg flex items-center justify-center font-bold text-white text-sm">P</div>
                                <span>Paytm</span>
                            </div>
                            <div class="p-2.5 bg-slate-700/50 hover:bg-slate-700 rounded-xl border border-slate-600/50 cursor-pointer">
                                <div class="w-8 h-8 mx-auto mb-1 bg-orange-600 rounded-lg flex items-center justify-center font-bold text-white text-sm">B</div>
                                <span>BHIM</span>
                            </div>
                        </div>
                    </div>

                    <div class="space-y-3">
                        <button onclick="triggerPayment('SUCCESS')" class="w-full py-3.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-xl shadow-lg transition active:scale-[0.98] flex items-center justify-center space-x-2">
                            <span>✓ Simulate Successful Payment</span>
                        </button>
                        <button onclick="triggerPayment('FAILED')" class="w-full py-2.5 bg-rose-600/80 hover:bg-rose-600 text-white font-semibold rounded-xl text-sm transition">
                            ✕ Simulate Payment Decline / Failure
                        </button>
                        <button onclick="triggerPayment('SUCCESS', true)" class="w-full py-2 bg-slate-700 hover:bg-slate-600 text-slate-300 rounded-xl text-xs font-mono transition">
                            ↻ Test Duplicate Callback (Idempotency Replay)
                        </button>
                    </div>

                    <div id="resultBox" class="mt-4 p-3 rounded-xl hidden text-xs font-mono"></div>
                </div>

                <script>
                    let lastIdempotencyKey = null;

                    async function triggerPayment(action, isReplay = false) {
                        const box = document.getElementById('resultBox');
                        box.className = 'mt-4 p-3 rounded-xl text-xs font-mono bg-blue-900/50 text-blue-200 border border-blue-700 block';
                        box.innerText = 'Dispatching server-signed HMAC callback...';

                        const payload = {
                            token: '%s',
                            action: action,
                            transactionRef: 'UPI-UTR-' + Math.floor(100000000000 + Math.random() * 900000000000),
                            idempotencyKey: isReplay && lastIdempotencyKey ? lastIdempotencyKey : ('IDEMP-' + Date.now() + '-' + Math.floor(Math.random()*10000))
                        };

                        if (!isReplay) {
                            lastIdempotencyKey = payload.idempotencyKey;
                        }

                        try {
                            const res = await fetch('/api/payments/mock-gateway/simulate-submit', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(payload)
                            });
                            const data = await res.json();
                            if (res.ok) {
                                box.className = 'mt-4 p-3 rounded-xl text-xs font-mono bg-emerald-900/60 text-emerald-200 border border-emerald-700 block';
                                box.innerHTML = '<strong>' + data.message + '</strong><br>Status: ' + data.status + '<br>Sync to Auction: ' + data.syncStatus;
                            } else {
                                box.className = 'mt-4 p-3 rounded-xl text-xs font-mono bg-rose-900/60 text-rose-200 border border-rose-700 block';
                                box.innerText = 'Error: ' + JSON.stringify(data);
                            }
                        } catch (err) {
                            box.className = 'mt-4 p-3 rounded-xl text-xs font-mono bg-rose-900/60 text-rose-200 border border-rose-700 block';
                            box.innerText = 'Network error: ' + err.message;
                        }
                    }
                </script>
            </body>
            </html>
        """.formatted(
                payment.getAuctionId(),
                payment.getWinnerId(),
                payment.getStatus().name(),
                payment.getAmount().setScale(2).toString(),
                token
        );
    }

    /**
     * Server-side trigger that calculates the private HMAC signature and calls
     * the webhook endpoint, guaranteeing that browser requests never directly alter payment status.
     */
    @PostMapping("/simulate-submit")
    public ResponseEntity<Map<String, Object>> simulateSubmit(@Valid @RequestBody MockPaymentSubmitRequest request) {
        Payment payment = paymentService.getPaymentByToken(request.getToken());

        String idempotencyKey = request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()
                ? request.getIdempotencyKey()
                : "IDEMP-" + UUID.randomUUID();

        String transactionRef = request.getTransactionRef() != null && !request.getTransactionRef().isBlank()
                ? request.getTransactionRef()
                : "UPI-UTR-" + System.currentTimeMillis();

        long timestamp = System.currentTimeMillis();

        // Build canonical payload
        String canonical = payment.getPaymentId() + "|" +
                payment.getAmount().setScale(2).toString() + "|" +
                idempotencyKey + "|" +
                request.getAction() + "|" +
                timestamp;

        // Calculate HMAC signature on server
        String signature = mockGatewayService.calculateSignature(canonical);

        // Dispatch to internal webhook processing
        WebhookPayload webhookPayload = new WebhookPayload(
                payment.getPaymentId(),
                payment.getAmount(),
                idempotencyKey,
                request.getAction(),
                transactionRef,
                timestamp
        );

        Payment updated = paymentService.processWebhook(webhookPayload, signature);

        return ResponseEntity.ok(Map.of(
                "paymentId", updated.getPaymentId(),
                "status", updated.getStatus().name(),
                "syncStatus", updated.getSyncStatus().name(),
                "transactionRef", updated.getTransactionRef() != null ? updated.getTransactionRef() : "",
                "message", "Payment callback verified and processed by server"
        ));
    }
}
