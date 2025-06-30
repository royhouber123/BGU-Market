import axios from 'axios';

// Create axios instance with base URL
const api = axios.create({
  baseURL: '/api'
});

// Add JWT token to requests if available
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  console.log('[PolicyService] Request interceptor called:', {
    method: config.method?.toUpperCase(),
    url: config.url,
    hasToken: !!token,
    tokenPreview: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
    existingAuthHeader: config.headers.Authorization
  });
  
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    console.log('[PolicyService] Authorization header set:', config.headers.Authorization ? 'YES' : 'NO');
  } else {
    console.warn('[PolicyService] No token found in localStorage!');
  }
  
  console.log('[PolicyService] Final request headers:', {
    Authorization: config.headers.Authorization,
    'Content-Type': config.headers['Content-Type']
  });
  
  return config;
}, error => {
  console.error('[PolicyService] Request interceptor error:', error);
  return Promise.reject(error);
});

// Add response interceptor for debugging
api.interceptors.response.use(response => {
  console.log('[PolicyService] Response received:', {
    status: response.status,
    method: response.config.method?.toUpperCase(),
    url: response.config.url
  });
  return response;
}, error => {
  console.error('[PolicyService] Response error:', {
    status: error.response?.status,
    method: error.config?.method?.toUpperCase(),
    url: error.config?.url,
    message: error.message
  });
  return Promise.reject(error);
});

export const policyService = {
  // Get discount policies for a store
  getDiscountPolicies: async (storeId, userId) => {
    console.log('[PolicyService] getDiscountPolicies called:', { storeId, userId });
    try {
      const response = await api.get(`/stores/${storeId}/policies/discounts?userId=${userId}`);
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        return apiResponse.data || [];
      } else {
        throw new Error('Failed to get discount policies');
      }
    } catch (error) {
      console.error('[PolicyService] getDiscountPolicies error:', error);
      if (error.response?.data?.error) {
        throw new Error('Failed to get discount policies');
      }
      throw new Error('Failed to get discount policies');
    }
  },

  // Add discount policy to a store
  addDiscountPolicy: async (storeId, userId, discountData) => {
    console.log('[PolicyService] addDiscountPolicy called:', { 
      storeId, 
      userId, 
      discountData,
      tokenExists: !!localStorage.getItem('token'),
      tokenPreview: localStorage.getItem('token') ? `${localStorage.getItem('token').substring(0, 20)}...` : 'NO TOKEN'
    });
    
    try {
      // Deep clone and sanitize the data to remove metadata before sending to backend
      const sanitizedData = JSON.parse(JSON.stringify(discountData));
      const removeMetadataRecursive = (obj) => {
        if (!obj || typeof obj !== 'object') return;
        delete obj.metadata;
        for (const key in obj) {
          if (obj.hasOwnProperty(key)) {
            const value = obj[key];
            if (Array.isArray(value)) {
              value.forEach(removeMetadataRecursive);
            } else if (value && typeof value === 'object') {
              removeMetadataRecursive(value);
            }
          }
        }
      };
      removeMetadataRecursive(sanitizedData);

      console.log('[PolicyService] Making POST request to:', `/stores/${storeId}/policies/discounts?userId=${userId}`);
      console.log('[PolicyService] Sanitized payload:', JSON.stringify(sanitizedData, null, 2));

      const response = await api.post(`/stores/${storeId}/policies/discounts?userId=${userId}`, sanitizedData, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      const apiResponse = response.data;
      
      if (apiResponse.success) {
        console.log('[PolicyService] addDiscountPolicy success:', apiResponse.data);
        return apiResponse.data;
      } else {
        throw new Error(apiResponse.error || 'Failed to add discount policy');
      }
    } catch (error) {
      console.error('[PolicyService] addDiscountPolicy error:', error);
      
      // Check if it's a response error with a specific message
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      } else if (error.response?.data?.message) {
        throw new Error(error.response.data.message);
      } else if (error.message) {
        throw new Error(error.message);
      } else {
        throw new Error('Failed to add discount policy');
      }
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
      subDiscounts: [baseDiscount],
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

  // Create a fixed discount policy
  createFixedDiscountPolicy: (scope, scopeId, value) => ({
    type: 'FIXED',
    scope: scope.toUpperCase(),
    scopeId: scopeId,
    value: parseFloat(value),
    couponCode: null,
    condition: null,
    subDiscounts: [],
    combinationType: null
  }),

  // Create a targeted discount (enhanced version that supports both percentage and fixed)
  createTargetedDiscountPolicy: (discountType, scope, scopeId, value) => ({
    type: discountType.toUpperCase(), // PERCENTAGE or FIXED
    scope: scope.toUpperCase(),
    scopeId: scopeId,
    value: parseFloat(value),
    couponCode: null,
    condition: null,
    subDiscounts: [],
    combinationType: null
  }),

  // Enhanced composite discount creator with validation
  createEnhancedCompositeDiscount: (subDiscounts, combinationType, metadata = {}) => {
    // Validate sub-discounts
    if (!Array.isArray(subDiscounts) || subDiscounts.length < 2) {
      throw new Error('Composite discount requires at least 2 sub-discounts');
    }

    // Validate each sub-discount
    subDiscounts.forEach((subDiscount, index) => {
      if (!subDiscount.type || !subDiscount.value) {
        throw new Error(`Sub-discount ${index + 1} is missing required fields`);
      }
    });

    const validCombinationTypes = ['SUM', 'MAXIMUM'];
    const comb = (combinationType || 'SUM').toUpperCase();
    if (!validCombinationTypes.includes(comb)) {
      throw new Error(`Invalid combination type. Must be one of: ${validCombinationTypes.join(', ')}`);
    }

    return {
      type: 'COMPOSITE',
      scope: null,
      scopeId: null,
      value: 0,
      couponCode: null,
      condition: null,
      subDiscounts: subDiscounts,
      combinationType: comb,
      metadata: metadata // For UI display purposes
    };
  },

  // Helper function to validate discount policy
  validateDiscountPolicy: (policy) => {
    const requiredFields = ['type', 'value'];
    const missingFields = requiredFields.filter(field => !policy[field]);
    
    if (missingFields.length > 0) {
      throw new Error(`Missing required fields: ${missingFields.join(', ')}`);
    }

    // Type-specific validations
    switch (policy.type?.toUpperCase()) {
      case 'PERCENTAGE':
        if (policy.value < 0 || policy.value > 100) {
          throw new Error('Percentage discount must be between 0 and 100');
        }
        break;
      case 'FIXED':
        if (policy.value < 0) {
          throw new Error('Fixed discount cannot be negative');
        }
        break;
      case 'CONDITIONAL':
        if (!policy.condition) {
          throw new Error('Conditional discount requires a condition');
        }
        break;
      case 'COMPOSITE':
        if (!policy.subDiscounts || policy.subDiscounts.length < 2) {
          throw new Error('Composite discount requires at least 2 sub-discounts');
        }
        break;
    }

    return true;
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
  },

  // Enhanced condition creator for conditional discounts
  createEnhancedDiscountCondition: (conditionType, params = {}) => {
    const validConditions = {
      'BASKET_TOTAL_AT_LEAST': ['minTotal'],
      'PRODUCT_QUANTITY_AT_LEAST': ['productId', 'minQuantity'],
      'PRODUCT_CATEGORY_CONTAINS': ['category', 'minQuantity']
    };

    const type = conditionType.toUpperCase();
    
    if (!validConditions[type]) {
      throw new Error(`Invalid condition type: ${type}`);
    }

    const requiredParams = validConditions[type];
    const missingParams = requiredParams.filter(param => params[param] === undefined);
    
    if (missingParams.length > 0) {
      throw new Error(`Missing required parameters for ${type}: ${missingParams.join(', ')}`);
    }

    return {
      type: type,
      params: params,
      subConditions: [],
      logic: null
    };
  },

  // Helper to get discount types for UI
  getAvailableDiscountTypes: () => [
    { value: 'PERCENTAGE', label: 'Percentage Discount', icon: 'percent', description: 'Discount by percentage' },
    { value: 'FIXED', label: 'Fixed Amount', icon: 'money', description: 'Fixed dollar amount off' },
    { value: 'CONDITIONAL', label: 'Conditional Discount', icon: 'condition', description: 'Discount with conditions' }
  ],

  // Helper to get target types for UI
  getAvailableTargetTypes: () => [
    { value: 'STORE', label: 'Store-wide', description: 'Apply to entire store' },
    { value: 'PRODUCT', label: 'Specific Product', description: 'Apply to specific product' },
    { value: 'CATEGORY', label: 'Product Category', description: 'Apply to product category' }
  ],

  // Helper to get combination types for UI
  getAvailableCombinationTypes: () => [
    { value: 'SUM', label: 'Sum All Discounts', description: 'Add all discounts together' },
    { value: 'MAXIMUM', label: 'Maximum Discount', description: 'Apply the largest discount only' }
  ],

  // Helper to get condition types for UI
  getAvailableConditionTypes: () => [
    { value: 'BASKET_TOTAL_AT_LEAST', label: 'Minimum Basket Total', description: 'Basket total must be at least $X' },
    { value: 'PRODUCT_QUANTITY_AT_LEAST', label: 'Minimum Product Quantity', description: 'Must have at least X of specific product' },
    { value: 'PRODUCT_CATEGORY_CONTAINS', label: 'Category Contains Items', description: 'Must have items from specific category' }
  ]
};