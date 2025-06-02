import axios from 'axios';
import { productService } from './productService';

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

const userService = {
  // User login - uses BCrypt for password verification on the backend
  login: async (username, password) => {
    try {
      const response = await api.post('/auth/login', { username, password });
      
      // Backend returns: {"success": true, "data": {"token": "..."}, "error": null}
      if (response.data.success && response.data.data?.token) {
        const token = response.data.data.token;
        localStorage.setItem('token', token);
        
        // Get user profile after successful login
        try {
          const userProfile = await userService.getProfile();
          localStorage.setItem('user', JSON.stringify(userProfile));
          return userProfile;
        } catch (profileError) {
          // If profile fetch fails, create a basic user object
          const basicUser = { userName: username };
          localStorage.setItem('user', JSON.stringify(basicUser));
          return basicUser;
        }
      } else {
        throw new Error(response.data.error || 'Login failed');
      }
    } catch (error) {
      console.error('Login error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Google login
  googleLogin: async () => {
    try {
      window.location.href = '/api/auth/google';
      return null;
    } catch (error) {
      console.error('Google login error:', error);
      throw error;
    }
  },

  // Register a new user
  register: async (userData) => {
    try {
      const response = await api.post('/auth/register', userData);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
      return response.data.user;
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  },

  // Logout user
  logout: async () => {
    try {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      return true;
    } catch (error) {
      console.error('Logout error:', error);
      throw error;
    }
  },

  // Get current user
  getCurrentUser: () => {
    try {
      const userJson = localStorage.getItem('user');
      return userJson ? JSON.parse(userJson) : null;
    } catch (error) {
      console.error('Get current user error:', error);
      return null;
    }
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },

  // Get authentication token
  getToken: () => {
    return localStorage.getItem('token');
  },

  // Get user profile
  getProfile: async () => {
    try {
      const response = await api.get('/users/me');
      // Backend returns: {"success": true, "data": {...user data...}, "error": null}
      if (response.data.success && response.data.data) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get user profile');
      }
    } catch (error) {
      console.error('Get profile error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Update user data
  updateUserData: async (userData) => {
    try {
      const response = await api.put('/users/me', userData);
      // Update local storage with new user data
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      const updatedUser = { ...currentUser, ...response.data };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      return response.data;
    } catch (error) {
      console.error('Update user data error:', error);
      throw error;
    }
  },

  // Add item to cart
  addToCart: async (item) => {
    try {
      const response = await api.post('/users/me/cart', item);
      // Update local storage
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      currentUser.cart = response.data.cart;
      localStorage.setItem('user', JSON.stringify(currentUser));
      return response.data;
    } catch (error) {
      console.error('Add to cart error:', error);
      throw error;
    }
  },

  // Add product to cart using backend API
  addProductToCart: async (storeId, listingId, quantity = 1) => {
    try {
      // Validate that we're getting a proper listing ID (UUID format)
      if (!listingId || typeof listingId !== 'string') {
        throw new Error('Invalid listing ID provided');
      }

      // Log the parameters for debugging
      console.log('[addProductToCart] Adding to cart:', { storeId, listingId, quantity });

      const response = await api.post('/users/cart/add', {
        storeId: storeId,
        productName: listingId, // Backend API expects "productName" but it should actually be the listing ID
        quantity: quantity
      });
      
      // After adding to backend cart, sync with frontend
      await userService.syncCartFromBackend();
      
      return response.data;
    } catch (error) {
      console.error('Add product to cart error:', error);
      throw error;
    }
  },

  // Remove item from cart
  removeFromCart: async (productId) => {
    try {
      const response = await api.delete(`/users/me/cart/${productId}`);
      // Update local storage
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      currentUser.cart = response.data.cart;
      localStorage.setItem('user', JSON.stringify(currentUser));
      return response.data;
    } catch (error) {
      console.error('Remove from cart error:', error);
      throw error;
    }
  },

  // Add item to watchlist
  addToWatchlist: async (productId) => {
    try {
      const response = await api.post('/users/me/watchlist', { productId });
      // Update local storage
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      currentUser.watchlist = response.data.watchlist;
      localStorage.setItem('user', JSON.stringify(currentUser));
      return response.data;
    } catch (error) {
      console.error('Add to watchlist error:', error);
      throw error;
    }
  },

  // Remove item from watchlist
  removeFromWatchlist: async (productId) => {
    try {
      const response = await api.delete(`/users/me/watchlist/${productId}`);
      // Update local storage
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      currentUser.watchlist = response.data.watchlist;
      localStorage.setItem('user', JSON.stringify(currentUser));
      return response.data;
    } catch (error) {
      console.error('Remove from watchlist error:', error);
      throw error;
    }
  },

  // Get user cart
  getCart: async () => {
    try {
      const response = await api.get('/users/me/cart');
      return response.data;
    } catch (error) {
      console.error('Get cart error:', error);
      throw error;
    }
  },

  // Get user cart using backend API
  getUserCart: async () => {
    try {
      const response = await api.get('/users/cart');
      // Backend returns: {"success": true, "data": {...cart data...}, "error": null}
      if (response.data.success && response.data.data) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get user cart');
      }
    } catch (error) {
      console.error('Get user cart error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Get user watchlist
  getWatchlist: async () => {
    try {
      const response = await api.get('/users/me/watchlist');
      return response.data;
    } catch (error) {
      console.error('Get watchlist error:', error);
      throw error;
    }
  },

  // Sync cart from backend to frontend user profile
  syncCartFromBackend: async () => {
    try {
      if (!userService.isAuthenticated()) {
        console.log('[syncCartFromBackend] User not authenticated, skipping sync');
        return [];
      }

      console.log('[syncCartFromBackend] Starting cart sync...');

      // Get cart from backend
      const backendCart = await userService.getUserCart();
      console.log('[syncCartFromBackend] Backend cart data:', backendCart);
      
      // Convert backend cart format to frontend format
      const frontendCart = [];
      
      // The backend returns a ShoppingCart object with allStoreBags property
      const storeBags = backendCart?.allStoreBags || [];
      console.log('[syncCartFromBackend] Found', storeBags.length, 'store bags');
      
      for (const storeBag of storeBags) {
        const storeId = storeBag.storeId;
        // StoreBag has both 'products' and 'productQuantities' - they're the same
        const products = storeBag.products || storeBag.productQuantities || {};
        console.log('[syncCartFromBackend] Processing store', storeId, 'with products:', products);
        
        for (const [productId, quantity] of Object.entries(products)) {
          console.log('[syncCartFromBackend] Processing product', productId, 'with quantity', quantity);
          
          // Try to get product details
          try {
            const productDetails = await productService.getListing(productId);
            if (productDetails) {
              console.log('[syncCartFromBackend] Successfully fetched details for product', productId);
              frontendCart.push({
                productId: productId,
                storeId: storeId,
                title: productDetails.title,
                price: productDetails.price,
                image: productDetails.images?.[0] || '',
                quantity: quantity
              });
            } else {
              console.warn('[syncCartFromBackend] No product details returned for product', productId);
              // Add basic cart item without full details
              frontendCart.push({
                productId: productId,
                storeId: storeId,
                title: `Product ${productId}`,
                price: 0,
                image: '',
                quantity: quantity
              });
            }
          } catch (error) {
            console.warn(`[syncCartFromBackend] Could not fetch details for product ${productId}:`, error);
            // Add basic cart item without full details
            frontendCart.push({
              productId: productId,
              storeId: storeId,
              title: `Product ${productId}`,
              price: 0,
              image: '',
              quantity: quantity
            });
          }
        }
      }

      console.log('[syncCartFromBackend] Final frontend cart:', frontendCart);

      // Update frontend user profile with synced cart
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      currentUser.cart = frontendCart;
      localStorage.setItem('user', JSON.stringify(currentUser));
      
      console.log('[syncCartFromBackend] Cart sync completed successfully');
      return frontendCart;
    } catch (error) {
      console.error('[syncCartFromBackend] Sync cart from backend error:', error);
      // Don't throw the error, just log it and return empty cart
      // This prevents the cart from breaking completely
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      currentUser.cart = [];
      localStorage.setItem('user', JSON.stringify(currentUser));
      return [];
    }
  }
};

export default userService;
