import React, { useState, useEffect } from 'react';
import { api, ApiError } from '../api/client';
import { X, AlertCircle, CheckCircle2, History } from 'lucide-react';

export const LotDetailModal = ({
  auction,
  currentUser,
  onClose,
  onAuctionUpdated,
  onOpenPaymentDrawer
}) => {
  const [bids, setBids] = useState([]);
  const [customBidAmount, setCustomBidAmount] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorBanner, setErrorBanner] = useState(null);
  const [conflictNewMinBid, setConflictNewMinBid] = useState(null);
  const [successBanner, setSuccessBanner] = useState(null);

  // Poll bid history for this auction every 3 seconds while modal is open
  useEffect(() => {
    if (!auction) return;

    let isMounted = true;
    const fetchBids = async () => {
      try {
        const fetchedBids = await api.bids.getForAuction(auction.auctionId);
        if (isMounted) {
          setBids(fetchedBids);
        }
      } catch (err) {
        // Silently handle polling glitch
      }
    };

    fetchBids();
    const interval = setInterval(fetchBids, 3000);

    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, [auction]);

  if (!auction) return null;

  const minNextBid = auction.currentHighestBid + 1000;

  const handlePlaceBid = async (amount) => {
    setIsSubmitting(true);
    setErrorBanner(null);
    setConflictNewMinBid(null);
    setSuccessBanner(null);

    const bidRequestId = 'bid-' + Date.now() + '-' + Math.floor(Math.random() * 10000);

    try {
      await api.bids.place({
        auctionId: auction.auctionId,
        bidAmount: amount,
        bidRequestId
      });

      setSuccessBanner(`Bid of ₹${amount.toLocaleString('en-IN')} accepted successfully!`);
      setCustomBidAmount('');
      onAuctionUpdated();

      // Refresh bids immediately
      const refreshedBids = await api.bids.getForAuction(auction.auctionId);
      setBids(refreshedBids);
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setErrorBanner(`High Concurrency Collision: Another bidder placed a higher bid!`);
        const latestHigh = auction.currentHighestBid + 2000;
        setConflictNewMinBid(latestHigh);
      } else {
        setErrorBanner(err.message || 'Failed to place bid. Please check your network.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCloseAuction = async () => {
    if (!window.confirm('Are you sure you want to close this auction and finalize the winner?')) return;
    try {
      setIsSubmitting(true);
      await api.auctions.close(auction.auctionId);
      onAuctionUpdated();
      setSuccessBanner('Auction closed successfully. Transitioned to PAYMENT_PENDING.');
    } catch (err) {
      setErrorBanner(err.message || 'Failed to close auction');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
      <div className="bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 w-full max-w-4xl shadow-2xl relative my-8 flex flex-col max-h-[90vh]">
        
        {/* Top Header */}
        <div className="flex items-center justify-between p-6 border-b border-zinc-200 dark:border-zinc-800 shrink-0">
          <div className="flex items-center gap-3">
            <span className="text-xs font-mono uppercase tracking-widest text-zinc-400">
              Lot #{String(auction.auctionId).padStart(2, '0')} · {auction.category}
            </span>
            <span className="text-xs font-mono font-bold px-2 py-0.5 bg-zinc-100 dark:bg-zinc-900 text-zinc-900 dark:text-zinc-100 uppercase">
              {auction.status}
            </span>
          </div>

          <button
            onClick={onClose}
            className="p-1 hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500 hover:text-zinc-900 dark:hover:text-white transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Scrollable Body */}
        <div className="p-6 overflow-y-auto flex-1 grid grid-cols-1 lg:grid-cols-12 gap-8">
          
          {/* Left Column: Image, Provenance & Description */}
          <div className="lg:col-span-7 space-y-6">
            <div className="aspect-[4/3] bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 overflow-hidden">
              <img
                src={auction.imageUrl || 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?q=80&w=1200&auto=format&fit=crop'}
                alt={auction.itemName}
                referrerPolicy="no-referrer"
                className="w-full h-full object-cover"
              />
            </div>

            <div>
              <h2 className="text-2xl font-black text-zinc-900 dark:text-white tracking-tight">
                {auction.itemName}
              </h2>
              <p className="mt-3 text-sm text-zinc-600 dark:text-zinc-400 leading-relaxed">
                {auction.description}
              </p>
            </div>

            {/* Provenance & Condition Report */}
            <div className="border-t border-zinc-200 dark:border-zinc-800 pt-6 space-y-4 text-xs">
              <div>
                <span className="font-mono uppercase tracking-wider text-zinc-400 block mb-1">Condition Report</span>
                <p className="text-zinc-700 dark:text-zinc-300 leading-relaxed font-sans">
                  {auction.conditionReport || 'Exceptional preservation. Complete mechanical and archival documentation intact.'}
                </p>
              </div>

              <div>
                <span className="font-mono uppercase tracking-wider text-zinc-400 block mb-1">Provenance &amp; History</span>
                <p className="text-zinc-700 dark:text-zinc-300 leading-relaxed font-sans">
                  {auction.provenance || 'Private European collection (acquired 1994). Sold with original certificate of authenticity.'}
                </p>
              </div>
            </div>
          </div>

          {/* Right Column: Live Bidding Console */}
          <div className="lg:col-span-5 flex flex-col justify-between space-y-6 border-l border-zinc-100 dark:border-zinc-800 lg:pl-8">
            <div>
              {/* Status & Highest Bid Box */}
              <div className="border-b border-zinc-200 dark:border-zinc-800 pb-5 mb-5">
                <span className="text-[11px] uppercase tracking-widest text-zinc-400 font-mono block mb-1">
                  Current Highest Offer
                </span>
                <div className="text-3xl font-black text-zinc-950 dark:text-white tabular-nums tracking-tight">
                  ₹ {auction.currentHighestBid.toLocaleString('en-IN')}
                </div>
                <div className="flex items-center gap-2 text-xs font-mono text-zinc-500 mt-1.5">
                  <span>Starting: ₹{auction.startingPrice.toLocaleString('en-IN')}</span>
                  <span>·</span>
                  <span>Reserve: ₹{auction.reservePrice.toLocaleString('en-IN')}</span>
                </div>
              </div>

              {/* Banners */}
              {errorBanner && (
                <div className="p-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-300 dark:border-rose-800 text-xs text-rose-800 dark:text-rose-200 mb-4">
                  <div className="flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
                    <div>
                      <p className="font-semibold">{errorBanner}</p>
                      {conflictNewMinBid && (
                        <button
                          onClick={() => handlePlaceBid(conflictNewMinBid)}
                          className="mt-2 px-3 py-1 bg-rose-900 text-white font-mono text-[11px] font-bold hover:bg-rose-800 transition"
                        >
                          Submit Recommended Bid (₹{conflictNewMinBid.toLocaleString('en-IN')})
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              )}

              {successBanner && (
                <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-300 dark:border-emerald-800 text-xs text-emerald-800 dark:text-emerald-200 mb-4 flex items-center gap-2">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>{successBanner}</span>
                </div>
              )}

              {/* Bidding Form or Settlement Callout */}
              {auction.status === 'ACTIVE' ? (
                <div className="space-y-4">
                  <div>
                    <label className="text-[11px] uppercase tracking-wider font-mono text-zinc-500 block mb-2">
                      Place Offer (Min: ₹{minNextBid.toLocaleString('en-IN')})
                    </label>

                    <div className="flex gap-2 mb-3">
                      <input
                        type="number"
                        min={minNextBid}
                        step="1000"
                        placeholder={`₹ ${minNextBid}`}
                        value={customBidAmount}
                        onChange={(e) => setCustomBidAmount(e.target.value)}
                        className="flex-1 px-3 py-2 text-sm font-mono border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900"
                      />
                      <button
                        disabled={isSubmitting || !customBidAmount || Number(customBidAmount) <= auction.currentHighestBid}
                        onClick={() => handlePlaceBid(Number(customBidAmount))}
                        className="px-5 py-2 bg-zinc-950 text-white dark:bg-white dark:text-zinc-950 font-bold text-xs uppercase tracking-wider hover:opacity-90 transition disabled:opacity-50"
                      >
                        Submit
                      </button>
                    </div>

                    {/* Quick increment buttons */}
                    <div className="grid grid-cols-3 gap-2 text-xs font-mono">
                      <button
                        onClick={() => handlePlaceBid(auction.currentHighestBid + 2000)}
                        className="p-2 border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-900 font-bold"
                      >
                        +₹2,000
                      </button>
                      <button
                        onClick={() => handlePlaceBid(auction.currentHighestBid + 5000)}
                        className="p-2 border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-900 font-bold"
                      >
                        +₹5,000
                      </button>
                      <button
                        onClick={() => handlePlaceBid(auction.currentHighestBid + 10000)}
                        className="p-2 border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-900 font-bold"
                      >
                        +₹10,000
                      </button>
                    </div>
                  </div>

                  {currentUser?.role === 'ROLE_SELLER' && (
                    <div className="pt-4 border-t border-zinc-200 dark:border-zinc-800">
                      <button
                        onClick={handleCloseAuction}
                        className="w-full py-2.5 border border-zinc-900 dark:border-zinc-100 text-zinc-900 dark:text-white font-bold text-xs uppercase tracking-wider hover:bg-zinc-900 hover:text-white dark:hover:bg-white dark:hover:text-zinc-900 transition"
                      >
                        Close Auction &amp; Trigger Clearance
                      </button>
                    </div>
                  )}
                </div>
              ) : auction.status === 'PAYMENT_PENDING' ? (
                <div className="p-4 bg-amber-50 dark:bg-amber-950/30 border border-amber-300 dark:border-amber-800 space-y-3">
                  <span className="text-xs uppercase font-bold text-amber-800 dark:text-amber-300 block font-mono">
                    Auction Concluded: Clearance Required
                  </span>
                  <p className="text-xs text-amber-900 dark:text-amber-200 leading-relaxed font-sans">
                    The highest offer of ₹{auction.currentHighestBid.toLocaleString('en-IN')} has won the lot.
                    The winner has 15 minutes to complete UPI clearance.
                  </p>
                  <button
                    onClick={() => onOpenPaymentDrawer(auction.auctionId)}
                    className="w-full py-2.5 bg-amber-600 text-white font-bold text-xs uppercase tracking-wider hover:bg-amber-500 transition shadow-sm"
                  >
                    Open Mock UPI Settlement Drawer
                  </button>
                </div>
              ) : (
                <div className="p-4 bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 text-xs font-mono text-zinc-500">
                  Auction Status: {auction.status}
                </div>
              )}

              {/* Real-Time Bid History Feed */}
              <div className="pt-6 border-t border-zinc-200 dark:border-zinc-800 mt-6">
                <span className="text-[11px] font-mono uppercase tracking-widest text-zinc-400 block mb-3 flex items-center gap-1.5">
                  <History className="w-3.5 h-3.5" />
                  <span>Real-Time Audit Trail ({bids.length} bids)</span>
                </span>

                <div className="max-h-44 overflow-y-auto space-y-1.5 text-xs font-mono">
                  {bids.length === 0 ? (
                    <p className="text-zinc-400 text-[11px]">No bids recorded yet. Place the first offer!</p>
                  ) : (
                    bids.map((b) => (
                      <div
                        key={b.bidId}
                        className="flex items-center justify-between p-2 bg-zinc-50 dark:bg-zinc-900/60 border border-zinc-200/60 dark:border-zinc-800/60"
                      >
                        <div className="flex items-center gap-2">
                          <span className={`w-1.5 h-1.5 rounded-full ${b.status === 'ACCEPTED' ? 'bg-emerald-500' : 'bg-zinc-400'}`} />
                          <span className="text-zinc-700 dark:text-zinc-300">Bidder #{b.userId}</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="font-bold text-zinc-900 dark:text-white">₹{b.bidAmount.toLocaleString('en-IN')}</span>
                          <span className="text-[10px] text-zinc-400">{b.bidTime?.substring(11, 19)}</span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
          </div>

        </div>

      </div>
    </div>
  );
};
