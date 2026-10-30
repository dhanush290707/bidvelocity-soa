package com.bidvelocity.auction.service;

import com.bidvelocity.auction.entity.Auction;
import com.bidvelocity.auction.entity.AuctionStatus;
import com.bidvelocity.auction.repository.AuctionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Automatically seeds starter luxury auction lots on initial startup
 * once Hibernate creates the database tables.
 */
@Component
public class AuctionDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AuctionDataSeeder.class);

    private final AuctionRepository auctionRepository;

    public AuctionDataSeeder(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Override
    public void run(String... args) {
        if (auctionRepository.count() == 0) {
            log.info("Auction database table 'auctions' is empty. Seeding curated starter lots...");

            LocalDateTime now = LocalDateTime.now();

            Auction lot1 = new Auction(
                    "1968 Rolex Submariner Ref. 5513 'Meters First'",
                    "Exceptional matte dial with original tritium luminous plots and untouched bevels on lugs. Calibre 1520 automatic movement.",
                    "Watches",
                    "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?q=80&w=1200&auto=format&fit=crop",
                    "Grade A. Very minor surface marks on bezel insert. Movement running +2 sec/day.",
                    "Private collection, Geneva (acquired 1994). Sold with original punched papers.",
                    new BigDecimal("250000.00"),
                    new BigDecimal("320000.00"),
                    now.minusHours(1),
                    now.plusHours(24),
                    1L
            );
            lot1.setCurrentHighestBid(new BigDecimal("275000.00"));
            lot1.setStatus(AuctionStatus.ACTIVE);

            Auction lot2 = new Auction(
                    "Dieter Rams Braun Audio 300 Hi-Fi Console (1969)",
                    "Iconic German minimalist functionalist design with acrylic dust cover, integrated turntable and solid aluminum faceplate.",
                    "Electronics",
                    "https://images.unsplash.com/photo-1546435770-a3e426bf472b?q=80&w=1200&auto=format&fit=crop",
                    "Mint original cosmetic condition. Recapped audio capacitors and calibrated turntable cartridge.",
                    "Estate of industrial designer Karl Schwanzer, Vienna.",
                    new BigDecimal("120000.00"),
                    new BigDecimal("160000.00"),
                    now.minusHours(2),
                    now.plusHours(18),
                    1L
            );
            lot2.setCurrentHighestBid(new BigDecimal("135000.00"));
            lot2.setStatus(AuctionStatus.ACTIVE);

            Auction lot3 = new Auction(
                    "Pierre Jeanneret Floating Back Armchair PJ-SI-28-A",
                    "Chandigarh collection ca. 1955-1956. Solid teak and hand-woven cane work with historical stenciled administrative markings.",
                    "Collectibles",
                    "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?q=80&w=1200&auto=format&fit=crop",
                    "Preserved original patina with structural stabilization. Re-caned according to archival specifications.",
                    "Administrative Secretariat, Chandigarh. Private collection, Paris.",
                    new BigDecimal("450000.00"),
                    new BigDecimal("550000.00"),
                    now.minusHours(3),
                    now.plusHours(30),
                    1L
            );
            lot3.setCurrentHighestBid(new BigDecimal("480000.00"));
            lot3.setStatus(AuctionStatus.ACTIVE);

            Auction lot4 = new Auction(
                    "Brutalist Cast Bronze Kinetic Sculpture by Lynn Chadwick",
                    "Signed and numbered limited bronze casting on honed Belgian black marble plinth.",
                    "Fine Art",
                    "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?q=80&w=1200&auto=format&fit=crop",
                    "Pristine cast condition with dark brown-green atmospheric patina.",
                    "Exhibited Marlborough Fine Art, London (1978). Authenticated by artist estate.",
                    new BigDecimal("750000.00"),
                    new BigDecimal("900000.00"),
                    now.minusMinutes(30),
                    now.plusHours(48),
                    1L
            );
            lot4.setCurrentHighestBid(new BigDecimal("750000.00"));
            lot4.setStatus(AuctionStatus.ACTIVE);

            auctionRepository.saveAll(List.of(lot1, lot2, lot3, lot4));
            log.info("Successfully seeded 4 starter luxury lots across Watches, Electronics, Collectibles, and Fine Art.");
        } else {
            log.info("Auction database 'auctions' table already contains {} records. Skipping seed.", auctionRepository.count());
        }
    }
}
