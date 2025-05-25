import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createPageUrl } from "../../utils";
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
  Avatar
} from "@mui/material";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";

export default function MiniCart({ cart = [], onClose }) {
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

          {cart.length === 0 ? (
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
                          <Typography variant="caption" fontWeight="medium">
                            ${(item.price * item.quantity).toFixed(2)}
                          </Typography>
                        </Box>
                      }
                    />
                  </ListItem>
                ))}

                {hasMoreItems && (
                  <ListItem className="mini-cart-more-items">
                    <Typography variant="caption" color="text.secondary">
                      And {cart.length - 3} more items...
                    </Typography>
                  </ListItem>
                )}
              </List>

              <Box className="mini-cart-footer">
                <Box className="mini-cart-subtotal">
                  <Typography variant="subtitle2">Subtotal:</Typography>
                  <Typography variant="subtitle1" fontWeight="bold">
                    ${cartTotal.toFixed(2)}
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

        {cart.length === 0 ? (
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
                        <Typography variant="caption" fontWeight="medium">
                          ${(item.price * item.quantity).toFixed(2)}
                        </Typography>
                      </Box>
                    }
                  />
                </ListItem>
              ))}

              {hasMoreItems && (
                <ListItem className="mini-cart-more-items">
                  <Typography variant="caption" color="text.secondary">
                    And {cart.length - 3} more items...
                  </Typography>
                </ListItem>
              )}
            </List>

            <Box className="mini-cart-footer">
              <Box className="mini-cart-subtotal">
                <Typography variant="subtitle2">Subtotal:</Typography>
                <Typography variant="subtitle1" fontWeight="bold">
                  ${cartTotal.toFixed(2)}
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
