import axios from 'axios';
import { productService } from './productService';

// Creating an axios instance with default configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

const guestService = {
  // Generate a unique guest UUID
  generateGuestUUID: () => {
    return `guest-${crypto.randomUUID()}`;
  },

  // Register guest immediately and get token
  registerGuest: async () => {
    try {
      const guestUUID = guestService.generateGuestUUID();
      
      // Register guest user on backend
      const registerResponse = await api.post('/users/register/guest', {
        username: guestUUID
      });

      if (!registerResponse.data.success) {
        throw new Error('Failed to register guest user');
      }

      // Get authentication token for guest
      const loginResponse = await api.post('/auth/guest-login', {
        guestId: guestUUID
      });

      if (!loginResponse.data.success) {
        throw new Error('Failed to authenticate guest user');
      }

      const token = loginResponse.data.data.token;
      
      // Set auth headers for all subsequent requests
      api.defaults.headers['Authorization'] = `Bearer ${token}`;
      
      return { guestUUID, token };
    } catch (error) {
      console.error('Error registering guest:', error);
      throw error;
    }
  },

  // Add item to cart (uses backend cart API)
  addToCart: async (product, quantity = 1) => {
    try {
      const response = await api.post('/users/cart/add', {
        storeId: product.storeId,
        productName: product.id,
        quantity: quantity
      });

      return response.data;
    } catch (error) {
      console.error('Error adding to guest cart:', error);
      throw error;
    }
  },

  // Update cart quantity
  updateCartQuantity: async (productId, newQuantity, currentQuantity, storeId) => {
    try {
      const difference = newQuantity - currentQuantity;
      
      if (difference > 0) {
        // Add more items
        await api.post('/users/cart/add', {
          storeId: storeId,
          productName: productId,
          quantity: difference
        });
      } else if (difference < 0) {
        // Remove items
        await api.post('/users/cart/remove', {
          storeId: storeId,
          productName: productId,
          quantity: Math.abs(difference)
        });
      }
      // If difference is 0, no change needed
    } catch (error) {
      console.error('Error updating guest cart quantity:', error);
      throw error;
    }
  },

  // Remove item from cart
  removeFromCart: async (productId, quantity, storeId) => {
    try {
      await api.post('/users/cart/remove', {
        storeId: storeId,
        productName: productId,
        quantity: quantity
      });
    } catch (error) {
      console.error('Error removing from guest cart:', error);
      throw error;
    }
  },

  // Get cart from backend
  getCart: async () => {
    try {
      const response = await api.get('/users/cart');
      if (response.data.success) {
        // Convert backend cart format to frontend format
        const backendCart = response.data.data;
        const cart = [];
        
        // Handle the backend cart structure - it uses 'storeBags' which is a map/object
        if (backendCart && backendCart.storeBags) {
          // Convert the storeBags object to an array of storeBag values
          const storeBagsMap = backendCart.storeBags;
          for (const [storeId, storeBag] of Object.entries(storeBagsMap)) {
            const products = storeBag.products || storeBag.productQuantities || {};
            
            for (const [productId, quantity] of Object.entries(products)) {
              // Try to fetch product details
              try {
                const productDetails = await productService.getListing(productId);
                if (productDetails) {
                  cart.push({
                    productId: productId,
                    storeId: storeId,
                    quantity: quantity,
                    title: productDetails.title,
                    price: productDetails.price,
                    image: productDetails.images?.[0] || ''
                  });
                } else {
                  // Fallback if product details can't be fetched
                  cart.push({
                    productId: productId,
                    storeId: storeId,
                    quantity: quantity,
                    title: `Product ${productId}`,
                    price: 0,
                    image: ''
                  });
                }
              } catch (error) {
                console.warn(`Could not fetch details for product ${productId}:`, error);
                // Fallback if product details can't be fetched
                cart.push({
                  productId: productId,
                  storeId: storeId,
                  quantity: quantity,
                  title: `Product ${productId}`,
                  price: 0,
                  image: ''
                });
              }
            }
          }
        }
        
        return cart;
      }
      return [];
    } catch (error) {
      console.error('Error getting guest cart:', error);
      return [];
    }
  },

  // Complete guest checkout with email conversion
  completeGuestCheckout: async (email, paymentDetails, shippingAddress) => {
    try {
      // First, register the guest with email (this changes the username from UUID to email)
      const registerResponse = await api.post('/users/register/guest-checkout', { 
        email: email 
      });

      if (!registerResponse.data.success) {
        throw new Error('Failed to register guest with email');
      }

      console.log('Sending purchase request with:', {
        paymentDetails: paymentDetails,
        shippingAddress: shippingAddress
      });

      // Execute the purchase using existing cart
      const purchaseResponse = await api.post('/purchases/execute', {
        paymentDetails: paymentDetails,
        shippingAddress: shippingAddress
      });

      if (!purchaseResponse.data.success) {
        throw new Error('Purchase failed');
      }

      return {
        success: true,
        message: 'Purchase completed successfully',
        details: purchaseResponse.data.data
      };

    } catch (error) {
      console.error('Error in guest checkout:', error);
      throw error;
    }
  },

  // Check if product can be interacted with by guest (only regular products)
  canGuestInteract: (product) => {
    const purchaseType = product.purchaseType || 'REGULAR';
    return purchaseType === 'REGULAR';
  },

  // Set authentication token (used when initializing guest)
  setAuthToken: (token) => {
    api.defaults.headers['Authorization'] = `Bearer ${token}`;
  },

  // Clear authentication (used when logging in as regular user)
  clearAuth: () => {
    delete api.defaults.headers['Authorization'];
  }
};

export default guestService; 