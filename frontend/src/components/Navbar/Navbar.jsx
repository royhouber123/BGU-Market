import React from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './Navbar.css';

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
    <AppBar position="static" color="default" elevation={1} className="navbar-appbar">
      <Container>
        <Toolbar className="navbar-toolbar">
          <Typography
            variant="h6"
            component={RouterLink}
            to="/"
            className="navbar-logo"
          >
            BGU Market
          </Typography>

          <Box className="navbar-actions">
            {isAuthenticated ? (
              <>
                <Typography variant="body2" color="text.secondary" className="navbar-welcome">
                  Welcome, {currentUser?.userName || 'User'}
                </Typography>
                <Button
                  component={RouterLink}
                  to="/dashboard"
                  color="primary"
                  className="navbar-dashboard-btn"
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
