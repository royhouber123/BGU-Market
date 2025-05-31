import axios from 'axios';

// Create axios instance with base URL
const api = axios.create({
  baseURL: '/api'
});

// Add JWT token to requests if available
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const purchaseService = {
  // Execute a purchase
  executePurchase: async (paymentDetails, shippingAddress) => {
    try {
      const response = await api.post('/purchases/execute', {
        paymentDetails,
        shippingAddress
      });
      
      // Backend returns: {"success": true, "data": "...", "error": null}
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Purchase failed');
      }
    } catch (error) {
      console.error('Purchase error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Submit auction offer
  submitAuctionOffer: async (storeId, productId, offerAmount) => {
    try {
      const response = await api.post('/purchases/auction/offer', {
        storeId,
        productId,
        offerAmount
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Auction offer failed');
      }
    } catch (error) {
      console.error('Auction offer error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Submit bid
  submitBid: async (storeId, productId, bidAmount) => {
    try {
      const response = await api.post('/purchases/bid/submit', {
        storeId,
        productId,
        bidAmount
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Bid submission failed');
      }
    } catch (error) {
      console.error('Bid submission error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Get purchase history for user
  getPurchaseHistory: async (userId) => {
    try {
      const response = await api.get(`/purchases/user/${userId}`);
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get purchase history');
      }
    } catch (error) {
      console.error('Get purchase history error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  }
};

export default purchaseService; 