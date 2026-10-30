import React, { useState } from 'react';
import { api } from '../api/client';
import { X, Upload, AlertCircle } from 'lucide-react';

export const ConsignLotModal = ({
  onClose,
  onLotCreated
}) => {
  const [itemName, setItemName] = useState('');
  const [category, setCategory] = useState('Watches');
  const [description, setDescription] = useState('');
  const [conditionReport, setConditionReport] = useState('');
  const [provenance, setProvenance] = useState('');
  const [startingPrice, setStartingPrice] = useState('');
  const [reservePrice, setReservePrice] = useState('');
  const [durationHours, setDurationHours] = useState(24);
  const [imageUrl, setImageUrl] = useState('https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?q=80&w=1200&auto=format&fit=crop');
  const [isUploading, setIsUploading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);

  const handleFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    setErrorMsg(null);
    try {
      const res = await api.auctions.uploadImage(file);
      setImageUrl(res.imageUrl);
    } catch (err) {
      setErrorMsg(err.message || 'Image upload failed. Ensure file is JPEG/PNG/WebP under 5MB.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg(null);
    setIsSubmitting(true);

    try {
      const start = new Date();
      const end = new Date(start.getTime() + durationHours * 60 * 60 * 1000);

      await api.auctions.create({
        itemName,
        category,
        description,
        conditionReport,
        provenance,
        imageUrl,
        startingPrice: Number(startingPrice),
        reservePrice: Number(reservePrice),
        endTime: end.toISOString()
      });

      onLotCreated();
      onClose();
    } catch (err) {
      setErrorMsg(err.message || 'Failed to create auction listing');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
      <div className="bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 w-full max-w-2xl shadow-2xl relative p-8 my-8">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-zinc-200 dark:border-zinc-800 pb-4 mb-6">
          <div>
            <h3 className="font-black text-lg text-zinc-900 dark:text-white tracking-tight">
              Consign &amp; List a Curated Lot
            </h3>
            <p className="text-xs text-zinc-400 font-mono mt-0.5">
              Auction Service listing management with atomic @Version state initialization
            </p>
          </div>
          <button onClick={onClose} className="p-1 hover:bg-zinc-100 dark:hover:bg-zinc-900 text-zinc-500">
            <X className="w-5 h-5" />
          </button>
        </div>

        {errorMsg && (
          <div className="p-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-300 dark:border-rose-800 text-xs text-rose-800 dark:text-rose-200 mb-6 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5 text-xs font-mono">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Item Title</label>
              <input
                required
                type="text"
                value={itemName}
                onChange={(e) => setItemName(e.target.value)}
                placeholder="e.g. 1972 Patek Philippe Golden Ellipse"
                className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-sans"
              />
            </div>

            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Category</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-sans"
              >
                <option value="Watches">Watches</option>
                <option value="Fine Art">Fine Art</option>
                <option value="Collectibles">Collectibles</option>
                <option value="Electronics">Electronics</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-zinc-500 uppercase tracking-wider mb-1">Description &amp; Specifications</label>
            <textarea
              required
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detailed description of craftsmanship, dimensions, and movements..."
              className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-sans"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Condition Report</label>
              <input
                type="text"
                value={conditionReport}
                onChange={(e) => setConditionReport(e.target.value)}
                placeholder="e.g. Grade A. Unpolished case, original dial."
                className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-sans"
              />
            </div>

            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Provenance</label>
              <input
                type="text"
                value={provenance}
                onChange={(e) => setProvenance(e.target.value)}
                placeholder="e.g. Acquired from original owner in Zurich, 1980."
                className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-sans"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Starting Price (₹)</label>
              <input
                required
                type="number"
                min="1000"
                step="1000"
                value={startingPrice}
                onChange={(e) => setStartingPrice(e.target.value)}
                placeholder="50000"
                className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-mono"
              />
            </div>

            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Reserve Price (₹)</label>
              <input
                required
                type="number"
                min="1000"
                step="1000"
                value={reservePrice}
                onChange={(e) => setReservePrice(e.target.value)}
                placeholder="75000"
                className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-mono"
              />
            </div>

            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Duration</label>
              <select
                value={durationHours}
                onChange={(e) => setDurationHours(Number(e.target.value))}
                className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-mono"
              >
                <option value={12}>12 Hours</option>
                <option value={24}>24 Hours</option>
                <option value={48}>48 Hours</option>
                <option value={72}>72 Hours</option>
              </select>
            </div>
          </div>

          {/* Secure Image Upload via Backend */}
          <div>
            <label className="block text-zinc-500 uppercase tracking-wider mb-2">Lot Photograph</label>
            <div className="flex items-center gap-4">
              <label className="px-4 py-2 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-800 transition cursor-pointer flex items-center gap-2">
                <Upload className="w-3.5 h-3.5" />
                <span>{isUploading ? 'Validating & Uploading...' : 'Upload Image (Max 5MB)'}</span>
                <input
                  type="file"
                  accept="image/png,image/jpeg,image/webp"
                  onChange={handleFileUpload}
                  className="hidden"
                />
              </label>
              <span className="text-[10px] text-zinc-400">Validated by backend Auction Service</span>
            </div>
          </div>

          {/* Actions */}
          <div className="pt-4 border-t border-zinc-200 dark:border-zinc-800 flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 border border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-900 font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || isUploading}
              className="px-6 py-2.5 bg-zinc-950 text-white dark:bg-white dark:text-zinc-950 font-bold uppercase tracking-wider hover:opacity-90 transition disabled:opacity-50"
            >
              {isSubmitting ? 'Publishing...' : 'Publish to Live Floor'}
            </button>
          </div>
        </form>

      </div>
    </div>
  );
};
