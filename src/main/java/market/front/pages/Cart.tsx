
import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import { Button } from "@/components/ui/button";
import { MinusCircle, PlusCircle, Trash2, ShoppingBag } from "lucide-react";
import { User } from "@/entities/User";
import { Link, useNavigate } from "react-router-dom";
import { createPageUrl } from "@/utils";

export default function Cart() {
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadCart();
  }, []);

  const loadCart = async () => {
    try {
      const user = await User.me();
      setCart(user.cart || []);
    } catch (error) {
      console.error("Error loading cart:", error);
    }
    setLoading(false);
  };

  const updateCart = async (newCart) => {
    try {
      await User.updateMyUserData({ cart: newCart });
      setCart(newCart);
    } catch (error) {
      console.error("Error updating cart:", error);
    }
  };

  const updateQuantity = (productId, change) => {
    const newCart = cart.map(item => {
      if (item.productId === productId) {
        const newQuantity = Math.max(1, item.quantity + change);
        return { ...item, quantity: newQuantity };
      }
      return item;
    });
    updateCart(newCart);
  };

  const removeItem = (productId) => {
    const newCart = cart.filter(item => item.productId !== productId);
    updateCart(newCart);
  };

  const calculateTotal = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  const handleCheckout = () => {
    navigate(createPageUrl("Checkout"));
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-8">
          <div className="animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/4 mb-8"></div>
            <div className="space-y-4">
              {[1, 2, 3].map(i => (
                <div key={i} className="h-24 bg-gray-200 rounded"></div>
              ))}
            </div>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold mb-8">Shopping Cart</h1>

        {cart.length > 0 ? (
          <div className="grid md:grid-cols-3 gap-8">
            <div className="md:col-span-2 space-y-4">
              {cart.map((item) => (
                <div key={item.productId} className="bg-white rounded-lg shadow p-6">
                  <div className="flex items-center gap-4">
                    <img 
                      src={item.image} 
                      alt={item.title} 
                      className="w-24 h-24 object-cover rounded-md"
                    />
                    <div className="flex-grow">
                      <h3 className="font-semibold">{item.title}</h3>
                      <p className="text-gray-500">${item.price.toFixed(2)}</p>
                      <div className="flex items-center gap-3 mt-2">
                        <Button
                          variant="outline"
                          size="icon"
                          onClick={() => updateQuantity(item.productId, -1)}
                        >
                          <MinusCircle className="h-4 w-4" />
                        </Button>
                        <span className="font-medium">{item.quantity}</span>
                        <Button
                          variant="outline"
                          size="icon"
                          onClick={() => updateQuantity(item.productId, 1)}
                        >
                          <PlusCircle className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-semibold">${(item.price * item.quantity).toFixed(2)}</p>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="text-red-500 hover:text-red-600 mt-2"
                        onClick={() => removeItem(item.productId)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="bg-white rounded-lg shadow p-6 h-fit">
              <h2 className="text-xl font-bold mb-4">Order Summary</h2>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span>Subtotal</span>
                  <span>${calculateTotal().toFixed(2)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Shipping</span>
                  <span className="text-green-600">Free</span>
                </div>
                <div className="border-t pt-3">
                  <div className="flex justify-between font-bold">
                    <span>Total</span>
                    <span>${calculateTotal().toFixed(2)}</span>
                  </div>
                </div>
              </div>
              <Button 
                className="w-full mt-6 bg-blue-600 hover:bg-blue-700"
                onClick={handleCheckout}
              >
                Procee to Checkout
              </Button>
            </div>
          </div>
        ) : (
          <div className="text-center py-16">
            <ShoppingBag className="w-16 h-16 mx-auto text-gray-400 mb-4" />
            <h2 className="text-xl font-semibold text-gray-600">Your cart is empty</h2>
            <p className="text-gray-500 mt-2">Looks like you haven't added any items yet</p>
            <Link to={createPageUrl("Home")}>
              <Button className="mt-6">
                Start Shopping
              </Button>
            </Link>
          </div>
        )}
      </main>
    </div>
  );
}
