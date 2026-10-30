import React, { useState, useEffect } from 'react';
import { Clock, ShieldCheck, ArrowUpRight, Zap } from 'lucide-react';

export const LiveFloorHero = ({
  auction,
  onOpenLot,
  onPlaceQuickBid,
  isBidding
}) => {
  const [timeLeft, setTimeLeft] = useState('00h 00m 00s');

  useEffect(() => {
    if (!auction) return;

    const updateTimer = () => {
      const now = new Date().getTime();
      const end = new Date(auction.endTime).getTime();
      const diff = end - now;

      if (diff <= 0) {
        setTimeLeft('AUCTION CLOSED');
        return;
      }

      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const seconds = Math.floor((diff % (1000 * 60)) / 1000);

      setTimeLeft(
        `${String(hours).padStart(2, '0')}h ${String(minutes).padStart(2, '0')}m ${String(seconds).padStart(2, '0')}s`
      );
    };

    updateTimer();
    const interval = setInterval(updateTimer, 1000);
    return () => clearInterval(interval);
  }, [auction]);

  if (!auction) {
    return null;
  }

  return (
    <section className="border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/40 py-10 px-8 lg:px-12 transition-colors w-full">
      <div className="w-full">
        
        {/* Editorial Eyebrow */}
        <div className="flex items-center justify-between mb-6 text-xs uppercase tracking-widest text-zinc-500 font-mono">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
            <span className="text-zinc-900 dark:text-zinc-200 font-semibold">Live Floor Session</span>
            <span>·</span>
            <span>Lot #{String(auction.auctionId).padStart(2, '0')}</span>
            <span>·</span>
            <span>{auction.category || 'Luxury'}</span>
          </div>
          <div className="flex items-center gap-2 font-mono">
            <Clock className="w-3.5 h-3.5 text-zinc-400" />
            <span className="tabular-nums font-bold text-zinc-800 dark:text-zinc-200">{timeLeft}</span>
          </div>
        </div>

        {/* 2-Column Split */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-stretch">
          
          {/* Left Column: Product Showcase */}
          <div className="lg:col-span-7 flex flex-col justify-between">
            <div className="relative aspect-[4/3] max-h-[520px] w-full rounded-none overflow-hidden bg-zinc-200 dark:bg-zinc-800 border border-zinc-300 dark:border-zinc-700/80">
              <img
                src={auction.imageUrl || 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?q=80&w=1200&auto=format&fit=crop'}
                alt={auction.itemName}
                referrerPolicy="no-referrer"
                className="w-full h-full object-cover object-center transition-transform duration-700 hover:scale-105"
              />
              <div className="absolute top-4 left-4 bg-zinc-900/90 text-white text-[10px] uppercase font-mono px-2.5 py-1 tracking-wider backdrop-blur">
                Reserve Price Met
              </div>
            </div>

            <div className="mt-6">
              <h2 className="text-2xl lg:text-3xl font-black text-zinc-950 dark:text-white tracking-tight leading-tight">
                {auction.itemName}
              </h2>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400 line-clamp-2 leading-relaxed">
                {auction.description}
              </p>
            </div>
          </div>

          {/* Right Column: High-Velocity Live Bidding Module */}
          <div className="lg:col-span-5 bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 p-8 flex flex-col justify-between shadow-sm">
            <div>
              <div className="border-b border-zinc-100 dark:border-zinc-800/80 pb-6 mb-6">
                <span className="text-[11px] uppercase tracking-widest text-zinc-400 font-mono block mb-1">
                  Current Highest Bid
                </span>
                <div className="text-4xl lg:text-5xl font-black text-zinc-950 dark:text-white tabular-nums tracking-tight">
                  ₹ {auction.currentHighestBid.toLocaleString('en-IN')}
                </div>
                <div className="mt-2 flex items-center gap-2 text-xs text-zinc-500 font-mono">
                  <span>Starting: ₹{auction.startingPrice.toLocaleString('en-IN')}</span>
                  <span>·</span>
                  <span>Reserve: ₹{auction.reservePrice.toLocaleString('en-IN')}</span>
                </div>
              </div>

              {/* Provenance & Authenticity Highlights */}
              <div className="space-y-3 mb-8 text-xs text-zinc-600 dark:text-zinc-400">
                <div className="flex items-start gap-2">
                  <ShieldCheck className="w-4 h-4 text-zinc-800 dark:text-zinc-200 shrink-0 mt-0.5" />
                  <span>
                    <strong className="text-zinc-900 dark:text-zinc-100">Verified Condition:</strong>{' '}
                    {auction.conditionReport || 'Grade A. Certified authentic with archive paperwork.'}
                  </span>
                </div>
                <div className="flex items-start gap-2">
                  <Zap className="w-4 h-4 text-zinc-800 dark:text-zinc-200 shrink-0 mt-0.5" />
                  <span>
                    <strong className="text-zinc-900 dark:text-zinc-100">Deterministic Clearance:</strong> Instant UPI settlement upon closing.
                  </span>
                </div>
              </div>
            </div>

            {/* Live Bidding Actions */}
            <div className="space-y-3 pt-6 border-t border-zinc-100 dark:border-zinc-800/80">
              <div className="grid grid-cols-3 gap-2 text-xs font-mono">
                <button
                  disabled={isBidding || auction.status !== 'ACTIVE'}
                  onClick={() => onPlaceQuickBid(auction.auctionId, auction.currentHighestBid + 2000)}
                  className="py-2.5 px-2 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition disabled:opacity-50 text-center text-zinc-900 dark:text-zinc-100 font-bold"
                >
                  +₹2,000
                </button>
                <button
                  disabled={isBidding || auction.status !== 'ACTIVE'}
                  onClick={() => onPlaceQuickBid(auction.auctionId, auction.currentHighestBid + 5000)}
                  className="py-2.5 px-2 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition disabled:opacity-50 text-center text-zinc-900 dark:text-zinc-100 font-bold"
                >
                  +₹5,000
                </button>
                <button
                  disabled={isBidding || auction.status !== 'ACTIVE'}
                  onClick={() => onPlaceQuickBid(auction.auctionId, auction.currentHighestBid + 10000)}
                  className="py-2.5 px-2 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition disabled:opacity-50 text-center text-zinc-900 dark:text-zinc-100 font-bold"
                >
                  +₹10,000
                </button>
              </div>

              <button
                onClick={() => onOpenLot(auction)}
                className="w-full py-3.5 bg-zinc-950 text-white dark:bg-white dark:text-zinc-950 font-bold text-xs uppercase tracking-widest hover:opacity-90 transition flex items-center justify-center gap-2 shadow-sm"
              >
                <span>Inspect Lot &amp; Enter Bidding Rail</span>
                <ArrowUpRight className="w-4 h-4" />
              </button>
            </div>

          </div>

        </div>

      </div>
    </section>
  );
};
