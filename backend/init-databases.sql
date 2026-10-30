-- ====================================================================
-- BidVelocity Marketplace: PostgreSQL Database Initialization
-- ====================================================================
-- NOTE: Hibernate automatically creates all tables ('users', 'auctions', 
-- 'processed_bid_requests', 'bids', 'payments') when the backend services boot!
--
-- You only need to execute the CREATE DATABASE statements below once in PostgreSQL:
-- ====================================================================

-- 1. Create the databases (if not already existing)
CREATE DATABASE bidvelocity_auth;
CREATE DATABASE bidvelocity_auction;
CREATE DATABASE bidvelocity_bidding;
CREATE DATABASE bidvelocity_payment;

-- Or if you prefer a single database for all services:
-- CREATE DATABASE bidvelocity;

-- ====================================================================
-- MANUAL DDL REFERENCE (OPTIONAL - Tables are auto-created by Spring Boot)
-- ====================================================================

\c bidvelocity_auth;
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_BUYER',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

\c bidvelocity_auction;
CREATE TABLE IF NOT EXISTS auctions (
    auction_id BIGSERIAL PRIMARY KEY,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) DEFAULT 'Collectibles',
    image_url TEXT,
    condition_report TEXT,
    provenance TEXT,
    starting_price NUMERIC(15,2) NOT NULL,
    reserve_price NUMERIC(15,2) NOT NULL,
    current_highest_bid NUMERIC(15,2) NOT NULL,
    start_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    seller_id BIGINT NOT NULL,
    winner_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    second_chance_offered BOOLEAN DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS processed_bid_requests (
    bid_request_id VARCHAR(100) PRIMARY KEY,
    auction_id BIGINT NOT NULL,
    applied_bid_amount NUMERIC(15,2) NOT NULL,
    processed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

\c bidvelocity_bidding;
CREATE TABLE IF NOT EXISTS bids (
    bid_id BIGSERIAL PRIMARY KEY,
    bid_request_id VARCHAR(100) UNIQUE NOT NULL,
    auction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    bid_amount NUMERIC(15,2) NOT NULL,
    bid_time TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACCEPTED'
);

\c bidvelocity_payment;
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGSERIAL PRIMARY KEY,
    auction_id BIGINT NOT NULL,
    winner_id BIGINT NOT NULL,
    payment_round INT DEFAULT 1,
    amount NUMERIC(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    checkout_token VARCHAR(255) UNIQUE NOT NULL,
    token_expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE,
    transaction_ref VARCHAR(255),
    webhook_signature VARCHAR(255),
    sync_status VARCHAR(50) NOT NULL DEFAULT 'SYNCED',
    retry_count INT DEFAULT 0,
    last_retry_at TIMESTAMP WITHOUT TIME ZONE,
    next_retry_at TIMESTAMP WITHOUT TIME ZONE,
    last_sync_error TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cleared_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uq_auction_winner_round UNIQUE (auction_id, winner_id, payment_round)
);
