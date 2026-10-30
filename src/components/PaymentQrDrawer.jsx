import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { X, CheckCircle2, AlertTriangle, QrCode } from 'lucide-react';

export const PaymentQrDrawer = ({
  paymentId,
  onClose,
  onPaymentCleared
}) => {
  const [checkout, setCheckout] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [lastIdempotencyKey, setLastIdempotencyKey] = useState('');

  useEffect(() => {
    let isMounted = true;
    const fetchCheckout = async () => {
      setIsLoading(true);
      setErrorMsg(null);
      setSuccessMsg(null);
      setCheckout(null);
      setLastIdempotencyKey('');
      try {
        let data;
        try {
          data = await api.payments.getCheckoutByAuction(paymentId);
        } catch {
          data = await api.payments.getCheckout(paymentId);
        }
        if (isMounted) {
          setCheckout(data);
        }
      } catch (err) {
        if (isMounted) {
          setErrorMsg(err.message || 'Failed to retrieve checkout order from Payment Service');
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchCheckout();
    return () => {
      isMounted = false;
    };
  }, [paymentId]);

  const handleSimulateServerWebhook = async (action, isReplay = false) => {
    if (!checkout) return;

    setIsSubmitting(true);
    setErrorMsg(null);
    setSuccessMsg(null);

    const idempotencyKey = isReplay && lastIdempotencyKey
      ? lastIdempotencyKey
      : 'IDEMP-' + Date.now() + '-' + Math.floor(Math.random() * 10000);

    if (!isReplay) {
      setLastIdempotencyKey(idempotencyKey);
    }

    const transactionRef = 'UPI-UTR-' + Math.floor(100000000000 + Math.random() * 900000000000);

    try {
      const response = await api.payments.simulateSubmit({
        token: checkout.checkoutToken,
        action,
        transactionRef,
        idempotencyKey
      });

      if (action === 'SUCCESS') {
        setSuccessMsg(`Server-signed webhook cleared payment #${response.paymentId} (Sync: ${response.syncStatus})`);
        onPaymentCleared();
      } else {
        setErrorMsg(`Payment declined via simulated failure callback.`);
      }

      // Refresh checkout
      const refreshed = await api.payments.getCheckout(paymentId);
      setCheckout(refreshed);
    } catch (err) {
      setErrorMsg(err.message || 'Webhook simulation trigger failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
      <div className="bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 w-full max-w-lg shadow-2xl relative p-8">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-zinc-200 dark:border-zinc-800 pb-4 mb-6">
          <div className="flex items-center gap-2">
            <QrCode className="w-5 h-5 text-zinc-900 dark:text-zinc-100" />
            <h3 className="font-bold text-sm uppercase tracking-wider text-zinc-900 dark:text-zinc-100">
              UPI Winner Settlement Drawer
            </h3>
          </div>
          <button onClick={onClose} className="p-1 hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500">
            <X className="w-5 h-5" />
          </button>
        </div>

        {isLoading ? (
          <div className="py-16 text-center font-mono text-xs text-zinc-400 animate-pulse">
            Retrieving payment order &amp; generating QR from Payment Service...
          </div>
        ) : errorMsg && !checkout ? (
          <div className="p-4 bg-rose-50 dark:bg-rose-950/40 border border-rose-300 dark:border-rose-800 text-xs text-rose-800 dark:text-rose-200">
            {errorMsg}
          </div>
        ) : checkout ? (
          <div className="space-y-6">
            
            {/* Amount Summary */}
            <div className="bg-zinc-50 dark:bg-zinc-900/60 p-4 border border-zinc-200 dark:border-zinc-800 flex justify-between items-center font-mono text-xs">
              <div>
                <span className="text-zinc-400 block text-[10px] uppercase">Settlement Order</span>
                <span className="font-bold text-zinc-900 dark:text-zinc-100">Payment #{checkout.paymentId}</span>
              </div>
              <div className="text-right">
                <span className="text-zinc-400 block text-[10px] uppercase">Winning Amount</span>
                <span className="text-2xl font-black text-emerald-600 dark:text-emerald-400 tabular-nums">
                  ₹ {checkout.amount.toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            {/* Visual SVG QR Code returned from backend Payment Service */}
            <div className="text-center p-4 bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800">
              <div
                className="w-48 h-48 mx-auto flex items-center justify-center"
                dangerouslySetInnerHTML={{ __html: checkout.qrCodeSvg }}
              />
              <p className="mt-3 text-xs font-mono text-zinc-600 dark:text-zinc-400">
                Scan with any UPI App or open URL below
              </p>
              <div className="mt-1 text-[10px] font-mono text-zinc-400 truncate max-w-sm mx-auto">
                {checkout.checkoutUrl}
              </div>
            </div>

            {/* Simulated UPI App Options (Explicitly Labeled) */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] uppercase tracking-wider font-mono text-zinc-500">
                  Simulated UPI Apps (Demo Options)
                </span>
                <span className="text-[10px] font-mono text-amber-600 dark:text-amber-400 uppercase font-semibold">
                  Sandbox Environment
                </span>
              </div>
              <div className="grid grid-cols-4 gap-2 text-center text-xs font-mono">
                <div className="p-2 border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50">
                  <div className="font-bold text-emerald-600">GPay</div>
                  <span className="text-[9px] text-zinc-400">Simulated</span>
                </div>
                <div className="p-2 border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50">
                  <div className="font-bold text-purple-600">PhonePe</div>
                  <span className="text-[9px] text-zinc-400">Simulated</span>
                </div>
                <div className="p-2 border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50">
                  <div className="font-bold text-sky-500">Paytm</div>
                  <span className="text-[9px] text-zinc-400">Simulated</span>
                </div>
                <div className="p-2 border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50">
                  <div className="font-bold text-orange-500">BHIM</div>
                  <span className="text-[9px] text-zinc-400">Simulated</span>
                </div>
              </div>
            </div>

            {/* Messages */}
            {successMsg && (
              <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-300 dark:border-emerald-800 text-xs text-emerald-800 dark:text-emerald-200 flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                <span>{successMsg}</span>
              </div>
            )}
            {errorMsg && (
              <div className="p-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-300 dark:border-rose-800 text-xs text-rose-800 dark:text-rose-200 flex items-center gap-2">
                <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0" />
                <span>{errorMsg}</span>
              </div>
            )}

            {/* Server-Signed Simulation Controls */}
            <div className="space-y-2 pt-2 border-t border-zinc-200 dark:border-zinc-800">
              <button
                disabled={isSubmitting || checkout.status === 'COMPLETED'}
                onClick={() => handleSimulateServerWebhook('SUCCESS')}
                className="w-full py-3 bg-zinc-950 text-white dark:bg-white dark:text-zinc-950 font-bold text-xs uppercase tracking-widest hover:opacity-90 transition disabled:opacity-50"
              >
                {checkout.status === 'COMPLETED' ? '✓ Payment Verified & Auction Cleared' : 'Simulate Verified Payment (Server Signed)'}
              </button>

              <div className="grid grid-cols-2 gap-2 text-xs font-mono">
                <button
                  disabled={isSubmitting || checkout.status === 'COMPLETED'}
                  onClick={() => handleSimulateServerWebhook('FAILED')}
                  className="py-2 border border-rose-300 dark:border-rose-800 text-rose-700 dark:text-rose-300 hover:bg-rose-50 dark:hover:bg-rose-950/40 font-semibold"
                >
                  Simulate Decline
                </button>
                <button
                  disabled={isSubmitting}
                  onClick={() => handleSimulateServerWebhook('SUCCESS', true)}
                  className="py-2 border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-600 dark:text-zinc-400 font-semibold"
                >
                  Test Idempotency Replay
                </button>
              </div>
            </div>

          </div>
        ) : null}

      </div>
    </div>
  );
};
