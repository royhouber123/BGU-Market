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

const userService = {
  // User login - uses BCrypt for password verification on the backend
  login: async (username, password) => {
    try {
      const response = await api.post('/auth/login', { username, password });
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
      return response.data.user;
    } catch (error) {
      console.error('Login error:', error);
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

  // Get user profile
  getProfile: async () => {
    try {
      const response = await api.get('/users/me');
      return response.data;
    } catch (error) {
      console.error('Get profile error:', error);
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

  // Get user watchlist
  getWatchlist: async () => {
    try {
      const response = await api.get('/users/me/watchlist');
      return response.data;
    } catch (error) {
      console.error('Get watchlist error:', error);
      throw error;
    }
  }
};

export default userService;
