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
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return { token: apiResponse.data.token }; // Extract token from AuthToken object
      } else {
        throw new Error(apiResponse.error || 'Login failed');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to login');
    }
  },

  // Logout using the backend authentication service
  logout: async (token) => {
    try {
      await apiClient.post('/auth/logout', { token });
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to logout');
    }
  },

  // Get current user information using the token
  getCurrentUser: async (token) => {
    try {
      const response = await apiClient.get('/users/me');
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error(apiResponse.error || 'Failed to get user information');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to get user information');
    }
  },

  // Register a new user
  register: async (userData) => {
    try {
      // Map frontend format to backend format (exclude email as backend doesn't expect it)
      const registerRequest = {
        username: userData.userName || userData.username,
        password: userData.password
      };
      
      const response = await apiClient.post('/users/register', registerRequest);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return { success: true, message: 'Registration successful!' };
      } else {
        throw new Error(apiResponse.error || 'Registration failed');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to register user');
    }
  }
};
