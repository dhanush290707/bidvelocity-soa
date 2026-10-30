package com.bidvelocity.payment.repository;

import com.bidvelocity.payment.entity.Payment;
import com.bidvelocity.payment.entity.PaymentStatus;
import com.bidvelocity.payment.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByCheckoutToken(String checkoutToken);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByAuctionIdAndWinnerIdAndPaymentRound(Long auctionId, Long winnerId, int paymentRound);
    Optional<Payment> findTopByAuctionIdOrderByCreatedAtDesc(Long auctionId);
    List<Payment> findByAuctionIdOrderByCreatedAtDesc(Long auctionId);
    List<Payment> findByWinnerIdOrderByCreatedAtDesc(Long winnerId);
    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.syncStatus = 'RETRY_PENDING' AND (p.nextRetryAt IS NULL OR p.nextRetryAt <= :now) AND p.retryCount < 5")
    List<Payment> findPendingReconciliations(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.tokenExpiresAt <= :now")
    List<Payment> findExpiredPendingPayments(@Param("now") LocalDateTime now);
}
