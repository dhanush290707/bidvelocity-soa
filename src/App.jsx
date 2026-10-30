import React, { useState, useEffect, useCallback } from 'react';
import { api } from './api/client';
import { TopBar } from './components/TopBar';
import { LandingPage } from './components/LandingPage';
import { SellerDashboard } from './components/SellerDashboard';
import { LiveFloorHero } from './components/LiveFloorHero';
import { CatalogGrid } from './components/CatalogGrid';
import { LotDetailModal } from './components/LotDetailModal';
import { PaymentQrDrawer } from './components/PaymentQrDrawer';
import { ConsignLotModal } from './components/ConsignLotModal';
import { GatewayDiagnostics } from './components/GatewayDiagnostics';
import { AuthModal } from './components/AuthModal';
import { RefreshCw } from 'lucide-react';

export default function App() {
  const [auctions, setAuctions] = useState([]);
  const [activeNav, setActiveNav] = useState('curated');
  const [selectedLot, setSelectedLot] = useState(null);
  const [activePaymentId, setActivePaymentId] = useState(null);
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [isConsignOpen, setIsConsignOpen] = useState(false);
  const [isDiagnosticsOpen, setIsDiagnosticsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isBidding, setIsBidding] = useState(false);
  const [user, setUser] = useState(null);

  // For sellers who want to preview the public/buyer floor
  const [sellerViewMode, setSellerViewMode] = useState('console');

  // Gateway & Microservice Health
  const [gatewayHealth, setGatewayHealth] = useState({
    isGatewayOnline: false,
    services: { auth: false, auction: false, bidding: false, payment: false },
    lastChecked: 'Checking...'
  });

  // Check auth session from localStorage on startup
  useEffect(() => {
    const savedUser = localStorage.getItem('bv_user');
    if (savedUser) {
      try {
        setUser(JSON.parse(savedUser));
      } catch {
        localStorage.removeItem('bv_user');
      }
    }

    const handleAuthExpired = () => {
      setUser(null);
      setIsAuthOpen(true);
    };

    window.addEventListener('auth_expired', handleAuthExpired);
    return () => window.removeEventListener('auth_expired', handleAuthExpired);
  }, []);

  // Prober for Spring Cloud Gateway and individual microservices
  const checkHealth = useCallback(async () => {
    const isGw = await api.diagnostics.checkGatewayHealth();
    const [authOk, auctionOk, biddingOk, paymentOk] = await Promise.all([
      api.diagnostics.checkService('/api/auth/validate'),
      api.diagnostics.checkService('/api/auctions'),
      api.diagnostics.checkService('/api/bids/my-bids'),
      api.diagnostics.checkService('/api/payments/my-payments')
    ]);

    setGatewayHealth({
      isGatewayOnline: isGw || auctionOk,
      services: {
        auth: authOk,
        auction: auctionOk,
        bidding: biddingOk,
        payment: paymentOk
      },
      lastChecked: new Date().toLocaleTimeString()
    });
  }, []);

  // Fetch all auctions from Spring Cloud Gateway
  const fetchAuctions = useCallback(async () => {
    try {
      const data = await api.auctions.list();
      setAuctions(data);
      setSelectedLot((current) => {
        if (!current) return null;
        const updated = data.find((a) => a.auctionId === current.auctionId);
        return updated || current;
      });
    } catch (err) {
      // Backend not running yet or connection failed
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Periodic REST polling every 4 seconds with clean unmount handling
  useEffect(() => {
    let isMounted = true;

    const runPoll = async () => {
      if (!isMounted) return;
      await Promise.all([fetchAuctions(), checkHealth()]);
    };

    runPoll();
    const interval = setInterval(runPoll, 4000);

    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, [fetchAuctions, checkHealth]);

  // Quick bid from Hero Spotlight
  const handleQuickBid = async (auctionId, amount) => {
    if (!user) {
      setIsAuthOpen(true);
      return;
    }

    setIsBidding(true);
    const bidRequestId = 'bid-' + Date.now() + '-' + Math.floor(Math.random() * 10000);

    try {
      await api.bids.place({
        auctionId,
        bidAmount: amount,
        bidRequestId
      });
      await fetchAuctions();
    } catch (err) {
      alert(err.message || 'Bid rejected: please check your connection or minimum threshold.');
    } finally {
      setIsBidding(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('bv_token');
    localStorage.removeItem('bv_user');
    setUser(null);
    setSellerViewMode('console');
    setActiveNav('curated');
  };

  const premierLiveLot = auctions.find((a) => a.status === 'ACTIVE') || auctions[0] || null;

  return (
    <div className="min-h-screen bg-white dark:bg-zinc-950 text-zinc-900 dark:text-zinc-50 flex flex-col font-sans transition-colors selection:bg-zinc-900 selection:text-white dark:selection:bg-white dark:selection:text-zinc-950">
      
      {/* Top Bar (1-Row, 3-Zone Contract) */}
      <TopBar
        user={user}
        activeNav={activeNav}
        onNavigate={setActiveNav}
        onOpenAuth={() => setIsAuthOpen(true)}
        onLogout={handleLogout}
        onOpenConsign={() => setIsConsignOpen(true)}
        onOpenDiagnostics={() => setIsDiagnosticsOpen(true)}
        gatewayHealth={gatewayHealth}
      />

      {/* Backend Connection Alert Banner */}
      {!gatewayHealth.isGatewayOnline && !isLoading && (
        <div className="bg-zinc-900 text-white border-b border-zinc-800 px-8 lg:px-12 py-3 text-xs font-mono">
          <div className="w-full flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-rose-500 animate-pulse shrink-0"></span>
              <span className="font-semibold text-rose-300">Gateway Offline:</span>
              <span className="text-zinc-300 font-sans">
                Connecting strictly to Spring Cloud Gateway on <code>http://localhost:8080</code>.
              </span>
            </div>
            <div className="flex items-center gap-3 shrink-0">
              <button
                onClick={() => setIsDiagnosticsOpen(true)}
                className="underline hover:text-zinc-100 transition font-sans"
              >
                View STS &amp; Docker Startup Commands
              </button>
              <button
                onClick={fetchAuctions}
                className="px-2.5 py-1 bg-white text-zinc-950 font-bold rounded-none hover:opacity-90 flex items-center gap-1.5 transition"
              >
                <RefreshCw className="w-3 h-3" />
                <span>Retry</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Main Content Area based on User Session & Role */}
      <main className="flex-1">
        {!user ? (
          /* 1. PUBLIC VISITOR LANDING PAGE */
          <LandingPage
            auctions={auctions}
            onOpenLot={setSelectedLot}
            onOpenAuth={() => setIsAuthOpen(true)}
            isLoading={isLoading}
          />
        ) : user.role === 'ROLE_SELLER' && sellerViewMode === 'console' ? (
          /* 2. DEDICATED SELLER CONSIGNMENT & REVENUE CONSOLE */
          <SellerDashboard
            user={user}
            auctions={auctions}
            onOpenConsign={() => setIsConsignOpen(true)}
            onOpenLot={setSelectedLot}
            onRefreshAuctions={fetchAuctions}
            onSwitchToBuyerView={() => setSellerViewMode('floor')}
          />
        ) : (
          /* 3. DEDICATED BUYER BIDDING PORTAL (or Seller Preview Floor) */
          <div>
            {user.role === 'ROLE_SELLER' && (
              <div className="bg-zinc-100 dark:bg-zinc-900 border-b border-zinc-200 dark:border-zinc-800 px-6 py-2.5 text-xs font-mono flex items-center justify-between">
                <span className="text-zinc-500">Previewing marketplace floor as buyer</span>
                <button
                  onClick={() => setSellerViewMode('console')}
                  className="font-bold underline text-zinc-900 dark:text-white"
                >
                  Return to Consignment Studio ➔
                </button>
              </div>
            )}

            {activeNav === 'live' ? (
              <div>
                <LiveFloorHero
                  auction={premierLiveLot}
                  onOpenLot={setSelectedLot}
                  onPlaceQuickBid={handleQuickBid}
                  isBidding={isBidding}
                />
                <div className="border-t border-zinc-200 dark:border-zinc-800">
                  <CatalogGrid
                    auctions={auctions.filter((a) => a.status === 'ACTIVE')}
                    onOpenLot={setSelectedLot}
                    isLoading={isLoading}
                  />
                </div>
              </div>
            ) : activeNav === 'past' ? (
              <div>
                <div className="py-12 px-8 lg:px-12 border-b border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/30">
                  <div className="w-full">
                    <span className="text-xs uppercase font-mono tracking-widest text-zinc-400 block mb-1">
                      Historical Archives
                    </span>
                    <h2 className="text-3xl font-black tracking-tight text-zinc-900 dark:text-white">
                    Past Realizations &amp; Settled Lots
                    </h2>
                    <p className="text-sm text-zinc-500 font-sans mt-2 max-w-2xl">
                      Historical record of concluded lots cleared via verified mock UPI payment gateway webhooks.
                    </p>
                  </div>
                </div>
                <CatalogGrid
                  auctions={auctions.filter((a) => a.status === 'CLEARED' || a.status === 'PAYMENT_FAILED')}
                  onOpenLot={setSelectedLot}
                  isLoading={isLoading}
                />
              </div>
            ) : (
              <div>
                <LiveFloorHero
                  auction={premierLiveLot}
                  onOpenLot={setSelectedLot}
                  onPlaceQuickBid={handleQuickBid}
                  isBidding={isBidding}
                />
                <CatalogGrid
                  auctions={auctions}
                  onOpenLot={setSelectedLot}
                  isLoading={isLoading}
                />
              </div>
            )}
          </div>
        )}
      </main>

      {/* Swiss Minimalist Footer */}
      <footer className="border-t border-zinc-200 dark:border-zinc-800 py-10 px-8 lg:px-12 text-xs text-zinc-500 font-mono transition-colors w-full">
        <div className="w-full flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-2">
            <span className="font-bold text-zinc-900 dark:text-zinc-100">BIDVELOCITY MARKETPLACE</span>
            <span>·</span>
            <span>Real-Time Distributed Bidding Engine</span>
          </div>
          <div className="flex items-center gap-6">
            <span>Spring Boot 3.4.3</span>
            <span>Spring Cloud Gateway</span>
            <span>PostgreSQL</span>
            <span>Java 21 LTS</span>
          </div>
        </div>
      </footer>

      {/* Modals & Drawers */}
      {selectedLot && (
        <LotDetailModal
          auction={selectedLot}
          currentUser={user}
          onClose={() => setSelectedLot(null)}
          onAuctionUpdated={fetchAuctions}
          onOpenPaymentDrawer={(pId) => {
            setActivePaymentId(pId);
            setSelectedLot(null);
          }}
        />
      )}

      {activePaymentId && (
        <PaymentQrDrawer
          paymentId={activePaymentId}
          onClose={() => setActivePaymentId(null)}
          onPaymentCleared={fetchAuctions}
        />
      )}

      {isConsignOpen && (
        <ConsignLotModal
          onClose={() => setIsConsignOpen(false)}
          onLotCreated={fetchAuctions}
        />
      )}

      {isDiagnosticsOpen && (
        <GatewayDiagnostics
          gatewayHealth={gatewayHealth}
          onClose={() => setIsDiagnosticsOpen(false)}
          onRefreshHealth={checkHealth}
        />
      )}

      {isAuthOpen && (
        <AuthModal
          onClose={() => setIsAuthOpen(false)}
          onAuthSuccess={(u) => {
            setUser(u);
            if (u.role === 'ROLE_SELLER') {
              setSellerViewMode('console');
            }
          }}
        />
      )}

    </div>
  );
}
