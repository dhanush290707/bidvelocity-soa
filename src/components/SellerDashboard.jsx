import React, { useState } from 'react';
import { api } from '../api/client';
import { PlusCircle, CheckCircle2, AlertTriangle, Eye, RefreshCw, Layers } from 'lucide-react';

export const SellerDashboard = ({
  user,
  auctions,
  onOpenConsign,
  onOpenLot,
  onRefreshAuctions,
  onSwitchToBuyerView
}) => {
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [feedbackMsg, setFeedbackMsg] = useState(null);

  // Compute Seller KPIs
  const myAuctions = auctions;
  const activeCount = myAuctions.filter((a) => a.status === 'ACTIVE').length;
  const pendingCount = myAuctions.filter((a) => a.status === 'PAYMENT_PENDING').length;
  const clearedCount = myAuctions.filter((a) => a.status === 'CLEARED').length;
  const totalRealizedSales = myAuctions
    .filter((a) => a.status === 'CLEARED')
    .reduce((sum, a) => sum + Number(a.currentHighestBid || 0), 0);

  const handleCloseAuction = async (auctionId) => {
    if (!window.confirm(`Close auction #${auctionId} and finalize the highest bidder as winner?`)) return;

    setActionLoadingId(auctionId);
    setFeedbackMsg(null);
    try {
      await api.auctions.close(auctionId);
      setFeedbackMsg({
        type: 'success',
        text: `Auction #${auctionId} closed. Shifted to PAYMENT_PENDING with 15-min UPI settlement order.`
      });
      onRefreshAuctions();
    } catch (err) {
      setFeedbackMsg({
        type: 'error',
        text: err.message || `Failed to close auction #${auctionId}`
      });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleSecondChance = async (auctionId) => {
    if (!window.confirm(`Offer lot #${auctionId} to the runner-up bidder for payment round 2?`)) return;

    setActionLoadingId(auctionId);
    setFeedbackMsg(null);
    try {
      await api.auctions.secondChance(auctionId);
      setFeedbackMsg({
        type: 'success',
        text: `Second chance offered to runner-up bidder for auction #${auctionId}. New payment order created.`
      });
      onRefreshAuctions();
    } catch (err) {
      setFeedbackMsg({
        type: 'error',
        text: err.message || `Failed to offer second chance for auction #${auctionId}`
      });
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div className="py-12 px-8 lg:px-12 w-full space-y-10">
      
      {/* Console Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 border-b border-zinc-200 dark:border-zinc-800 pb-6">
        <div>
          <div className="flex items-center gap-2 text-xs font-mono uppercase tracking-widest text-zinc-400 mb-1">
            <span>Accredited Consignor Portal</span>
            <span>·</span>
            <span>User #{user.id} ({user.username})</span>
          </div>
          <h2 className="text-3xl font-black text-zinc-950 dark:text-white tracking-tight">
            Consignment &amp; Auction Studio
          </h2>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <button
            onClick={onSwitchToBuyerView}
            className="px-4 py-2 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-900 text-xs font-mono font-semibold uppercase tracking-wider text-zinc-800 dark:text-zinc-200 transition flex items-center gap-1.5"
          >
            <Eye className="w-3.5 h-3.5" />
            <span>Preview Floor as Buyer</span>
          </button>

          <button
            onClick={onOpenConsign}
            className="px-5 py-2 bg-zinc-950 text-white dark:bg-white dark:text-zinc-950 text-xs font-mono font-bold uppercase tracking-wider hover:opacity-90 transition flex items-center gap-1.5 shadow"
          >
            <PlusCircle className="w-3.5 h-3.5" />
            <span>Consign New Lot</span>
          </button>
        </div>
      </div>

      {/* Feedback Banner */}
      {feedbackMsg && (
        <div
          className={`p-4 border text-xs font-mono flex items-center justify-between ${
            feedbackMsg.type === 'success'
              ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-300 dark:border-emerald-800 text-emerald-900 dark:text-emerald-200'
              : 'bg-rose-50 dark:bg-rose-950/40 border-rose-300 dark:border-rose-800 text-rose-900 dark:text-rose-200'
          }`}
        >
          <div className="flex items-center gap-2">
            {feedbackMsg.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
            ) : (
              <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0" />
            )}
            <span>{feedbackMsg.text}</span>
          </div>
          <button onClick={() => setFeedbackMsg(null)} className="underline text-[10px]">Dismiss</button>
        </div>
      )}

      {/* KPI Cards Strip */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 font-mono text-xs">
        <div className="p-5 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shadow-sm">
          <span className="text-zinc-400 block text-[10px] uppercase mb-1">Realized Gross Sales</span>
          <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400 tabular-nums">
            ₹ {totalRealizedSales.toLocaleString('en-IN')}
          </span>
          <span className="text-[10px] text-zinc-500 block mt-1">Cleared via UPI Webhook</span>
        </div>

        <div className="p-5 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shadow-sm">
          <span className="text-zinc-400 block text-[10px] uppercase mb-1">Active Floor Lots</span>
          <span className="text-2xl font-black text-zinc-900 dark:text-white tabular-nums">
            {activeCount}
          </span>
          <span className="text-[10px] text-zinc-500 block mt-1">Accepting concurrent bids</span>
        </div>

        <div className="p-5 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shadow-sm">
          <span className="text-zinc-400 block text-[10px] uppercase mb-1">Pending Settlements</span>
          <span className="text-2xl font-black text-amber-600 dark:text-amber-400 tabular-nums">
            {pendingCount}
          </span>
          <span className="text-[10px] text-zinc-500 block mt-1">15-minute checkout window</span>
        </div>

        <div className="p-5 border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shadow-sm">
          <span className="text-zinc-400 block text-[10px] uppercase mb-1">Total Consigned</span>
          <span className="text-2xl font-black text-zinc-900 dark:text-white tabular-nums">
            {myAuctions.length}
          </span>
          <span className="text-[10px] text-zinc-500 block mt-1">Archived in auction DB</span>
        </div>
      </div>

      {/* Consignment Inventory Table */}
      <div className="border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 shadow-sm overflow-hidden">
        <div className="p-4 border-b border-zinc-200 dark:border-zinc-800 flex items-center justify-between bg-zinc-50/50 dark:bg-zinc-900/50">
          <div className="flex items-center gap-2">
            <Layers className="w-4 h-4 text-zinc-500" />
            <h3 className="text-xs uppercase font-mono font-bold tracking-wider text-zinc-900 dark:text-white">
              Inventory &amp; Lifecycle Controls
            </h3>
          </div>
          <button
            onClick={onRefreshAuctions}
            className="p-1 text-zinc-400 hover:text-zinc-900 dark:hover:text-white"
            title="Refresh List"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs font-mono">
            <thead className="bg-zinc-100/50 dark:bg-zinc-900/50 text-[10px] text-zinc-400 uppercase tracking-widest border-b border-zinc-200 dark:border-zinc-800">
              <tr>
                <th className="p-4">Lot ID</th>
                <th className="p-4">Item &amp; Category</th>
                <th className="p-4">Current High Offer</th>
                <th className="p-4">Reserve Status</th>
                <th className="p-4">Lifecycle State</th>
                <th className="p-4 text-right">Administrative Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800/80">
              {myAuctions.map((lot) => {
                const reserveMet = lot.currentHighestBid >= lot.reservePrice;
                const isLoadingAction = actionLoadingId === lot.auctionId;

                return (
                  <tr key={lot.auctionId} className="hover:bg-zinc-50/60 dark:hover:bg-zinc-900/30 transition-colors">
                    <td className="p-4 text-zinc-500 font-bold">
                      #{String(lot.auctionId).padStart(2, '0')}
                    </td>
                    <td className="p-4">
                      <div className="font-bold text-zinc-900 dark:text-white line-clamp-1 font-sans">
                        {lot.itemName}
                      </div>
                      <div className="text-[10px] text-zinc-400 mt-0.5">
                        {lot.category} · Version #{lot.version}
                      </div>
                    </td>
                    <td className="p-4 tabular-nums font-bold text-zinc-900 dark:text-white">
                      ₹ {lot.currentHighestBid.toLocaleString('en-IN')}
                    </td>
                    <td className="p-4">
                      {reserveMet ? (
                        <span className="text-emerald-600 dark:text-emerald-400 font-bold text-[10px]">
                          ✓ MET (₹{lot.reservePrice.toLocaleString('en-IN')})
                        </span>
                      ) : (
                        <span className="text-amber-600 dark:text-amber-400 text-[10px]">
                          UNDER (₹{lot.reservePrice.toLocaleString('en-IN')})
                        </span>
                      )}
                    </td>
                    <td className="p-4">
                      <span className={`px-2 py-0.5 text-[10px] font-bold ${
                        lot.status === 'ACTIVE'
                          ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300'
                          : lot.status === 'PAYMENT_PENDING'
                          ? 'bg-amber-50 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300'
                          : lot.status === 'CLEARED'
                          ? 'bg-zinc-900 text-white dark:bg-zinc-100 dark:text-zinc-900'
                          : 'bg-rose-50 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300'
                      }`}>
                        {lot.status}
                      </span>
                    </td>
                    <td className="p-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => onOpenLot(lot)}
                          className="px-2.5 py-1 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-800 text-[11px]"
                        >
                          View Rail
                        </button>

                        {lot.status === 'ACTIVE' && (
                          <button
                            disabled={isLoadingAction}
                            onClick={() => handleCloseAuction(lot.auctionId)}
                            className="px-3 py-1 bg-zinc-950 text-white dark:bg-white dark:text-zinc-950 font-bold hover:opacity-90 disabled:opacity-50 text-[11px]"
                          >
                            {isLoadingAction ? 'Closing...' : 'Close & Finalize Winner'}
                          </button>
                        )}

                        {lot.status === 'PAYMENT_FAILED' && !lot.secondChanceOffered && (
                          <button
                            disabled={isLoadingAction}
                            onClick={() => handleSecondChance(lot.auctionId)}
                            className="px-3 py-1 bg-amber-600 text-white font-bold hover:bg-amber-500 disabled:opacity-50 text-[11px]"
                          >
                            {isLoadingAction ? 'Processing...' : 'Offer Second-Chance'}
                          </button>
                        )}

                        {lot.status === 'PAYMENT_PENDING' && (
                          <span className="text-[10px] text-zinc-400 italic">
                            Awaiting Winner UPI
                          </span>
                        )}

                        {lot.status === 'CLEARED' && (
                          <span className="text-[10px] text-emerald-600 dark:text-emerald-400 font-bold">
                            Settled ✓
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
};
