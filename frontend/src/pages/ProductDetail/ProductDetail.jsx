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
  CircularProgress,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup
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
import LocationOnIcon from "@mui/icons-material/LocationOn";
import CreditCardIcon from "@mui/icons-material/CreditCard";
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
  const [bidShippingAddress, setBidShippingAddress] = useState({
    fullName: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: ""
  });

  // Add detailed bid form states
  const [bidCardDetails, setBidCardDetails] = useState({
    cardName: "",
    cardNumber: "",
    expiryDate: "",
    cvv: ""
  });

  const [bidPaymentMethod, setBidPaymentMethod] = useState("credit");

  // Raffle-specific states
  const [showRaffleDialog, setShowRaffleDialog] = useState(false);
  const [joiningRaffle, setJoiningRaffle] = useState(false);

  // Auction-specific states
  const [offerAmount, setOfferAmount] = useState('');
  const [showOfferDialog, setShowOfferDialog] = useState(false);
  const [submittingOffer, setSubmittingOffer] = useState(false);
  const [auctionStatus, setAuctionStatus] = useState(null);

  // New auction states for real-time updates
  const [timeLeft, setTimeLeft] = useState(null);
  const [auctionEnded, setAuctionEnded] = useState(false);
  const [refreshingAuctionStatus, setRefreshingAuctionStatus] = useState(false);

  // Auction offer form states (similar to bid forms)
  const [offerShippingAddress, setOfferShippingAddress] = useState({
    fullName: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: ""
  });

  const [offerCardDetails, setOfferCardDetails] = useState({
    cardName: "",
    cardNumber: "",
    expiryDate: "",
    cvv: ""
  });

  const [offerPaymentMethod, setOfferPaymentMethod] = useState("credit");

  const { refreshCart, cart, addToCart: addToCartContext, isAuthenticated, isGuest, guestService } = useAuth();

  // Add form handler functions
  const handleBidShippingAddressChange = (e) => {
    setBidShippingAddress({
      ...bidShippingAddress,
      [e.target.name]: e.target.value
    });
  };

  const handleBidCardDetailsChange = (e) => {
    setBidCardDetails({
      ...bidCardDetails,
      [e.target.name]: e.target.value
    });
  };

  const handleBidPaymentMethodChange = (e) => {
    setBidPaymentMethod(e.target.value);
  };

  // Add form handler functions for auction offers
  const handleOfferShippingAddressChange = (e) => {
    setOfferShippingAddress({
      ...offerShippingAddress,
      [e.target.name]: e.target.value
    });
  };

  const handleOfferCardDetailsChange = (e) => {
    setOfferCardDetails({
      ...offerCardDetails,
      [e.target.name]: e.target.value
    });
  };

  const handleOfferPaymentMethodChange = (e) => {
    setOfferPaymentMethod(e.target.value);
  };

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
      // Add debugging to check user state
      if (isAuthenticated) {
        console.log('[ProductDetail] User is authenticated, attempting to fetch auction status');
        // Check what user data we have in context
        const currentUser = userService.getCurrentUser();
        console.log('[ProductDetail] Current user from service:', currentUser);
      } else {
        console.log('[ProductDetail] User not authenticated, skipping auction status fetch');
      }

      fetchAuctionStatus();
    }
  }, [product, isAuthenticated]);

  // Add effect for auction timer countdown
  useEffect(() => {
    let interval = null;

    if (product?.purchaseType === 'AUCTION' && auctionStatus && !auctionEnded) {
      interval = setInterval(() => {
        const now = Date.now();
        const timeLeftMs = auctionStatus.timeLeftMillis - (now - auctionStatus.fetchedAt);

        if (timeLeftMs <= 0) {
          setTimeLeft(0);
          setAuctionEnded(true);
          clearInterval(interval);
        } else {
          setTimeLeft(timeLeftMs);
        }
      }, 1000);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [auctionStatus, auctionEnded, product]);

  // Add effect for periodic auction status refresh
  useEffect(() => {
    let refreshInterval = null;

    if (product?.purchaseType === 'AUCTION' && !auctionEnded && isAuthenticated) {
      // Refresh auction status every 30 seconds
      refreshInterval = setInterval(() => {
        fetchAuctionStatus();
      }, 30000);
    }

    return () => {
      if (refreshInterval) clearInterval(refreshInterval);
    };
  }, [product, auctionEnded, isAuthenticated]);

  const refreshDiscountedPrice = async () => {
    if (product) {
      await fetchDiscountedPrice(product);
    }
  };

  // Default image for products without images - professional product placeholder
  const DEFAULT_IMAGE = "https://placehold.co/600x400/e2e8f0/1e293b?text=Product+Image";

  const fetchProduct = async () => {
    try {
      // Try to fetch the listing directly by listing ID first
      try {
        // Direct fetch by listing ID is more efficient
        const foundProduct = await productService.getListing(productId);
        if (foundProduct) {
          setProduct(foundProduct);
          setMainImage(foundProduct.images?.[0] || DEFAULT_IMAGE);

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
        setMainImage(foundProduct.images?.[0] || DEFAULT_IMAGE);

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

    // Validate shipping address
    if (!bidShippingAddress.fullName || !bidShippingAddress.addressLine1 ||
      !bidShippingAddress.city || !bidShippingAddress.state ||
      !bidShippingAddress.postalCode || !bidShippingAddress.country) {
      toast({
        title: "Error",
        description: "Please complete all required shipping address fields",
        variant: "destructive",
      });
      return;
    }

    // Validate payment details for credit card
    if (bidPaymentMethod === 'credit') {
      if (!bidCardDetails.cardName || !bidCardDetails.cardNumber ||
        !bidCardDetails.expiryDate || !bidCardDetails.cvv) {
        toast({
          title: "Error",
          description: "Please complete all credit card details",
          variant: "destructive",
        });
        return;
      }
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
      // Prepare payment details based on payment method
      let paymentDetails = '';
      if (bidPaymentMethod === 'credit') {
        paymentDetails = `Credit Card - ${bidCardDetails.cardName} - ****${bidCardDetails.cardNumber.slice(-4)}`;
      } else {
        paymentDetails = 'PayPal Payment';
      }

      // Prepare shipping address string
      const shippingAddressString = `${bidShippingAddress.fullName}, ${bidShippingAddress.addressLine1}${bidShippingAddress.addressLine2 ? ', ' + bidShippingAddress.addressLine2 : ''}, ${bidShippingAddress.city}, ${bidShippingAddress.state} ${bidShippingAddress.postalCode}, ${bidShippingAddress.country}`;

      // Debug logging
      console.log("Submitting bid with product data:", {
        productId: product.id,
        storeId: product.storeId,
        bidAmount: bidAmount,
        shippingAddress: shippingAddressString,
        paymentDetails: paymentDetails,
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
          productId: String(product.id),
          bidAmount: parseFloat(bidAmount),
          quantity: 1,
          shippingAddress: shippingAddressString,
          paymentDetails: paymentDetails
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
        setBidShippingAddress({
          fullName: "",
          addressLine1: "",
          addressLine2: "",
          city: "",
          state: "",
          postalCode: "",
          country: ""
        });
        setBidCardDetails({
          cardName: "",
          cardNumber: "",
          expiryDate: "",
          cvv: ""
        });
        setBidPaymentMethod("credit");
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

  const fetchAuctionStatus = async () => {
    if (!product || !userService.isAuthenticated()) return;

    // Validate product data
    if (!product.id || !product.storeId) {
      console.error("Product ID or Store ID is missing");
      return;
    }

    setRefreshingAuctionStatus(true);

    try {
      let user;

      // Try to get user profile, fallback to getCurrentUser if needed
      try {
        user = await userService.getProfile();
      } catch (profileError) {
        console.warn("Failed to get profile from API, using cached user:", profileError);
        user = userService.getCurrentUser();
      }

      console.log('[fetchAuctionStatus] Raw user data:', user);

      // The backend API expects a userId in the URL but actually uses the username from JWT token
      // So we can use any placeholder ID (like 0) since it's ignored by the backend
      const userIdPlaceholder = 0;

      // Validate that we have at least a username for authentication
      if (!user || !user.userName) {
        console.error("Username not found in user data. User object structure:", JSON.stringify(user, null, 2));

        toast({
          title: "Authentication Error",
          description: "User identification not available. Please log out and log back in.",
          variant: "destructive",
        });
        return;
      }

      console.log("Fetching auction status for:", {
        userName: user.userName,
        userIdPlaceholder: userIdPlaceholder,
        storeId: product.storeId,
        productId: product.id
      });

      // Debug the JWT token being sent
      const token = userService.getToken();
      console.log('[fetchAuctionStatus] JWT Token being sent:', token ? `${token.substring(0, 20)}...` : 'NO TOKEN');
      console.log('[fetchAuctionStatus] Token exists:', !!token);
      console.log('[fetchAuctionStatus] Authorization header will be:', `Bearer ${token ? token.substring(0, 20) + '...' : 'NO TOKEN'}`);

      // Test if authentication works with a simple endpoint first
      console.log('[fetchAuctionStatus] Testing authentication with profile endpoint...');
      try {
        const profileTestResponse = await fetch('http://localhost:8080/api/users/me', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        console.log('[fetchAuctionStatus] Profile test response status:', profileTestResponse.status);
        if (!profileTestResponse.ok) {
          console.error('[fetchAuctionStatus] Authentication test failed - token may be invalid');
          toast({
            title: "Authentication Error",
            description: "Your session has expired. Please log out and log back in.",
            variant: "destructive",
          });
          return;
        }
      } catch (authTestError) {
        console.error('[fetchAuctionStatus] Authentication test error:', authTestError);
        toast({
          title: "Connection Error",
          description: "Cannot connect to server. Please check your connection.",
          variant: "destructive",
        });
        return;
      }

      const response = await fetch(`http://localhost:8080/api/purchases/auction/status/${userIdPlaceholder}/${product.storeId}/${String(product.id)}`, {
        headers: {
          'Authorization': `Bearer ${userService.getToken()}`
        }
      });

      console.log('[fetchAuctionStatus] Response status:', response.status);
      console.log('[fetchAuctionStatus] Response headers:', Object.fromEntries(response.headers.entries()));

      // Check if response is ok before trying to parse JSON
      if (!response.ok) {
        if (response.status === 403) {
          console.error("Access forbidden - check authentication");
          toast({
            title: "Access Denied",
            description: "You don't have permission to view this auction status. Please verify your login.",
            variant: "destructive",
          });
          return;
        } else if (response.status === 404) {
          console.error("Auction not found");
          toast({
            title: "Not Found",
            description: "Auction information not found",
            variant: "destructive",
          });
          return;
        } else {
          const errorText = await response.text();
          console.error(`HTTP error! status: ${response.status}, body: ${errorText}`);
          throw new Error(`HTTP error! status: ${response.status}`);
        }
      }

      // Check if response has content before parsing JSON
      const contentType = response.headers.get('content-type');
      console.log('[fetchAuctionStatus] Response content-type:', contentType);

      if (!contentType || !contentType.includes('application/json')) {
        const responseText = await response.text();
        console.error("Response is not JSON:", responseText);
        throw new Error("Server response is not valid JSON");
      }

      const result = await response.json();
      console.log('[fetchAuctionStatus] API response:', result);

      if (result.success && result.data) {
        const statusData = {
          ...result.data,
          fetchedAt: Date.now() // Track when we fetched this data
        };
        setAuctionStatus(statusData);

        // Update time left and auction ended status
        const timeLeftMs = statusData.timeLeftMillis;
        setTimeLeft(timeLeftMs);
        setAuctionEnded(timeLeftMs <= 0);

        console.log('[fetchAuctionStatus] Auction status updated:', statusData);
      } else {
        console.error("Auction status API returned error:", result);
        if (result.error) {
          toast({
            title: "Error",
            description: result.error,
            variant: "destructive",
          });
        }
      }
    } catch (error) {
      console.error("Error fetching auction status:", error);
      toast({
        title: "Error",
        description: "Could not load auction status. Please try again.",
        variant: "destructive",
      });
    } finally {
      setRefreshingAuctionStatus(false);
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
    const minimumPrice = auctionStatus?.currentMaxOffer || product.price;
    if (parseFloat(offerAmount) <= minimumPrice) {
      toast({
        title: "Error",
        description: `Offer amount must be higher than the current highest bid of $${minimumPrice.toFixed(2)}`,
        variant: "destructive",
      });
      return;
    }

    // Validate shipping address
    if (!offerShippingAddress.fullName || !offerShippingAddress.addressLine1 ||
      !offerShippingAddress.city || !offerShippingAddress.state ||
      !offerShippingAddress.postalCode || !offerShippingAddress.country) {
      toast({
        title: "Error",
        description: "Please complete all required shipping address fields",
        variant: "destructive",
      });
      return;
    }

    // Validate payment details for credit card
    if (offerPaymentMethod === 'credit') {
      if (!offerCardDetails.cardName || !offerCardDetails.cardNumber ||
        !offerCardDetails.expiryDate || !offerCardDetails.cvv) {
        toast({
          title: "Error",
          description: "Please complete all credit card details",
          variant: "destructive",
        });
        return;
      }
    }

    // Check if auction has ended
    if (auctionEnded || (timeLeft !== null && timeLeft <= 0)) {
      toast({
        title: "Error",
        description: "This auction has already ended",
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
      // Prepare payment details based on payment method
      let paymentDetails = '';
      if (offerPaymentMethod === 'credit') {
        paymentDetails = `Credit Card - ${offerCardDetails.cardName} - ****${offerCardDetails.cardNumber.slice(-4)}`;
      } else {
        paymentDetails = 'PayPal Payment';
      }

      // Prepare shipping address string
      const shippingAddressString = `${offerShippingAddress.fullName}, ${offerShippingAddress.addressLine1}${offerShippingAddress.addressLine2 ? ', ' + offerShippingAddress.addressLine2 : ''}, ${offerShippingAddress.city}, ${offerShippingAddress.state} ${offerShippingAddress.postalCode}, ${offerShippingAddress.country}`;

      const response = await fetch('http://localhost:8080/api/purchases/auction/offer', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userService.getToken()}`
        },
        body: JSON.stringify({
          userId: 0, // Placeholder - server uses JWT token username instead
          storeId: parseInt(product.storeId),
          productId: String(product.id),
          offerAmount: parseFloat(offerAmount),
          shippingAddress: shippingAddressString,
          paymentDetails: paymentDetails // Changed from contactInfo to paymentDetails
        })
      });

      const result = await response.json();

      if (result.success) {
        toast({
          title: "Offer Submitted",
          description: `Your offer of $${offerAmount} has been submitted successfully! The current highest bid is now $${offerAmount}.`,
          variant: "default",
        });
        setShowOfferDialog(false);
        setOfferAmount('');

        // Reset form data
        setOfferShippingAddress({
          fullName: "",
          addressLine1: "",
          addressLine2: "",
          city: "",
          state: "",
          postalCode: "",
          country: ""
        });
        setOfferCardDetails({
          cardName: "",
          cardNumber: "",
          expiryDate: "",
          cvv: ""
        });
        setOfferPaymentMethod("credit");

        // Immediately refresh auction status to show new price
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

  // Helper function to format time left
  const formatTimeLeft = (timeInMs) => {
    if (!timeInMs || timeInMs <= 0) return "Auction Ended";

    const totalSeconds = Math.floor(timeInMs / 1000);
    const days = Math.floor(totalSeconds / (24 * 3600));
    const hours = Math.floor((totalSeconds % (24 * 3600)) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    if (days > 0) {
      return `${days}d ${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h ${minutes}m ${seconds}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds}s`;
    } else {
      return `${seconds}s`;
    }
  };

  // Helper function to get auction status color
  const getAuctionStatusColor = (timeInMs) => {
    if (!timeInMs || timeInMs <= 0) return 'error';
    if (timeInMs < 3600000) return 'warning'; // Less than 1 hour
    return 'success';
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
              startIcon={auctionEnded ? <AccessTimeIcon /> : <TrendingUpIcon />}
              onClick={() => setShowOfferDialog(true)}
              disabled={auctionEnded}
              sx={{
                flex: 1,
                py: 1.5,
                fontSize: '1.1rem',
                fontWeight: 'bold',
                bgcolor: auctionEnded ? 'grey.400' : 'info.main',
                '&:hover': {
                  bgcolor: auctionEnded ? 'grey.400' : 'info.dark',
                },
                '&:disabled': {
                  bgcolor: 'grey.400',
                  color: 'white'
                }
              }}
            >
              {auctionEnded ? 'Auction Ended' : 'Place Offer'}
            </Button>
            {auctionStatus && timeLeft !== null && !auctionEnded && (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, ml: 2 }}>
                <AccessTimeIcon fontSize="small" color={getAuctionStatusColor(timeLeft)} />
                <Typography variant="body2" color={getAuctionStatusColor(timeLeft) + '.main'} fontWeight="medium">
                  {formatTimeLeft(timeLeft)}
                </Typography>
              </Box>
            )}
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
                width: '220px',
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
      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
      <main className="container px-4 py-8" style={{ maxWidth: '1600px' }}>
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

          {/* Main content area with image on the left and details on the right */}
          <Grid container spacing={4} sx={{ display: 'flex', flexWrap: 'nowrap' }}>
            {/* Product Images on the left */}
            <Grid item xs={12} md={5} sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', flexShrink: 0 }}>
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
                  src={mainImage || product.images?.[0] || DEFAULT_IMAGE}
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
                {(product.images && product.images.length > 0) ? product.images.map((image, index) => (
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
                )) : (
                  <Box
                    sx={{
                      width: '70px',
                      height: '70px',
                      borderRadius: 1,
                      overflow: 'hidden',
                      border: '2px solid #1976d2',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease'
                    }}
                  >
                    <img
                      src={DEFAULT_IMAGE}
                      alt={`${product.title} default view`}
                      style={{
                        width: '100%',
                        height: '100%',
                        objectFit: 'cover'
                      }}
                    />
                  </Box>
                )}
              </Box>
            </Grid>

            {/* Product Details on the right */}
            <Grid item xs={12} md={7} sx={{ pl: { xs: 0, md: 4 } }}>
              <Paper elevation={0} sx={{ p: { xs: 2, md: 3 }, border: '1px solid rgba(0,0,0,0.08)', borderRadius: 2 }}>
                {/* Price and badges */}
                <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1, mb: 1 }}>
                  {product?.purchaseType === 'BID' ? (
                    <Box>
                      <Typography variant="h4" component="div" sx={{ fontWeight: 'bold', color: 'warning.main' }}>
                        Bid Product
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Submit your offer - no fixed price
                      </Typography>
                      {product.minBidAmount && (
                        <Typography variant="body2" color="warning.dark" sx={{ mt: 0.5 }}>
                          Minimum bid: ${parseFloat(product.minBidAmount).toFixed(2)}
                        </Typography>
                      )}
                    </Box>
                  ) : discountedPrice !== null && discountedPrice < product.price ? (
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
                {product?.purchaseType === 'AUCTION' && (
                  <Box sx={{ mb: 3 }}>
                    <Paper
                      variant="outlined"
                      sx={{
                        p: 3,
                        bgcolor: auctionEnded ? 'grey.50' : 'info.50',
                        borderColor: auctionEnded ? 'grey.300' : 'info.200',
                        border: '2px solid',
                        borderRadius: 2
                      }}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                        <Typography variant="h6" color={auctionEnded ? 'text.secondary' : 'info.main'} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <GavelIcon />
                          {auctionEnded ? 'Auction Ended' : 'Live Auction'}
                        </Typography>
                        {auctionStatus && (
                          <Button
                            size="small"
                            variant="outlined"
                            onClick={fetchAuctionStatus}
                            disabled={refreshingAuctionStatus}
                            sx={{ minWidth: 'auto', px: 1 }}
                          >
                            {refreshingAuctionStatus ? (
                              <CircularProgress size={16} />
                            ) : (
                              '↻'
                            )}
                          </Button>
                        )}
                      </Box>

                      {auctionStatus ? (
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                          {/* Current Highest Bid */}
                          <Box sx={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            p: 2,
                            bgcolor: 'background.paper',
                            borderRadius: 1,
                            border: '1px solid',
                            borderColor: 'divider'
                          }}>
                            <Typography variant="subtitle1" fontWeight="medium">
                              Current Highest Bid
                            </Typography>
                            <Typography variant="h5" fontWeight="bold" color="primary.main">
                              ${(auctionStatus.currentMaxOffer || auctionStatus.startingPrice || product.price).toFixed(2)}
                            </Typography>
                          </Box>

                          {/* Starting Price */}
                          {auctionStatus.startingPrice && (
                            <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                              <Typography variant="body2" color="text.secondary">
                                Starting Price
                              </Typography>
                              <Typography variant="body2" fontWeight="medium">
                                ${auctionStatus.startingPrice.toFixed(2)}
                              </Typography>
                            </Box>
                          )}

                          {/* Time Left */}
                          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <Typography variant="body2" color="text.secondary">
                              {auctionEnded ? 'Auction Status' : 'Time Remaining'}
                            </Typography>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <AccessTimeIcon
                                fontSize="small"
                                color={getAuctionStatusColor(timeLeft)}
                              />
                              <Chip
                                label={timeLeft !== null ? formatTimeLeft(timeLeft) : 'Loading...'}
                                color={getAuctionStatusColor(timeLeft)}
                                size="medium"
                                sx={{ fontWeight: 'bold', minWidth: '120px' }}
                              />
                            </Box>
                          </Box>

                          {/* Additional auction info */}
                          {auctionEnded && (
                            <Box sx={{
                              mt: 1,
                              p: 2,
                              bgcolor: 'warning.50',
                              borderRadius: 1,
                              border: '1px solid',
                              borderColor: 'warning.200'
                            }}>
                              <Typography variant="body2" color="warning.dark" sx={{ textAlign: 'center' }}>
                                🏁 This auction has ended. No more bids can be placed.
                              </Typography>
                            </Box>
                          )}

                          {!auctionEnded && auctionStatus.currentMaxOffer > auctionStatus.startingPrice && (
                            <Box sx={{
                              mt: 1,
                              p: 2,
                              bgcolor: 'success.50',
                              borderRadius: 1,
                              border: '1px solid',
                              borderColor: 'success.200'
                            }}>
                              <Typography variant="body2" color="success.dark" sx={{ textAlign: 'center' }}>
                                🔥 {((auctionStatus.currentMaxOffer - auctionStatus.startingPrice) / auctionStatus.startingPrice * 100).toFixed(1)}% above starting price!
                              </Typography>
                            </Box>
                          )}
                        </Box>
                      ) : (
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                          <CircularProgress size={20} />
                          <Typography variant="body2" color="text.secondary">
                            Loading auction status...
                          </Typography>
                        </Box>
                      )}
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
        <Dialog open={showBidDialog} onClose={() => setShowBidDialog(false)} maxWidth="md" fullWidth>
          <DialogTitle>Place Your Bid</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Enter your bid details for "{product?.title}". Your bid will be reviewed by the seller.
              If your bid is approved, we'll use the shipping and payment information provided to complete your purchase.
            </Typography>

            {product?.minBidAmount && (
              <Box sx={{ mb: 3, p: 2, bgcolor: 'warning.50', borderRadius: 1, border: '1px solid', borderColor: 'warning.200' }}>
                <Typography variant="body2" color="warning.dark">
                  <strong>Minimum bid required:</strong> ${parseFloat(product.minBidAmount).toFixed(2)}
                </Typography>
              </Box>
            )}

            <Grid container spacing={2}>
              {/* Bid Amount */}
              <Grid item xs={12}>
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
                  helperText={
                    product?.minBidAmount
                      ? `Must be at least $${parseFloat(product.minBidAmount).toFixed(2)}`
                      : "Enter your bid amount"
                  }
                  required
                />
              </Grid>

              {/* Shipping Address Section */}
              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <LocationOnIcon sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="h6">
                    Shipping Address
                  </Typography>
                </Box>
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Full Name"
                  name="fullName"
                  value={bidShippingAddress.fullName}
                  onChange={handleBidShippingAddressChange}
                  required
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Address Line 1"
                  name="addressLine1"
                  value={bidShippingAddress.addressLine1}
                  onChange={handleBidShippingAddressChange}
                  required
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Address Line 2 (Optional)"
                  name="addressLine2"
                  value={bidShippingAddress.addressLine2}
                  onChange={handleBidShippingAddressChange}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="City"
                  name="city"
                  value={bidShippingAddress.city}
                  onChange={handleBidShippingAddressChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="State/Province"
                  name="state"
                  value={bidShippingAddress.state}
                  onChange={handleBidShippingAddressChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Postal Code"
                  name="postalCode"
                  value={bidShippingAddress.postalCode}
                  onChange={handleBidShippingAddressChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Country"
                  name="country"
                  value={bidShippingAddress.country}
                  onChange={handleBidShippingAddressChange}
                  required
                />
              </Grid>

              {/* Payment Method Section */}
              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <CreditCardIcon sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="h6">
                    Payment Method
                  </Typography>
                </Box>
              </Grid>

              <Grid item xs={12}>
                <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
                  <FormControl component="fieldset">
                    <RadioGroup
                      value={bidPaymentMethod}
                      onChange={handleBidPaymentMethodChange}
                    >
                      <FormControlLabel
                        value="credit"
                        control={<Radio />}
                        label={
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            <CreditCardIcon sx={{ mr: 1 }} />
                            Credit Card
                          </Box>
                        }
                      />
                      <FormControlLabel
                        value="paypal"
                        control={<Radio />}
                        label={
                          <Box sx={{ display: 'flex', alignItems: 'center' }}>
                            <img
                              src="https://www.paypalobjects.com/webstatic/mktg/logo/pp_cc_mark_37x23.jpg"
                              alt="PayPal"
                              style={{ height: 18, marginRight: 8 }}
                            />
                            PayPal
                          </Box>
                        }
                      />
                    </RadioGroup>
                  </FormControl>
                </Paper>
              </Grid>

              {/* Credit Card Details (shown only if credit card is selected) */}
              {bidPaymentMethod === "credit" && (
                <>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Name on Card"
                      name="cardName"
                      value={bidCardDetails.cardName}
                      onChange={handleBidCardDetailsChange}
                      required
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Card Number"
                      name="cardNumber"
                      value={bidCardDetails.cardNumber}
                      onChange={handleBidCardDetailsChange}
                      required
                      inputProps={{ maxLength: 19 }}
                      placeholder="0000 0000 0000 0000"
                    />
                  </Grid>

                  <Grid item xs={6}>
                    <TextField
                      fullWidth
                      label="Expiry Date"
                      name="expiryDate"
                      value={bidCardDetails.expiryDate}
                      onChange={handleBidCardDetailsChange}
                      required
                      placeholder="MM/YY"
                      inputProps={{ maxLength: 5 }}
                    />
                  </Grid>

                  <Grid item xs={6}>
                    <TextField
                      fullWidth
                      label="CVV"
                      name="cvv"
                      value={bidCardDetails.cvv}
                      onChange={handleBidCardDetailsChange}
                      required
                      inputProps={{ maxLength: 4 }}
                      type="password"
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Box sx={{ display: 'flex', alignItems: 'center', color: 'text.secondary' }}>
                      <SecurityIcon fontSize="small" sx={{ mr: 1 }} />
                      <Typography variant="caption">
                        Your payment information is secure and encrypted
                      </Typography>
                    </Box>
                  </Grid>
                </>
              )}
            </Grid>

            <Box sx={{ mt: 3, p: 2, bgcolor: 'info.50', borderRadius: 1 }}>
              <Typography variant="body2" color="info.dark">
                <strong>Note:</strong> By submitting this bid, you agree to purchase this item at your bid price if approved.
                Your payment and shipping information will be used to complete the transaction.
              </Typography>
            </Box>
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
        <Dialog open={showOfferDialog} onClose={() => setShowOfferDialog(false)} maxWidth="md" fullWidth>
          <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TrendingUpIcon color="primary" />
            Place Your Offer
            {auctionEnded && (
              <Chip label="ENDED" color="error" size="small" />
            )}
          </DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Enter your offer details for "{product?.title}". Your offer must be higher than the current highest bid.
              If your offer wins the auction, we'll use the shipping and payment information provided to complete your purchase.
            </Typography>

            {/* Auction Status Summary */}
            {auctionStatus && (
              <Paper variant="outlined" sx={{ p: 2, mb: 3, bgcolor: auctionEnded ? 'grey.50' : 'success.50' }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="subtitle2" fontWeight="bold">
                      Current Highest Bid
                    </Typography>
                    <Typography variant="h6" color="primary.main" fontWeight="bold">
                      ${(auctionStatus.currentMaxOffer || auctionStatus.startingPrice || product?.price || 0).toFixed(2)}
                    </Typography>
                  </Box>

                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="body2" color="text.secondary">
                      Time Remaining
                    </Typography>
                    <Chip
                      label={timeLeft !== null ? formatTimeLeft(timeLeft) : 'Loading...'}
                      color={getAuctionStatusColor(timeLeft)}
                      size="small"
                      sx={{ fontWeight: 'bold' }}
                    />
                  </Box>

                  {auctionStatus.startingPrice && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="body2" color="text.secondary">
                        Starting Price
                      </Typography>
                      <Typography variant="body2">
                        ${auctionStatus.startingPrice.toFixed(2)}
                      </Typography>
                    </Box>
                  )}
                </Box>
              </Paper>
            )}

            {auctionEnded ? (
              <Alert severity="error" sx={{ mb: 2 }}>
                This auction has ended. No more offers can be placed.
              </Alert>
            ) : (
              <Grid container spacing={2}>
                {/* Offer Amount */}
                <Grid item xs={12}>
                  <TextField
                    label="Offer Amount ($)"
                    type="number"
                    value={offerAmount}
                    onChange={(e) => setOfferAmount(e.target.value)}
                    fullWidth
                    inputProps={{
                      min: ((auctionStatus?.currentMaxOffer || product?.price || 0) + 0.01).toFixed(2),
                      step: "0.01"
                    }}
                    helperText={`Must be higher than $${(auctionStatus?.currentMaxOffer || product?.price || 0).toFixed(2)}`}
                    error={parseFloat(offerAmount) <= (auctionStatus?.currentMaxOffer || product?.price || 0)}
                    required
                  />
                </Grid>

                {/* Shipping Address Section */}
                <Grid item xs={12}>
                  <Divider sx={{ my: 2 }} />
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <LocationOnIcon sx={{ mr: 1, color: 'primary.main' }} />
                    <Typography variant="h6">
                      Shipping Address
                    </Typography>
                  </Box>
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Full Name"
                    name="fullName"
                    value={offerShippingAddress.fullName}
                    onChange={handleOfferShippingAddressChange}
                    required
                  />
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Address Line 1"
                    name="addressLine1"
                    value={offerShippingAddress.addressLine1}
                    onChange={handleOfferShippingAddressChange}
                    required
                  />
                </Grid>

                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Address Line 2 (Optional)"
                    name="addressLine2"
                    value={offerShippingAddress.addressLine2}
                    onChange={handleOfferShippingAddressChange}
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="City"
                    name="city"
                    value={offerShippingAddress.city}
                    onChange={handleOfferShippingAddressChange}
                    required
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="State/Province"
                    name="state"
                    value={offerShippingAddress.state}
                    onChange={handleOfferShippingAddressChange}
                    required
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Postal Code"
                    name="postalCode"
                    value={offerShippingAddress.postalCode}
                    onChange={handleOfferShippingAddressChange}
                    required
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Country"
                    name="country"
                    value={offerShippingAddress.country}
                    onChange={handleOfferShippingAddressChange}
                    required
                  />
                </Grid>

                {/* Payment Method Section */}
                <Grid item xs={12}>
                  <Divider sx={{ my: 2 }} />
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <CreditCardIcon sx={{ mr: 1, color: 'primary.main' }} />
                    <Typography variant="h6">
                      Payment Method
                    </Typography>
                  </Box>
                </Grid>

                <Grid item xs={12}>
                  <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
                    <FormControl component="fieldset">
                      <RadioGroup
                        value={offerPaymentMethod}
                        onChange={handleOfferPaymentMethodChange}
                      >
                        <FormControlLabel
                          value="credit"
                          control={<Radio />}
                          label={
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                              <CreditCardIcon sx={{ mr: 1 }} />
                              Credit Card
                            </Box>
                          }
                        />
                        <FormControlLabel
                          value="paypal"
                          control={<Radio />}
                          label={
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                              <img
                                src="https://www.paypalobjects.com/webstatic/mktg/logo/pp_cc_mark_37x23.jpg"
                                alt="PayPal"
                                style={{ height: 18, marginRight: 8 }}
                              />
                              PayPal
                            </Box>
                          }
                        />
                      </RadioGroup>
                    </FormControl>
                  </Paper>
                </Grid>

                {/* Credit Card Details (shown only if credit card is selected) */}
                {offerPaymentMethod === "credit" && (
                  <>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Name on Card"
                        name="cardName"
                        value={offerCardDetails.cardName}
                        onChange={handleOfferCardDetailsChange}
                        required
                      />
                    </Grid>

                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Card Number"
                        name="cardNumber"
                        value={offerCardDetails.cardNumber}
                        onChange={handleOfferCardDetailsChange}
                        required
                        inputProps={{ maxLength: 19 }}
                        placeholder="0000 0000 0000 0000"
                      />
                    </Grid>

                    <Grid item xs={6}>
                      <TextField
                        fullWidth
                        label="Expiry Date"
                        name="expiryDate"
                        value={offerCardDetails.expiryDate}
                        onChange={handleOfferCardDetailsChange}
                        required
                        placeholder="MM/YY"
                        inputProps={{ maxLength: 5 }}
                      />
                    </Grid>

                    <Grid item xs={6}>
                      <TextField
                        fullWidth
                        label="CVV"
                        name="cvv"
                        value={offerCardDetails.cvv}
                        onChange={handleOfferCardDetailsChange}
                        required
                        inputProps={{ maxLength: 4 }}
                        type="password"
                      />
                    </Grid>

                    <Grid item xs={12}>
                      <Box sx={{ display: 'flex', alignItems: 'center', color: 'text.secondary' }}>
                        <SecurityIcon fontSize="small" sx={{ mr: 1 }} />
                        <Typography variant="caption">
                          Your payment information is secure and encrypted
                        </Typography>
                      </Box>
                    </Grid>
                  </>
                )}

                <Grid item xs={12}>
                  <Box sx={{ mt: 2, p: 2, bgcolor: 'info.50', borderRadius: 1 }}>
                    <Typography variant="body2" color="info.dark">
                      <strong>Note:</strong> By submitting this offer, you agree to purchase this item at your offer price if you win the auction.
                      Your payment and shipping information will be used to complete the transaction if you are the highest bidder when the auction ends.
                    </Typography>
                  </Box>
                </Grid>
              </Grid>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowOfferDialog(false)}>Cancel</Button>
            {!auctionEnded && (
              <Button
                onClick={handleOfferSubmit}
                variant="contained"
                disabled={submittingOffer || parseFloat(offerAmount) <= (auctionStatus?.currentMaxOffer || product?.price || 0)}
                startIcon={submittingOffer ? <CircularProgress size={16} /> : <TrendingUpIcon />}
              >
                {submittingOffer ? 'Submitting...' : 'Submit Offer'}
              </Button>
            )}
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
    </Box>
  );
}
