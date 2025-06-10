import axios from 'axios';
import { useEffect } from 'react';

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

const notificationService = {
  // Fetch all notifications for the current user
  getNotifications: async (userId) => {
    try {
      const response = await api.get(`/notifications/${userId}`);
      // Backend returns a list of notifications
      return response.data;
    } catch (error) {
      console.error('Get notifications error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Mark a notification as read for the current user
  markAsRead: async (userId, notificationId) => {
    try {
      await api.post(`/notifications/${userId}/read/${notificationId}`);
      return true;
    } catch (error) {
      console.error('Mark notification as read error:', error);
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      throw error;
    }
  },

  // Delete a notification for the current user
  deleteNotification: async (userId, notificationId) => {
    await api.delete(`/notifications/${userId}/${notificationId}`);
  },
};

export default notificationService;

