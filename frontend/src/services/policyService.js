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

export const policyService = {
  // Get discount policies for a store
  getDiscountPolicies: async (storeId, userId) => {
    try {
      const response = await api.get(`/stores/${storeId}/policies/discounts?userId=${userId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data || [];
      } else {
        throw new Error(apiResponse.error || 'Failed to get discount policies');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to get discount policies');
    }
  },

  // Add discount policy to a store
  addDiscountPolicy: async (storeId, userId, discountData) => {
    try {
      const response = await api.post(`/stores/${storeId}/policies/discounts?userId=${userId}`, discountData);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error(apiResponse.error || 'Failed to add discount policy');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to add discount policy');
    }
  },

  // Remove discount policy from a store
  removeDiscountPolicy: async (storeId, userId, discountData) => {
    try {
      console.log('Making DELETE request for discount policy:', {
        url: `/stores/${storeId}/policies/discounts?userId=${userId}`,
        data: discountData
      });
      
      const response = await api.delete(`/stores/${storeId}/policies/discounts?userId=${userId}`, {
        data: discountData
      });
      
      console.log('DELETE response for discount policy:', response.data);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error(apiResponse.error || 'Failed to remove discount policy');
      }
    } catch (error) {
      console.error('Error in removeDiscountPolicy:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to remove discount policy');
    }
  },

  // Get purchase policies for a store
  getPurchasePolicies: async (storeId, userId) => {
    try {
      const response = await api.get(`/stores/${storeId}/policies/purchase?userId=${userId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data || [];
      } else {
        throw new Error(apiResponse.error || 'Failed to get purchase policies');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to get purchase policies');
    }
  },

  // Add purchase policy to a store
  addPurchasePolicy: async (storeId, userId, policyData) => {
    try {
      const response = await api.post(`/stores/${storeId}/policies/purchase?userId=${userId}`, policyData);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error(apiResponse.error || 'Failed to add purchase policy');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to add purchase policy');
    }
  },

  // Remove purchase policy from a store
  removePurchasePolicy: async (storeId, userId, policyData) => {
    try {
      console.log('Making DELETE request for purchase policy:', {
        url: `/stores/${storeId}/policies/purchase?userId=${userId}`,
        data: policyData
      });
      
      const response = await api.delete(`/stores/${storeId}/policies/purchase?userId=${userId}`, {
        data: policyData
      });
      
      console.log('DELETE response for purchase policy:', response.data);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data;
      } else {
        throw new Error(apiResponse.error || 'Failed to remove purchase policy');
      }
    } catch (error) {
      console.error('Error in removePurchasePolicy:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw new Error(error.message || 'Failed to remove purchase policy');
    }
  },

  // Helper methods for creating policy objects
  createPurchasePolicy: (type, value) => ({
    type: type.toUpperCase(),
    value: parseInt(value)
  }),

  createBasicDiscountPolicy: (type, scope, scopeId, value) => ({
    type: type.toUpperCase(),
    scope: scope.toUpperCase(),
    scopeId: scopeId,
    value: parseFloat(value),
    couponCode: null,
    condition: null,
    subDiscounts: [],
    combinationType: "SUM"
  })
}; 