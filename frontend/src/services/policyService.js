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
        throw new Error('Failed to get discount policies');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get discount policies');
      }
      throw new Error('Failed to get discount policies');
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
        throw new Error('Failed to add discount policy');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to add discount policy');
      }
      throw new Error('Failed to add discount policy');
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
        throw new Error('Failed to remove discount policy');
      }
    } catch (error) {
      console.error('Error in removeDiscountPolicy:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to remove discount policy');
      }
      throw new Error('Failed to remove discount policy');
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
        throw new Error('Failed to get purchase policies');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to get purchase policies');
      }
      throw new Error('Failed to get purchase policies');
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
        throw new Error('Failed to add purchase policy');
      }
    } catch (error) {
      if (error.response?.data?.error) {
        throw new Error('Failed to add purchase policy');
      }
      throw new Error('Failed to add purchase policy');
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
        throw new Error('Failed to remove purchase policy');
      }
    } catch (error) {
      console.error('Error in removePurchasePolicy:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to remove purchase policy');
      }
      throw new Error('Failed to remove purchase policy');
    }
  },

  // Helper methods for creating policy objects
  createPurchasePolicy: (type, value) => ({
    type: type.toUpperCase(),
    value: parseInt(value)
  }),

  // Create a percentage or fixed discount
  createBasicDiscountPolicy: (type, scope, scopeId, value) => ({
    type: type.toUpperCase(),
    scope: scope.toUpperCase(),
    scopeId: scopeId,
    value: parseFloat(value),
    couponCode: null,
    condition: null,
    subDiscounts: [],
    combinationType: null
  }),

  // Create a coupon discount
  createCouponDiscountPolicy: (couponCode, value) => ({
    type: "COUPON",
    scope: null,
    scopeId: null,
    value: parseFloat(value),
    couponCode: couponCode,
    condition: null,
    subDiscounts: [],
    combinationType: null
  }),

  // Create a conditional discount aligned with backend DTO
  createConditionalDiscountPolicy: (
    baseDiscount,
    conditionType,
    conditionValue,
    conditionTarget
  ) => {
    let condType = conditionType.toUpperCase();
    const params = {};

    switch (condType) {
      case 'MIN_PRICE':
      case 'BASKET_TOTAL_AT_LEAST':
        condType = 'BASKET_TOTAL_AT_LEAST';
        params.minTotal = parseFloat(conditionValue);
        break;
      case 'MIN_ITEMS': // assume product quantity on specific product
      case 'PRODUCT_QUANTITY_AT_LEAST':
        condType = 'PRODUCT_QUANTITY_AT_LEAST';
        params.productId = conditionTarget;
        params.minQuantity = parseInt(conditionValue);
        break;
      case 'CATEGORY_QUANTITY_AT_LEAST':
      case 'MIN_CATEGORY_ITEMS':
        condType = 'CATEGORY_QUANTITY_AT_LEAST';
        params.category = conditionTarget;
        params.minQuantity = parseInt(conditionValue);
        break;
      default:
        throw new Error('Unsupported condition type');
    }

    const conditionDTO = {
      type: condType,
      params,
      subConditions: [],
      logic: null
    };

    return {
      type: 'CONDITIONAL',
      scope: baseDiscount.scope,
      scopeId: baseDiscount.scopeId,
      value: baseDiscount.value,
      couponCode: baseDiscount.couponCode,
      condition: conditionDTO,
      subDiscounts: [],
      combinationType: null
    };
  },

  // Create a composite discount
  createCompositeDiscountPolicy: (subDiscounts, combinationType = 'SUM') => {
    // Fallback safety: ensure at least 2 sub-discounts and valid combination type.
    if (!Array.isArray(subDiscounts) || subDiscounts.length < 2) {
      throw new Error('Composite discount requires at least two sub-discounts');
    }
    const comb = (combinationType || 'SUM').toUpperCase();
    if (comb !== 'SUM' && comb !== 'MAXIMUM') {
      throw new Error('Invalid combination type for composite discount');
    }
    return {
      type: 'COMPOSITE',
      scope: null,
      scopeId: null,
      value: 0,
      couponCode: null,
      condition: null,
      subDiscounts,
      combinationType: comb
    };
  },

  // Helper to create discount conditions
  createDiscountCondition: (type, value, targetId = null) => {
    const condition = {
      type: type.toUpperCase(), // MIN_ITEMS, MIN_PRICE, MIN_CATEGORY_ITEMS, MIN_PRODUCT_ITEMS, etc.
      value: parseFloat(value)
    };
    
    if (targetId) {
      condition.targetId = targetId;
    }
    
    return condition;
  }
};