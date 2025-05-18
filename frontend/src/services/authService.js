import axios from 'axios';

// Creating an axios instance with default configuration
const apiClient = axios.create({
  baseURL: '/api', // This will use the proxy setting from package.json
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add an interceptor to include the auth token in requests when available
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

export const authService = {
  // Login using the backend authentication service
  login: async (username, password) => {
    try {
      const response = await apiClient.post('/auth/login', { username, password });
      return response.data; // Should return { token: 'jwt-token' }
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to login');
    }
  },

  // Logout using the backend authentication service
  logout: async (token) => {
    try {
      await apiClient.post('/auth/logout', { token });
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to logout');
    }
  },

  // Get current user information using the token
  getCurrentUser: async (token) => {
    try {
      const response = await apiClient.get('/users/me');
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to get user information');
    }
  },

  // Register a new user
  register: async (userData) => {
    try {
      const response = await apiClient.post('/users/register', userData);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to register user');
    }
  }
};
