import React from "react";
import Header from "../components/Header.jsx";
import FeaturedSection from "../components/FeaturedSection.jsx";
import { Button, Typography, Box, Paper } from "@mui/material";
import { Link } from "react-router-dom";
import { createPageUrl } from "../utils";
import CheckIcon from "@mui/icons-material/Check";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";

export default function OrderConfirmation() {
  // Generate a random order number
  const orderNumber = `BGU-${Math.floor(Math.random() * 1000000)
    .toString()
    .padStart(6, "0")}`;

  // Calculate estimated delivery date (5 days from now)
  const deliveryDate = new Date();
  deliveryDate.setDate(deliveryDate.getDate() + 5);
  const formattedDeliveryDate = deliveryDate.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
  });

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
              Thank you for shopping with BGU-Marketplace. Your order has been
              received.
            </p>
          </div>

          <div className="border-t border-b py-4 my-6">
            <div className="flex justify-between items-center py-2">
              <span className="text-gray-500">Order Number</span>
              <span className="font-medium">{orderNumber}</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-gray-500">Order Date</span>
              <span className="font-medium">
                {new Date().toLocaleDateString()}
              </span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-gray-500">Payment Method</span>
              <span className="font-medium">
                Credit Card (•••• •••• •••• 1234)
              </span>
            </div>
          </div>

          <div className="my-6">
            <h2 className="text-lg font-semibold mb-4">Shipping Information</h2>
            <div className="bg-gray-50 rounded-lg p-4">
              <div className="flex items-center mb-3">
                <LocalShippingIcon style={{ marginRight: '8px', color: '#2563eb' }} />
                <div>
                  <h3 className="font-medium text-gray-900">Shipping</h3>
                  <p className="text-sm text-gray-500">Estimated delivery: 3-5 business days</p>
                </div>
              </div>
              <div className="flex items-center">
                <AccessTimeIcon style={{ marginRight: '8px', color: '#2563eb' }} />
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
              to={createPageUrl("Home")}  
              startIcon={<ShoppingBagIcon />}
              fullWidth
            >
              Continue Shopping
            </Button>
            
            <Button 
              variant="contained" 
              color="primary" 
              endIcon={<ArrowForwardIcon />}
              fullWidth
            >
              Track Your Order
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

        {/* Add recommendations section */}
        <div className="mt-12">
          <FeaturedSection
            title="You may also like"
            subtitle="Recommended products for you"
            limit={4}
          />
        </div>
      </main>
    </div>
  );
}
