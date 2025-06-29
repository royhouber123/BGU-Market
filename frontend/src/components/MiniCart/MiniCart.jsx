import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { createPageUrl } from "../../utils";
import { fetchDiscountedPrice, getEffectivePrice, hasDiscount, calculateSavings, formatPrice } from "../../utils/priceUtils";
import { productService } from "../../services/productService";
import "./MiniCart.css";

// Material-UI imports
import {
  Popover,
  Button,
  Box,
  Typography,
  Badge,
  IconButton,
  Divider,
  Paper,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Chip
} from "@mui/material";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";

export default function MiniCart({ cart = [], onClose }) {
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState(null);
  const [cartWithPrices, setCartWithPrices] = useState([]);
  const [loadingPrices, setLoadingPrices] = useState(false);

  useEffect(() => {
    if (cart.length > 0) {
      fetchCartPrices();
    } else {
      setCartWithPrices([]);
    }
  }, [cart]);

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

      console.log("🔍 MiniCart grouped by store:", itemsByStore);

      // Process each store's items in batch
      const storePromises = Object.entries(itemsByStore).map(async ([storeId, items]) => {
        try {
          // Create quantity map for batch API call
          const productsToQuantity = {};
          items.forEach(item => {
            productsToQuantity[item.productId] = item.quantity;
          });

          console.log(`💰 MiniCart Store ${storeId} - Products to quantity:`, productsToQuantity);

          // Get both original and discounted prices for the entire bag
          const [originalBagPrice, discount] = await Promise.all([
            productService.getBagPrice(storeId, productsToQuantity),
            productService.getBagDiscountPrice(storeId, productsToQuantity)
          ]);

          let discountedBagPrice = originalBagPrice - discount;

          console.log(`💰 MiniCart Store ${storeId} - Original: $${originalBagPrice}, Discounted: $${discountedBagPrice}`);

          // Calculate discount ratio
          const discountRatio = originalBagPrice > 0 ? discountedBagPrice / originalBagPrice : 1;
          const hasStoreDiscount = discountRatio < 1;

          console.log(`💸 MiniCart Store ${storeId} - Discount Ratio: ${discountRatio}, Has Discount: ${hasStoreDiscount}`);

          // Apply proportional discount to each item
          return items.map(item => {
            // Calculate discounted price for this item
            const itemDiscountedPrice = item.price * discountRatio;
            const itemSavingsPerUnit = item.price - itemDiscountedPrice;
            
            const result = {
              ...item,
              originalPrice: item.price,
              discountedPrice: hasStoreDiscount ? itemDiscountedPrice : null,
              effectivePrice: hasStoreDiscount ? itemDiscountedPrice : item.price,
              hasDiscount: hasStoreDiscount,
              savings: hasStoreDiscount ? itemSavingsPerUnit : 0 // Savings per unit
            };

            console.log(`🛍️ MiniCart Item ${item.title}: $${item.price} → $${result.effectivePrice} (Savings: $${result.savings} per unit)`);
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

      console.log("✅ MiniCart final cart with prices:", cartItemsWithPrices);
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

  const displayCart = cartWithPrices.length > 0 ? cartWithPrices : cart;
  const totalItems = displayCart.reduce((sum, item) => sum + item.quantity, 0);
  const cartTotal = displayCart.reduce(
    (sum, item) => sum + (item.effectivePrice || item.price) * item.quantity,
    0
  );

  // Show at most 3 items in mini cart
  const displayItems = displayCart.slice(0, 3);
  const hasMoreItems = displayCart.length > 3;

  const handleViewCart = () => {
    handleClose();
    navigate(createPageUrl("Cart"));
  };

  const handleCheckout = () => {
    handleClose();
    navigate(createPageUrl("Checkout"));
  };

  const handleOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
    if (onClose) {
      onClose();
    }
  };

  const open = Boolean(anchorEl);

  // If onClose is provided, show as a modal/dialog instead of a popover
  if (onClose) {
    return (
      <Box
        sx={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          bgcolor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999
        }}
        onClick={handleClose}
      >
        <Paper
          sx={{
            width: 400,
            maxHeight: '80vh',
            overflow: 'auto',
            borderRadius: 2
          }}
          onClick={(e) => e.stopPropagation()}
        >
          <Box className="mini-cart-header">
            <Typography variant="subtitle1" fontWeight="medium">
              Your Cart ({totalItems} items)
            </Typography>
          </Box>

          {displayCart.length === 0 ? (
            <Box className="mini-cart-empty">
              <Typography color="text.secondary">Your cart is empty</Typography>
            </Box>
          ) : (
            <>
              <List className="mini-cart-list">
                {displayItems.map((item) => (
                  <ListItem key={item.productId} divider className="mini-cart-item">
                    <ListItemAvatar>
                      <Avatar
                        src={item.image}
                        alt={item.title}
                        variant="rounded"
                        className="mini-cart-avatar"
                      />
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Typography variant="body2" noWrap fontWeight="medium">
                          {item.title}
                        </Typography>
                      }
                      secondary={
                        <Box className="mini-cart-item-details">
                          <Typography variant="caption" color="text.secondary">
                            Qty: {item.quantity}
                          </Typography>
                          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                            {item.hasDiscount ? (
                              <>
                                <Typography variant="caption" fontWeight="medium" color="primary">
                                  ${formatPrice((item.effectivePrice || item.price) * item.quantity)}
                                </Typography>
                                <Typography
                                  variant="caption"
                                  sx={{ textDecoration: 'line-through', color: 'text.secondary' }}
                                >
                                  ${formatPrice(item.originalPrice * item.quantity)}
                                </Typography>
                                <Chip
                                  label={`Save $${formatPrice(item.savings * item.quantity)}`}
                                  color="success"
                                  size="small"
                                  sx={{ height: 16, fontSize: '0.65rem', mt: 0.25 }}
                                />
                              </>
                            ) : (
                              <Typography variant="caption" fontWeight="medium">
                                ${formatPrice((item.effectivePrice || item.price) * item.quantity)}
                              </Typography>
                            )}
                          </Box>
                        </Box>
                      }
                    />
                  </ListItem>
                ))}

                {hasMoreItems && (
                  <ListItem className="mini-cart-more-items">
                    <Typography variant="caption" color="text.secondary">
                      And {displayCart.length - 3} more items...
                    </Typography>
                  </ListItem>
                )}
              </List>

              <Box className="mini-cart-footer">
                <Box className="mini-cart-subtotal">
                  <Typography variant="subtitle2">Subtotal:</Typography>
                  <Typography variant="subtitle1" fontWeight="bold">
                    ${formatPrice(cartTotal)}
                  </Typography>
                </Box>

                <Box className="mini-cart-actions">
                  <Button
                    variant="outlined"
                    fullWidth
                    onClick={handleViewCart}
                    size="small"
                  >
                    View Cart
                  </Button>
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={handleCheckout}
                    size="small"
                  >
                    Checkout
                  </Button>
                </Box>
              </Box>
            </>
          )}
        </Paper>
      </Box>
    );
  }

  return (
    <Box>
      <Button
        color="inherit"
        startIcon={
          <Badge badgeContent={totalItems} color="error" overlap="circular" max={99}>
            <ShoppingCartIcon />
          </Badge>
        }
        onClick={handleOpen}
        size="small"
      >
        Cart
      </Button>

      <Popover
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        PaperProps={{
          elevation: 4,
          className: 'mini-cart-popover'
        }}
      >
        <Box className="mini-cart-header">
          <Typography variant="subtitle1" fontWeight="medium">
            Your Cart ({totalItems} items)
          </Typography>
        </Box>

        {displayCart.length === 0 ? (
          <Box className="mini-cart-empty">
            <Typography color="text.secondary">Your cart is empty</Typography>
          </Box>
        ) : (
          <>
            <List className="mini-cart-list">
              {displayItems.map((item) => (
                <ListItem key={item.productId} divider className="mini-cart-item">
                  <ListItemAvatar>
                    <Avatar
                      src={item.image}
                      alt={item.title}
                      variant="rounded"
                      className="mini-cart-avatar"
                    />
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Typography variant="body2" noWrap fontWeight="medium">
                        {item.title}
                      </Typography>
                    }
                    secondary={
                      <Box className="mini-cart-item-details">
                        <Typography variant="caption" color="text.secondary">
                          Qty: {item.quantity}
                        </Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                          {item.hasDiscount ? (
                            <>
                              <Typography variant="caption" fontWeight="medium" color="primary">
                                ${formatPrice((item.effectivePrice || item.price) * item.quantity)}
                              </Typography>
                              <Typography
                                variant="caption"
                                sx={{ textDecoration: 'line-through', color: 'text.secondary' }}
                              >
                                ${formatPrice(item.originalPrice * item.quantity)}
                              </Typography>
                              <Chip
                                label={`Save $${formatPrice(item.savings * item.quantity)}`}
                                color="success"
                                size="small"
                                sx={{ height: 16, fontSize: '0.65rem', mt: 0.25 }}
                              />
                            </>
                          ) : (
                            <Typography variant="caption" fontWeight="medium">
                              ${formatPrice((item.effectivePrice || item.price) * item.quantity)}
                            </Typography>
                          )}
                        </Box>
                      </Box>
                    }
                  />
                </ListItem>
              ))}

              {hasMoreItems && (
                <ListItem className="mini-cart-more-items">
                  <Typography variant="caption" color="text.secondary">
                    And {displayCart.length - 3} more items...
                  </Typography>
                </ListItem>
              )}
            </List>

            <Box className="mini-cart-footer">
              <Box className="mini-cart-subtotal">
                <Typography variant="subtitle2">Subtotal:</Typography>
                <Typography variant="subtitle1" fontWeight="bold">
                  ${formatPrice(cartTotal)}
                </Typography>
              </Box>

              <Box className="mini-cart-actions">
                <Button
                  variant="outlined"
                  fullWidth
                  onClick={handleViewCart}
                  size="small"
                >
                  View Cart
                </Button>
                <Button
                  variant="contained"
                  fullWidth
                  onClick={handleCheckout}
                  size="small"
                >
                  Checkout
                </Button>
              </Box>
            </Box>
          </>
        )}
      </Popover>
    </Box>
  );
}
