import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './pages/Login/Login';
import Register from './pages/Register/Register';
import Dashboard from './pages/Dashboard/Dashboard';
import Cart from './pages/Cart/Cart';
import ProductDetail from './pages/ProductDetail/ProductDetail';
import SearchResults from './pages/SearchResults/SearchResults';
import Profile from './pages/Profile/Profile';
import StoreManagement from './pages/StoreManagement/StoreManagement';
import Checkout from './pages/Checkout/Checkout';
import OrderConfirmation from './pages/OrderConfirmation/OrderConfirmation';
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
            <Route path="/" element={<Dashboard />} /> {/* Dashboard serves as home page */}
            <Route path="/dashboard" element={<Dashboard />} /> {/* Keep dashboard route for compatibility */}
            <Route path="/product/:id" element={<ProductDetail />} />
            <Route path="/search" element={<SearchResults />} />

            {/* Protected routes that require authentication */}
            <Route path="/cart" element={<Cart />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/store/:storeId/manage" element={<StoreManagement />} />
            <Route path="/checkout" element={<Checkout />} />
            <Route path="/order-confirmation" element={<OrderConfirmation />} />
          </Routes>
        </div>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
