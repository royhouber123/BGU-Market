import React, { useState, useEffect } from "react";
import Header from "../../components/Header/Header";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import userService from "../../services/userService";
import {
  Button,
  TextField,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup,
  Divider,
  Tabs,
  Tab,
  Box,
  Typography,
  Paper,
  Alert,
  IconButton,
  Grid,
  Card,
  CardContent,
  Snackbar,
  Checkbox
} from "@mui/material";
import CreditCardIcon from "@mui/icons-material/CreditCard";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import CheckIcon from "@mui/icons-material/Check";
import SecurityIcon from "@mui/icons-material/Security";
import { useNavigate, Link } from "react-router-dom";
import { createPageUrl } from "../../utils";
import './Checkout.css';

export default function Checkout() {
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [paymentMethod, setPaymentMethod] = useState("credit");
  const [showAuthDialog, setShowAuthDialog] = useState(false);
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

  const [processingPayment, setProcessingPayment] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = async () => {
    try {
      // Get current user's cart
      if (userService.isAuthenticated()) {
        const user = await userService.getProfile();
        setCart(user.cart || []);

        // Pre-fill name if available
        if (user.fullName) {
          setShippingAddress(prev => ({
            ...prev,
            fullName: user.fullName
          }));
        }
      } else {
        setCart([]);
      }
    } catch (error) {
      console.error("Error loading cart:", error);
    }
    setLoading(false);
  };

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
    setProcessingPayment(true);

    // Simulate payment processing
    setTimeout(async () => {
      try {
        // Clear the cart
        await userService.updateUserData({ cart: [] });

        // Show success toast
        toast({
          title: "Order placed successfully!",
          description: "Your order has been confirmed.",
          variant: "default",
        });

        // Redirect to confirmation page
        navigate(createPageUrl("OrderConfirmation"));
      } catch (error) {
        console.error("Error placing order:", error);

        toast({
          title: "Error",
          description: "There was a problem placing your order.",
          variant: "destructive",
        });

        setProcessingPayment(false);
      }
    }, 2000);
  };

  const calculateTotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
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

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
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

  if (cart.length === 0) {
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
          <Grid item xs={12} md={8}>
            <Box sx={{ width: '100%', mb: 4 }}>
              <Tabs
                value={tabValue}
                onChange={handleTabChange}
                aria-label="checkout steps"
                sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}
              >
                <Tab label="Shipping" />
                <Tab label="Payment" />
              </Tabs>

              {tabValue === 0 && (
                <Box sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom>
                    Shipping Address
                  </Typography>

                  <Grid container spacing={2}>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        label="Full Name"
                        name="fullName"
                        value={shippingAddress.fullName}
                        onChange={handleShippingAddressChange}
                        required
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

                    <Grid item xs={12}>
                      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
                        <Button
                          variant="contained"
                          color="primary"
                          onClick={() => setTabValue(1)}
                        >
                          Continue to Payment
                        </Button>
                      </Box>
                    </Grid>
                  </Grid>
                </Box>
              )}

              {tabValue === 1 && (
                <Box sx={{ p: 2 }}>
                  <Box sx={{ mb: 4 }}>
                    <Typography variant="h6" gutterBottom>
                      Payment Method
                    </Typography>

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
                      <Paper variant="outlined" sx={{ p: 2 }}>
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

                  <Box sx={{ mt: 4 }}>
                    <Typography variant="h6" gutterBottom>
                      Billing Address
                    </Typography>

                    <FormControlLabel
                      control={<Checkbox checked={true} />}
                      label="Same as shipping address"
                    />
                  </Box>

                  <Divider sx={{ my: 3 }} />

                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Button
                      variant="outlined"
                      onClick={() => setTabValue(0)}
                    >
                      Back to Shipping
                    </Button>

                    <Button
                      variant="contained"
                      color="primary"
                      onClick={placeOrder}
                      disabled={!cardDetails.cardNumber || !cardDetails.expiryDate || !cardDetails.cvv}
                    >
                      Place Order
                    </Button>
                  </Box>
                </Box>
              )}
            </Box>
          </Grid>

          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, borderRadius: 2 }}>
              <Typography variant="h6" component="h2" gutterBottom>
                Order Summary
              </Typography>

              <Box sx={{ mb: 3, maxHeight: 300, overflow: 'auto' }}>
                {cart.map(item => (
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
                      <Typography variant="body2" fontWeight="medium">
                        ${(item.price * item.quantity).toFixed(2)}
                      </Typography>
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
                    ${calculateTotal().toFixed(2)}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2" color="textSecondary">
                    Tax (7%)
                  </Typography>
                  <Typography variant="body1">
                    ${calculateTax().toFixed(2)}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2" color="textSecondary">
                    Shipping
                  </Typography>
                  <Typography variant="body1">
                    {calculateShipping() === 0 ? 'Free' : `$${calculateShipping().toFixed(2)}`}
                  </Typography>
                </Box>
              </Box>

              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="h6">
                  Total
                </Typography>
                <Typography variant="h6" color="primary">
                  ${calculateGrandTotal().toFixed(2)}
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
          </Grid>
        </Grid>
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
