package com.bidvelocity.bidding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * High-Concurrency Bidding Engine Microservice for BidVelocity Marketplace.
 * Processes real-time bids, enforces idempotency, calls Auction Service atomically via REST,
 * and maintains audit history of all accepted and outbid bids.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BiddingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiddingServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
