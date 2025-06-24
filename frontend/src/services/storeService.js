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
              purchaseType: listing.purchaseType,
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
        throw new Error('Failed to get stores and products');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get stores and products');
      }
      throw new Error('Failed to get stores and products');
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
        throw new Error('Failed to fetch store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to fetch store');
      }
      throw new Error('Failed to fetch store');
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
        throw new Error('Failed to create store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to create store');
      }
      throw new Error('Failed to create store');
    }
  },

  // Add new listing/product
  addListing: async (userName, storeID, productId, productName, productCategory, productDescription, quantity, price, purchaseType = 'REGULAR') => {
    try {
      const response = await api.post('/stores/listings/add', {
        userName,
        storeID,
        productId,
        productName,
        productCategory,
        productDescription,
        quantity,
        price,
        purchaseType
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          productId: apiResponse.data,
          message: 'Product added successfully'
        };
      } else {
        throw new Error('Failed to add product');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to add product');
      }
      throw new Error('Failed to add product');
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
        throw new Error('Failed to remove listing');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to remove listing');
      }
      throw new Error('Failed to remove listing');
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
        throw new Error('Failed to update listing');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to update listing');
      }
      throw new Error('Failed to update listing');
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

  // Check if user is founder of store
  isFounder: async (storeID, userID) => {
    try {
      const response = await api.get(`/stores/${storeID}/founders/${userID}/check`);
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
        throw new Error('Failed to close store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to close store');
      }
      throw new Error('Failed to close store');
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
        throw new Error('Failed to open store');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to open store');
      }
      throw new Error('Failed to open store');
    }
  },

  // Edit listing price
  editListingPrice: async (userName, storeID, listingId, newPrice) => {
    try {
      const response = await api.put('/stores/listings/price', {
        userName,
        storeID,
        listingId,
        newPrice
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          message: 'Price updated successfully'
        };
      } else {
        throw new Error('Failed to update price');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to update price');
      }
      throw new Error('Failed to update price');
    }
  },

  // Edit listing product name
  editListingName: async (userName, storeID, listingId, newValue) => {
    try {
      const response = await api.put('/stores/listings/name', {
        userName,
        storeID,
        listingId,
        newValue
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          message: 'Product name updated successfully'
        };
      } else {
        throw new Error('Failed to update product name');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to update product name');
      }
      throw new Error('Failed to update product name');
    }
  },

  // Edit listing description
  editListingDescription: async (userName, storeID, listingId, newValue) => {
    try {
      const response = await api.put('/stores/listings/description', {
        userName,
        storeID,
        listingId,
        newValue
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          message: 'Description updated successfully'
        };
      } else {
        throw new Error('Failed to update description');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to update description');
      }
      throw new Error('Failed to update description');
    }
  },

  // Edit listing quantity
  editListingQuantity: async (userName, storeID, listingId, newQuantity) => {
    try {
      const response = await api.put('/stores/listings/quantity', {
        userName,
        storeID,
        listingId,
        newQuantity
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          message: 'Quantity updated successfully'
        };
      } else {
        throw new Error('Failed to update quantity');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to update quantity');
      }
      throw new Error('Failed to update quantity');
    }
  },

  // Edit listing category
  editListingCategory: async (userName, storeID, listingId, newValue) => {
    try {
      const response = await api.put('/stores/listings/category', {
        userName,
        storeID,
        listingId,
        newValue
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          message: 'Category updated successfully'
        };
      } else {
        throw new Error('Failed to update category');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to update category');
      }
      throw new Error('Failed to update category');
    }
  },

  // Add permission to manager
  addPermissionToManager: async (managerID, appointerID, permissionID, storeID) => {
    try {
      const response = await api.post('/stores/managers/permissions/add', {
        managerID,
        appointerID,
        permissionID,
        storeID
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return {
          success: true,
          message: 'Permission added successfully'
        };
      } else {
        throw new Error('Failed to add permission');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to add permission');
      }
      throw new Error('Failed to add permission');
    }
  },

  // Get manager permissions
  getManagersPermissions: async (managerID, whoIsAsking, storeID) => {
    try {
      const response = await api.get(`/stores/${storeID}/managers/${managerID}/permissions?whoIsAsking=${whoIsAsking}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error('Failed to get permissions');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get permissions');
      }
      throw new Error('Failed to get permissions');
    }
  },

  // Get current user's permissions and role in store
  getCurrentUserPermissions: async (storeID, userID) => {
    try {
      const response = await api.get(`/stores/${storeID}/user/${userID}/permissions`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error('Failed to get user permissions');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get user permissions');
      }
      throw new Error('Failed to get user permissions');
    }
  },

  // Get all store users (owners and managers)
  getStoreUsers: async (storeID, requesterId) => {
    try {
      const response = await api.get(`/stores/${storeID}/users?requesterId=${requesterId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error('Failed to get store users');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get store users');
      }
      throw new Error('Failed to get store users');
    }
  },

  // Add additional store owner
  addAdditionalStoreOwner: async (appointerID, newOwnerID, storeID) => {
    try {
      const response = await api.post('/stores/owners/add', {
        appointerID,
        newOwnerID,
        storeID
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return { success: true, message: 'Owner added successfully' };
      } else {
        throw new Error('Failed to add owner');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to add owner');
      }
      throw new Error('Failed to add owner');
    }
  },

  // Add new manager
  addNewManager: async (appointerID, newManagerName, storeID) => {
    try {
      const response = await api.post('/stores/managers/add', {
        appointerID,
        newManagerName,
        storeID
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return { success: true, message: 'Manager added successfully' };
      } else {
        throw new Error('Failed to add manager');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to add manager');
      }
      throw new Error('Failed to add manager');
    }
  },

  // Remove owner
  removeOwner: async (requesterId, toRemove, storeID) => {
    try {
      const response = await api.delete(`/stores/${storeID}/owners/${toRemove}?requesterId=${requesterId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return { 
          success: true, 
          message: 'Owner removed successfully',
          removedUsers: apiResponse.data 
        };
      } else {
        throw new Error('Failed to remove owner');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to remove owner');
      }
      throw new Error('Failed to remove owner');
    }
  },

  // Remove manager
  removeManager: async (appointerID, managerID, storeID) => {
    try {
      const response = await api.delete(`/stores/${storeID}/managers/${managerID}?appointerID=${appointerID}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return { success: true, message: 'Manager removed successfully' };
      } else {
        throw new Error('Failed to remove manager');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to remove manager');
      }
      throw new Error('Failed to remove manager');
    }
  },

  // Remove permission from manager
  removePermissionFromManager: async (managerID, permissionID, appointerID, storeID) => {
    try {
      const response = await api.delete(`/stores/${storeID}/managers/${managerID}/permissions/${permissionID}?appointerID=${appointerID}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return { success: true, message: 'Permission removed successfully' };
      } else {
        throw new Error('Failed to remove permission');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to remove permission');
      }
      throw new Error('Failed to remove permission');
    }
  },

  // Validate if user exists in the system
  validateUserExists: async (userId) => {
    try {
      const response = await api.get(`/users/validate/${encodeURIComponent(userId)}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return { exists: apiResponse.data.exists };
      } else {
        return { exists: false };
      }
    } catch (error) {
      console.error('Error validating user existence:', error);
      return { exists: false };
    }
  }
}; 