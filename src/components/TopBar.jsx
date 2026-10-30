import React from 'react';
import { Activity, PlusCircle } from 'lucide-react';

export const TopBar = ({
  user,
  activeNav,
  onNavigate,
  onOpenAuth,
  onLogout,
  onOpenConsign,
  onOpenDiagnostics,
  gatewayHealth
}) => {
  return (
    <header className="border-b border-zinc-200 dark:border-zinc-800 bg-white/95 dark:bg-zinc-950/95 backdrop-blur-md sticky top-0 z-40 transition-colors w-full">
      <div className="w-full px-8 lg:px-12 h-18 flex items-center justify-between gap-8">
        
        {/* Zone 1: Single Wordmark */}
        <div className="flex items-center gap-3 shrink-0">
          <button
            onClick={() => onNavigate('curated')}
            className="text-xl font-black tracking-tight text-zinc-900 dark:text-zinc-50 hover:opacity-80 transition font-sans whitespace-nowrap"
          >
            BIDVELOCITY
          </button>
        </div>

        {/* Zone 2: Navigation Links */}
        <nav className="hidden md:flex items-center gap-8 text-xs uppercase tracking-widest font-medium text-zinc-500 dark:text-zinc-400">
          <button
            onClick={() => onNavigate('curated')}
            className={`transition-colors py-1 relative whitespace-nowrap shrink-0 ${
              activeNav === 'curated'
                ? 'text-zinc-950 dark:text-white font-bold after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-zinc-900 dark:after:bg-white'
                : 'hover:text-zinc-950 dark:hover:text-white'
            }`}
          >
            Curated Lots
          </button>
          <button
            onClick={() => onNavigate('live')}
            className={`transition-colors py-1 relative whitespace-nowrap shrink-0 ${
              activeNav === 'live'
                ? 'text-zinc-950 dark:text-white font-bold after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-zinc-900 dark:after:bg-white'
                : 'hover:text-zinc-950 dark:hover:text-white'
            }`}
          >
            Live Floor
          </button>
          <button
            onClick={() => onNavigate('past')}
            className={`transition-colors py-1 relative whitespace-nowrap shrink-0 ${
              activeNav === 'past'
                ? 'text-zinc-950 dark:text-white font-bold after:absolute after:bottom-0 after:left-0 after:right-0 after:h-0.5 after:bg-zinc-900 dark:after:bg-white'
                : 'hover:text-zinc-950 dark:hover:text-white'
            }`}
          >
            Past Realizations
          </button>
          {user?.role === 'ROLE_SELLER' && (
            <button
              onClick={onOpenConsign}
              className="text-zinc-950 dark:text-white font-semibold hover:opacity-80 transition flex items-center gap-1.5 whitespace-nowrap shrink-0"
            >
              <PlusCircle className="w-3.5 h-3.5" />
              <span>Consign Lot</span>
            </button>
          )}
        </nav>

        {/* Zone 3: Gateway Health Indicator & Identity Action */}
        <div className="flex items-center gap-4 shrink-0">
          <button
            onClick={onOpenDiagnostics}
            title="Inspect Spring Cloud Gateway & Microservices Status"
            className="flex items-center gap-2 text-xs font-mono px-2.5 py-1.5 rounded border border-zinc-200 dark:border-zinc-800 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition text-zinc-600 dark:text-zinc-400"
          >
            <Activity className="w-3.5 h-3.5 text-zinc-500" />
            <span className="hidden sm:inline">Gateway :8080</span>
            <span
              className={`w-2 h-2 rounded-full ${
                gatewayHealth?.isGatewayOnline ? 'bg-emerald-500' : 'bg-rose-500 animate-pulse'
              }`}
            />
          </button>

          {user ? (
            <div className="flex items-center gap-3">
              <div className="text-right hidden sm:block">
                <div className="text-xs font-bold text-zinc-900 dark:text-zinc-100 leading-none">
                  {user.username}
                </div>
                <div className="text-[10px] text-zinc-400 font-mono tracking-wider uppercase mt-0.5">
                  {user.role === 'ROLE_SELLER' ? 'Seller Account' : 'Verified Bidder'}
                </div>
              </div>
              <button
                onClick={onLogout}
                className="text-xs font-medium px-3 py-1.5 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-900 hover:text-white dark:hover:bg-white dark:hover:text-zinc-900 rounded transition"
              >
                Sign Out
              </button>
            </div>
          ) : (
            <button
              onClick={onOpenAuth}
              className="text-xs uppercase tracking-wider font-semibold px-4 py-2 bg-zinc-900 text-white dark:bg-white dark:text-zinc-900 rounded hover:opacity-90 transition shadow-sm"
            >
              Sign In / Register
            </button>
          )}
        </div>

      </div>
    </header>
  );
};
