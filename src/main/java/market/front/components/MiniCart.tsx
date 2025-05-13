import React from "react";
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from "@/components/ui/hover-card";
import { Button } from "@/components/ui/button";
import { ShoppingCart } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { createPageUrl } from "@/utils";

export default function MiniCart({ cart = [] }) {
  const navigate = useNavigate();

  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  const cartTotal = cart.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  // Show at most 3 items in mini cart
  const displayItems = cart.slice(0, 3);
  const hasMoreItems = cart.length > 3;

  const handleViewCart = () => {
    navigate(createPageUrl("Cart"));
  };

  const handleCheckout = () => {
    navigate(createPageUrl("Checkout"));
  };

  return (
    <HoverCard openDelay={100} closeDelay={200}>
      <HoverCardTrigger asChild>
        <button
          className="text-gray-600 hover:text-blue-600 transition-colors relative"
          onClick={handleViewCart}
        >
          <ShoppingCart size={20} className="inline mr-1" />
          <span className="text-sm">Cart</span>
          {totalItems > 0 && (
            <div className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
              {totalItems}
            </div>
          )}
        </button>
      </HoverCardTrigger>
      <HoverCardContent align="end" className="w-80 p-0">
        <div className="p-4 border-b">
          <h3 className="font-semibold">Your Cart ({totalItems} items)</h3>
        </div>

        {cart.length === 0 ? (
          <div className="p-4 text-center">
            <p className="text-gray-500">Your cart is empty</p>
          </div>
        ) : (
          <>
            <div className="max-h-60 overflow-y-auto">
              {displayItems.map((item) => (
                <div
                  key={item.productId}
                  className="flex items-center gap-3 p-3 border-b"
                >
                  <img
                    src={item.image}
                    alt={item.title}
                    className="w-12 h-12 object-cover rounded"
                  />
                  <div className="flex-grow">
                    <h4 className="text-sm font-medium line-clamp-1">
                      {item.title}
                    </h4>
                    <div className="flex justify-between items-center mt-1">
                      <span className="text-sm text-gray-500">
                        Qty: {item.quantity}
                      </span>
                      <span className="text-sm font-medium">
                        ${(item.price * item.quantity).toFixed(2)}
                      </span>
                    </div>
                  </div>
                </div>
              ))}

              {hasMoreItems && (
                <div className="p-3 text-sm text-center text-gray-500 border-b">
                  And {cart.length - 3} more items...
                </div>
              )}
            </div>

            <div className="p-4 border-t">
              <div className="flex justify-between mb-4">
                <span className="font-medium">Subtotal:</span>
                <span className="font-bold">${cartTotal.toFixed(2)}</span>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={handleViewCart}
                >
                  View Cart
                </Button>
                <Button className="w-full" onClick={handleCheckout}>
                  Checkout
                </Button>
              </div>
            </div>
          </>
        )}
      </HoverCardContent>
    </HoverCard>
  );
}
