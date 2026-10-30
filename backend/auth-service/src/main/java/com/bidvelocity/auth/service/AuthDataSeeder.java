package com.bidvelocity.auth.service;

import com.bidvelocity.auth.entity.Role;
import com.bidvelocity.auth.entity.User;
import com.bidvelocity.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Automatically seeds starter buyer and seller accounts on initial startup
 * once Hibernate creates the database tables.
 */
@Component
public class AuthDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Auth database table 'users' is empty. Seeding default starter accounts...");

            // Starter Buyer account
            User buyer = new User(
                    "buyer1",
                    "buyer1@bidvelocity.io",
                    passwordEncoder.encode("password123"),
                    Role.ROLE_BUYER
            );
            userRepository.save(buyer);
            log.info("Seeded user: buyer1 (Password: password123, Role: ROLE_BUYER)");

            // Starter Seller account
            User seller = new User(
                    "seller1",
                    "seller1@bidvelocity.io",
                    passwordEncoder.encode("sellerpass123"),
                    Role.ROLE_SELLER
            );
            userRepository.save(seller);
            log.info("Seeded user: seller1 (Password: sellerpass123, Role: ROLE_SELLER)");

            // Optional Admin account
            User admin = new User(
                    "admin",
                    "admin@bidvelocity.io",
                    passwordEncoder.encode("adminpass123"),
                    Role.ROLE_ADMIN
            );
            userRepository.save(admin);
            log.info("Seeded user: admin (Password: adminpass123, Role: ROLE_ADMIN)");

            log.info("Starter accounts seeded successfully into PostgreSQL.");
        } else {
            log.info("Auth database 'users' table already contains {} records. Skipping seed.", userRepository.count());
        }
    }
}
