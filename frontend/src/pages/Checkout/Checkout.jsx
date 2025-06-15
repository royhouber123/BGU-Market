import React, { useState, useEffect, useCallback } from "react";
import Header from "../../components/Header/Header";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import PurchaseDialog from "../../components/PurchaseDialog/PurchaseDialog";
import purchaseService from "../../services/purchaseService";
import guestService from "../../services/guestService";
import { productService } from "../../services/productService";
import { useAuth } from "../../contexts/AuthContext";
import { fetchDiscountedPrice, getEffectivePrice, hasDiscount, calculateSavings, formatPrice } from "../../utils/priceUtils";
import {
  Button,
  TextField,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup,
  Divider,
  Box,
  Typography,
  Paper,
  Alert,
  Grid,
  Snackbar,
  Checkbox,
  Chip
} from "@mui/material";
import CreditCardIcon from "@mui/icons-material/CreditCard";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import SecurityIcon from "@mui/icons-material/Security";
import { useNavigate, Link } from "react-router-dom";
import { createPageUrl } from "../../utils";
import './Checkout.css';

export default function Checkout() {
  const { cart, currentUser, refreshCart, isGuest } = useAuth();
  const [loading, setLoading] = useState(true);
  const [cartWithPrices, setCartWithPrices] = useState([]);
  const [loadingPrices, setLoadingPrices] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState("credit");
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [purchaseDialog, setPurchaseDialog] = useState({
    open: false,
    success: false,
    title: '',
    message: '',
    details: ''
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });

  const [shippingAddress, setShippingAddress] = useState({
    fullName: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: ""
  });

  const [billingAddress, setBillingAddress] = useState({
    fullName: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    state: "",
    postalCode: "",
    country: ""
  });

  const [cardDetails, setCardDetails] = useState({
    cardName: "",
    cardNumber: "",
    expiryDate: "",
    cvv: ""
  });

  const [useSameAddress, setUseSameAddress] = useState(true);
  const [processingPayment, setProcessingPayment] = useState(false);
  const navigate = useNavigate();

  const loadInitialData = useCallback(async () => {
    try {
      // Pre-fill name if available
      if (currentUser?.fullName) {
        setShippingAddress(prev => ({
          ...prev,
          fullName: currentUser.fullName
        }));
      }
    } catch (error) {
      console.error("Error loading initial data:", error);
    }
    setLoading(false);
  }, [currentUser]);

  useEffect(() => {
    loadInitialData();
  }, [loadInitialData]);

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
      const cartItemsWithPrices = await Promise.all(
        cart.map(async (item) => {
          // Create a product object for the price utility
          const product = {
            id: item.productId,
            storeId: item.storeId,
            price: item.price // Original stored price
          };

          // Fetch current discounted price
          const discountedPrice = await fetchDiscountedPrice(product);

          return {
            ...item,
            originalPrice: item.price,
            discountedPrice,
            effectivePrice: getEffectivePrice(product, discountedPrice),
            hasDiscount: hasDiscount(product, discountedPrice),
            savings: calculateSavings(product, discountedPrice)
          };
        })
      );

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

  // Debug effect to monitor purchaseDialog changes
  useEffect(() => {
    console.log("🔍 PurchaseDialog state changed:", purchaseDialog);
    if (purchaseDialog.open) {
      console.log("🚨 Dialog should be visible now!");
    }
  }, [purchaseDialog]);

  // Debug effect to monitor cart and user state
  useEffect(() => {
    console.log("Cart state:", cart);
    console.log("Current user:", currentUser);
    console.log("Auth token:", localStorage.getItem('token'));
  }, [cart, currentUser]);

  const handleShippingAddressChange = (e) => {
    setShippingAddress({
      ...shippingAddress,
      [e.target.name]: e.target.value
    });
  };

  const handleBillingAddressChange = (e) => {
    setBillingAddress({
      ...billingAddress,
      [e.target.name]: e.target.value
    });
  };

  const handleCardDetailsChange = (e) => {
    setCardDetails({
      ...cardDetails,
      [e.target.name]: e.target.value
    });
  };

  const handlePaymentMethodChange = (e) => {
    setPaymentMethod(e.target.value);
  };

  const placeOrder = async () => {
    console.log("=== PLACE ORDER STARTED ===");
    console.log("Current user:", currentUser);
    console.log("Is guest:", isGuest);
    console.log("Cart contents:", cart);

    setProcessingPayment(true);

    try {
      // Prepare payment details for backend external API format
      let paymentDetails = '';
      
      if (paymentMethod === 'credit') {
        // Format: currency,amount,cardNumber,month,year,holder,cvv
        const currency = 'USD';
        const amount = Math.round(calculateGrandTotal() * 100) / 100;
        
        // Parse expiry date (MM/YY) to get month and full year
        const [month, year] = cardDetails.expiryDate.split('/');
        const fullYear = year ? `20${year}` : new Date().getFullYear().toString();
        
        // Clean card number (remove spaces and dashes)
        const cleanCardNumber = cardDetails.cardNumber.replace(/\s|-/g, '');
        
        paymentDetails = `${currency},${amount},${cleanCardNumber},${month},${fullYear},${cardDetails.cardName},${cardDetails.cvv}`;
      } else {
        paymentDetails = 'PayPal Payment';
      }

      // Prepare shipping address for backend
      // Keep your original format but assign to correct variable name
      const shippingAddressForBackend = `${shippingAddress.addressLine1}, ${shippingAddress.city}, ${shippingAddress.state} ${shippingAddress.country}, ${shippingAddress.postalCode}`;

      console.log("💳 Payment details:", paymentDetails);
      console.log("📦 Shipping address:", shippingAddressForBackend);

      let result;

      if (isGuest) {
        // Guest checkout - extract email
        let guestEmail = shippingAddress.fullName;

        // Simple email validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(guestEmail)) {
          throw new Error("Please provide a valid email address in the 'Full Name' field for guest checkout");
        }

        console.log("🚀 Processing guest checkout with email:", guestEmail);
        result = await guestService.completeGuestCheckout(
          guestEmail,
          paymentDetails,
          shippingAddressForBackend
        );

        // Initialize new guest session after purchase
        console.log("🔄 Initializing new guest session after purchase...");
        await guestService.registerGuest();
      } else {
        // Authenticated user checkout
        console.log("🚀 Making API call to purchase service...");
        result = await purchaseService.executePurchase(
          paymentDetails,
          shippingAddressForBackend
        );
      }

      console.log("✅ Purchase completed successfully:", result);

      // Clear cart and show success
      await refreshCart();
      
      setPurchaseDialog({
        open: true,
        success: true,
        title: 'Order Placed Successfully!',
        message: isGuest
          ? `Your guest order has been confirmed! Your email has been registered for this purchase.`
          : 'Your order has been confirmed and will be processed soon.',
        details: `Order Details: ${result.message || result.data || 'Purchase completed'}`
      });

    } catch (error) {
      console.error("❌ Error placing order:", error);

      setPurchaseDialog({
        open: true,
        success: false,
        title: 'Order Failed',
        message: 'There was a problem placing your order. Please try again.',
        details: error.message || 'Unknown error occurred'
      });

    } finally {
      setProcessingPayment(false);
    }
  };

  const handlePurchaseDialogClose = () => {
    setPurchaseDialog(prev => ({ ...prev, open: false }));
  };

  const handleContinueShopping = () => {
    // Navigate to dashboard for continue shopping
    navigate(createPageUrl("Dashboard"));
  };

  const handleViewOrderDetails = () => {
    // Close the dialog and let user continue shopping
    setPurchaseDialog(prev => ({ ...prev, open: false }));
  };

  const calculateTotal = () => {
    const displayCart = cartWithPrices.length > 0 ? cartWithPrices : cart;
    return displayCart.reduce((total, item) => {
      const price = Number(item.effectivePrice) || Number(item.price) || 0;
      const quantity = Number(item.quantity) || 0;
      return total + (price * quantity);
    }, 0);
  };

  const calculateTotalSavings = () => {
    return cartWithPrices.reduce((total, item) => {
      const savings = Number(item.savings) || 0;
      const quantity = Number(item.quantity) || 0;
      return total + (savings * quantity);
    }, 0);
  };

  const calculateTax = () => {
    return calculateTotal() * 0.07; // 7% tax
  };

  const calculateShipping = () => {
    // Free shipping over $50
    return calculateTotal() > 50 ? 0 : 5.99;
  };

  const calculateGrandTotal = () => {
    return calculateTotal() + calculateTax() + calculateShipping();
  };

  // Form validation
  const isShippingComplete = () => {
    return (
      shippingAddress.fullName &&
      shippingAddress.addressLine1 &&
      shippingAddress.city &&
      shippingAddress.state &&
      shippingAddress.postalCode &&
      shippingAddress.country
    );
  };

  const isPaymentComplete = () => {
    if (paymentMethod === "paypal") return true;
    return (
      cardDetails.cardName &&
      cardDetails.cardNumber &&
      cardDetails.expiryDate &&
      cardDetails.cvv
    );
  };

  const isFormComplete = () => {
    return isShippingComplete() && isPaymentComplete();
  };

  if (processingPayment) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Header />
        <main className="container mx-auto px-4 py-12">
          <div className="max-w-2xl mx-auto text-center">
            <Typography variant="h5" gutterBottom>
              Processing your order...
            </Typography>
            <Typography variant="body1" color="textSecondary" gutterBottom>
              Please do not close this page.
            </Typography>
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </Box>
          </div>
        </main>

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

        {/* Auth Dialog */}
        {showAuthDialog && <AuthDialog open={showAuthDialog} onClose={() => setShowAuthDialog(false)} />}
      </Box>
    );
  }

  if (loading) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Header />
        <main className="container mx-auto px-4 py-8">
          <div className="flex flex-col gap-6">
            <Box sx={{ height: 32, bgcolor: 'grey.200', borderRadius: 1, width: '25%' }} />
            <Box sx={{ height: 400, bgcolor: 'grey.200', borderRadius: 1 }} />
          </div>
        </main>
      </Box>
    );
  }

  // Show empty cart message only if cart is empty AND we're not in a purchase process AND dialog is not open
  if (cart.length === 0 && !processingPayment && !purchaseDialog.open) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Header />
        <main className="container mx-auto px-4 py-8">
          <Box sx={{ textAlign: 'center', py: 8 }}>
            <ShoppingBagIcon sx={{ fontSize: 48, color: 'text.disabled', mb: 2 }} />
            <Typography variant="h5" gutterBottom>
              Your cart is empty
            </Typography>
            <Typography variant="body1" color="textSecondary" gutterBottom>
              Add some items to your cart to proceed with checkout.
            </Typography>
            <Button
              component={Link}
              to="/"
              variant="contained"
              color="primary"
              sx={{ mt: 2 }}
            >
              Continue Shopping
            </Button>
          </Box>
        </main>
      </Box>
    );
  }

  const displayCart = cartWithPrices.length > 0 ? cartWithPrices : cart;

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Header />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <Typography variant="h4" component="h1" gutterBottom>
            Checkout
          </Typography>
          <Typography variant="body1" color="textSecondary">
            Complete your purchase by providing shipping and payment details
          </Typography>
        </div>

        <Grid container spacing={3}>
          <Grid item xs={12} lg={7}>
            <Paper sx={{ p: 3, borderRadius: 2 }}>
              {/* Shipping Address Section */}
              <Box sx={{ mb: 4 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                  <LocationOnIcon sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="h6">
                    Shipping Address
                  </Typography>
                </Box>

                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label={isGuest ? "Email Address" : "Full Name"}
                      name="fullName"
                      value={shippingAddress.fullName}
                      onChange={handleShippingAddressChange}
                      required
                      helperText={isGuest ? "We'll use this email to register your guest account" : ""}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Address Line 1"
                      name="addressLine1"
                      value={shippingAddress.addressLine1}
                      onChange={handleShippingAddressChange}
                      required
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      label="Address Line 2 (Optional)"
                      name="addressLine2"
                      value={shippingAddress.addressLine2}
                      onChange={handleShippingAddressChange}
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="City"
                      name="city"
                      value={shippingAddress.city}
                      onChange={handleShippingAddressChange}
                      required
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="State/Province"
                      name="state"
                      value={shippingAddress.state}
                      onChange={handleShippingAddressChange}
                      required
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="Postal Code"
                      name="postalCode"
                      value={shippingAddress.postalCode}
                      onChange={handleShippingAddressChange}
                      required
                    />
                  </Grid>

                  <Grid item xs={12} sm={6}>
                    <TextField
                      fullWidth
                      label="Country"
                      name="country"
                      value={shippingAddress.country}
                      onChange={handleShippingAddressChange}
                      required
                    />
                  </Grid>
                </Grid>
              </Box>

              <Divider sx={{ my: 4 }} />

              {/* Payment Method Section */}
              <Box sx={{ mb: 4 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                  <CreditCardIcon sx={{ mr: 1, color: 'primary.main' }} />
                  <Typography variant="h6">
                    Payment Method
                  </Typography>
                </Box>

                <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
                  <FormControl component="fieldset">
                    <RadioGroup
                      value={paymentMethod}
                      onChange={handlePaymentMethodChange}
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

                {paymentMethod === "credit" && (
                  <Paper variant="outlined" sx={{ p: 2, mb: 3 }}>
                    <Grid container spacing={2}>
                      <Grid item xs={12}>
                        <TextField
                          fullWidth
                          label="Name on Card"
                          name="cardName"
                          value={cardDetails.cardName}
                          onChange={handleCardDetailsChange}
                          required
                        />
                      </Grid>

                      <Grid item xs={12}>
                        <TextField
                          fullWidth
                          label="Card Number"
                          name="cardNumber"
                          value={cardDetails.cardNumber}
                          onChange={handleCardDetailsChange}
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
                          value={cardDetails.expiryDate}
                          onChange={handleCardDetailsChange}
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
                          value={cardDetails.cvv}
                          onChange={handleCardDetailsChange}
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
                    </Grid>
                  </Paper>
                )}
              </Box>

              <Divider sx={{ my: 4 }} />

              {/* Billing Address Section */}
              <Box sx={{ mb: 4 }}>
                <Typography variant="h6" gutterBottom>
                  Billing Address
                </Typography>

                <FormControlLabel
                  control={
                    <Checkbox
                      checked={useSameAddress}
                      onChange={(e) => setUseSameAddress(e.target.checked)}
                    />
                  }
                  label="Same as shipping address"
                />

                {!useSameAddress && (
                  <Grid container spacing={2} sx={{ mt: 2 }}>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Full Name"
                        name="fullName"
                        value={billingAddress.fullName}
                        onChange={handleBillingAddressChange}
                        required
                      />
                    </Grid>

                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Address Line 1"
                        name="addressLine1"
                        value={billingAddress.addressLine1}
                        onChange={handleBillingAddressChange}
                        required
                      />
                    </Grid>

                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Address Line 2 (Optional)"
                        name="addressLine2"
                        value={billingAddress.addressLine2}
                        onChange={handleBillingAddressChange}
                      />
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="City"
                        name="city"
                        value={billingAddress.city}
                        onChange={handleBillingAddressChange}
                        required
                      />
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="State/Province"
                        name="state"
                        value={billingAddress.state}
                        onChange={handleBillingAddressChange}
                        required
                      />
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Postal Code"
                        name="postalCode"
                        value={billingAddress.postalCode}
                        onChange={handleBillingAddressChange}
                        required
                      />
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <TextField
                        fullWidth
                        label="Country"
                        name="country"
                        value={billingAddress.country}
                        onChange={handleBillingAddressChange}
                        required
                      />
                    </Grid>
                  </Grid>
                )}
              </Box>

              <Divider sx={{ my: 4 }} />

              {/* Place Order Button */}
              <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                <Button
                  variant="contained"
                  color="primary"
                  size="large"
                  onClick={placeOrder}
                  disabled={!isFormComplete()}
                  sx={{ px: 4, py: 1.5 }}
                >
                  {isGuest ? `Place Order as Guest - $${formatPrice(calculateGrandTotal())}` : `Place Order - $${formatPrice(calculateGrandTotal())}`}
                </Button>
              </Box>
            </Paper>
          </Grid>

          <Grid item xs={12} lg={5}>
            <Box sx={{ position: 'sticky', top: 24 }}>
              <Paper sx={{ p: 3, borderRadius: 2 }}>
                <Typography variant="h6" component="h2" gutterBottom>
                  Order Summary
                </Typography>

                <Box sx={{ mb: 3, maxHeight: 400, overflow: 'auto' }}>
                  {displayCart.map(item => (
                    <Box key={item.productId} sx={{ display: 'flex', mb: 2, pb: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
                      <Box
                        component="img"
                        src={item.image || "https://via.placeholder.com/60"}
                        alt={item.title}
                        sx={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 1, mr: 2 }}
                      />
                      <Box sx={{ flexGrow: 1 }}>
                        <Typography variant="subtitle2" gutterBottom>
                          {item.title}
                        </Typography>
                        <Typography variant="body2" color="textSecondary">
                          Qty: {item.quantity}
                        </Typography>

                        {/* Price Display with Discount Info */}
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                          {item.hasDiscount ? (
                            <>
                              <Typography variant="body2" fontWeight="medium">
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
                                sx={{ height: 18, fontSize: '0.6rem' }}
                              />
                            </>
                          ) : (
                            <Typography variant="body2" fontWeight="medium">
                              ${formatPrice((item.effectivePrice || item.price) * item.quantity)}
                            </Typography>
                          )}
                          {loadingPrices && (
                            <Box
                              sx={{
                                display: 'inline-block',
                                width: 10,
                                height: 10,
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
                    </Box>
                  ))}
                </Box>

                <Divider sx={{ mb: 2 }} />

                <Box sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2" color="textSecondary">
                      Subtotal
                    </Typography>
                    <Typography variant="body1">
                      ${formatPrice(calculateTotal())}
                    </Typography>
                  </Box>

                  {/* Show total savings if any discounts are applied */}
                  {calculateTotalSavings() > 0 && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2" color="success.main">
                        Your Savings
                      </Typography>
                      <Typography variant="body1" color="success.main">
                        -${formatPrice(calculateTotalSavings())}
                      </Typography>
                    </Box>
                  )}

                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2" color="textSecondary">
                      Tax (7%)
                    </Typography>
                    <Typography variant="body1">
                      ${formatPrice(calculateTax())}
                    </Typography>
                  </Box>

                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2" color="textSecondary">
                      Shipping
                    </Typography>
                    <Typography variant="body1">
                      {calculateShipping() === 0 ? 'Free' : `$${formatPrice(calculateShipping())}`}
                    </Typography>
                  </Box>
                </Box>

                <Divider sx={{ mb: 2 }} />

                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="h6">
                    Total
                  </Typography>
                  <Typography variant="h6" color="primary">
                    ${formatPrice(calculateGrandTotal())}
                  </Typography>
                </Box>

                <Box sx={{ mt: 3, p: 2, bgcolor: 'success.light', borderRadius: 1, color: 'success.contrastText' }}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <SecurityIcon fontSize="small" sx={{ mr: 1 }} />
                    <Typography variant="body2">
                      Secure Checkout
                    </Typography>
                  </Box>
                </Box>
              </Paper>
            </Box>
          </Grid>
        </Grid>
      </main>

      {/* Purchase Dialog */}
      <PurchaseDialog
        open={purchaseDialog.open}
        onClose={handlePurchaseDialogClose}
        success={purchaseDialog.success}
        title={purchaseDialog.title}
        message={purchaseDialog.message}
        details={purchaseDialog.details}
        onContinue={purchaseDialog.success ? handleContinueShopping : null}
        onViewOrder={purchaseDialog.success ? handleViewOrderDetails : null}
      />

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

      {/* Auth Dialog */}
      {showAuthDialog && <AuthDialog open={showAuthDialog} onClose={() => setShowAuthDialog(false)} />}
    </Box>
  );
}