import React, { useEffect, useState } from "react";
import Header from "../../components/Header/Header";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Box, Container, Typography, Button, Paper, Divider, Skeleton, Alert } from "@mui/material";
import { createPageUrl } from "../../utils";
import purchaseService from "../../services/purchaseService";
import { useAuth } from "../../contexts/AuthContext";
import { formatPrice } from "../../utils/priceUtils";
import './OrderConfirmation.css';
import CheckIcon from "@mui/icons-material/Check";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";

export default function OrderConfirmation() {
  const [loading, setLoading] = useState(true);
  const [orderDetails, setOrderDetails] = useState(null);
  const [error, setError] = useState(null);
  const { currentUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Get order details from URL params or state
  const orderInfo = location.state?.orderInfo;

  useEffect(() => {
    loadOrderDetails();
  }, []);

  const loadOrderDetails = async () => {
    try {
      setLoading(true);

      // If we have order info from checkout, use it
      if (orderInfo) {
        setOrderDetails({
          orderNumber: orderInfo.orderNumber || generateOrderNumber(),
          orderDate: orderInfo.orderDate ? new Date(orderInfo.orderDate).toLocaleDateString() : new Date().toLocaleDateString(),
          total: orderInfo.total || 0,
          paymentMethod: orderInfo.paymentMethod || "Credit Card",
          items: orderInfo.items || [],
          shippingAddress: orderInfo.shippingAddress || "",
          success: true,
          isPastPurchase: orderInfo.isPastPurchase || false,
          // Preserve additional pricing details
          subtotal: orderInfo.subtotal,
          tax: orderInfo.tax,
          totalSavings: orderInfo.totalSavings,
          shipping: orderInfo.shipping || 0
        });
      } else {
        // Only try to get the most recent purchase if no order info was passed
        if (currentUser) {
          const purchases = await purchaseService.getPurchaseHistory(currentUser.id || 1);
          if (purchases && purchases.length > 0) {
            const latestPurchase = purchases[0]; // Most recent purchase
            setOrderDetails({
              orderNumber: generateOrderNumber(),
              orderDate: new Date(latestPurchase.timestamp).toLocaleDateString(),
              total: latestPurchase.totalPrice,
              paymentMethod: latestPurchase.contactInfo || "Payment processed",
              items: latestPurchase.products || [],
              shippingAddress: latestPurchase.shippingAddress || "",
              success: true,
              shipping: latestPurchase.shippingCost || 0
            });
          } else {
            // No recent purchases found
            setOrderDetails({
              orderNumber: generateOrderNumber(),
              orderDate: new Date().toLocaleDateString(),
              success: false,
              message: "No recent order found"
            });
          }
        }
      }
    } catch (error) {
      console.error("Error loading order details:", error);
      setError("Failed to load order details");
      setOrderDetails({
        orderNumber: generateOrderNumber(),
        orderDate: new Date().toLocaleDateString(),
        success: false,
        message: "Error loading order details"
      });
    } finally {
      setLoading(false);
    }
  };

  const generateOrderNumber = () => {
    return `BGU-${Math.floor(Math.random() * 1000000).toString().padStart(6, "0")}`;
  };

  // Calculate estimated delivery date (5 days from now)
  const deliveryDate = new Date();
  deliveryDate.setDate(deliveryDate.getDate() + 5);
  const formattedDeliveryDate = deliveryDate.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-12">
          <div className="max-w-2xl mx-auto bg-white rounded-xl shadow-sm p-8">
            <Box sx={{ textAlign: 'center', mb: 4 }}>
              <Skeleton variant="circular" width={64} height={64} sx={{ mx: 'auto', mb: 2 }} />
              <Skeleton variant="text" width="60%" height={40} sx={{ mx: 'auto', mb: 1 }} />
              <Skeleton variant="text" width="80%" height={20} sx={{ mx: 'auto' }} />
            </Box>
            <Box sx={{ my: 4 }}>
              <Skeleton variant="rectangular" height={120} sx={{ borderRadius: 1 }} />
            </Box>
            <Box sx={{ my: 4 }}>
              <Skeleton variant="rectangular" height={80} sx={{ borderRadius: 1 }} />
            </Box>
          </div>
        </main>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-12">
          <div className="max-w-2xl mx-auto bg-white rounded-xl shadow-sm p-8">
            <Alert severity="error" sx={{ mb: 4 }}>
              {error}
            </Alert>
            <Box sx={{ textAlign: 'center' }}>
              <Button
                variant="contained"
                onClick={() => navigate(createPageUrl("Dashboard"))}
                startIcon={<ShoppingBagIcon />}
              >
                Return to Dashboard
              </Button>
            </Box>
          </div>
        </main>
      </div>
    );
  }

  if (!orderDetails?.success) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-12">
          <div className="max-w-2xl mx-auto bg-white rounded-xl shadow-sm p-8">
            <Box sx={{ textAlign: 'center' }}>
              <Typography variant="h5" gutterBottom>
                {orderDetails?.message || "No order information available"}
              </Typography>
              <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                We couldn't find details for your recent order.
              </Typography>
              <Button
                variant="contained"
                onClick={() => navigate(createPageUrl("Dashboard"))}
                startIcon={<ShoppingBagIcon />}
              >
                Continue Shopping
              </Button>
            </Box>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-12">
        <div className="max-w-2xl mx-auto bg-white rounded-xl shadow-sm p-8">
          <div className="text-center mb-8">
            <div className="flex items-center justify-center mb-6">
              <div className="bg-green-100 rounded-full p-3">
                <CheckIcon style={{ width: '32px', height: '32px', color: '#16a34a' }} />
              </div>
            </div>
            <h1 className="text-2xl font-bold text-gray-900">
              Order Confirmed!
            </h1>
            <p className="text-gray-600 mt-1">
              Thank you for shopping with BGU-Marketplace. Your order has been received and is being processed.
            </p>
          </div>

          <div className="border-t border-b py-4 my-6">
            <div className="flex justify-between items-center py-2">
              <span className="text-gray-500">Order Number</span>
              <span className="font-medium">{orderDetails.orderNumber}</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-gray-500">Order Date</span>
              <span className="font-medium">{orderDetails.orderDate}</span>
            </div>

            {/* Show detailed pricing breakdown if available */}
            {orderDetails && orderDetails.subtotal !== undefined && (
              <>
                <div className="flex justify-between items-center py-2">
                  <span className="text-gray-500">Subtotal</span>
                  <span className="font-medium">${formatPrice(orderDetails.subtotal)}</span>
                </div>
                {orderDetails.totalSavings > 0 && (
                  <div className="flex justify-between items-center py-2">
                    <span className="text-gray-500 text-green-600">Total Savings</span>
                    <span className="font-medium text-green-600">-${formatPrice(orderDetails.totalSavings)}</span>
                  </div>
                )}
                {orderDetails.tax !== undefined && (
                  <div className="flex justify-between items-center py-2">
                    <span className="text-gray-500">Tax</span>
                    <span className="font-medium">${formatPrice(orderDetails.tax)}</span>
                  </div>
                )}
                {orderDetails.shipping !== undefined && (
                  <div className="flex justify-between items-center py-2">
                    <span className="text-gray-500">Shipping</span>
                    <span className="font-medium">
                      {orderDetails.shipping === 0 ? 'Free' : `$${formatPrice(orderDetails.shipping)}`}
                    </span>
                  </div>
                )}
              </>
            )}

            <div className="flex justify-between items-center py-2">
              <span className="text-gray-500">Total Amount</span>
              <span className="font-medium text-green-600">
                ${formatPrice(orderDetails.total)}
              </span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-gray-500">Payment Method</span>
              <span className="font-medium">
                {orderDetails.paymentMethod}
              </span>
            </div>
          </div>

          {/* Order Items */}
          {orderDetails.items && orderDetails.items.length > 0 && (
            <div className="my-6">
              <h2 className="text-lg font-semibold mb-4">Order Items</h2>
              <div className="bg-gray-50 rounded-lg p-4">
                {orderDetails.items.map((item, index) => (
                  <div key={index} className="flex justify-between items-center py-2 border-b border-gray-200 last:border-b-0">
                    <div>
                      <Typography variant="body2" fontWeight="medium">
                        {item.title || `Product ${item.productId}`}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Qty: {item.quantity}
                      </Typography>
                    </div>
                    <div className="text-right">
                      {item.hasDiscount ? (
                        <div className="flex flex-col items-end">
                          <Typography variant="body2" fontWeight="medium" color="primary">
                            ${formatPrice((item.unitPrice || item.price || 0) * item.quantity)}
                          </Typography>
                          <Typography
                            variant="caption"
                            sx={{ textDecoration: 'line-through', color: 'text.secondary' }}
                          >
                            ${formatPrice((item.originalPrice || item.unitPrice || item.price || 0) * item.quantity)}
                          </Typography>
                          <Typography variant="caption" color="success.main">
                            Saved ${formatPrice((item.savings || 0) * item.quantity)}
                          </Typography>
                        </div>
                      ) : (
                        <Typography variant="body2" fontWeight="medium">
                          ${formatPrice((item.unitPrice || item.price || 0) * item.quantity)}
                        </Typography>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Shipping Information */}
          <div className="my-6">
            <h2 className="text-lg font-semibold mb-4">Shipping Information</h2>
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-center mb-3">
                <LocalShippingIcon style={{ marginRight: '8px', color: '#2563eb' }} />
                <div>
                  <h3 className="font-medium text-gray-900">Shipping Address</h3>
                  <p className="text-sm text-gray-500">
                    {orderDetails.shippingAddress || "Address will be confirmed via email"}
                  </p>
                </div>
              </div>
              <div className="flex items-center mb-3">
                <AccessTimeIcon style={{ marginRight: '8px', color: '#2563eb' }} />
                <div>
                  <h3 className="font-medium text-gray-900">Estimated Delivery</h3>
                  <p className="text-sm text-gray-500">{formattedDeliveryDate}</p>
                </div>
              </div>
              <div className="flex items-center">
                <CheckIcon style={{ marginRight: '8px', color: '#16a34a' }} />
                <div>
                  <h3 className="font-medium text-gray-900">Order Processing</h3>
                  <p className="text-sm text-gray-500">Your order will be processed within 24 hours</p>
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 my-6">
            <Button
              variant="outlined"
              component={Link}
              to={createPageUrl("Dashboard")}
              startIcon={<ShoppingBagIcon />}
              fullWidth
            >
              Continue Shopping
            </Button>

            <Button
              variant="contained"
              color="primary"
              onClick={() => navigate(createPageUrl("Profile"))}
              endIcon={<ArrowForwardIcon />}
              fullWidth
            >
              View Order History
            </Button>
          </div>

          <div className="text-center text-sm text-gray-500 mt-8">
            <p>
              Having trouble?{" "}
              <a href="#" className="text-blue-600 hover:underline">
                Contact our support team
              </a>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
