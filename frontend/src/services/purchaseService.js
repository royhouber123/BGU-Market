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
    console.log("🔍 Frontend purchase service sending:", { 
      paymentDetails, 
      shippingAddress 
    });
    try {
      const response = await api.post('/purchases/execute', {
        paymentDetails,
        shippingAddress
      });
      
      // Backend returns: {"success": true, "data": "...", "error": null}
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error('Purchase failed');
      }
    } catch (error) {
      console.error('Purchase error:', error);
      if (error.response?.data?.error) {
        throw new Error('Purchase failed');
      }
      throw error;
    }
  },

  // Submit auction offer
  submitAuctionOffer: async (storeId, productId, offerAmount, shippingAddress, paymentDetails) => {
    try {
      const response = await api.post('/purchases/auction/offer', {
        userId: 0, // Placeholder - server uses JWT token username instead
        storeId: parseInt(storeId),
        productId: String(productId),
        offerAmount: parseFloat(offerAmount),
        shippingAddress,
        paymentDetails
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error('Auction offer failed');
      }
    } catch (error) {
      console.error('Auction offer error:', error);
      if (error.response?.data?.error) {
        throw new Error('Auction offer failed');
      }
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
        throw new Error('Failed to open auction');
      }
    } catch (error) {
      console.error('Open auction error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to open auction');
      }
      throw error;
    }
  },

  // Submit bid
  submitBid: async (storeId, productId, bidAmount, quantity, shippingAddress, paymentDetails) => {
    try {
      const response = await api.post('/purchases/bid/submit', {
        storeId: parseInt(storeId),
        productId: String(productId),
        bidAmount: parseFloat(bidAmount),
        quantity: quantity || 1,
        shippingAddress,
        paymentDetails
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error('Bid submission failed');
      }
    } catch (error) {
      console.error('Bid submission error:', error);
      if (error.response?.data?.error) {
        throw new Error('Bid submission failed');
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
        throw new Error('Failed to get purchase history');
      }
    } catch (error) {
      console.error('Get purchase history error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to get purchase history');
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
        throw new Error('Failed to get purchase history');
      }
    } catch (error) {
      console.error('Purchase history error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to get purchase history');
      }
      throw error;
    }
  },

  // Get purchase history by store
  getStorePurchaseHistory: async (storeId) => {
    try {
      const response = await api.get(`/purchases/store/${storeId}`);
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error('Failed to get store purchase history');
      }
    } catch (error) {
      console.error('Store purchase history error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to get store purchase history');
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
        throw new Error('Failed to get product bids');
      }
    } catch (error) {
      console.error('Get product bids error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to get product bids');
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
        throw new Error('Failed to get my product bids');
      }
    } catch (error) {
      console.error('Get my product bids error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to get my product bids');
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
        throw new Error('Failed to approve bid');
      }
    } catch (error) {
      console.error('Approve bid error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to approve bid');
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
        throw new Error('Failed to reject bid');
      }
    } catch (error) {
      console.error('Reject bid error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to reject bid');
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
        throw new Error('Failed to propose counter bid');
      }
    } catch (error) {
      console.error('Propose counter bid error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to propose counter bid');
      }
      throw error;
    }
  },

  // Accept counter offer - aligns with PurchaseController.acceptCounterOffer
  acceptCounterOffer: async (storeId, productId) => {
    try {
      const response = await api.post('/purchases/bid/counter/accept', {
        storeId: parseInt(storeId),
        productId: productId
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error('Failed to accept counter offer');
      }
    } catch (error) {
      console.error('Accept counter offer error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to accept counter offer');
      }
      throw error;
    }
  },

  // Decline counter offer - aligns with PurchaseController.declineCounterOffer
  declineCounterOffer: async (storeId, productId) => {
    try {
      const response = await api.post('/purchases/bid/counter/decline', {
        storeId: parseInt(storeId),
        productId: productId
      });
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error('Failed to decline counter offer');
      }
    } catch (error) {
      console.error('Decline counter offer error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to decline counter offer');
      }
      throw error;
    }
  }
};

export default purchaseService; 