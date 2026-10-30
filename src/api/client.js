// VITE_API_BASE_URL is the documented setting. Keep VITE_API_URL as a
// backwards-compatible alias for existing local environments.
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor(status, message, data) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

async function request(endpoint, options = {}) {
  const token = localStorage.getItem('bv_token');
  const headers = new Headers(options.headers || {});

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const url = `${API_BASE_URL}${endpoint}`;
  try {
    const response = await fetch(url, { ...options, headers });

    if (response.status === 401) {
      localStorage.removeItem('bv_token');
      localStorage.removeItem('bv_user');
      window.dispatchEvent(new Event('auth_expired'));
      throw new ApiError(401, 'Session expired. Please sign in again.');
    }

    if (!response.ok) {
      let errorData;
      try {
        errorData = await response.json();
      } catch {
        errorData = await response.text();
      }
      const message =
        (typeof errorData === 'object' && errorData?.error) ||
        (typeof errorData === 'object' && errorData?.message) ||
        (typeof errorData === 'string' && errorData) ||
        `HTTP Error ${response.status}`;
      throw new ApiError(response.status, message, errorData);
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }
    return await response.text();
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(
      503,
      'Backend connection failed. Ensure Spring Cloud Gateway is running on port 8080.',
      error
    );
  }
}

export const api = {
  // Auth Service
  auth: {
    register: (data) =>
      request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(data)
      }),
    login: (data) =>
      request('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(data)
      }),
    getMe: () => request('/api/auth/me')
  },

  // Auction Service
  auctions: {
    list: (status) => {
      const query = status ? `?status=${status}` : '';
      return request(`/api/auctions${query}`);
    },
    getById: (id) => request(`/api/auctions/${id}`),
    create: (data) =>
      request('/api/auctions', {
        method: 'POST',
        body: JSON.stringify(data)
      }),
    close: (id) =>
      request(`/api/auctions/${id}/close`, {
        method: 'PUT'
      }),
    secondChance: (id) =>
      request(`/api/auctions/${id}/second-chance`, {
        method: 'POST'
      }),
    uploadImage: (file) => {
      const formData = new FormData();
      formData.append('file', file);
      return request('/api/auctions/upload-image', {
        method: 'POST',
        body: formData
      });
    }
  },

  // Bidding Service
  bids: {
    place: (data) =>
      request('/api/bids', {
        method: 'POST',
        body: JSON.stringify(data)
      }),
    getForAuction: (auctionId) => request(`/api/bids/auction/${auctionId}`),
    getMyBids: () => request('/api/bids/my-bids')
  },

  // Payment Service
  payments: {
    getCheckout: (paymentId) => request(`/api/payments/${paymentId}/checkout`),
    getCheckoutByAuction: (auctionId) => request(`/api/payments/auction/${auctionId}/checkout`),
    simulateSubmit: (data) =>
      request('/api/payments/mock-gateway/simulate-submit', {
        method: 'POST',
        body: JSON.stringify(data)
      }),
    getMyPayments: () => request('/api/payments/my-payments')
  },

  // Gateway Diagnostics & Health Probing
  diagnostics: {
    checkGatewayHealth: async () => {
      try {
        // The actuator endpoint is for infrastructure tooling. Probe a public
        // gateway route instead so browser diagnostics use the same CORS path
        // as the application itself.
        const res = await fetch(`${API_BASE_URL}/api/auctions`, { method: 'GET' });
        return res.ok;
      } catch {
        return false;
      }
    },
    checkService: async (path) => {
      try {
        const res = await fetch(`${API_BASE_URL}${path}`, { method: 'GET' });
        return res.status !== 503 && res.status !== 502;
      } catch {
        return false;
      }
    }
  }
};
