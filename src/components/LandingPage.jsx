import React from 'react';
import { ArrowUpRight, ArrowRight } from 'lucide-react';

export const LandingPage = ({
  auctions,
  onOpenLot,
  onOpenAuth,
  isLoading
}) => {
  const featuredLot = auctions[0] || null;
  const previewLots = auctions.slice(0, 4);

  return (
    <div className="space-y-24 pb-24">
      
      {/* Editorial Swiss Gallery Hero */}
      <section className="pt-16 pb-20 px-8 lg:px-12 border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/30 w-full">
        <div className="w-full grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          
          <div className="lg:col-span-7 space-y-6">
            <div className="flex items-center gap-2 text-xs font-mono uppercase tracking-widest text-zinc-500">
              <span className="w-2 h-2 rounded-full bg-zinc-900 dark:bg-zinc-100"></span>
              <span>Geneva · London · Singapore · Zurich</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black text-zinc-950 dark:text-white tracking-tight leading-[1.08] text-balance">
              High-Velocity Clearance for Rare &amp; Curated Lots.
            </h1>

            <p className="text-base text-zinc-600 dark:text-zinc-400 max-w-xl leading-relaxed font-sans">
              BidVelocity is an enterprise-grade digital auction house powered by distributed microservices.
              Engineered for atomic sub-millisecond bid execution, deterministic auction resolution, and instant UPI settlement.
            </p>

            <div className="pt-4 flex flex-wrap items-center gap-4 text-xs font-mono">
              <button
                onClick={() => {
                  const el = document.getElementById('preview-catalog');
                  el?.scrollIntoView({ behavior: 'smooth' });
                }}
                className="px-6 py-3.5 bg-zinc-950 text-white dark:bg-white dark:text-zinc-950 font-bold uppercase tracking-widest hover:opacity-90 transition shadow-sm flex items-center gap-2"
              >
                <span>Explore Live Catalogue</span>
                <ArrowRight className="w-4 h-4" />
              </button>

              <button
                onClick={onOpenAuth}
                className="px-6 py-3.5 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-900 font-bold uppercase tracking-widest text-zinc-900 dark:text-white transition"
              >
                Sign In / Join as Collector
              </button>
            </div>

            {/* Subtle Metrics Strip */}
            <div className="pt-8 border-t border-zinc-200 dark:border-zinc-800 grid grid-cols-3 gap-6 font-mono text-xs">
              <div>
                <span className="text-zinc-400 block text-[10px] uppercase">Resolution Window</span>
                <span className="text-xl font-bold text-zinc-900 dark:text-white tabular-nums">15 Mins</span>
              </div>
              <div>
                <span className="text-zinc-400 block text-[10px] uppercase">Locking Strategy</span>
                <span className="text-xl font-bold text-zinc-900 dark:text-white">@Version</span>
              </div>
              <div>
                <span className="text-zinc-400 block text-[10px] uppercase">Clearance Method</span>
                <span className="text-xl font-bold text-emerald-600 dark:text-emerald-400">Mock UPI QR</span>
              </div>
            </div>
          </div>

          {/* Right Hero Image Card */}
          {featuredLot && (
            <div className="lg:col-span-5">
              <div
                onClick={() => onOpenLot(featuredLot)}
                className="group border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 p-6 shadow-xl cursor-pointer hover:border-zinc-900 dark:hover:border-zinc-500 transition-all"
              >
                <div className="aspect-[4/3] overflow-hidden bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 mb-5 relative">
                  <img
                    src={featuredLot.imageUrl || 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?q=80&w=1200&auto=format&fit=crop'}
                    alt={featuredLot.itemName}
                    referrerPolicy="no-referrer"
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                  />
                  <div className="absolute top-3 left-3 bg-zinc-950/90 text-white text-[10px] uppercase font-mono px-2 py-0.5 backdrop-blur">
                    Premier Highlight
                  </div>
                </div>

                <div className="flex items-center gap-2 text-[11px] font-mono text-zinc-400 mb-1 uppercase tracking-wider">
                  <span>{featuredLot.category || 'Luxury'}</span>
                  <span>·</span>
                  <span>Lot #{String(featuredLot.auctionId).padStart(2, '0')}</span>
                </div>

                <h3 className="text-lg font-bold text-zinc-900 dark:text-white tracking-tight line-clamp-1">
                  {featuredLot.itemName}
                </h3>

                <div className="mt-4 pt-4 border-t border-zinc-100 dark:border-zinc-800 flex justify-between items-center font-mono">
                  <div>
                    <span className="text-[10px] uppercase text-zinc-400 block">Current High Offer</span>
                    <span className="text-xl font-black text-zinc-900 dark:text-white tabular-nums">
                      ₹ {featuredLot.currentHighestBid?.toLocaleString('en-IN')}
                    </span>
                  </div>
                  <span className="text-xs font-bold text-zinc-900 dark:text-white underline flex items-center gap-1">
                    <span>Inspect Lot</span>
                    <ArrowUpRight className="w-3.5 h-3.5" />
                  </span>
                </div>
              </div>
            </div>
          )}

        </div>
      </section>

      {/* The 3 Clearance Standards */}
      <section className="w-full px-8 lg:px-12 py-8">
        <div className="border-b border-zinc-200 dark:border-zinc-800 pb-4 mb-12">
          <span className="text-xs uppercase font-mono tracking-widest text-zinc-400 block mb-1">
            The BidVelocity Standard
          </span>
          <h2 className="text-2xl font-black tracking-tight text-zinc-900 dark:text-white">
            Architecture Engineered for Auction Integrity
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="p-8 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 flex flex-col justify-between">
            <div>
              <div className="w-10 h-10 border border-zinc-200 dark:border-zinc-800 flex items-center justify-center font-mono font-bold text-sm mb-6 bg-zinc-50 dark:bg-zinc-900">
                01
              </div>
              <h3 className="text-base font-bold text-zinc-900 dark:text-white mb-2">
                Deterministic Concurrency
              </h3>
              <p className="text-xs text-zinc-500 dark:text-zinc-400 leading-relaxed font-sans">
                Sub-millisecond race condition prevention using database <code className="text-zinc-900 dark:text-zinc-100">@Version</code> optimistic locking. Bids colliding during closing seconds are atomically evaluated without locking catalog lookups.
              </p>
            </div>
            <div className="pt-6 mt-6 border-t border-zinc-100 dark:border-zinc-800/80 text-[11px] font-mono text-zinc-400">
              Auction Service :8082
            </div>
          </div>

          <div className="p-8 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 flex flex-col justify-between">
            <div>
              <div className="w-10 h-10 border border-zinc-200 dark:border-zinc-800 flex items-center justify-center font-mono font-bold text-sm mb-6 bg-zinc-50 dark:bg-zinc-900">
                02
              </div>
              <h3 className="text-base font-bold text-zinc-900 dark:text-white mb-2">
                Server-Signed Mock UPI Clearance
              </h3>
              <p className="text-xs text-zinc-500 dark:text-zinc-400 leading-relaxed font-sans">
                Dynamic QR code checkout links with constant-time HMAC-SHA256 webhook callbacks. State transitions to <code className="text-zinc-900 dark:text-zinc-100">CLEARED</code> only upon cryptographic signature verification.
              </p>
            </div>
            <div className="pt-6 mt-6 border-t border-zinc-100 dark:border-zinc-800/80 text-[11px] font-mono text-zinc-400">
              Payment Service :8084
            </div>
          </div>

          <div className="p-8 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 flex flex-col justify-between">
            <div>
              <div className="w-10 h-10 border border-zinc-200 dark:border-zinc-800 flex items-center justify-center font-mono font-bold text-sm mb-6 bg-zinc-50 dark:bg-zinc-900">
                03
              </div>
              <h3 className="text-base font-bold text-zinc-900 dark:text-white mb-2">
                Decoupled PostgreSQL Isolation
              </h3>
              <p className="text-xs text-zinc-500 dark:text-zinc-400 leading-relaxed font-sans">
                Strict database boundaries with 4 dedicated PostgreSQL databases. No shared tables or cross-database entity leaks; inter-service calls use Eureka-discovered REST clients.
              </p>
            </div>
            <div className="pt-6 mt-6 border-t border-zinc-100 dark:border-zinc-800/80 text-[11px] font-mono text-zinc-400">
              Spring Cloud Gateway :8080
            </div>
          </div>
        </div>
      </section>

      {/* Preview Catalogue */}
      <section id="preview-catalog" className="w-full px-8 lg:px-12 py-8">
        <div className="flex flex-col sm:flex-row sm:items-end justify-between border-b border-zinc-200 dark:border-zinc-800 pb-4 mb-10 gap-4">
          <div>
            <span className="text-xs uppercase font-mono tracking-widest text-zinc-400 block mb-1">
              Active Floor Lots
            </span>
            <h2 className="text-2xl font-black tracking-tight text-zinc-900 dark:text-white">
              Curated Catalogue Preview
            </h2>
          </div>
          <button
            onClick={onOpenAuth}
            className="text-xs uppercase font-mono font-bold text-zinc-900 dark:text-white underline"
          >
            Sign in to Place Live Bids ➔
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {previewLots.map((lot) => (
            <article
              key={lot.auctionId}
              onClick={() => onOpenLot(lot)}
              className="group border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 p-4 flex flex-col justify-between hover:border-zinc-900 dark:hover:border-zinc-500 transition-all cursor-pointer shadow-sm"
            >
              <div>
                <div className="aspect-[4/3] overflow-hidden bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 mb-4">
                  <img
                    src={lot.imageUrl || 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?q=80&w=1200&auto=format&fit=crop'}
                    alt={lot.itemName}
                    referrerPolicy="no-referrer"
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  />
                </div>

                <div className="text-[10px] font-mono text-zinc-400 uppercase mb-1">
                  {lot.category} · Lot #{String(lot.auctionId).padStart(2, '0')}
                </div>

                <h4 className="text-sm font-bold text-zinc-900 dark:text-white line-clamp-1 group-hover:text-zinc-600 dark:group-hover:text-zinc-300 transition-colors">
                  {lot.itemName}
                </h4>
              </div>

              <div className="pt-3 mt-4 border-t border-zinc-100 dark:border-zinc-800 flex justify-between items-center font-mono">
                <div>
                  <span className="text-[9px] uppercase text-zinc-400 block">Current High</span>
                  <span className="text-sm font-bold text-zinc-900 dark:text-white tabular-nums">
                    ₹ {lot.currentHighestBid?.toLocaleString('en-IN')}
                  </span>
                </div>
                <span className="text-[11px] underline font-bold text-zinc-900 dark:text-white">
                  Inspect
                </span>
              </div>
            </article>
          ))}
        </div>
      </section>

      {/* Consignor Banner */}
      <section className="w-full px-8 lg:px-12 py-8">
        <div className="p-10 border border-zinc-200 dark:border-zinc-800 bg-zinc-900 text-white flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="space-y-2 max-w-xl">
            <span className="text-xs uppercase font-mono tracking-widest text-zinc-400">
              For Gallerists, Dealers &amp; Collectors
            </span>
            <h3 className="text-2xl font-black tracking-tight text-white">
              Consign Your Rarities with BidVelocity
            </h3>
            <p className="text-xs text-zinc-400 font-sans leading-relaxed">
              Register as an accredited consignor to manage listing lifecycles, monitor live bidding momentum, and receive automated settlement through our payment clearance engine.
            </p>
          </div>

          <button
            onClick={onOpenAuth}
            className="px-6 py-3.5 bg-white text-zinc-950 font-bold uppercase tracking-widest text-xs hover:opacity-90 transition shrink-0 shadow"
          >
            Register Consignor Account
          </button>
        </div>
      </section>

    </div>
  );
};
