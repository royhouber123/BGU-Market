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

export const storeService = {
  // Get store details by ID (internal helper)
  getStoreById: async (storeId) => {
    try {
      // Since we don't have a direct getStoreById endpoint, we'll try to find it
      // by getting all stores and finding the one with matching ID
      // For now, return a default structure
      return {
        id: storeId,
        name: `Store ${storeId}`,
        description: 'Store description not available',
        isActive: true,
        founderId: 'unknown'
      };
    } catch (error) {
      console.warn(`Failed to get store details for ID ${storeId}:`, error);
      return {
        id: storeId,
        name: `Store ${storeId}`,
        description: 'Store description not available',
        isActive: true,
        founderId: 'unknown'
      };
    }
  },

  // Get all stores and their products
  getAllStoresAndProducts: async () => {
    try {
      const response = await api.get('/stores/info');
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        const stores = [];
        const products = [];
        
        // Handle new API response format: List of {store, listings}
        apiResponse.data.forEach(storeData => {
          const store = storeData.store;
          const listings = storeData.listings;
          
          // Add store to stores array
          stores.push({
            id: store.storeID,
            name: store.storeName,
            description: store.description || 'No description available',
            isActive: store.isActive !== false,
            founderId: store.founderId || 'unknown',
            rating: store.rating || 0,
            totalProducts: listings.length
          });
          
          // Transform listings to products
          listings.forEach(listing => {
            products.push({
              id: listing.listingId,
              title: listing.productName,
              price: listing.price,
              status: listing.active ? 'active' : 'inactive',
              images: listing.images || [],
              category: listing.category,
              shipping_cost: listing.shippingCost || 0,
              featured: listing.active,
              description: listing.productDescription,
              quantity: listing.quantityAvailable,
              storeId: store.storeID,
              storeName: store.storeName,
              productId: listing.productId,
              seller: {
                id: store.storeID,
                name: store.storeName,
                rating: store.rating || 0
              },
              created_date: listing.createdDate || new Date().toISOString(),
              rating: listing.rating || 0,
              reviews: listing.reviews || []
            });
          });
        });
        
        return { stores, products };
      } else {
        throw new Error(apiResponse.error || 'Failed to get stores and products');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to get stores and products');
    }
  },

  // Get store by name
  getStore: async (storeName) => {
    try {
      const response = await api.get(`/stores/${storeName}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          id: apiResponse.data.storeID,
          name: apiResponse.data.storeName,
          description: apiResponse.data.description || 'No description available',
          isActive: apiResponse.data.isActive,
          founderId: apiResponse.data.founderId
        };
      } else {
        throw new Error(apiResponse.error || 'Failed to fetch store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to fetch store');
    }
  },

  // Create a new store
  createStore: async (storeData) => {
    try {
      const response = await api.post('/stores/create', storeData);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          id: apiResponse.data.storeId,
          name: storeData.storeName,
          founderId: storeData.founderId,
          isActive: true
        };
      } else {
        throw new Error(apiResponse.error || 'Failed to create store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to create store');
    }
  },

  // Add a listing to a store
  addListing: async (listingData) => {
    try {
      const response = await api.post('/stores/listings/add', listingData);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          listingId: apiResponse.data.listingId || 'unknown',
          message: 'Listing added successfully'
        };
      } else {
        throw new Error(apiResponse.error || 'Failed to add listing');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to add listing');
    }
  },

  // Remove a listing from a store
  removeListing: async (removeData) => {
    try {
      const response = await api.post('/stores/listings/remove', removeData);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          message: 'Listing removed successfully'
        };
      } else {
        throw new Error(apiResponse.error || 'Failed to remove listing');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to remove listing');
    }
  },

  // Update a listing in a store
  updateListing: async (updateData) => {
    try {
      const response = await api.post('/stores/listings/update', updateData);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          message: 'Listing updated successfully'
        };
      } else {
        throw new Error(apiResponse.error || 'Failed to update listing');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to update listing');
    }
  },

  // Get all stores
  getAllStores: async () => {
    try {
      const { stores } = await this.getAllStoresAndProducts();
      return stores;
    } catch (error) {
      console.error('Get all stores error:', error);
      throw error;
    }
  },

  // Get products for a specific store
  getStoreProducts: async (storeId) => {
    try {
      const { products } = await this.getAllStoresAndProducts();
      return products.filter(product => product.storeId === storeId);
    } catch (error) {
      console.error('Get store products error:', error);
      throw error;
    }
  },

  // Check if user is owner of store
  isOwner: async (storeID, userID) => {
    try {
      const response = await api.get(`/stores/${storeID}/owners/${userID}/check`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        return false;
      }
    } catch (error) {
      return false;
    }
  },

  // Check if user is manager of store
  isManager: async (storeID, userID) => {
    try {
      const response = await api.get(`/stores/${storeID}/managers/${userID}/check`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        return false;
      }
    } catch (error) {
      return false;
    }
  },

  // Close store
  closeStore: async (storeID, userName) => {
    try {
      const response = await api.post(`/stores/${storeID}/close?userName=${userName}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error(apiResponse.error || 'Failed to close store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to close store');
    }
  },

  // Open store
  openStore: async (storeID, userName) => {
    try {
      const response = await api.post(`/stores/${storeID}/open?userName=${userName}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error(apiResponse.error || 'Failed to open store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to open store');
    }
  }
}; 