import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Home from './pages/Home';
import Cart from './pages/Cart';
import ProductDetail from './pages/ProductDetail';
import SearchResults from './pages/SearchResults';
import WatchList from './pages/WatchList';
import Checkout from './pages/Checkout';
import OrderConfirmation from './pages/OrderConfirmation';
import { AuthProvider } from './contexts/AuthContext';

// Material UI Theme Provider
import { ThemeProvider, createTheme } from '@mui/material/styles';

// Create theme with BGU Market colors
const theme = createTheme({
  palette: {
    primary: {
      main: '#2563eb',
    },
    secondary: {
      main: '#9333ea',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <AuthProvider>
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
          <Routes>
            {/* Authentication routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            
            {/* Public routes */}
            <Route path="/" element={<Home />} /> {/* Home page */}
            <Route path="/product" element={<ProductDetail />} />
            <Route path="/search" element={<SearchResults />} />
            
            {/* Protected routes that require authentication */}
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/cart" element={<Cart />} />
            <Route path="/watchlist" element={<WatchList />} />
            <Route path="/checkout" element={<Checkout />} />
            <Route path="/order-confirmation" element={<OrderConfirmation />} />
          </Routes>
        </div>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
