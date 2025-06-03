import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// Material-UI imports
import { CircularProgress, Box, Typography } from '@mui/material';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  // If loading, show a loading spinner
  if (loading) {
    return (
      <Box 
        sx={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100px',
          flexDirection: 'column',
          gap: 2
        }}
      >
        <CircularProgress size={24} />
        <Typography variant="body2" color="text.secondary">
          Verifying secure authentication...
        </Typography>
      </Box>
    );
  }

  // If not authenticated, redirect to login page
  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  // If authenticated, render the children components
  return children;
};

export default ProtectedRoute;
