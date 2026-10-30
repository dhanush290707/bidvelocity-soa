import React, { useState } from 'react';
import { ArrowUpRight, Search } from 'lucide-react';

export const CatalogGrid = ({
  auctions,
  onOpenLot,
  isLoading
}) => {
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const categories = ['ALL', 'Watches', 'Fine Art', 'Collectibles', 'Electronics'];

  const filteredAuctions = auctions.filter((lot) => {
    const matchesCat =
      selectedCategory === 'ALL' ||
      lot.category?.toLowerCase() === selectedCategory.toLowerCase();
    const matchesSearch =
      searchQuery === '' ||
      lot.itemName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      lot.description?.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCat && matchesSearch;
  });

  return (
    <section className="py-12 px-8 lg:px-12 w-full">
      {/* Category Filter & Search Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 pb-6 border-b border-zinc-200 dark:border-zinc-800 mb-10">
        <div>
          <h3 className="text-xl font-black text-zinc-900 dark:text-zinc-50 tracking-tight">
            Curated Lots &amp; Catalogue
          </h3>
          <p className="text-xs text-zinc-500 font-mono mt-1">
            Displaying {filteredAuctions.length} registered lots across luxury categories
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {/* Category Tabs (Segmented Clean Style, No Pills) */}
          <div className="flex items-center gap-1 border border-zinc-200 dark:border-zinc-800 p-1 bg-zinc-100/50 dark:bg-zinc-900/50">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-3 py-1.5 text-xs font-mono uppercase tracking-wider transition-colors ${
                  selectedCategory === cat
                    ? 'bg-zinc-900 text-white dark:bg-white dark:text-zinc-900 font-bold'
                    : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Search Box */}
          <div className="relative">
            <Search className="w-3.5 h-3.5 text-zinc-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search lot title..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-8 pr-3 py-1.5 text-xs border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 dark:focus:border-white font-mono w-44"
            />
          </div>
        </div>
      </div>

      {/* Grid of Product Lots */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
          {[1, 2, 3, 4].map((n) => (
            <div key={n} className="border border-zinc-200 dark:border-zinc-800 p-4 animate-pulse">
              <div className="aspect-[4/3] bg-zinc-200 dark:bg-zinc-800 mb-4"></div>
              <div className="h-4 bg-zinc-200 dark:bg-zinc-800 w-3/4 mb-2"></div>
              <div className="h-3 bg-zinc-200 dark:bg-zinc-800 w-1/2"></div>
            </div>
          ))}
        </div>
      ) : filteredAuctions.length === 0 ? (
        <div className="text-center py-20 border border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/20">
          <p className="text-sm font-mono text-zinc-500 uppercase tracking-widest">No lots found matching criteria</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
          {filteredAuctions.map((lot) => (
            <article
              key={lot.auctionId}
              onClick={() => onOpenLot(lot)}
              className="group border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 p-5 flex flex-col justify-between hover:border-zinc-900 dark:hover:border-zinc-500 transition-all cursor-pointer shadow-sm hover:shadow-md"
            >
              <div>
                {/* Product Image Frame */}
                <div className="relative aspect-[4/3] overflow-hidden bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800/80 mb-5">
                  <img
                    src={lot.imageUrl || 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?q=80&w=1200&auto=format&fit=crop'}
                    alt={lot.itemName}
                    referrerPolicy="no-referrer"
                    className="w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-500"
                  />
                  {lot.status !== 'ACTIVE' && (
                    <div className="absolute top-3 right-3 bg-zinc-950 text-white text-[10px] uppercase font-mono px-2 py-0.5 tracking-widest">
                      {lot.status}
                    </div>
                  )}
                </div>

                {/* Unboxed Metadata (Zero-Pill Rule) */}
                <div className="flex items-center gap-2 text-[11px] font-mono text-zinc-400 mb-1.5 uppercase tracking-wider">
                  <span>{lot.category || 'Collectibles'}</span>
                  <span aria-hidden="true">·</span>
                  <span>Lot #{String(lot.auctionId).padStart(2, '0')}</span>
                  <span aria-hidden="true">·</span>
                  <span className="text-emerald-600 dark:text-emerald-400">Reserve Met</span>
                </div>

                <h4 className="text-base font-bold text-zinc-900 dark:text-zinc-50 tracking-tight group-hover:text-zinc-600 dark:group-hover:text-zinc-300 transition-colors line-clamp-1">
                  {lot.itemName}
                </h4>

                <p className="mt-1 text-xs text-zinc-500 dark:text-zinc-400 line-clamp-2 leading-relaxed">
                  {lot.description}
                </p>
              </div>

              {/* Price & Action Footer */}
              <div className="pt-4 mt-6 border-t border-zinc-100 dark:border-zinc-800/80 flex items-center justify-between">
                <div>
                  <span className="text-[10px] uppercase tracking-wider text-zinc-400 font-mono block">
                    {lot.status === 'CLEARED' ? 'Realized Price' : 'Current Bid'}
                  </span>
                  <span className="text-lg font-black text-zinc-900 dark:text-zinc-50 tabular-nums">
                    ₹ {lot.currentHighestBid.toLocaleString('en-IN')}
                  </span>
                </div>

                <div className="w-8 h-8 rounded-none border border-zinc-300 dark:border-zinc-700 flex items-center justify-center text-zinc-800 dark:text-zinc-200 group-hover:bg-zinc-950 group-hover:text-white dark:group-hover:bg-white dark:group-hover:text-zinc-950 transition-colors">
                  <ArrowUpRight className="w-4 h-4" />
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
};
