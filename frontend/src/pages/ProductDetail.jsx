import React, { useState, useEffect } from "react";
import productService from "../services/productService";
import userService from "../services/userService";
import { Link } from "react-router-dom";
// Import all relevant components
import Header from "../components/Header.jsx";
import MiniCart from "../components/MiniCart.jsx";
import AuthDialog from "../components/AuthDialog.jsx";
import FeaturedSection from "../components/FeaturedSection.jsx";
import { 
  Button, 
  Skeleton, 
  Chip, 
  Rating, 
  Tabs, 
  Tab, 
  Box, 
  Typography, 
  Snackbar, 
  Alert,
  Grid,
  Paper,
  Divider
} from "@mui/material";
import FavoriteIcon from "@mui/icons-material/Favorite";
import FavoriteBorderIcon from "@mui/icons-material/FavoriteBorder";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import InventoryIcon from "@mui/icons-material/Inventory";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import SecurityIcon from "@mui/icons-material/Security";
import StarIcon from "@mui/icons-material/Star";
import StarHalfIcon from "@mui/icons-material/StarHalf";
import StoreIcon from "@mui/icons-material/Store";
import CheckIcon from "@mui/icons-material/Check";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import { format } from "date-fns";

export default function ProductDetail() {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [mainImage, setMainImage] = useState("");
  const [isInCart, setIsInCart] = useState(false);
  const [isInWatchlist, setIsInWatchlist] = useState(false);
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [showMiniCart, setShowMiniCart] = useState(false);
  const [tabValue, setTabValue] = useState(0);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });
  
  const toast = (props) => {
    setSnackbar({
      open: true,
      message: props.description || props.title,
      severity: props.variant === 'destructive' ? 'error' : 'success'
    });
  };

  const urlParams = new URLSearchParams(window.location.search);
  const productId = urlParams.get("id");

  useEffect(() => {
    fetchProduct();
    checkUserLists();
  }, [productId]);

  const fetchProduct = async () => {
    try {
      // In a real app, you would fetch a single product by ID
      // For this example, we'll fetch all and find the one we want
      const products = await productService.getAllProducts();
      const foundProduct = products.find((p) => p.id === productId);

      if (foundProduct) {
        setProduct(foundProduct);
        setMainImage(foundProduct.images?.[0] || "");
      }
    } catch (error) {
      console.error("Error fetching product:", error);
    } finally {
      setLoading(false);
    }
  };

  const checkUserLists = async () => {
    try {
      if (!userService.isAuthenticated()) {
        return;
      }
      const user = await userService.getProfile();

      // Check if product is in cart
      const cartItem = (user.cart || []).find(
        (item) => item.productId === productId
      );
      setIsInCart(!!cartItem);

      // Check if product is in watchlist
      const watchlistItem = (user.watchlist || []).find(
        (item) => item.productId === productId
      );
      setIsInWatchlist(!!watchlistItem);
    } catch (error) {
      console.error("Error checking user lists:", error);
    }
  };

  const addToCart = async () => {
    try {
      if (!userService.isAuthenticated()) {
        setShowAuthDialog(true);
        return;
      }
      
      const user = await userService.getProfile();
      const cart = user.cart || [];

      // Check if product is already in cart
      const existingItemIndex = cart.findIndex(
        (item) => item.productId === productId
      );

      if (existingItemIndex > -1) {
        // Update quantity
        cart[existingItemIndex].quantity += 1;
      } else {
        // Add new item
        cart.push({
          productId: productId,
          title: product.title,
          price: product.price,
          image: product.images?.[0] || "",
          quantity: 1
        });
      }

      await userService.updateUserData({ cart });
      setIsInCart(true);
      setShowMiniCart(true);

      toast({
        title: "Added to cart",
        description: "Item has been added to your cart",
        variant: "default",
      });
    } catch (error) {
      console.error("Error adding to cart:", error);
      
      toast({
        title: "Error",
        description: "Could not add item to cart",
        variant: "destructive",
      });
    }
  };

  const toggleWatchlist = async () => {
    try {
      if (!userService.isAuthenticated()) {
        setShowAuthDialog(true);
        return;
      }
      
      const user = await userService.getProfile();
      let watchlist = user.watchlist || [];

      if (isInWatchlist) {
        // Remove from watchlist
        watchlist = watchlist.filter(
          (item) => item.productId !== productId
        );
        
        toast({
          title: "Removed from watchlist",
          description: "Item has been removed from your watchlist",
          variant: "default",
        });
      } else {
        // Add to watchlist
        watchlist.push({
          productId: productId,
          addedAt: new Date().toISOString(),
        });
        
        toast({
          title: "Added to watchlist",
          description: "Item has been added to your watchlist",
          variant: "default",
        });
      }

      await userService.updateUserData({ watchlist });
      setIsInWatchlist(!isInWatchlist);
    } catch (error) {
      toast({
        title: "Error",
        description: "Could not update watchlist",
        variant: "destructive",
      });
    }
  };

  const formatDate = (dateString) => {
    try {
      return format(new Date(dateString), "MMMM d, yyyy");
    } catch (error) {
      return "Unknown date";
    }
  };

  const calculateTimeLeft = () => {
    if (!product || !product.end_date) return 0;
    
    const endDate = new Date(product.end_date);
    const now = new Date();
    const timeLeft = endDate.getTime() - now.getTime();
    
    return Math.max(0, timeLeft);
  };

  const formatTimeLeft = () => {
    const timeLeft = calculateTimeLeft();
    
    if (timeLeft <= 0) {
      return "This listing has ended";
    }
    
    const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
    const hours = Math.floor((timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
    
    return `${days}d ${hours}h ${minutes}m left`;
  };

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  if (loading) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Header />
        <main className="container mx-auto px-4 py-8">
          <div className="grid md:grid-cols-2 gap-8">
            <Skeleton className="aspect-square rounded-lg" />
            <div>
              <Skeleton height={32} width="70%" className="mb-4" />
              <Skeleton height={24} width="40%" className="mb-2" />
              <Skeleton height={24} width="40%" className="mb-6" />
              <Skeleton height={64} width="100%" className="mb-3" />
              <Skeleton height={64} width="100%" className="mb-6" />
              <Skeleton height={20} width="100%" className="mb-2" />
              <Skeleton height={20} width="90%" className="mb-2" />
              <Skeleton height={20} width="80%" className="mb-2" />
            </div>
          </div>
        </main>
      </Box>
    );
  }

  if (!product) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Header />
        <main className="container mx-auto px-4 py-8 text-center">
          <h1 className="text-2xl font-bold text-gray-700">
            Product not found
          </h1>
          <p className="text-gray-600 mt-2">
            The product you are looking for does not exist or has been removed.
          </p>
          <Button 
            component={Link} 
            to="/" 
            variant="contained" 
            color="primary"
            sx={{ mt: 4 }}
          >
            Back to Home
          </Button>
        </main> 
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Header />
      <main className="container mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow-sm p-6 md:p-8">
          <div className="grid md:grid-cols-2 gap-8">
            {/* Product Images */}
            <div>
              <div className="aspect-square overflow-hidden rounded-lg mb-4 bg-gray-100">
                <img
                  src={mainImage || product.images?.[0]}
                  alt={product.title}
                  className="w-full h-full object-contain"
                />
              </div>

              <div className="grid grid-cols-4 gap-2">
                {product.images?.map((image, index) => (
                  <div
                    key={index}
                    className={`aspect-square rounded border-2 overflow-hidden cursor-pointer ${
                      image === mainImage
                        ? "border-blue-500"
                        : "border-transparent"
                    }`}
                    onClick={() => setMainImage(image)}
                  >
                    <img
                      src={image}
                      alt={`${product.title} view ${index + 1}`}
                      className="w-full h-full object-cover"
                    />
                  </div>
                ))}
              </div>
            </div>

            {/* Product Details */}
            <div>
              <h1 className="text-2xl font-bold text-gray-900 mb-2">
                {product.title}
              </h1>

              {calculateTimeLeft() > 0 && (
                <div className="text-sm text-blue-600 mb-4">
                  {formatTimeLeft()}
                </div>
              )}

              <div className="flex items-center gap-2 mb-4">
                <Chip 
                  label={product.condition ? product.condition.replace("_", " ") : "New"}
                  variant="outlined"
                  size="small"
                />
                <Chip 
                  label={product.category || "Uncategorized"}
                  variant="outlined"
                  size="small"
                />
              </div>

              <div className="text-3xl font-bold mb-2">
                ${product.price?.toFixed(2) || "0.00"}
              </div>

              {product.shipping_cost === 0 ? (
                <Typography variant="body2" color="success.main" gutterBottom>
                  Free shipping
                </Typography>
              ) : (
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  +${product.shipping_cost?.toFixed(2) || "0.00"} shipping
                </Typography>
              )}

              <Typography variant="body1" sx={{ mb: 3 }}>
                {product.description}
              </Typography>

              <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, gap: 2, mb: 3 }}>
                <Button 
                  variant="contained"
                  color="primary"
                  fullWidth
                  startIcon={isInCart ? <CheckIcon /> : <ShoppingCartIcon />}
                  onClick={addToCart}
                >
                  {isInCart ? "Added to Cart" : "Add to Cart"}
                </Button>

                <Button
                  variant={isInWatchlist ? "contained" : "outlined"}
                  color={isInWatchlist ? "error" : "primary"}
                  fullWidth
                  startIcon={<FavoriteIcon />}
                  onClick={toggleWatchlist}
                >
                  {isInWatchlist ? "Watching" : "Add to Watchlist"}
                </Button>
              </Box>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, mt: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <StoreIcon fontSize="small" sx={{ mr: 1, color: 'text.secondary' }} />
                  <Typography variant="body2" color="text.secondary">
                    Sold by:{" "}
                    <Box component="span" fontWeight="medium">
                      {product.seller || "MarketPlace Seller"}
                    </Box>
                  </Typography>
                </Box>
                
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <StarIcon fontSize="small" sx={{ mr: 1, color: 'warning.main' }} />
                  <Typography variant="body2" color="text.secondary">
                    Top Rated Plus with fast shipping and excellent service
                  </Typography>
                </Box>
                
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <SecurityIcon fontSize="small" sx={{ mr: 1, color: 'success.main' }} />
                  <Typography variant="body2" color="text.secondary">
                    Money back guarantee
                  </Typography>
                </Box>
                
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <LocalShippingIcon fontSize="small" sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="body2" color="text.secondary">
                    Estimated delivery: 3-5 business days
                  </Typography>
                </Box>
              </Box>
            </div>
          </div>

          <Divider sx={{ my: 4 }} />

          {/* Product Tabs */}
          <Box sx={{ width: '100%', mb: 4 }}>
            <Tabs 
              value={tabValue}
              onChange={handleTabChange}
              aria-label="product details tabs"
              sx={{ mb: 3 }}
            >
              <Tab label="Details" />
              <Tab label="Shipping" />
              <Tab label="Returns" />
            </Tabs>
            
            {tabValue === 0 && (
              <Box>
                <Typography variant="h6" gutterBottom>
                  Product Details
                </Typography>
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Paper variant="outlined" sx={{ p: 2 }}>
                      <Typography variant="subtitle2" gutterBottom>
                        Specifications
                      </Typography>
                      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="body2" color="text.secondary">Condition</Typography>
                          <Typography variant="body2">{product.condition?.replace("_", " ") || "New"}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="body2" color="text.secondary">Category</Typography>
                          <Typography variant="body2">{product.category || "Uncategorized"}</Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="body2" color="text.secondary">Brand</Typography>
                          <Typography variant="body2">{product.brand || "Unbranded"}</Typography>
                        </Box>
                        {product.weight && (
                          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="body2" color="text.secondary">Weight</Typography>
                            <Typography variant="body2">{product.weight} kg</Typography>
                          </Box>
                        )}
                      </Box>
                    </Paper>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Paper variant="outlined" sx={{ p: 2 }}>
                      <Typography variant="subtitle2" gutterBottom>
                        Seller Information
                      </Typography>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                        <StoreIcon fontSize="small" sx={{ mr: 1 }} />
                        <Typography variant="body2">
                          {product.seller || "MarketPlace Seller"}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <Rating 
                          value={4.5} 
                          precision={0.5} 
                          readOnly 
                          size="small"
                          sx={{ mr: 1 }}
                        />
                        <Typography variant="body2">
                          4.5 (123 reviews)
                        </Typography>
                      </Box>
                    </Paper>
                  </Grid>
                </Grid>
              </Box>
            )}
            
            {tabValue === 1 && (
              <Box>
                <Typography variant="h6" gutterBottom>
                  Shipping Information
                </Typography>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 2 }}>
                    <LocalShippingIcon sx={{ mr: 1, mt: 0.5 }} />
                    <Box>
                      <Typography variant="subtitle2">
                        Standard Shipping
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Estimated delivery: 3-5 business days
                      </Typography>
                      <Typography variant="body2" fontWeight="medium" sx={{ mt: 0.5 }}>
                        {product.shipping_cost === 0 
                          ? "Free" 
                          : `$${product.shipping_cost?.toFixed(2) || "0.00"}`}
                      </Typography>
                    </Box>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    All items are carefully packaged to ensure they arrive safely.
                    Tracking information will be provided after your order is shipped.
                  </Typography>
                  <Button
                    variant="text"
                    endIcon={<ArrowForwardIcon />}
                    sx={{ mt: 2 }}
                  >
                    View shipping policies
                  </Button>
                </Paper>
              </Box>
            )}
            
            {tabValue === 2 && (
              <Box>
                <Typography variant="h6" gutterBottom>
                  Return Policy
                </Typography>
                <Paper variant="outlined" sx={{ p: 2 }}>
                  <Typography variant="body2" paragraph>
                    We want you to be completely satisfied with your purchase. If you're not
                    happy with your order for any reason, you can return it within 30 days
                    of delivery.
                  </Typography>
                  <Typography variant="body2" color="text.secondary" paragraph>
                    Items must be returned in the original packaging and in the same
                    condition as when you received them. Please include a copy of your
                    receipt or order confirmation.
                  </Typography>
                  <Typography variant="subtitle2">
                    Return shipping:
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    The buyer is responsible for return shipping costs unless the item
                    is defective or not as described.
                  </Typography>
                </Paper>
              </Box>
            )}
          </Box>

          {/* Related Products Section */}
          <FeaturedSection 
            title="You may also like" 
            subtitle="Similar products you might be interested in" 
            limit={4} 
          />
        </div>

        {/* Mini Cart and Auth Dialog with conditional rendering */}
        {showMiniCart && <MiniCart onClose={() => setShowMiniCart(false)} />}
        {showAuthDialog && <AuthDialog open={showAuthDialog} onClose={() => setShowAuthDialog(false)} />}
        
        {/* Snackbar for notifications */}
        <Snackbar
          open={snackbar.open}
          autoHideDuration={6000}
          onClose={() => setSnackbar({...snackbar, open: false})}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        >
          <Alert 
            onClose={() => setSnackbar({...snackbar, open: false})} 
            severity={snackbar.severity}
            variant="filled"
          >
            {snackbar.message}
          </Alert>
        </Snackbar>
      </main>
    </Box>
  );
}
