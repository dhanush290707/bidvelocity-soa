package com.bidvelocity.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Reactive Spring Cloud API Gateway for BidVelocity Marketplace.
 * Single entry point handling route forwarding via Eureka (lb://),
 * untrusted header sanitization, and JWT authentication claim propagation.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
