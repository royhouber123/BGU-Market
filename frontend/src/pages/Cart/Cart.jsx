import React, { useState, useEffect } from "react";
import Header from "../../components/Header/Header";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import { Button, IconButton, Typography, Box, Container, Grid, Paper, Divider, Chip } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import { productService } from "../../services/productService";
import RemoveIcon from "@mui/icons-material/Remove";
import DeleteIcon from "@mui/icons-material/Delete";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import { Link, useNavigate } from "react-router-dom";
import { createPageUrl } from "../../utils";
import { useAuth } from "../../contexts/AuthContext";
import { fetchDiscountedPrice, getEffectivePrice, hasDiscount, calculateSavings, formatPrice } from "../../utils/priceUtils";
import './Cart.css';

export default function Cart() {
  const [loading, setLoading] = useState(true);
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [cartWithPrices, setCartWithPrices] = useState([]);
  const [loadingPrices, setLoadingPrices] = useState(false);
  const navigate = useNavigate();
  const { cart, refreshCart, isAuthenticated, updateCartQuantity, removeFromCart } = useAuth();

  useEffect(() => {
    loadCart();
  }, [isAuthenticated]);

  useEffect(() => {
    if (cart.length > 0) {
      fetchCartPrices();
    } else {
      setCartWithPrices([]);
    }
  }, [cart]);

  const loadCart = async () => {
    try {
      // The cart is automatically managed by AuthContext for both guest and authenticated users
      await refreshCart();
    } catch (error) {
      console.error("Error loading cart:", error);
    }
    setLoading(false);
  };

  const fetchCartPrices = async () => {
    setLoadingPrices(true);
    try {
      // Group cart items by store for batch processing
      const itemsByStore = {};
      cart.forEach(item => {
        if (!itemsByStore[item.storeId]) {
          itemsByStore[item.storeId] = [];
        }
        itemsByStore[item.storeId].push(item);
      });

      console.log("🔍 Cart grouped by store:", itemsByStore);

      // Process each store's items in batch
      const storePromises = Object.entries(itemsByStore).map(async ([storeId, items]) => {
        try {
          // Create quantity map for batch API call
          const productsToQuantity = {};
          items.forEach(item => {
            productsToQuantity[item.productId] = item.quantity;
          });

          console.log(`💰 Store ${storeId} - Products to quantity:`, productsToQuantity);

          // Get both original and discounted prices for the entire bag
          const [originalBagPrice, discount] = await Promise.all([
            productService.getBagPrice(storeId, productsToQuantity),
            productService.getBagDiscountPrice(storeId, productsToQuantity)
          ]);

          let discountedBagPrice = originalBagPrice - discount;

          console.log(`💰 Store ${storeId} - Original: $${originalBagPrice}, Discounted: $${discountedBagPrice}`);

          // Calculate the actual discount amount (not ratio)
          const totalSavingsForStore = originalBagPrice - discountedBagPrice;
          const hasStoreDiscount = totalSavingsForStore > 0;

          console.log(`💸 Store ${storeId} - Total Savings: $${totalSavingsForStore}, Has Discount: ${hasStoreDiscount}`);

          // Calculate how much each item contributed to the original total
          const storeOriginalTotal = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

          // Apply proportional discount to each item
          return items.map(item => {
            const itemOriginalTotal = item.price * item.quantity;
            
            // Calculate this item's share of the total discount
            const itemProportion = storeOriginalTotal > 0 ? itemOriginalTotal / storeOriginalTotal : 0;
            const itemSavings = totalSavingsForStore * itemProportion;
            const itemDiscountedTotal = itemOriginalTotal - itemSavings;
            const itemDiscountedPrice = itemDiscountedTotal / item.quantity;
            
            const result = {
              ...item,
              originalPrice: item.price,
              discountedPrice: hasStoreDiscount ? itemDiscountedPrice : null,
              effectivePrice: hasStoreDiscount ? itemDiscountedPrice : item.price,
              hasDiscount: hasStoreDiscount,
              savings: hasStoreDiscount ? (itemSavings / item.quantity) : 0 // Savings per unit
            };

            console.log(`🛍️ Item ${item.title}: $${item.price} → $${result.effectivePrice} (Savings: $${result.savings} per unit)`);
            return result;
          });
        } catch (error) {
          console.warn(`Failed to get batch discounts for store ${storeId}:`, error);
          // Fallback to original prices if batch fails
          return items.map(item => ({
            ...item,
            originalPrice: item.price,
            discountedPrice: null,
            effectivePrice: item.price,
            hasDiscount: false,
            savings: 0
          }));
        }
      });

      // Wait for all stores to complete and flatten results
      const results = await Promise.all(storePromises);
      const cartItemsWithPrices = results.flat();

      console.log("✅ Final cart with prices:", cartItemsWithPrices);
      setCartWithPrices(cartItemsWithPrices);
    } catch (error) {
      console.error("Error fetching cart prices:", error);
      // Fallback to original cart items
      setCartWithPrices(cart.map(item => ({
        ...item,
        originalPrice: item.price,
        discountedPrice: null,
        effectivePrice: item.price,
        hasDiscount: false,
        savings: 0
      })));
    }
    setLoadingPrices(false);
  };

  const updateQuantity = async (productId, change) => {
    try {
      // Find the current item in cart
      const currentItem = cart.find(item => item.productId === productId);
      if (!currentItem) {
        console.error('Product not found in cart:', productId);
        return;
      }

      // Calculate new quantity (ensure it's at least 1)
      const newQuantity = Math.max(1, currentItem.quantity + change);

      // Don't update if quantity is the same
      if (newQuantity === currentItem.quantity) return;

      // Use context method which handles both guest and authenticated users
      await updateCartQuantity(productId, newQuantity);
    } catch (error) {
      console.error('Error updating quantity:', error);
    }
  };

  const removeItem = async (productId) => {
    try {
      // Use context method which handles both guest and authenticated users
      await removeFromCart(productId);
    } catch (error) {
      console.error('Error removing item from cart:', error);
    }
  };

  const calculateTotal = () => {
    return cartWithPrices.reduce((total, item) => total + (item.effectivePrice * item.quantity), 0);
  };

  const calculateTotalSavings = () => {
    return cartWithPrices.reduce((total, item) => total + (item.savings * item.quantity), 0);
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

  const displayCart = cartWithPrices.length > 0 ? cartWithPrices : cart;

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
                  {displayCart.map((item) => (
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

                        {/* Price Display with Discount Info */}
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                          {item.hasDiscount ? (
                            <>
                              <Typography variant="body1" fontWeight="medium" color="primary">
                                ${formatPrice(item.effectivePrice * item.quantity)}
                              </Typography>
                              <Typography
                                variant="body2"
                                sx={{ textDecoration: 'line-through', color: 'text.secondary' }}
                              >
                                ${formatPrice(item.originalPrice * item.quantity)}
                              </Typography>
                              <Chip
                                label={`Save $${formatPrice(item.savings * item.quantity)}`}
                                color="success"
                                size="small"
                                sx={{ height: 20, fontSize: '0.7rem' }}
                              />
                            </>
                          ) : (
                            <Typography variant="body1" fontWeight="medium" color="primary">
                              ${formatPrice((item.effectivePrice || item.price) * item.quantity)}
                            </Typography>
                          )}
                          {loadingPrices && (
                            <Box
                              sx={{
                                display: 'inline-block',
                                width: 12,
                                height: 12,
                                border: '1px solid currentColor',
                                borderTop: '1px solid transparent',
                                borderRadius: '50%',
                                animation: 'spin 1s linear infinite',
                                '@keyframes spin': {
                                  '0%': { transform: 'rotate(0deg)' },
                                  '100%': { transform: 'rotate(360deg)' }
                                }
                              }}
                            />
                          )}
                        </Box>
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
                  
                  {/* Original Price */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2" color="text.secondary">
                      Original Price
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                      ${formatPrice(calculateTotal() + calculateTotalSavings())}
                    </Typography>
                  </Box>

                  {/* Discount (if any) */}
                  {calculateTotalSavings() > 0 && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2" color="success.main">
                        Discount
                      </Typography>
                      <Typography variant="body1" fontWeight="medium" color="success.main">
                        -${formatPrice(calculateTotalSavings())}
                      </Typography>
                    </Box>
                  )}

                  {/* Shipping */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                      Shipping
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                      Free
                    </Typography>
                  </Box>

                  <Divider sx={{ mb: 2 }} />

                  {/* Final Total */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="h6" component="h2" fontWeight="medium" color="text.primary">
                      Total
                    </Typography>
                    <Typography variant="h6" component="h2" fontWeight="medium" color="primary">
                      ${formatPrice(calculateTotal())}
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
