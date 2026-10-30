package com.bidvelocity.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGatewayFilter.class);

    private final SecretKey key;

    // Endpoints that do not require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/validate",
            "/api/payments/mock-gateway",
            "/api/payments/webhook",
            "/actuator"
    );

    public JwtAuthGatewayFilter(
            @Value("${jwt.secret:BidVelocitySecretKey2026ForAcademicDistributedSystemsEvaluationMustBeVeryLong32CharsMin}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Mandatory Header Sanitization:
        // Strip any incoming untrusted identity headers to prevent spoofing
        ServerHttpRequest.Builder requestBuilder = request.mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Role");
                    httpHeaders.remove("X-User-Email");
                    httpHeaders.remove("X-User-Name");
                });

        // 2. Check if route is public
        boolean isPublic = PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)
                || (request.getMethod().name().equalsIgnoreCase("GET") && path.contains("/checkout"))
                || (request.getMethod().name().equalsIgnoreCase("GET") && path.startsWith("/api/auctions"))
                || (request.getMethod().name().equalsIgnoreCase("GET") && path.startsWith("/api/bids/auction"));

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isPublic) {
                return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
            }
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7).trim();
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (claims.getExpiration().before(new Date())) {
                return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
            }

            Object userId = claims.get("userId");
            Object role = claims.get("role");
            Object email = claims.get("email");
            String username = claims.getSubject();

            // 3. Inject verified claims into downstream request
            requestBuilder.header("X-User-Id", userId != null ? userId.toString() : "");
            requestBuilder.header("X-User-Role", role != null ? role.toString() : "");
            requestBuilder.header("X-User-Email", email != null ? email.toString() : "");
            requestBuilder.header("X-User-Name", username != null ? username : "");

            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());

        } catch (Exception e) {
            log.warn("JWT validation failed for path {}: {}", path, e.getMessage());
            if (isPublic) {
                return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
            }
            return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        String errorJson = "{\"error\": \"" + message + "\", \"status\": " + status.value() + "}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -100; // High precedence before routing
    }
}
