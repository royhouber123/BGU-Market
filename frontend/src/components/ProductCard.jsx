import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { createPageUrl } from "../utils";
import { useNavigate } from "react-router-dom";
import userService from "../services/userService";

// Material-UI imports
import { Card, CardContent, CardMedia, Typography, Chip, IconButton, Box, Snackbar, Alert } from "@mui/material";
import FavoriteIcon from "@mui/icons-material/Favorite";
import AccessTimeIcon from "@mui/icons-material/AccessTime";

export default function ProductCard({ product }) {
  const [isInWatchlist, setIsInWatchlist] = useState(false);
  const navigate = useNavigate();
  const [openToast, setOpenToast] = useState(false);
  const [toastMessage, setToastMessage] = useState({ severity: "success", title: "", description: "" });

  useEffect(() => {
    checkWatchlist();
  }, []);

  const checkWatchlist = async () => {
    try {
      if (userService.isAuthenticated()) {
        const user = await userService.getProfile();
        const watchlist = user.watchlist || [];
        setIsInWatchlist(watchlist.includes(product.id));
      }
    } catch (error) {
      // User not logged in or no watchlist
    }
  };

  const timeLeft = product.bid_end_date
    ? new Date(product.bid_end_date) - new Date()
    : null;

  const formatTimeLeft = () => {
    if (!timeLeft || timeLeft <= 0) return "Ended";

    const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
    const hours = Math.floor(
      (timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
    );

    if (days > 0) return `${days}d ${hours}h left`;

    const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
    if (hours > 0) return `${hours}h ${minutes}m left`;

    return `${minutes}m left`;
  };

  const handleProductClick = (e) => {
    e.preventDefault();
    navigate(`/product/${product.id}`);
  };

  const toggleWatchlist = async (e) => {
    e.preventDefault();
    e.stopPropagation();

    try {
      if (!userService.isAuthenticated()) {
        setToastMessage({
          severity: "error",
          title: "Sign in required",
          description: "Please sign in to manage your watchlist",
        });
        setOpenToast(true);
        return;
      }

      if (isInWatchlist) {
        // Remove from watchlist
        await userService.removeFromWatchlist(product.id);
        setToastMessage({
          severity: "success",
          title: "Removed from watchlist",
          description: "Item has been removed from your watchlist",
        });
        setOpenToast(true);
      } else {
        // Add to watchlist
        await userService.addToWatchlist(product.id);
        setToastMessage({
          severity: "success",
          title: "Added to watchlist",
          description: "Item has been added to your watchlist",
        });
        setOpenToast(true);
      }
      
      setIsInWatchlist(!isInWatchlist);
    } catch (error) {
      setToastMessage({
        severity: "error",
        title: "Error",
        description: "There was an error updating your watchlist",
      });
      setOpenToast(true);
    }
  };

  const handleCloseToast = () => {
    setOpenToast(false);
  };

  return (
    <Box sx={{ 
      transition: 'transform 0.2s ease-in-out', 
      '&:hover': { transform: 'translateY(-5px)' },
      height: '100%'
    }}>
      <Card 
        sx={{ 
          height: '100%',
          display: 'flex', 
          flexDirection: 'column',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          '&:hover': { boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }
        }}
      >
        <Box sx={{ position: 'relative' }} onClick={handleProductClick} component="a" href="#">
          <CardMedia
            component="img"
            height="192"
            image={product.images[0]}
            alt={product.title}
            sx={{ objectFit: 'cover' }}
          />
          <IconButton
            size="small"
            onClick={toggleWatchlist}
            sx={{
              position: 'absolute',
              top: 8,
              right: 8,
              bgcolor: 'rgba(255, 255, 255, 0.8)',
              '&:hover': { bgcolor: 'rgba(255, 255, 255, 1)' },
              color: isInWatchlist ? 'error.main' : 'text.secondary'
            }}
          >
            <FavoriteIcon fontSize="small" sx={{ fill: isInWatchlist ? 'currentColor' : 'none' }} />
          </IconButton>

          {product.featured && (
            <Chip 
              label="Featured" 
              color="primary" 
              size="small"
              sx={{ 
                position: 'absolute', 
                top: 8, 
                left: 8,
                bgcolor: '#2563eb',
                '&:hover': { bgcolor: '#1e40af' }
              }}
            />
          )}
        </Box>

        <CardContent sx={{ flexGrow: 1, p: 2 }} onClick={handleProductClick} component="a" href="#" style={{ textDecoration: 'none', color: 'inherit' }}>
          <Typography variant="h6" component="h3" noWrap>
            {product.title}
          </Typography>

          <Box sx={{ mt: 1.5, display: 'flex', alignItems: 'baseline', gap: 1 }}>
            <Typography variant="h6" component="span" fontWeight="bold">
              ${product.price.toFixed(2)}
            </Typography>

            {product.shipping_cost === 0 ? (
              <Typography variant="body2" color="success.main">
                Free shipping
              </Typography>
            ) : (
              <Typography variant="body2" color="text.secondary">
                +${product.shipping_cost?.toFixed(2)} shipping
              </Typography>
            )}
          </Box>

          <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Chip
              label={product.category}
              variant="outlined"
              size="small"
              sx={{ textTransform: 'capitalize', cursor: 'pointer' }}
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                navigate(
                  createPageUrl("SearchResults") +
                    `?category=${product.category}`
                );
              }}
            />

            {timeLeft && (
              <Box sx={{ display: 'flex', alignItems: 'center', typography: 'body2', color: 'text.secondary' }}>
                <AccessTimeIcon sx={{ fontSize: 14, mr: 0.5 }} />
                {formatTimeLeft()}
              </Box>
            )}
          </Box>
        </CardContent>
      </Card>
      
      <Snackbar open={openToast} autoHideDuration={6000} onClose={handleCloseToast}>
        <Alert onClose={handleCloseToast} severity={toastMessage.severity} sx={{ width: '100%' }}>
          <Typography variant="subtitle2">{toastMessage.title}</Typography>
          <Typography variant="body2">{toastMessage.description}</Typography>
        </Alert>
      </Snackbar>
    </Box>
  );
}
