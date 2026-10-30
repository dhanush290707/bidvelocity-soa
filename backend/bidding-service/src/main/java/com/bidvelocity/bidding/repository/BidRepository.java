package com.bidvelocity.bidding.repository;

import com.bidvelocity.bidding.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    Optional<Bid> findByBidRequestId(String bidRequestId);
    List<Bid> findByAuctionIdOrderByBidAmountDesc(Long auctionId);
    List<Bid> findByUserIdOrderByBidTimeDesc(Long userId);

    @Modifying
    @Query("UPDATE Bid b SET b.status = 'OUTBID' WHERE b.auctionId = :auctionId AND b.status = 'ACCEPTED' AND b.bidId <> :newBidId")
    void markPreviousBidsOutbid(@Param("auctionId") Long auctionId, @Param("newBidId") Long newBidId);

    @Query("SELECT b FROM Bid b WHERE b.auctionId = :auctionId AND b.userId <> :excludeUserId ORDER BY b.bidAmount DESC")
    List<Bid> findRunnerUpBids(@Param("auctionId") Long auctionId, @Param("excludeUserId") Long excludeUserId);
}
