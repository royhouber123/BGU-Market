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
  Divider,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress
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
import GavelIcon from "@mui/icons-material/Gavel";
import CasinoIcon from "@mui/icons-material/Casino";
import ScheduleIcon from "@mui/icons-material/Schedule";
import TrendingUpIcon from "@mui/icons-material/TrendingUp";
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
  const [discountedPrice, setDiscountedPrice] = useState(null);
  const [loadingDiscount, setLoadingDiscount] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });

  // Bid-specific states
  const [bidAmount, setBidAmount] = useState('');
  const [showBidDialog, setShowBidDialog] = useState(false);
  const [submittingBid, setSubmittingBid] = useState(false);

  // Raffle-specific states
  const [showRaffleDialog, setShowRaffleDialog] = useState(false);
  const [joiningRaffle, setJoiningRaffle] = useState(false);

  // Auction-specific states
  const [offerAmount, setOfferAmount] = useState('');
  const [showOfferDialog, setShowOfferDialog] = useState(false);
  const [submittingOffer, setSubmittingOffer] = useState(false);
  const [auctionStatus, setAuctionStatus] = useState(null);

  const { refreshCart, cart, addToCart: addToCartContext, isAuthenticated, isGuest, guestService } = useAuth();

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

  // Add effect to refetch discount when product changes
  useEffect(() => {
    if (product) {
      fetchDiscountedPrice(product);
    }
  }, [product?.storeId, product?.id]);

  // Refresh discounted price when page becomes visible (in case policies changed)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden && product) {
        fetchDiscountedPrice(product);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [product]);

  // Fetch auction status for auction type products
  useEffect(() => {
    if (product && product.purchaseType === 'AUCTION') {
      fetchAuctionStatus();
    }
  }, [product]);

  const refreshDiscountedPrice = async () => {
    if (product) {
      await fetchDiscountedPrice(product);
    }
  };

  const fetchProduct = async () => {
    try {
      // Try to fetch the listing directly by listing ID first
      try {
        // Direct fetch by listing ID is more efficient
        const foundProduct = await productService.getListing(productId);
        if (foundProduct) {
          setProduct(foundProduct);
          setMainImage(foundProduct.images?.[0] || "");

          // Fetch discounted price after setting the product
          await fetchDiscountedPrice(foundProduct);
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

        // Fetch discounted price after setting the product
        await fetchDiscountedPrice(foundProduct);
      } else {
        console.error("Product not found", productId);
      }
    } catch (error) {
      console.error("Error fetching product:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchDiscountedPrice = async (productData) => {
    if (!productData || !productData.storeId || !productData.id) {
      return;
    }

    setLoadingDiscount(true);
    try {
      const response = await fetch(`http://localhost:8080/api/stores/${productData.storeId}/products/${productData.id}/discounted-price`);
      const apiResponse = await response.json();

      if (apiResponse.success && apiResponse.data !== undefined) {
        const discountPrice = apiResponse.data;
        // Only set discounted price if it's actually different from the original price
        if (discountPrice < productData.price) {
          setDiscountedPrice(discountPrice);
        } else {
          setDiscountedPrice(null);
        }
      } else {
        setDiscountedPrice(null);
      }
    } catch (error) {
      console.warn(`Could not fetch discounted price for product ${productData.id}:`, error);
      setDiscountedPrice(null);
    } finally {
      setLoadingDiscount(false);
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

      // Use the context addToCart method which handles both guest and authenticated users
      await addToCartContext(product, quantity);

      // Refresh user lists check for authenticated users
      if (isAuthenticated) {
        await checkUserLists();
      }

      setIsInCart(true);
      setShowMiniCart(true);

      const effectivePrice = discountedPrice !== null ? discountedPrice : product.price;
      const savings = discountedPrice !== null ? ((product.price - discountedPrice) * quantity) : 0;

      toast({
        title: "Added to cart",
        description: savings > 0
          ? `${quantity} item(s) added to your cart. You saved $${savings.toFixed(2)}!`
          : `${quantity} item(s) added to your cart`,
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

  const handleBidSubmit = async () => {
    if (!bidAmount || parseFloat(bidAmount) <= 0) {
      toast({
        title: "Error",
        description: "Please enter a valid bid amount",
        variant: "destructive",
      });
      return;
    }

    // Check minimum bid amount if product has one
    if (product.minBidAmount && parseFloat(bidAmount) < parseFloat(product.minBidAmount)) {
      toast({
        title: "Error",
        description: `Bid amount must be at least $${parseFloat(product.minBidAmount).toFixed(2)}`,
        variant: "destructive",
      });
      return;
    }

    if (!userService.isAuthenticated()) {
      setShowAuthDialog(true);
      return;
    }

    // Validate product data before submitting bid
    if (!product || !product.id || !product.storeId) {
      toast({
        title: "Error",
        description: "Product information is not available. Please refresh the page and try again.",
        variant: "destructive",
      });
      return;
    }

    setSubmittingBid(true);

    try {
      // Debug logging
      console.log("Submitting bid with product data:", {
        productId: product.id,
        storeId: product.storeId,
        bidAmount: bidAmount,
        productIdType: typeof product.id,
        storeIdType: typeof product.storeId
      });

      const response = await fetch('http://localhost:8080/api/purchases/bid/submit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userService.getToken()}`
        },
        body: JSON.stringify({
          storeId: parseInt(product.storeId),
          productId: String(product.id), // Send as string instead of parseInt
          bidAmount: parseFloat(bidAmount),
          quantity: 1
        })
      });

      const result = await response.json();

      if (result.success) {
        toast({
          title: "Bid Submitted",
          description: `Your bid of $${bidAmount} has been submitted successfully! The seller will review your bid.`,
          variant: "default",
        });
        setShowBidDialog(false);
        setBidAmount('');
      } else {
        throw new Error(result.error || result.message || 'Bid submission failed');
      }
    } catch (error) {
      console.error("Error submitting bid:", error);
      toast({
        title: "Error",
        description: error.message || "Could not submit bid. Please try again.",
        variant: "destructive",
      });
    } finally {
      setSubmittingBid(false);
    }
  };

  const handleOfferSubmit = async () => {
    if (!offerAmount || parseFloat(offerAmount) <= 0) {
      toast({
        title: "Error",
        description: "Please enter a valid offer amount",
        variant: "destructive",
      });
      return;
    }

    // Check minimum price based on current auction status or product price
    const minimumPrice = auctionStatus?.currentHighestBid || product.price;
    if (parseFloat(offerAmount) <= minimumPrice) {
      toast({
        title: "Error",
        description: `Offer amount must be higher than the current price of $${minimumPrice.toFixed(2)}`,
        variant: "destructive",
      });
      return;
    }

    if (!userService.isAuthenticated()) {
      setShowAuthDialog(true);
      return;
    }

    // Validate product data before submitting offer
    if (!product || !product.id || !product.storeId) {
      toast({
        title: "Error",
        description: "Product information is not available. Please refresh the page and try again.",
        variant: "destructive",
      });
      return;
    }

    setSubmittingOffer(true);

    try {
      const response = await fetch('http://localhost:8080/api/purchases/auction/offer', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userService.getToken()}`
        },
        body: JSON.stringify({
          storeId: parseInt(product.storeId),
          productId: String(product.id), // Send as string instead of parseInt
          offerAmount: parseFloat(offerAmount)
        })
      });

      const result = await response.json();

      if (result.success) {
        toast({
          title: "Offer Submitted",
          description: `Your offer of $${offerAmount} has been submitted successfully! You'll be notified if you win the auction.`,
          variant: "default",
        });
        setShowOfferDialog(false);
        setOfferAmount('');
        // Refresh auction status after submitting offer
        await fetchAuctionStatus();
      } else {
        throw new Error(result.error || result.message || 'Offer submission failed');
      }
    } catch (error) {
      console.error("Error submitting offer:", error);
      toast({
        title: "Error",
        description: error.message || "Could not submit offer. Please try again.",
        variant: "destructive",
      });
    } finally {
      setSubmittingOffer(false);
    }
  };

  const fetchAuctionStatus = async () => {
    if (!product || !userService.isAuthenticated()) return;

    // Validate product data
    if (!product.id || !product.storeId) {
      console.error("Product ID or Store ID is missing");
      return;
    }

    try {
      const user = await userService.getProfile();
      const response = await fetch(`http://localhost:8080/api/purchases/auction/status/${user.id}/${product.storeId}/${String(product.id)}`, {
        headers: {
          'Authorization': `Bearer ${userService.getToken()}`
        }
      });

      const result = await response.json();

      if (result.success && result.data) {
        setAuctionStatus(result.data);
      }
    } catch (error) {
      console.error("Error fetching auction status:", error);
    }
  };

  const handleRaffleJoin = async () => {
    if (!userService.isAuthenticated()) {
      setShowAuthDialog(true);
      return;
    }

    setJoiningRaffle(true);

    try {
      // Call raffle join API - this would need to be implemented
      // For now, we'll show a placeholder message
      toast({
        title: "Raffle Entry",
        description: "Raffle functionality is coming soon! Your interest has been noted.",
        variant: "default",
      });
      setShowRaffleDialog(false);
    } catch (error) {
      console.error("Error joining raffle:", error);
      toast({
        title: "Error",
        description: error.message || "Could not join raffle",
        variant: "destructive",
      });
    } finally {
      setJoiningRaffle(false);
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

  const getPurchaseTypeDisplay = () => {
    if (!product?.purchaseType) return null;

    switch (product.purchaseType) {
      case 'BID':
        return {
          label: 'Bidding Product',
          color: 'warning',
          icon: <GavelIcon />,
          description: 'You can submit a bid for this item'
        };
      case 'AUCTION':
        return {
          label: 'Auction Product',
          color: 'info',
          icon: <ScheduleIcon />,
          description: 'Time-limited auction - highest bidder wins'
        };
      case 'RAFFLE':
        return {
          label: 'Raffle Product',
          color: 'secondary',
          icon: <CasinoIcon />,
          description: 'Join the raffle for a chance to win'
        };
      default:
        return {
          label: 'Regular Product',
          color: 'primary',
          icon: <ShoppingCartIcon />,
          description: 'Available for immediate purchase'
        };
    }
  };

  const renderActionButtons = () => {
    if (!product) return null;

    const purchaseType = product.purchaseType || 'REGULAR';
    const typeDisplay = getPurchaseTypeDisplay();

    // Check if guest can interact with this product
    if (isGuest && !guestService.canGuestInteract(product)) {
      return (
        <Box sx={{ mb: 4 }}>
          <Alert severity="info" sx={{ mb: 2 }}>
            <Typography variant="body2">
              Guests can only purchase regular products. Please{' '}
              <Button
                color="inherit"
                onClick={() => setShowAuthDialog(true)}
                sx={{ textDecoration: 'underline', p: 0, minWidth: 'auto' }}
              >
                login or register
              </Button>
              {' '}to interact with {typeDisplay?.label?.toLowerCase()} products.
            </Typography>
          </Alert>
        </Box>
      );
    }

    switch (purchaseType) {
      case 'BID':
        return (
          <>
            <Button
              variant="contained"
              size="large"
              startIcon={<GavelIcon />}
              onClick={() => setShowBidDialog(true)}
              sx={{
                flex: 1,
                py: 1.5,
                fontSize: '1.1rem',
                fontWeight: 'bold',
                bgcolor: 'warning.main',
                '&:hover': {
                  bgcolor: 'warning.dark',
                }
              }}
            >
              Place Bid
            </Button>
          </>
        );

      case 'RAFFLE':
        return (
          <>
            <Button
              variant="contained"
              size="large"
              startIcon={<CasinoIcon />}
              onClick={() => setShowRaffleDialog(true)}
              sx={{
                flex: 1,
                py: 1.5,
                fontSize: '1.1rem',
                fontWeight: 'bold',
                bgcolor: 'secondary.main',
                '&:hover': {
                  bgcolor: 'secondary.dark',
                }
              }}
            >
              Join Raffle
            </Button>
          </>
        );

      case 'AUCTION':
        return (
          <>
            <Button
              variant="contained"
              size="large"
              startIcon={<TrendingUpIcon />}
              onClick={() => setShowOfferDialog(true)}
              sx={{
                flex: 1,
                py: 1.5,
                fontSize: '1.1rem',
                fontWeight: 'bold',
                bgcolor: 'info.main',
                '&:hover': {
                  bgcolor: 'info.dark',
                }
              }}
            >
              Place Offer
            </Button>
          </>
        );

      default: // REGULAR
        return (
          <>
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

            {/* Total Price Display */}
            {quantity > 1 && (
              <Box sx={{ display: 'flex', alignItems: 'center', ml: 2 }}>
                <Typography variant="body2" color="text.secondary" sx={{ mr: 1 }}>
                  Total:
                </Typography>
                <Typography variant="body1" fontWeight="medium">
                  ${((discountedPrice !== null ? discountedPrice : product.price) * quantity).toFixed(2)}
                  {discountedPrice !== null && (
                    <Typography
                      component="span"
                      variant="body2"
                      sx={{
                        ml: 1,
                        textDecoration: 'line-through',
                        color: 'text.secondary'
                      }}
                    >
                      ${(product.price * quantity).toFixed(2)}
                    </Typography>
                  )}
                </Typography>
              </Box>
            )}

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
          </>
        );
    }
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

  const typeDisplay = getPurchaseTypeDisplay();

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Header />
      <main className="container mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow-sm p-6 md:p-8">
          {/* Product Title at the top */}
          <Typography variant="h4" component="h1" sx={{ mb: 3, fontWeight: 'bold', textAlign: 'center' }}>
            {product.title}
          </Typography>

          {/* Product Type Badge */}
          {typeDisplay && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
              <Chip
                icon={typeDisplay.icon}
                label={typeDisplay.label}
                color={typeDisplay.color}
                variant="filled"
                size="medium"
                sx={{ fontSize: '1rem', py: 1, px: 2 }}
              />
            </Box>
          )}

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
                  {discountedPrice !== null && discountedPrice < product.price ? (
                    <>
                      <Typography
                        variant="h4"
                        component="div"
                        sx={{ fontWeight: 'bold', color: 'blue' }}
                      >
                        ${discountedPrice?.toFixed(2) || "0.00"}
                      </Typography>
                      <Typography
                        variant="h6"
                        component="div"
                        sx={{
                          textDecoration: 'line-through',
                          color: 'text.secondary',
                          fontWeight: 'normal'
                        }}
                      >
                        ${product.price?.toFixed(2) || "0.00"}
                      </Typography>
                      <Chip
                        label={`${Math.round(((product.price - discountedPrice) / product.price) * 100)}% OFF`}
                        color="error"
                        size="small"
                        sx={{ ml: 1 }}
                      />
                    </>
                  ) : (
                    <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
                      ${product.price?.toFixed(2) || "0.00"}
                      {loadingDiscount && (
                        <Box
                          component="span"
                          sx={{
                            ml: 1,
                            display: 'inline-block',
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
                      )}
                    </Typography>
                  )}
                </Box>

                {/* Product Type Description */}
                {typeDisplay && (
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                      {typeDisplay.description}
                    </Typography>
                  </Box>
                )}

                {/* Availability Info */}
                <Box sx={{ mb: 3 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                    <InventoryIcon fontSize="small" color={product?.quantity > 0 ? 'success' : 'error'} />
                    <Typography variant="body2" color={product?.quantity > 0 ? 'success.main' : 'error.main'}>
                      {product?.quantity > 0 ? `${product.quantity} in stock` : 'Out of stock'}
                    </Typography>
                  </Box>
                </Box>

                {/* Auction Status Info (for auction products) */}
                {product?.purchaseType === 'AUCTION' && auctionStatus && (
                  <Box sx={{ mb: 3 }}>
                    <Paper variant="outlined" sx={{ p: 2, bgcolor: 'info.50', borderColor: 'info.200' }}>
                      <Typography variant="subtitle2" color="info.main" gutterBottom>
                        Auction Status
                      </Typography>
                      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="body2" color="text.secondary">Current Highest Bid</Typography>
                          <Typography variant="body2" fontWeight="medium">
                            ${auctionStatus.currentHighestBid?.toFixed(2) || product.price.toFixed(2)}
                          </Typography>
                        </Box>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                          <Typography variant="body2" color="text.secondary">Status</Typography>
                          <Chip
                            label={auctionStatus.status || 'ACTIVE'}
                            color={auctionStatus.isActive ? 'success' : 'error'}
                            size="small"
                          />
                        </Box>
                        {auctionStatus.endTime && (
                          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                            <Typography variant="body2" color="text.secondary">Ends</Typography>
                            <Typography variant="body2">
                              {format(new Date(auctionStatus.endTime), "MMM d, yyyy 'at' h:mm a")}
                            </Typography>
                          </Box>
                        )}
                      </Box>
                    </Paper>
                  </Box>
                )}

                {/* Minimum Bid Info (for bid products) */}
                {product?.purchaseType === 'BID' && product.minBidAmount && (
                  <Box sx={{ mb: 3 }}>
                    <Paper variant="outlined" sx={{ p: 2, bgcolor: 'warning.50', borderColor: 'warning.200' }}>
                      <Typography variant="body2" color="warning.dark">
                        <strong>Minimum Bid:</strong> ${parseFloat(product.minBidAmount).toFixed(2)}
                      </Typography>
                    </Paper>
                  </Box>
                )}

                {/* Action Buttons - Type-specific */}
                <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
                  {renderActionButtons()}

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
                                <Typography variant="body2" color="text.secondary">Type</Typography>
                                <Typography variant="body2">{typeDisplay?.label || "Regular Product"}</Typography>
                              </Box>
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

        {/* Bid Dialog */}
        <Dialog open={showBidDialog} onClose={() => setShowBidDialog(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Place Your Bid</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Enter your bid amount for "{product?.title}". Your bid will be reviewed by the seller.
            </Typography>

            {product?.minBidAmount && (
              <Box sx={{ mb: 2, p: 2, bgcolor: 'warning.50', borderRadius: 1, border: '1px solid', borderColor: 'warning.200' }}>
                <Typography variant="body2" color="warning.dark">
                  <strong>Minimum bid required:</strong> ${parseFloat(product.minBidAmount).toFixed(2)}
                </Typography>
              </Box>
            )}

            <TextField
              label="Bid Amount ($)"
              type="number"
              value={bidAmount}
              onChange={(e) => setBidAmount(e.target.value)}
              fullWidth
              inputProps={{
                min: product?.minBidAmount || "0.01",
                step: "0.01"
              }}
              sx={{ mb: 2 }}
              helperText={
                product?.minBidAmount
                  ? `Must be at least $${parseFloat(product.minBidAmount).toFixed(2)}`
                  : "Enter your bid amount"
              }
            />
            <Typography variant="caption" color="text.secondary">
              Current price: ${product?.price?.toFixed(2)}
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowBidDialog(false)}>Cancel</Button>
            <Button
              onClick={handleBidSubmit}
              variant="contained"
              disabled={submittingBid}
              startIcon={submittingBid ? <CircularProgress size={16} /> : <GavelIcon />}
            >
              {submittingBid ? 'Submitting...' : 'Submit Bid'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Auction Offer Dialog */}
        <Dialog open={showOfferDialog} onClose={() => setShowOfferDialog(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Place Your Offer</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Enter your offer amount for "{product?.title}". Your offer must be higher than the current highest bid.
            </Typography>

            {auctionStatus && (
              <Box sx={{ mb: 2, p: 2, bgcolor: 'info.50', borderRadius: 1, border: '1px solid', borderColor: 'info.200' }}>
                <Typography variant="body2" color="info.dark">
                  <strong>Current highest bid:</strong> ${(auctionStatus.currentHighestBid || product?.price || 0).toFixed(2)}
                </Typography>
                <Typography variant="body2" color="info.dark" sx={{ mt: 0.5 }}>
                  Your offer must be higher than this amount.
                </Typography>
              </Box>
            )}

            <TextField
              label="Offer Amount ($)"
              type="number"
              value={offerAmount}
              onChange={(e) => setOfferAmount(e.target.value)}
              fullWidth
              inputProps={{
                min: ((auctionStatus?.currentHighestBid || product?.price || 0) + 0.01).toFixed(2),
                step: "0.01"
              }}
              sx={{ mb: 2 }}
              helperText={`Must be higher than $${(auctionStatus?.currentHighestBid || product?.price || 0).toFixed(2)}`}
            />

            {auctionStatus?.endTime && (
              <Typography variant="caption" color="text.secondary">
                Auction ends: {format(new Date(auctionStatus.endTime), "MMM d, yyyy 'at' h:mm a")}
              </Typography>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowOfferDialog(false)}>Cancel</Button>
            <Button
              onClick={handleOfferSubmit}
              variant="contained"
              disabled={submittingOffer}
              startIcon={submittingOffer ? <CircularProgress size={16} /> : <TrendingUpIcon />}
            >
              {submittingOffer ? 'Submitting...' : 'Submit Offer'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Raffle Dialog */}
        <Dialog open={showRaffleDialog} onClose={() => setShowRaffleDialog(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Join Raffle</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Join the raffle for "{product?.title}" for a chance to win at the listed price of ${product?.price?.toFixed(2)}.
            </Typography>
            <Typography variant="body2" sx={{ mb: 2 }}>
              How it works:
            </Typography>
            <Typography variant="body2" component="ul" sx={{ pl: 2, mb: 2 }}>
              <li>Enter the raffle by clicking "Join"</li>
              <li>Wait for the raffle period to end</li>
              <li>Winners are selected randomly</li>
              <li>If you win, you'll be notified and can complete the purchase</li>
            </Typography>
            <Typography variant="body2" color="warning.main">
              Note: Raffle functionality is currently in development.
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowRaffleDialog(false)}>Cancel</Button>
            <Button
              onClick={handleRaffleJoin}
              variant="contained"
              disabled={joiningRaffle}
              startIcon={joiningRaffle ? <CircularProgress size={16} /> : <CasinoIcon />}
            >
              {joiningRaffle ? 'Joining...' : 'Join Raffle'}
            </Button>
          </DialogActions>
        </Dialog>

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
