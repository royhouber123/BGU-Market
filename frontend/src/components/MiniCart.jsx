import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createPageUrl } from "../utils";

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
  Avatar
} from "@mui/material";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";

export default function MiniCart({ cart = [] }) {
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState(null);

  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  const cartTotal = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  // Show at most 3 items in mini cart
  const displayItems = cart.slice(0, 3);
  const hasMoreItems = cart.length > 3;

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
  };
  
  const open = Boolean(anchorEl);

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
          sx: { width: 320, borderRadius: 2, overflow: 'hidden' }
        }}
      >
        <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
          <Typography variant="subtitle1" fontWeight="medium">
            Your Cart ({totalItems} items)
          </Typography>
        </Box>

        {cart.length === 0 ? (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <Typography color="text.secondary">Your cart is empty</Typography>
          </Box>
        ) : (
          <>
            <List sx={{ maxHeight: 240, overflow: 'auto', py: 0 }}>
              {displayItems.map((item) => (
                <ListItem key={item.productId} divider sx={{ py: 1.5 }}>
                  <ListItemAvatar>
                    <Avatar
                      src={item.image}
                      alt={item.title}
                      variant="rounded"
                      sx={{ width: 48, height: 48, borderRadius: 1 }}
                    />
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Typography variant="body2" noWrap fontWeight="medium">
                        {item.title}
                      </Typography>
                    }
                    secondary={
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 0.5 }}>
                        <Typography variant="caption" color="text.secondary">
                          Qty: {item.quantity}
                        </Typography>
                        <Typography variant="caption" fontWeight="medium">
                          ${(item.price * item.quantity).toFixed(2)}
                        </Typography>
                      </Box>
                    }
                  />
                </ListItem>
              ))}

              {hasMoreItems && (
                <ListItem sx={{ justifyContent: 'center', py: 1.5, borderBottom: '1px solid', borderColor: 'divider' }}>
                  <Typography variant="caption" color="text.secondary">
                    And {cart.length - 3} more items...
                  </Typography>
                </ListItem>
              )}
            </List>

            <Box sx={{ p: 2, borderTop: '1px solid', borderColor: 'divider' }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                <Typography variant="subtitle2">Subtotal:</Typography>
                <Typography variant="subtitle1" fontWeight="bold">
                  ${cartTotal.toFixed(2)}
                </Typography>
              </Box>

              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1 }}>
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
