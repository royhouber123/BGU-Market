import React, { createContext, useContext, useState, useEffect } from 'react';
import userService from '../services/userService';

const AuthContext = createContext();

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

  // Initialize auth state
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        const token = localStorage.getItem('token');
        if (token) {
          const user = await userService.getProfile();
          setCurrentUser(user);
          setIsAuthenticated(true);
          
          // Load cart data
          await refreshCart();
        }
      } catch (error) {
        console.error('Auth initialization error:', error);
        // Clear invalid token
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  // Function to refresh cart data
  const refreshCart = async () => {
    try {
      if (userService.isAuthenticated()) {
        console.log('[AuthContext] Refreshing cart...');
        // Sync cart from backend and get the synced cart data
        const syncedCart = await userService.syncCartFromBackend();
        console.log('[AuthContext] Synced cart:', syncedCart);
        setCart(syncedCart || []);
      } else {
        console.log('[AuthContext] User not authenticated, clearing cart');
        setCart([]);
      }
    } catch (error) {
      console.error('[AuthContext] Error refreshing cart:', error);
      // Fallback to local storage cart
      const localUser = userService.getCurrentUser();
      const fallbackCart = localUser?.cart || [];
      console.log('[AuthContext] Using fallback cart:', fallbackCart);
      setCart(fallbackCart);
    }
  };

  const login = async (username, password) => {
    try {
      const user = await userService.login(username, password);
      setCurrentUser(user);
      setIsAuthenticated(true);
      await refreshCart();
      return user;
    } catch (error) {
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const user = await userService.register(userData);
      setCurrentUser(user);
      setIsAuthenticated(true);
      await refreshCart();
      return user;
    } catch (error) {
      throw error;
    }
  };

  const logout = async () => {
    try {
      await userService.logout();
      setCurrentUser(null);
      setIsAuthenticated(false);
      setCart([]);
    } catch (error) {
      throw error;
    }
  };

  const value = {
    currentUser,
    isAuthenticated,
    loading,
    cart,
    refreshCart,
    login,
    register,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
