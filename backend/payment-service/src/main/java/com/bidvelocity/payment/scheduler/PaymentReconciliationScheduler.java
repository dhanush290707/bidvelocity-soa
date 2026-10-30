package com.bidvelocity.payment.scheduler;

import com.bidvelocity.payment.entity.Payment;
import com.bidvelocity.payment.repository.PaymentRepository;
import com.bidvelocity.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationScheduler.class);

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentReconciliationScheduler(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    /**
     * Reconciles payments whose clearance sync to Auction Service failed temporarily.
     * Retries with exponential backoff up to 5 times before transitioning to SYNC_FAILED.
     */
    @Scheduled(fixedDelay = 15000)
    public void retryPendingReconciliations() {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> pending = paymentRepository.findPendingReconciliations(now);

        for (Payment payment : pending) {
            log.info("Reconciling payment clearance for paymentId: {}, auctionId: {} (attempt {}/5)",
                    payment.getPaymentId(), payment.getAuctionId(), payment.getRetryCount() + 1);
            paymentService.syncAuctionClearance(payment);
        }
    }
}
