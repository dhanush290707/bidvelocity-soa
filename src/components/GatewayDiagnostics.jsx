import React, { useState } from 'react';
import { X, RefreshCw, Server, Terminal, CheckCircle2, AlertTriangle } from 'lucide-react';

export const GatewayDiagnostics = ({
  onClose,
  gatewayHealth,
  onRefreshHealth
}) => {
  const [isChecking, setIsChecking] = useState(false);

  const handleRefresh = async () => {
    setIsChecking(true);
    await onRefreshHealth();
    setIsChecking(false);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
      <div className="bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 w-full max-w-xl shadow-2xl relative p-8">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-zinc-200 dark:border-zinc-800 pb-4 mb-6">
          <div className="flex items-center gap-2">
            <Server className="w-5 h-5 text-zinc-900 dark:text-white" />
            <h3 className="font-bold text-sm uppercase tracking-wider text-zinc-900 dark:text-white">
              Spring Cloud Gateway Diagnostics
            </h3>
          </div>
          <button onClick={onClose} className="p-1 hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="space-y-6 text-xs font-mono">
          
          {/* Gateway Overall Status Box */}
          <div className={`p-4 border ${
            gatewayHealth.isGatewayOnline
              ? 'bg-emerald-50 dark:bg-emerald-950/30 border-emerald-300 dark:border-emerald-800 text-emerald-900 dark:text-emerald-200'
              : 'bg-rose-50 dark:bg-rose-950/30 border-rose-300 dark:border-rose-800 text-rose-900 dark:text-rose-200'
          }`}>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                {gatewayHealth.isGatewayOnline ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                ) : (
                  <AlertTriangle className="w-4 h-4 text-rose-600 dark:text-rose-400" />
                )}
                <span className="font-bold">
                  {gatewayHealth.isGatewayOnline
                    ? 'Gateway Online (http://localhost:8080)'
                    : 'Gateway Unreachable on http://localhost:8080'}
                </span>
              </div>
              <button
                onClick={handleRefresh}
                disabled={isChecking}
                className="p-1 hover:opacity-75 transition"
                title="Refresh Health Probe"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${isChecking ? 'animate-spin' : ''}`} />
              </button>
            </div>
            <p className="mt-1 text-[11px] opacity-80 font-sans">
              Last checked: {gatewayHealth.lastChecked}
            </p>
          </div>

          {/* Microservice Probes */}
          <div>
            <span className="text-[11px] uppercase tracking-wider text-zinc-400 block mb-3">
              Downstream Microservice Health
            </span>
            <div className="grid grid-cols-2 gap-3">
              <div className="p-3 border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50 flex items-center justify-between">
                <div>
                  <span className="font-bold text-zinc-900 dark:text-white block">Auth Service</span>
                  <span className="text-[10px] text-zinc-400">lb://AUTH-SERVICE (:8081)</span>
                </div>
                <span className={`w-2 h-2 rounded-full ${gatewayHealth.services.auth ? 'bg-emerald-500' : 'bg-zinc-400'}`} />
              </div>

              <div className="p-3 border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50 flex items-center justify-between">
                <div>
                  <span className="font-bold text-zinc-900 dark:text-white block">Auction Service</span>
                  <span className="text-[10px] text-zinc-400">lb://AUCTION-SERVICE (:8082)</span>
                </div>
                <span className={`w-2 h-2 rounded-full ${gatewayHealth.services.auction ? 'bg-emerald-500' : 'bg-zinc-400'}`} />
              </div>

              <div className="p-3 border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50 flex items-center justify-between">
                <div>
                  <span className="font-bold text-zinc-900 dark:text-white block">Bidding Service</span>
                  <span className="text-[10px] text-zinc-400">lb://BIDDING-SERVICE (:8083)</span>
                </div>
                <span className={`w-2 h-2 rounded-full ${gatewayHealth.services.bidding ? 'bg-emerald-500' : 'bg-zinc-400'}`} />
              </div>

              <div className="p-3 border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50 flex items-center justify-between">
                <div>
                  <span className="font-bold text-zinc-900 dark:text-white block">Payment Service</span>
                  <span className="text-[10px] text-zinc-400">lb://PAYMENT-SERVICE (:8084)</span>
                </div>
                <span className={`w-2 h-2 rounded-full ${gatewayHealth.services.payment ? 'bg-emerald-500' : 'bg-zinc-400'}`} />
              </div>
            </div>
          </div>

          {/* Quick Start Guidance */}
          <div className="border-t border-zinc-200 dark:border-zinc-800 pt-5">
            <span className="text-[11px] uppercase tracking-wider text-zinc-400 block mb-2 flex items-center gap-1.5">
              <Terminal className="w-3.5 h-3.5" />
              <span>How to Start Backend on Your Laptop</span>
            </span>

            <div className="space-y-3 font-sans text-xs text-zinc-600 dark:text-zinc-400">
              <div className="p-3 bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800">
                <strong className="text-zinc-900 dark:text-white block mb-1">Option A: Spring Tool Suite (STS) / Eclipse</strong>
                <p className="text-[11px]">
                  Import <code>backend/pom.xml</code> into STS. Run in order:
                  <br />
                  1. EurekaServer (8761) ➔ 2. AuthService (8081) ➔ 3. AuctionService (8082) ➔ 4. BiddingService (8083) ➔ 5. PaymentService (8084) ➔ 6. ApiGateway (8080).
                </p>
              </div>

              <div className="p-3 bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 font-mono text-[11px]">
                <strong className="text-zinc-900 dark:text-white block mb-1 font-sans">Option B: Docker Compose</strong>
                <code>cd backend &amp;&amp; docker-compose up -d</code>
              </div>
            </div>
          </div>

        </div>

      </div>
    </div>
  );
};
