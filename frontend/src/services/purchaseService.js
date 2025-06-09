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
  // Execute a purchase - flatten payment details
  executePurchase: async (paymentDetails, shippingAddress, contactInfo) => {
    try {
      const response = await api.post('/purchases/execute', {
        shippingAddress,
        contactInfo,
        // Send as individual string fields, not nested object
        currency: paymentDetails.currency || 'USD',
        cardNumber: paymentDetails.cardNumber,
        month: paymentDetails.month,
        year: paymentDetails.year,
        holder: paymentDetails.holder,
        ccv: paymentDetails.ccv
      });
      return response.data;
    } catch (error) {
      console.error('Purchase failed:', error);
      throw error;
    }
  },

  // Submit bid - flatten payment details
  submitBid: async (storeId, productId, bidAmount, shippingAddress, contactInfo, paymentDetails) => {
    try {
      const response = await api.post('/purchases/bid/submit', {
        storeId,
        productId,
        bidAmount,
        shippingAddress,
        contactInfo,
        // Send as individual string fields
        currency: paymentDetails.currency || 'USD',
        cardNumber: paymentDetails.cardNumber,
        month: paymentDetails.month,
        year: paymentDetails.year,
        holder: paymentDetails.holder,
        ccv: paymentDetails.ccv
      });
      return response.data;
    } catch (error) {
      console.error('Bid submission failed:', error);
      throw error;
    }
  },

  // Submit auction offer - flatten payment details
  submitAuctionOffer: async (storeId, productId, offerAmount, shippingAddress, contactInfo, paymentDetails) => {
    try {
      const response = await api.post('/purchases/auction/offer', {
        storeId,
        productId,
        offerAmount,
        shippingAddress,
        contactInfo,
        // Send as individual string fields
        currency: paymentDetails.currency || 'USD',
        cardNumber: paymentDetails.cardNumber,
        month: paymentDetails.month,
        year: paymentDetails.year,
        holder: paymentDetails.holder,
        ccv: paymentDetails.ccv
      });
      return response.data;
    } catch (error) {
      console.error('Auction offer failed:', error);
      throw error;
    }
  },

  // Open auction
  openAuction: async (storeId, productId, productName, productCategory, productDescription, startingPrice, endTimeMillis) => {
    try {
      const response = await api.post('/purchases/auction/open', {
        storeId: parseInt(storeId),
        productId,
        productName,
        productCategory,
        productDescription,
        startingPrice: parseInt(startingPrice),
        endTimeMillis
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to open auction');
      }
    } catch (error) {
      console.error('Open auction error:', error);
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
  },

  // Get purchase history for user
  getUserPurchaseHistory: async () => {
    try {
      const response = await api.get('/purchases/user/1'); // Will be updated when user ID is properly handled
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get purchase history');
      }
    } catch (error) {
      console.error('Purchase history error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Bid Management Functions
  
  // Get all bids for a specific product
  getProductBids: async (storeId, productId) => {
    try {
      const response = await api.get(`/purchases/bids/${storeId}/${productId}`);
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get product bids');
      }
    } catch (error) {
      console.error('Get product bids error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Get current user's bids for a specific product
  getMyProductBids: async (storeId, productId) => {
    try {
      const response = await api.get(`/purchases/my-bids/${storeId}/${productId}`);
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get my product bids');
      }
    } catch (error) {
      console.error('Get my product bids error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Approve a bid
  approveBid: async (storeId, productId, bidderUsername) => {
    try {
      const response = await api.post('/purchases/bid/approve', {
        storeId: parseInt(storeId),
        productId: productId,
        bidderUsername,
        approved: true
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to approve bid');
      }
    } catch (error) {
      console.error('Approve bid error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Reject a bid
  rejectBid: async (storeId, productId, bidderUsername) => {
    try {
      const response = await api.post('/purchases/bid/reject', {
        storeId: parseInt(storeId),
        productId: productId,
        bidderUsername,
        approved: false
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to reject bid');
      }
    } catch (error) {
      console.error('Reject bid error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Propose counter bid
  proposeCounterBid: async (storeId, productId, bidderUsername, counterAmount) => {
    try {
      const response = await api.post('/purchases/bid/counter', {
        storeId: parseInt(storeId),
        productId: productId,
        bidderUsername,
        counterAmount
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to propose counter bid');
      }
    } catch (error) {
      console.error('Propose counter bid error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  }
};

export default purchaseService;