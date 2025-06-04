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

const adminService = {
  /**
   * Check if the current user is an admin
   */
  isAdmin: async () => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (!user?.userName) {
        return false;
      }
      
      const response = await api.get(`/admin/verify/${user.userName}`);
      return response.data.success && response.data.data === true;
    } catch (error) {
      console.error('Admin verification error:', error);
      return false;
    }
  },

  /**
   * Close a store by admin request
   */
  closeStore: async (storeId) => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (!user?.userName) {
        throw new Error('User not authenticated');
      }
      
      const response = await api.post('/admin/stores/close', {
        adminId: user.userName,
        storeId: storeId
      });
      
      return response.data.success;
    } catch (error) {
      console.error('Close store error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  /**
   * Get all stores for admin management
   */
  getAllStores: async () => {
    try {
      const response = await api.get('/admin/stores');
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get stores');
      }
    } catch (error) {
      console.error('Get all stores error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  /**
   * Get all users for admin management
   */
  getAllUsers: async () => {
    try {
      const response = await api.get('/admin/users');
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get users');
      }
    } catch (error) {
      console.error('Get all users error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  /**
   * Suspend a user for a specified duration
   * @param {string} userId - ID of the user to suspend
   * @param {number} durationHours - Duration of suspension in hours (0 = permanent)
   */
  suspendUser: async (userId, durationHours = 0) => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (!user?.userName) {
        throw new Error('User not authenticated');
      }
      
      const response = await api.post('/admin/users/suspend', {
        adminId: user.userName,
        userId: userId,
        durationHours: durationHours
      });
      
      return response.data.success;
    } catch (error) {
      console.error('Suspend user error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  /**
   * Unsuspend a previously suspended user
   * @param {string} userId - ID of the user to unsuspend
   */
  unsuspendUser: async (userId) => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (!user?.userName) {
        throw new Error('User not authenticated');
      }
      
      const response = await api.post('/admin/users/unsuspend', {
        adminId: user.userName,
        userId: userId
      });
      
      return response.data.success;
    } catch (error) {
      console.error('Unsuspend user error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  /**
   * Get list of all suspended users
   * @returns {Promise<string[]>} - Array of suspended user IDs
   */
  getSuspendedUsers: async () => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      if (!user?.userName) {
        throw new Error('User not authenticated');
      }
      
      const response = await api.get(`/admin/users/suspended?adminId=${user.userName}`);
      
      if (response.data.success) {
        return response.data.data;
      } else {
        throw new Error(response.data.error || 'Failed to get suspended users');
      }
    } catch (error) {
      console.error('Get suspended users error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  }
};

export default adminService;
