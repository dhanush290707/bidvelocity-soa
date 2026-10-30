package com.bidvelocity.auction.repository;

import com.bidvelocity.auction.entity.ProcessedBidRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedBidRequestRepository extends JpaRepository<ProcessedBidRequest, String> {
    Optional<ProcessedBidRequest> findByBidRequestId(String bidRequestId);
    boolean existsByBidRequestId(String bidRequestId);
}
