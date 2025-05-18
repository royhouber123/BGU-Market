import React, { useState, useEffect } from "react";
import Header from "../components/Header.jsx";
import AuthDialog from "../components/AuthDialog.jsx";
import { Button, IconButton, Typography, Box, Container, Grid, Paper, Divider } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import RemoveIcon from "@mui/icons-material/Remove";
import DeleteIcon from "@mui/icons-material/Delete";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import userService from "../services/userService";
import { Link, useNavigate } from "react-router-dom";
import { createPageUrl } from "../utils";

export default function Cart() {
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = async () => {
    try {
      if (userService.isAuthenticated()) {
        const user = await userService.getProfile();
        setCart(user.cart || []);
      } else {
        // If not authenticated, use empty cart or redirect to login
        setCart([]);
      }
    } catch (error) {
      console.error("Error loading cart:", error);
    }
    setLoading(false);
  };

  const updateCart = async (newCart) => {
    try {
      // Use userService to update the cart data
      await userService.updateUserData({ cart: newCart });
      setCart(newCart);
    } catch (error) {
      console.error("Error updating cart:", error);
    }
  };

  const updateQuantity = (productId, change) => {
    const newCart = cart.map(item => {
      if (item.productId === productId) {
        const newQuantity = Math.max(1, item.quantity + change);
        return { ...item, quantity: newQuantity };
      }
      return item;
    });
    updateCart(newCart);
  };

  const removeItem = (productId) => {
    const newCart = cart.filter(item => item.productId !== productId);
    updateCart(newCart);
  };

  const calculateTotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  const handleCheckout = () => {
    navigate(createPageUrl("Checkout"));
  };

  if (loading) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Header />
        <Container sx={{ py: 4 }}>
          <Box sx={{ width: '100%' }}>
            <Box sx={{ height: 32, bgcolor: 'grey.200', borderRadius: 1, width: '25%', mb: 4 }} />
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {[1, 2, 3].map(i => (
                <Box key={i} sx={{ height: 96, bgcolor: 'grey.200', borderRadius: 1 }} />
              ))}
            </Box>
          </Box>
        </Container>
        {showAuthDialog && <AuthDialog open={showAuthDialog} onClose={() => setShowAuthDialog(false)} />}
      </Box>
    );
  }

  return (  
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Header />
      <Container sx={{ py: 4 }}>
        <Typography variant="h4" component="h1" fontWeight="bold" gutterBottom>
          Your Cart
        </Typography>

        {cart.length > 0 ? (
          <Grid container spacing={3}>
            <Grid item xs={12} md={8}>
              <Paper sx={{ p: 3, borderRadius: 2 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                  {cart.map((item) => (
                    <Box
                      key={item.productId}
                      sx={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        borderBottom: '1px solid', 
                        borderColor: 'divider',
                        py: 2,
                        '&:last-child': { borderBottom: 0 }
                      }}
                    >
                      <Box
                        component="img"
                        src={item.image}
                        alt={item.title}
                        sx={{ 
                          height: 80, 
                          width: 80, 
                          objectFit: 'cover', 
                          borderRadius: 1,
                          mr: 2,
                          flexShrink: 0
                        }}
                      />
                      <Box sx={{ flexGrow: 1 }}>
                        <Typography variant="subtitle1" fontWeight="medium" color="text.primary">
                          {item.title}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Quantity: {item.quantity}
                        </Typography>
                        <Typography variant="body1" fontWeight="medium" color="primary">
                          ${(item.price * item.quantity).toFixed(2)}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <IconButton
                          onClick={() => updateQuantity(item.productId, -1)}
                          size="small"
                          sx={{ mr: 1 }}
                        >
                          <RemoveIcon fontSize="small" />
                        </IconButton>
                        <Typography variant="body1" fontWeight="medium">
                          {item.quantity}
                        </Typography>
                        <IconButton
                          onClick={() => updateQuantity(item.productId, 1)}
                          size="small"
                          sx={{ ml: 1 }}
                        >
                          <AddIcon fontSize="small" />
                        </IconButton>
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <IconButton
                          onClick={() => removeItem(item.productId)}
                          color="error"
                          size="small"
                        >
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Box>
                    </Box>
                  ))}
                </Box>
              </Paper>
            </Grid>

            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 3, borderRadius: 2 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                  <Typography variant="h6" component="h2" fontWeight="medium" color="text.primary" gutterBottom>
                    Order Summary
                  </Typography>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                      Subtotal
                    </Typography>
                    <Typography variant="body1" fontWeight="medium" color="primary">
                      ${(calculateTotal()).toFixed(2)}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                      Shipping
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                      Free
                    </Typography>
                  </Box>
                  <Divider sx={{ mb: 2 }} />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="h6" component="h2" fontWeight="medium" color="text.primary">
                      Total
                    </Typography>
                    <Typography variant="h6" component="h2" fontWeight="medium" color="primary">
                      ${(calculateTotal()).toFixed(2)}
                    </Typography>
                  </Box>
                  <Button 
                    variant="contained" 
                    color="primary" 
                    sx={{ mt: 2, width: '100%' }}
                    onClick={handleCheckout}
                  >
                    Proceed to Checkout
                  </Button>
                </Box>
              </Paper>
            </Grid>
          </Grid>
        ) : (
          <Box 
            sx={{ 
              textAlign: 'center', 
              py: 8,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center'
            }}
          >
            <Box 
              sx={{ 
                display: 'inline-flex', 
                alignItems: 'center', 
                justifyContent: 'center', 
                width: 64, 
                height: 64, 
                bgcolor: 'grey.100', 
                borderRadius: '50%',
                mb: 2
              }}
            >
              <ShoppingCartIcon sx={{ fontSize: 32, color: 'text.secondary' }} />
            </Box>
            <Typography variant="h5" component="h2" fontWeight="medium" color="text.secondary" gutterBottom>
              Your cart is empty
            </Typography>
            <Typography color="text.secondary" sx={{ mb: 3 }}>
              Looks like you haven't added any items to your cart yet.
            </Typography>
            <Button component={Link} to="/" variant="contained" color="primary">
              Browse products
            </Button>
          </Box>
        )}
      </Container>
    </Box>
  );
}
