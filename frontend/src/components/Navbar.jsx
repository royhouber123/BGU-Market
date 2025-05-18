import React from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// Material-UI imports
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Button, 
  Box, 
  Container
} from '@mui/material';

const Navbar = () => {
  const { currentUser, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <AppBar position="static" color="default" elevation={1} sx={{ backgroundColor: 'white' }}>
      <Container>
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Typography 
            variant="h6" 
            component={RouterLink} 
            to="/"
            sx={{ 
              textDecoration: 'none',
              color: 'inherit',
              fontWeight: 'bold',
              backgroundImage: 'linear-gradient(to right, #2563eb, #9333ea)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent'
            }}
          >
            BGU Market
          </Typography>
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {isAuthenticated ? (
              <>
                <Typography variant="body2" color="text.secondary" sx={{ mr: 2 }}>
                  Welcome, {currentUser?.userName || 'User'}
                </Typography>
                <Button 
                  component={RouterLink} 
                  to="/dashboard"
                  color="primary"
                  sx={{ textTransform: 'none' }}
                >
                  Dashboard
                </Button>
                <Button 
                  onClick={handleLogout} 
                  color="error"
                  variant="outlined"
                  size="small"
                >
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Button 
                  component={RouterLink} 
                  to="/login"
                  color="primary"
                  variant="text"
                >
                  Login
                </Button>
                <Button 
                  component={RouterLink} 
                  to="/register"
                  color="primary"
                  variant="contained"
                >
                  Register
                </Button>
              </>
            )}
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
};

export default Navbar;
