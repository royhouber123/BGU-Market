import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { createPageUrl } from "@/utils";
import { Search, ShoppingCart, Heart, Bell, User, Menu, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import MiniCart from "./MiniCart";
import AuthDialog from "./AuthDialog";
import { User as UserEntity } from "@/entities/User";

export default function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [userCart, setUserCart] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchUserCart();
  }, []);

  const fetchUserCart = async () => {
    try {
      const user = await UserEntity.me();
      if (user.cart) {
        setUserCart(user.cart);
      }
    } catch (error) {
      console.log("User not logged in or cart empty");
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    const searchQuery = e.target.search.value.trim();
    if (searchQuery) {
      navigate(
        createPageUrl("SearchResults") + `?q=${encodeURIComponent(searchQuery)}`
      );
    }
  };

  const handleWatchlistClick = () => {
    navigate(createPageUrl("Watchlist"));
  };

  return (
    <header className="sticky top-0 z-50 bg-white shadow-sm">
      <div className="container mx-auto px-4 py-3">
        <div className="flex items-center justify-between">
          {/* Logo */}
          <Link to={createPageUrl("Home")} className="flex-shrink-0">
            <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              BGU-Marketplace
            </h1>
          </Link>

          {/* Search Bar */}
          <form
            onSubmit={handleSearch}
            className="hidden md:flex flex-grow mx-8 relative"
          >
            <Input
              name="search"
              type="text"
              placeholder="Search for anything..."
              className="w-full pr-10 border-gray-300 focus:ring-blue-500 focus:border-blue-500"
            />
            <button
              type="submit"
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              <Search size={18} />
            </button>
          </form>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center space-x-6">
            <MiniCart cart={userCart} />

            <button
              onClick={handleWatchlistClick}
              className="text-gray-600 hover:text-blue-600 transition-colors"
            >
              <Heart size={20} className="inline mr-1" />
              <span className="text-sm">Watchlist</span>
            </button>

            <Link
              to={createPageUrl("Home")}
              className="text-gray-600 hover:text-blue-600 transition-colors"
            >
              <Bell size={20} className="inline mr-1" />
              <span className="text-sm">Notifications</span>
            </Link>

            <Button
              variant="outline"
              className="rounded-full flex items-center gap-2"
              onClick={() => setShowAuthDialog(true)}
            >
              <User size={16} />
              <span>Sign in</span>
            </Button>
          </div>

          {/* Mobile menu button */}
          <div className="md:hidden flex items-center">
            <button
              type="button"
              className="p-2 text-gray-600"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            >
              {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>

        {/* Mobile search - with form */}
        <form onSubmit={handleSearch} className="mt-3 md:hidden relative">
          <Input
            name="search"
            type="text"
            placeholder="Search for anything..."
            className="w-full pr-10 border-gray-300"
          />
          <button
            type="submit"
            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400"
          >
            <Search size={18} />
          </button>
        </form>

        {/* Mobile Navigation Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden pt-2 pb-4 space-y-2 mt-2 border-t">
            <button
              onClick={() => navigate(createPageUrl("Cart"))}
              className="block w-full px-3 py-2 text-left text-gray-600 hover:bg-gray-100 rounded-md"
            >
              <ShoppingCart size={20} className="inline mr-2" /> Cart
              {userCart.length > 0 && (
                <span className="ml-1 bg-red-500 text-white text-xs rounded-full px-2 py-0.5">
                  {userCart.reduce((sum, item) => sum + item.quantity, 0)}
                </span>
              )}
            </button>

            <button
              onClick={() => handleWatchlistClick}
              className="block w-full px-3 py-2 text-left text-gray-600 hover:bg-gray-100 rounded-md"
            >
              <Heart size={20} className="inline mr-2" /> Watchlist
            </button>

            <Link
              to={createPageUrl("Home")}
              className="block px-3 py-2 text-gray-600 hover:bg-gray-100 rounded-md"
            >
              <Bell size={20} className="inline mr-2" /> Notifications
            </Link>

            <button
              onClick={() => setShowAuthDialog(true)}
              className="block w-full px-3 py-2 text-left text-gray-600 hover:bg-gray-100 rounded-md"
            >
              <User size={20} className="inline mr-2" /> Sign in
            </button>
            <Link
              to={createPageUrl("Checkout")}
              className="block px-3 py-2 text-gray-600 hover:bg-gray-100 rounded-md"
            >
              <ShoppingCart size={20} className="inline mr-2" /> Checkout
            </Link>
          </div>
        )}
      </div>

      <AuthDialog open={showAuthDialog} onOpenChange={setShowAuthDialog} />
    </header>
  );
}
