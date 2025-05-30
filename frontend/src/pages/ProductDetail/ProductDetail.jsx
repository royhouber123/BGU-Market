import React, { useState, useEffect } from "react";
import { productService } from "../../services/productService";
import userService from "../../services/userService";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
// Import all relevant components
import Header from "../../components/Header/Header";
import MiniCart from "../../components/MiniCart/MiniCart";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import FeaturedSection from "../../components/FeaturedSection/FeaturedSection";
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
import './ProductDetail.css';

export default function ProductDetail() {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [mainImage, setMainImage] = useState("");
  const [isInCart, setIsInCart] = useState(false);
  const [isInWatchlist, setIsInWatchlist] = useState(false);
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [showMiniCart, setShowMiniCart] = useState(false);
  const [tabValue, setTabValue] = useState(0);
  const [addingToCart, setAddingToCart] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });

  const { refreshCart, cart } = useAuth();

  const toast = (props) => {
    setSnackbar({
      open: true,
      message: props.description || props.title,
      severity: props.variant === 'destructive' ? 'error' : 'success'
    });
  };

  // Get productId from URL parameters
  const { id: productId } = useParams();

  useEffect(() => {
    fetchProduct();
    checkUserLists();
  }, [productId]);

  const fetchProduct = async () => {
    try {
      // Try to fetch the listing directly by listing ID first
      try {
        // Direct fetch by listing ID is more efficient
        const foundProduct = await productService.getListing(productId);
        if (foundProduct) {
          setProduct(foundProduct);
          setMainImage(foundProduct.images?.[0] || "");
          return;
        }
      } catch (listingError) {
        console.log("Could not fetch by listing ID directly, trying alternative approach");
      }

      // Fallback: fetch all products and find the one we want
      const products = await productService.getAllProducts();
      const foundProduct = products.find(
        // Make sure to compare strings with strings
        (p) => String(p.id) === String(productId)
      );

      if (foundProduct) {
        setProduct(foundProduct);
        setMainImage(foundProduct.images?.[0] || "");
      } else {
        console.error("Product not found", productId);
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

      // Sync cart from backend first
      await userService.syncCartFromBackend();
      const user = await userService.getProfile();

      // Check if product is in cart - use string comparison to avoid type issues
      const cartItem = (user.cart || []).find(
        (item) => String(item.productId) === String(productId)
      );
      setIsInCart(!!cartItem);

      // Check if product is in watchlist - use string comparison to avoid type issues
      const watchlistItem = (user.watchlist || []).find(
        (item) => String(item.productId) === String(productId)
      );
      setIsInWatchlist(!!watchlistItem);
    } catch (error) {
      console.error("Error checking user lists:", error);
    }
  };

  const addToCart = async () => {
    if (addingToCart) return; // Prevent double-clicking

    try {
      if (!userService.isAuthenticated()) {
        setShowAuthDialog(true);
        return;
      }

      if (!product) {
        toast({
          title: "Error",
          description: "Product information not available",
          variant: "destructive",
        });
        return;
      }

      if (quantity <= 0) {
        toast({
          title: "Error",
          description: "Please select a valid quantity",
          variant: "destructive",
        });
        return;
      }

      setAddingToCart(true);

      // Use the backend API to add product to cart
      // Note: Despite the parameter name "productName", the backend expects the listing ID
      await userService.addProductToCart(
        product.storeId || "1", // Use the product's store ID or default to "1"
        product.id, // Use product ID (which is the listing ID) - this is what the backend expects
        quantity // Use selected quantity
      );

      // Refresh cart state after adding
      await refreshCart();
      await checkUserLists();

      setIsInCart(true);
      setShowMiniCart(true);

      toast({
        title: "Added to cart",
        description: `${quantity} item(s) added to your cart`,
        variant: "default",
      });
    } catch (error) {
      console.error("Error adding to cart:", error);

      toast({
        title: "Error",
        description: error.message || "Could not add item to cart",
        variant: "destructive",
      });
    } finally {
      setAddingToCart(false);
    }
  };

  const toggleWatchlist = async () => {
    try {
      if (!userService.isAuthenticated()) {
        setShowAuthDialog(true);
        return;
      }

      if (!product) {
        toast({
          title: "Error",
          description: "Product information not available",
          variant: "destructive",
        });
        return;
      }

      const user = await userService.getProfile();
      let watchlist = user.watchlist || [];

      if (isInWatchlist) {
        // Remove from watchlist - use string comparison
        watchlist = watchlist.filter(
          (item) => String(item.productId) !== String(productId)
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
    // In this system, listings don't have expiration dates
    // Return a positive value to indicate the listing is active
    return 1;
  };

  const formatTimeLeft = () => {
    // In this system, listings don't have expiration dates
    return "Available";
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
          {/* Product Title at the top */}
          <Typography variant="h4" component="h1" sx={{ mb: 3, fontWeight: 'bold', textAlign: 'center' }}>
            {product.title}
          </Typography>

          {/* Main content area with image in the center and details on the right */}
          <Grid container spacing={4}>
            {/* Left spacer on large screens */}
            <Grid item xs={false} md={1} />

            {/* Product Images in the middle */}
            <Grid item xs={12} md={5} sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <Box sx={{
                width: '100%',
                maxWidth: '500px',
                aspectRatio: '1/1',
                borderRadius: 2,
                overflow: 'hidden',
                mb: 2,
                boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
                bgcolor: 'background.paper'
              }}>
                <img
                  src={mainImage || product.images?.[0]}
                  alt={product.title}
                  style={{
                    width: '100%',
                    height: '100%',
                    objectFit: 'contain'
                  }}
                />
              </Box>

              <Box sx={{
                display: 'flex',
                gap: 1,
                flexWrap: 'wrap',
                justifyContent: 'center',
                maxWidth: '500px'
              }}>
                {product.images?.map((image, index) => (
                  <Box
                    key={index}
                    sx={{
                      width: '70px',
                      height: '70px',
                      borderRadius: 1,
                      overflow: 'hidden',
                      border: image === mainImage ? '2px solid #1976d2' : '2px solid transparent',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease'
                    }}
                    onClick={() => setMainImage(image)}
                  >
                    <img
                      src={image}
                      alt={`${product.title} view ${index + 1}`}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                  </Box>
                ))}
              </Box>
            </Grid>

            {/* Product Details on the right */}
            <Grid item xs={12} md={5}>
              <Paper elevation={0} sx={{ p: { xs: 2, md: 3 }, border: '1px solid rgba(0,0,0,0.08)', borderRadius: 2 }}>
                {/* Price and badges */}
                <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1, mb: 1 }}>
                  <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
                    ${product.price?.toFixed(2) || "0.00"}
                  </Typography>
                </Box>

                {/* Availability Info */}
                <Box sx={{ mb: 3 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                    <InventoryIcon fontSize="small" color={product?.quantity > 0 ? 'success' : 'error'} />
                    <Typography variant="body2" color={product?.quantity > 0 ? 'success.main' : 'error.main'}>
                      {product?.quantity > 0 ? `${product.quantity} in stock` : 'Out of stock'}
                    </Typography>
                  </Box>
                </Box>

                {/* Action Buttons */}
                <Box sx={{ display: 'flex', gap: 2, mb: 4 }}>
                  {/* Quantity Selector */}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="body2" color="text.secondary">
                      Qty:
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', border: '1px solid #ddd', borderRadius: 1 }}>
                      <Button
                        size="small"
                        onClick={() => setQuantity(Math.max(1, quantity - 1))}
                        sx={{ minWidth: '32px', height: '32px', p: 0 }}
                      >
                        -
                      </Button>
                      <Typography
                        sx={{
                          px: 2,
                          py: 0.5,
                          minWidth: '40px',
                          textAlign: 'center',
                          borderLeft: '1px solid #ddd',
                          borderRight: '1px solid #ddd'
                        }}
                      >
                        {quantity}
                      </Typography>
                      <Button
                        size="small"
                        onClick={() => setQuantity(Math.min(product?.quantity || 99, quantity + 1))}
                        sx={{ minWidth: '32px', height: '32px', p: 0 }}
                      >
                        +
                      </Button>
                    </Box>
                  </Box>

                  <Button
                    variant="contained"
                    size="large"
                    startIcon={addingToCart ? null : <ShoppingCartIcon />}
                    onClick={addToCart}
                    disabled={addingToCart || (product?.quantity || 0) <= 0}
                    sx={{
                      flex: 1,
                      py: 1.5,
                      fontSize: '1.1rem',
                      fontWeight: 'bold',
                      bgcolor: 'primary.main',
                      '&:hover': {
                        bgcolor: 'primary.dark',
                      },
                      '&:disabled': {
                        bgcolor: 'grey.300',
                        color: 'grey.500'
                      }
                    }}
                  >
                    {addingToCart ? (
                      <>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Box
                            sx={{
                              width: 16,
                              height: 16,
                              border: '2px solid currentColor',
                              borderTop: '2px solid transparent',
                              borderRadius: '50%',
                              animation: 'spin 1s linear infinite',
                              '@keyframes spin': {
                                '0%': { transform: 'rotate(0deg)' },
                                '100%': { transform: 'rotate(360deg)' }
                              }
                            }}
                          />
                          Adding...
                        </Box>
                      </>
                    ) : (product?.quantity || 0) <= 0 ? (
                      'Out of Stock'
                    ) : isInCart ? (
                      'Added to Cart'
                    ) : (
                      'Add to Cart'
                    )}
                  </Button>

                  <Button
                    variant="outlined"
                    size="large"
                    onClick={toggleWatchlist}
                    sx={{
                      minWidth: '60px',
                      py: 1.5,
                      borderColor: 'primary.main',
                      color: isInWatchlist ? 'primary.main' : 'text.secondary',
                      '&:hover': {
                        borderColor: 'primary.dark',
                        bgcolor: 'primary.50',
                      }
                    }}
                  >
                    {isInWatchlist ? <FavoriteIcon /> : <FavoriteBorderIcon />}
                  </Button>
                </Box>

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
                                {product.seller?.name || "MarketPlace Seller"}
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
              </Paper>
            </Grid>
            <Grid item xs={false} md={1} />
          </Grid>

          {/* Related Products Section */}
          {/* <FeaturedSection 
          title="You may also like" 
          subtitle="Similar products you might be interested in" 
          limit={4} 
        /> */}
        </div>

        {/* Mini Cart and Auth Dialog with conditional rendering */}
        {showMiniCart && <MiniCart cart={cart} onClose={() => setShowMiniCart(false)} />}
        {showAuthDialog && <AuthDialog open={showAuthDialog} onClose={() => setShowAuthDialog(false)} />}

        {/* Snackbar for notifications */}
        <Snackbar
          open={snackbar.open}
          autoHideDuration={6000}
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        >
          <Alert
            onClose={() => setSnackbar({ ...snackbar, open: false })}
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
