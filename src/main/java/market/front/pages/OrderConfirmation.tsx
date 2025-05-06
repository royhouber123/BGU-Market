import React from "react";
import Header from "../components/Header";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";
import { createPageUrl } from "@/utils";
import {
  CheckCircle2,
  ShoppingBag,
  Truck,
  Clock,
  ChevronRight,
} from "lucide-react";

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
            <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mb-4">
              <CheckCircle2 className="w-8 h-8 text-green-600" />
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
              <div className="flex items-start gap-3">
                <Truck className="w-5 h-5 text-gray-500 mt-1" />
                <div>
                  <p className="font-medium">Estimated Delivery</p>
                  <p className="text-gray-600">{formattedDeliveryDate}</p>
                  <div className="flex items-center mt-3 text-sm text-green-600">
                    <Clock className="w-4 h-4 mr-1" />
                    <span>
                      Shipping confirmation will be sent to your email
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 my-6">
            <Button variant="outline" className="w-full" asChild>
              <Link to={createPageUrl("Home")}>
                <ShoppingBag className="w-4 h-4 mr-2" />
                Continue Shopping
              </Link>
            </Button>
            <Button className="w-full bg-blue-600 hover:bg-blue-700">
              Track Your Order
              <ChevronRight className="w-4 h-4 ml-1" />
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
