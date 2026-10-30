import React, { useState } from 'react';
import { api } from '../api/client';
import { X, AlertCircle } from 'lucide-react';

export const AuthModal = ({
  onClose,
  onAuthSuccess
}) => {
  const [isLoginMode, setIsLoginMode] = useState(true);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ROLE_BUYER');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrorMsg(null);

    try {
      if (isLoginMode) {
        const res = await api.auth.login({ username, password });
        localStorage.setItem('bv_token', res.token);
        const userObj = {
          id: res.userId,
          username: res.username,
          email: res.email,
          role: res.role
        };
        localStorage.setItem('bv_user', JSON.stringify(userObj));
        onAuthSuccess(userObj);
        onClose();
      } else {
        const res = await api.auth.register({ username, email, password, role });
        localStorage.setItem('bv_token', res.token);
        const userObj = {
          id: res.userId,
          username: res.username,
          email: res.email,
          role: res.role
        };
        localStorage.setItem('bv_user', JSON.stringify(userObj));
        onAuthSuccess(userObj);
        onClose();
      }
    } catch (err) {
      setErrorMsg(err.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 w-full max-w-md shadow-2xl relative p-8">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-zinc-200 dark:border-zinc-800 pb-4 mb-6">
          <div>
            <h3 className="font-black text-lg text-zinc-900 dark:text-white tracking-tight">
              {isLoginMode ? 'Access Bidding Account' : 'Register Collector Profile'}
            </h3>
            <p className="text-xs text-zinc-400 font-mono mt-0.5">
              JWT Authenticated session verified by Auth Service (:8081)
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

        <form onSubmit={handleSubmit} className="space-y-4 text-xs font-mono">
          <div>
            <label className="block text-zinc-500 uppercase tracking-wider mb-1">Username / Handle</label>
            <input
              required
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="e.g. charlie_collector"
              className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-sans"
            />
          </div>

          {!isLoginMode && (
            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Email Address</label>
              <input
                required
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="charlie@auction.com"
                className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-sans"
              />
            </div>
          )}

          <div>
            <label className="block text-zinc-500 uppercase tracking-wider mb-1">Password</label>
            <input
              required
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-3 py-2 border border-zinc-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white rounded-none focus:outline-none focus:border-zinc-900 font-sans"
            />
          </div>

          {!isLoginMode && (
            <div>
              <label className="block text-zinc-500 uppercase tracking-wider mb-1">Account Role</label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setRole('ROLE_BUYER')}
                  className={`py-2 text-center border font-bold transition ${
                    role === 'ROLE_BUYER'
                      ? 'border-zinc-900 bg-zinc-900 text-white dark:border-white dark:bg-white dark:text-zinc-950'
                      : 'border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400'
                  }`}
                >
                  Verified Buyer
                </button>
                <button
                  type="button"
                  onClick={() => setRole('ROLE_SELLER')}
                  className={`py-2 text-center border font-bold transition ${
                    role === 'ROLE_SELLER'
                      ? 'border-zinc-900 bg-zinc-900 text-white dark:border-white dark:bg-white dark:text-zinc-950'
                      : 'border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400'
                  }`}
                >
                  Seller / Consignor
                </button>
              </div>
            </div>
          )}

          <div className="pt-4 border-t border-zinc-200 dark:border-zinc-800">
            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full py-3 bg-zinc-950 text-white dark:bg-white dark:text-zinc-950 font-bold uppercase tracking-widest hover:opacity-90 transition disabled:opacity-50"
            >
              {isSubmitting
                ? 'Processing...'
                : isLoginMode
                ? 'Sign In to Marketplace'
                : 'Complete Registration'}
            </button>

            <button
              type="button"
              onClick={() => {
                setIsLoginMode(!isLoginMode);
                setErrorMsg(null);
              }}
              className="w-full text-center text-zinc-500 hover:text-zinc-900 dark:hover:text-white mt-3 text-[11px]"
            >
              {isLoginMode ? 'Need an account? Register here' : 'Already registered? Sign in instead'}
            </button>
          </div>
        </form>

      </div>
    </div>
  );
};
