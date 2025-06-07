import React, { createContext, useContext, useState, useEffect } from 'react';
import userService from '../services/userService';
import guestService from '../services/guestService';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [cart, setCart] = useState([]);
  const [isGuest, setIsGuest] = useState(false);
  const [guestSession, setGuestSession] = useState(null); // Store guest UUID and token

  // Initialize auth state
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const token = localStorage.getItem('token');
        
        if (token) {
          // Regular authenticated user
          const user = await userService.getProfile();
          setCurrentUser(user);
          setIsAuthenticated(true);
          setIsGuest(false);
          await refreshCart();
        } else {
          // New visitor - automatically register as guest
          await initializeAsGuest();
        }
      } catch (error) {
        console.error('Auth initialization error:', error);
        // Clear invalid tokens and initialize as guest
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        await initializeAsGuest();
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  // Initialize as guest user - automatically register on backend
  const initializeAsGuest = async () => {
    try {
      console.log('[AuthContext] Initializing as guest...');
      const session = await guestService.registerGuest();
      setGuestSession(session);
      setIsGuest(true);
      setIsAuthenticated(false);
      await refreshGuestCart();
      console.log('[AuthContext] Guest initialized successfully with UUID:', session.guestUUID);
    } catch (error) {
      console.error('[AuthContext] Error initializing guest:', error);
      setCart([]);
      setIsGuest(true);
      setIsAuthenticated(false);
    }
  };

  // Function to refresh cart data for authenticated users
  const refreshCart = async () => {
    try {
      if (userService.isAuthenticated()) {
        console.log('[AuthContext] Refreshing authenticated user cart...');
        const syncedCart = await userService.syncCartFromBackend();
        console.log('[AuthContext] Synced cart:', syncedCart);
        setCart(syncedCart || []);
      } else {
        console.log('[AuthContext] User not authenticated, clearing cart');
        setCart([]);
      }
    } catch (error) {
      console.error('[AuthContext] Error refreshing cart:', error);
      const localUser = userService.getCurrentUser();
      const fallbackCart = localUser?.cart || [];
      console.log('[AuthContext] Using fallback cart:', fallbackCart);
      setCart(fallbackCart);
    }
  };

  // Function to refresh guest cart
  const refreshGuestCart = async () => {
    try {
      console.log('[AuthContext] Refreshing guest cart...');
      const guestCart = await guestService.getCart();
      console.log('[AuthContext] Guest cart:', guestCart);
      setCart(guestCart || []);
    } catch (error) {
      console.error('[AuthContext] Error refreshing guest cart:', error);
      setCart([]);
    }
  };

  // Login function
  const login = async (username, password) => {
    try {
      console.log('[AuthContext] Logging in user:', username);
      
      // Clear guest authentication before login (do NOT transfer guest cart)
      if (isGuest) {
        guestService.clearAuth();
      }

      const user = await userService.login(username, password);
      setCurrentUser(user);
      setIsAuthenticated(true);
      setIsGuest(false);
      setGuestSession(null);
      
      // Fetch the user's existing cart from server (ignore guest cart)
      console.log('[AuthContext] Fetching user cart from server...');
      await refreshCart();
      return true;
    } catch (error) {
      console.error('[AuthContext] Login error:', error);
      throw error;
    }
  };

  // Register function for guest users with cart transfer
  const registerWithCart = async (userData) => {
    try {
      console.log('[AuthContext] Registering user with guest cart transfer:', userData.username);
      
      // Get current guest cart before registration
      let guestCart = [];
      if (isGuest && guestSession) {
        try {
          guestCart = await guestService.getCart();
          console.log('[AuthContext] Guest cart to transfer during registration:', guestCart);
        } catch (error) {
          console.error('[AuthContext] Error getting guest cart for registration:', error);
        }
      }

      // Register the new user
      const { authService } = await import('../services/authService');
      const response = await authService.register(userData);

      // Transfer guest cart to the newly registered user (WITHOUT logging them in)
      if (guestCart.length > 0) {
        try {
          console.log('[AuthContext] Transferring guest cart to newly registered user account...');
          
          // Temporarily log in to transfer the cart, then log out
          const tempUser = await userService.login(userData.username, userData.password);
          
          // Transfer each cart item
          for (const item of guestCart) {
            await userService.addProductToCart(
              item.storeId,
              item.productId,
              item.quantity
            );
          }
          
          // Log out the temporary session
          await userService.logout();
          
          console.log('[AuthContext] Guest cart transferred successfully to new user account');
        } catch (error) {
          console.error('[AuthContext] Error transferring guest cart to new user:', error);
        }
      }

      // Initialize a new guest session after registration (user remains logged out)
      await initializeAsGuest();
      
      return response;
    } catch (error) {
      console.error('[AuthContext] Registration with cart error:', error);
      throw error;
    }
  };

  // Logout function
  const logout = async () => {
    try {
      await userService.logout();
      setCurrentUser(null);
      setIsAuthenticated(false);
      // Initialize new guest session after logout
      await initializeAsGuest();
    } catch (error) {
      console.error('[AuthContext] Logout error:', error);
      setCurrentUser(null);
      setIsAuthenticated(false);
      await initializeAsGuest();
    }
  };

  // Add to cart function (works for both guest and authenticated users)
  const addToCart = async (product, quantity = 1) => {
    try {
      if (isAuthenticated) {
        // Authenticated user - use userService
        await userService.addProductToCart(
          product.storeId || "1",
          product.id,
          quantity
        );
        await refreshCart();
      } else {
        // Guest user - use guestService
        await guestService.addToCart(product, quantity);
        await refreshGuestCart();
      }
    } catch (error) {
      console.error('[AuthContext] Error adding to cart:', error);
      throw error;
    }
  };

  // Update cart quantity function
  const updateCartQuantity = async (productId, newQuantity) => {
    try {
      if (isAuthenticated) {
        // For authenticated users, calculate the difference
        const currentItem = cart.find(item => item.productId === productId);
        if (currentItem) {
          const difference = newQuantity - currentItem.quantity;
          if (difference > 0) {
            await userService.addProductToCart(
              currentItem.storeId,
              productId,
              difference
            );
          } else if (difference < 0) {
            await userService.removeFromCart(
              currentItem.storeId,
              productId,
              Math.abs(difference)
            );
          }
          await refreshCart();
        }
      } else {
        // Guest user - use guestService
        const currentItem = cart.find(item => item.productId === productId);
        if (currentItem) {
          await guestService.updateCartQuantity(
            productId, 
            newQuantity, 
            currentItem.quantity, 
            currentItem.storeId
          );
          await refreshGuestCart();
        }
      }
    } catch (error) {
      console.error('[AuthContext] Error updating cart quantity:', error);
      throw error;
    }
  };

  // Remove from cart function
  const removeFromCart = async (productId) => {
    try {
      if (isAuthenticated) {
        const item = cart.find(item => item.productId === productId);
        if (item) {
          await userService.removeFromCart(
            item.storeId,
            productId,
            item.quantity
          );
          await refreshCart();
        }
      } else {
        // Guest user - use guestService
        const item = cart.find(item => item.productId === productId);
        if (item) {
          await guestService.removeFromCart(
            productId,
            item.quantity,
            item.storeId
          );
          await refreshGuestCart();
        }
      }
    } catch (error) {
      console.error('[AuthContext] Error removing from cart:', error);
      throw error;
    }
  };

  const value = {
    currentUser,
    isAuthenticated,
    loading,
    cart,
    isGuest,
    guestSession,
    login,
    logout,
    refreshCart: isAuthenticated ? refreshCart : refreshGuestCart,
    addToCart,
    updateCartQuantity,
    removeFromCart,
    guestService, // Expose guest service for checkout
    registerWithCart
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
