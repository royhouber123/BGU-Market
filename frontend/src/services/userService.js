import axios from 'axios';

// MOCK DATA IMPLEMENTATION
// TODO: Uncomment the real API implementation when backend is ready

// Mock user data
const mockUsers = [
  {
    id: "1",
    userName: "john_doe",
    firstName: "John",
    lastName: "Doe",
    email: "john@example.com",
    role: "USER",
    cart: [
      {
        productId: "101",
        title: "Wireless Headphones",
        price: 99.99,
        image: "https://source.unsplash.com/random/300x200?sig=101",
        quantity: 1
      }
    ],
    watchlist: [
      {
        productId: "104",
        addedAt: "2025-05-15T14:30:00Z"
      },
      {
        productId: "102",
        addedAt: "2025-05-18T10:15:00Z"
      }
    ],
    stores: [
      { id: 1, name: 'Electronics Hub' }
    ]
  },
  {
    id: "2",
    userName: "jane_smith",
    firstName: "Jane",
    lastName: "Smith",
    email: "jane@example.com",
    role: "USER",
    cart: [],
    watchlist: [],
    stores: [
      { id: 2, name: 'Fashion Outlet' }
    ]
  },
  {
    id: "3",
    userName: "admin",
    firstName: "Admin",
    lastName: "User",
    email: "admin@example.com",
    role: "ADMIN",
    cart: [],
    watchlist: [],
    stores: []
  }
];

// Mock current user - will be set on login/register and cleared on logout
let mockCurrentUser = null;

// Create axios instance with base URL (for future use)
const api = axios.create({
  baseURL: '/api'
});

// Add JWT token to requests if available (for future use)
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/* 
 * MOCK DATA SERVICE IMPLEMENTATION
 * TODO: Replace mock implementations with actual API calls when backend is ready
 * Each function includes commented code for the real implementation
 */
const userService = {
  // User login - uses BCrypt for password verification on the backend
  login: async (username, password) => {
    try {
      // MOCK DATA: Simulate login
      // TODO: Uncomment when backend is ready
      // const response = await api.post('/auth/login', { username, password });
      // localStorage.setItem('token', response.data.token);
      // localStorage.setItem('user', JSON.stringify(response.data.user));
      // return response.data.user;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 800));
      
      // Find user with matching username
      const user = mockUsers.find(u => u.userName === username);
      
      // For mock purposes, we're accepting any password
      if (user) {
        // Create a fake token
        const token = `mock-jwt-token-${Date.now()}`;
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        mockCurrentUser = user;
        return user;
      } else {
        throw new Error('Invalid username or password');
      }
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  },

  // Google login
  googleLogin: async () => {
    try {
      // MOCK DATA: Simulate Google login with first mock user
      // TODO: Uncomment when backend is ready
      // window.location.href = '/api/auth/google';
      // return null;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 1200));
      
      // Just use the first user for mock Google login
      const user = mockUsers[0];
      const token = `mock-google-jwt-token-${Date.now()}`;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      mockCurrentUser = user;
      return user;
    } catch (error) {
      console.error('Google login error:', error);
      throw error;
    }
  },

  // Register a new user
  register: async (userData) => {
    try {
      // MOCK DATA: Simulate registration
      // TODO: Uncomment when backend is ready
      // const response = await api.post('/auth/register', userData);
      // localStorage.setItem('token', response.data.token);
      // localStorage.setItem('user', JSON.stringify(response.data.user));
      // return response.data.user;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Check if username already exists
      if (mockUsers.some(u => u.userName === userData.userName)) {
        throw new Error('Username already exists');
      }
      
      // Create new user with provided data
      const newUser = {
        id: String(mockUsers.length + 1),
        userName: userData.userName,
        firstName: userData.firstName || '',
        lastName: userData.lastName || '',
        email: userData.email,
        role: 'USER',
        cart: [],
        watchlist: [],
        stores: []
      };
      
      // In a real app we would add the user to the database
      // mockUsers.push(newUser); // We won't actually modify mockUsers to avoid issues when refreshing
      
      // Create a fake token
      const token = `mock-jwt-token-${Date.now()}`;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(newUser));
      mockCurrentUser = newUser;
      
      return newUser;
    } catch (error) {
      console.error('Registration error:', error);
      throw error;
    }
  },

  // Logout user
  logout: async () => {
    try {
      // MOCK DATA: Simulate logout (same as real implementation)
      // TODO: No change needed when backend is ready
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      mockCurrentUser = null;
      return true;
    } catch (error) {
      console.error('Logout error:', error);
      throw error;
    }
  },

  // Get current user
  getCurrentUser: () => {
    try {
      // MOCK DATA: Get current user from localStorage (same as real implementation)
      // TODO: No change needed when backend is ready
      const userJson = localStorage.getItem('user');
      return userJson ? JSON.parse(userJson) : null;
    } catch (error) {
      console.error('Get current user error:', error);
      return null;
    }
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    // MOCK DATA: Check authentication (same as real implementation)
    // TODO: No change needed when backend is ready
    return !!localStorage.getItem('token');
  },

  // Get user profile
  getProfile: async () => {
    try {
      // MOCK DATA: Get user profile
      // TODO: Uncomment when backend is ready
      // const response = await api.get('/users/me');
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 400));
      
      const user = JSON.parse(localStorage.getItem('user'));
      if (!user) {
        throw new Error('User not authenticated');
      }
      
      return user;
    } catch (error) {
      console.error('Get profile error:', error);
      throw error;
    }
  },

  // Update user data
  updateUserData: async (userData) => {
    try {
      // MOCK DATA: Update user data
      // TODO: Uncomment when backend is ready
      // const response = await api.put('/users/me', userData);
      // Update local storage with new user data
      // const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      // const updatedUser = { ...currentUser, ...response.data };
      // localStorage.setItem('user', JSON.stringify(updatedUser));
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 600));
      
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      const updatedUser = { ...currentUser, ...userData };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      
      return updatedUser;
    } catch (error) {
      console.error('Update user data error:', error);
      throw error;
    }
  },

  // Add item to cart
  addToCart: async (item) => {
    try {
      // MOCK DATA: Add item to cart
      // TODO: Uncomment when backend is ready
      // const response = await api.post('/users/me/cart', item);
      // Update local storage
      // const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      // currentUser.cart = response.data.cart;
      // localStorage.setItem('user', JSON.stringify(currentUser));
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 500));
      
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.cart) currentUser.cart = [];
      
      // Check if item already exists in cart
      const existingItemIndex = currentUser.cart.findIndex(
        cartItem => cartItem.productId === item.productId
      );
      
      if (existingItemIndex >= 0) {
        // Update quantity
        currentUser.cart[existingItemIndex].quantity += item.quantity || 1;
      } else {
        // Add new item
        currentUser.cart.push(item);
      }
      
      localStorage.setItem('user', JSON.stringify(currentUser));
      
      return { cart: currentUser.cart };
    } catch (error) {
      console.error('Add to cart error:', error);
      throw error;
    }
  },

  // Remove item from cart
  removeFromCart: async (productId) => {
    try {
      // MOCK DATA: Remove item from cart
      // TODO: Uncomment when backend is ready
      // const response = await api.delete(`/users/me/cart/${productId}`);
      // Update local storage
      // const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      // currentUser.cart = response.data.cart;
      // localStorage.setItem('user', JSON.stringify(currentUser));
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 400));
      
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.cart) currentUser.cart = [];
      
      currentUser.cart = currentUser.cart.filter(
        item => item.productId !== productId
      );
      
      localStorage.setItem('user', JSON.stringify(currentUser));
      
      return { cart: currentUser.cart };
    } catch (error) {
      console.error('Remove from cart error:', error);
      throw error;
    }
  },

  // Add item to watchlist
  addToWatchlist: async (productId) => {
    try {
      // MOCK DATA: Add item to watchlist
      // TODO: Uncomment when backend is ready
      // const response = await api.post('/users/me/watchlist', { productId });
      // Update local storage
      // const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      // currentUser.watchlist = response.data.watchlist;
      // localStorage.setItem('user', JSON.stringify(currentUser));
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 400));
      
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.watchlist) currentUser.watchlist = [];
      
      // Check if item already exists in watchlist
      const exists = currentUser.watchlist.some(
        item => item.productId === productId
      );
      
      if (!exists) {
        currentUser.watchlist.push({
          productId,
          addedAt: new Date().toISOString()
        });
      }
      
      localStorage.setItem('user', JSON.stringify(currentUser));
      
      return { watchlist: currentUser.watchlist };
    } catch (error) {
      console.error('Add to watchlist error:', error);
      throw error;
    }
  },

  // Remove item from watchlist
  removeFromWatchlist: async (productId) => {
    try {
      // MOCK DATA: Remove item from watchlist
      // TODO: Uncomment when backend is ready
      // const response = await api.delete(`/users/me/watchlist/${productId}`);
      // Update local storage
      // const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      // currentUser.watchlist = response.data.watchlist;
      // localStorage.setItem('user', JSON.stringify(currentUser));
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 400));
      
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      if (!currentUser.watchlist) currentUser.watchlist = [];
      
      currentUser.watchlist = currentUser.watchlist.filter(
        item => item.productId !== productId
      );
      
      localStorage.setItem('user', JSON.stringify(currentUser));
      
      return { watchlist: currentUser.watchlist };
    } catch (error) {
      console.error('Remove from watchlist error:', error);
      throw error;
    }
  },

  // Get user cart
  getCart: async () => {
    try {
      // MOCK DATA: Get user cart
      // TODO: Uncomment when backend is ready
      // const response = await api.get('/users/me/cart');
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      return currentUser.cart || [];
    } catch (error) {
      console.error('Get cart error:', error);
      throw error;
    }
  },

  // Get user watchlist
  getWatchlist: async () => {
    try {
      // MOCK DATA: Get user watchlist
      // TODO: Uncomment when backend is ready
      // const response = await api.get('/users/me/watchlist');
      // return response.data;
      
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
      return currentUser.watchlist || [];
    } catch (error) {
      console.error('Get watchlist error:', error);
      throw error;
    }
  }
};

export default userService;
