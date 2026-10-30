package com.bidvelocity.auction.repository;

import com.bidvelocity.auction.entity.Auction;
import com.bidvelocity.auction.entity.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findBySellerId(Long sellerId);

    @Query("SELECT a FROM Auction a WHERE a.status = 'DRAFT' AND a.startTime <= :now")
    List<Auction> findPendingAuctionsToActivate(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' AND a.endTime <= :now")
    List<Auction> findActiveAuctionsToClose(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Auction a SET a.status = 'PAYMENT_PENDING' WHERE a.auctionId = :id AND a.status = 'ACTIVE'")
    int transitionToPaymentPendingAtomic(@Param("id") Long id);
}
