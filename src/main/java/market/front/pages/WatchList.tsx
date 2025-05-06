import React, { useState, useEffect } from "react";
import Header from "../components/Header";
import { User } from "@/entities/User";
import { Product } from "@/entities/Product";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Skeleton } from "@/components/ui/skeleton";
import { Heart, ShoppingBag, X, ShoppingCart } from "lucide-react";
import { Link } from "react-router-dom";
import { createPageUrl } from "@/utils";
import { useToast } from "@/components/ui/use-toast";

export default function Watchlist() {
  const [watchlistItems, setWatchlistItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  useEffect(() => {
    fetchWatchlist();
  }, []);

  const fetchWatchlist = async () => {
    try {
      setLoading(true);
      const user = await User.me();
      const watchlistIds = user.watchlist || [];
      
      if (watchlistIds.length === 0) {
        setWatchlistItems([]);
        setLoading(false);
        return;
      }
      
      // Fetch all products and filter those in the watchlist
      const allProducts = await Product.list();
      const watchlistProducts = allProducts.filter(product => 
        watchlistIds.includes(product.id)
      );
      
      setWatchlistItems(watchlistProducts);
    } catch (error) {
      console.error("Error fetching watchlist:", error);
    }
    setLoading(false);
  };

  const removeFromWatchlist = async (productId) => {
    try {
      const user = await User.me();
      const watchlist = (user.watchlist || []).filter(id => id !== productId);
      
      await User.updateMyUserData({ watchlist });
      setWatchlistItems(prev => prev.filter(item => item.id !== productId));
      
      toast({
        title: "Removed from watchlist",
        description: "Item has been removed from your watchlist",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to remove item from watchlist",
        variant: "destructive"
      });
    }
  };

  const addToCart = async (product) => {
    try {
      const user = await User.me();
      const cart = user.cart || [];
      
      // Check if product is already in cart
      const existingItemIndex = cart.findIndex(item => item.productId === product.id);
      
      if (existingItemIndex >= 0) {
        // Increment quantity if already in cart
        cart[existingItemIndex].quantity += 1;
      } else {
        // Add new item to cart
        cart.push({
          productId: product.id,
          title: product.title,
          price: product.price,
          image: product.images[0],
          quantity: 1
        });
      }
      
      await User.updateMyUserData({ cart });
      
      toast({
        title: "Added to cart",
        description: "Item has been added to your cart",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to add item to cart",
        variant: "destructive"
      });
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-2xl font-bold">My Watchlist</h1>
        </div>

        <Tabs defaultValue="active">
          <TabsList className="mb-6">
            <TabsTrigger value="active">Active ({watchlistItems.length})</TabsTrigger>
            <TabsTrigger value="ended">Ended (0)</TabsTrigger>
          </TabsList>
          
          <TabsContent value="active">
            {loading ? (
              <div className="grid gap-4">
                {[1, 2, 3].map(i => (
                  <div key={i} className="bg-white rounded-lg p-4 shadow-sm animate-pulse">
                    <div className="flex items-center gap-4">
                      <Skeleton className="w-24 h-24 rounded" />
                      <div className="flex-1">
                        <Skeleton className="h-6 w-3/4 mb-2" />
                        <Skeleton className="h-5 w-1/4 mb-1" />
                        <Skeleton className="h-4 w-2/4" />
                      </div>
                      <div className="flex gap-2">
                        <Skeleton className="w-10 h-10 rounded-full" />
                        <Skeleton className="w-10 h-10 rounded-full" />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : watchlistItems.length > 0 ? (
              <div className="grid gap-4">
                {watchlistItems.map(product => (
                  <div key={product.id} className="bg-white rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow">
                    <div className="flex items-center gap-4">
                      <Link to={createPageUrl("ProductDetail") + `?id=${product.id}`}>
                        <img 
                          src={product.images[0]} 
                          alt={product.title} 
                          className="w-24 h-24 object-cover rounded"
                        />
                      </Link>
                      
                      <div className="flex-1">
                        <Link 
                          to={createPageUrl("ProductDetail") + `?id=${product.id}`}
                          className="text-lg font-medium hover:text-blue-600"
                        >
                          {product.title}
                        </Link>
                        <p className="text-xl font-bold mt-1">${product.price.toFixed(2)}</p>
                        <p className="text-sm text-gray-500 mt-1">
                          {product.shipping_cost === 0 
                            ? "Free shipping" 
                            : `+$${product.shipping_cost?.toFixed(2)} shipping`}
                        </p>
                      </div>
                      
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          size="icon"
                          className="rounded-full hover:text-red-600"
                          onClick={() => removeFromWatchlist(product.id)}
                          title="Remove from watchlist"
                        >
                          <X className="h-5 w-5" />
                        </Button>
                        
                        <Button
                          variant="outline"
                          size="icon"
                          className="rounded-full hover:bg-blue-50 hover:text-blue-600"
                          onClick={() => addToCart(product)}
                          title="Add to cart"
                        >
                          <ShoppingCart className="h-5 w-5" />
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-16 bg-white rounded-lg shadow-sm">
                <Heart className="w-16 h-16 mx-auto text-gray-300 mb-4" />
                <h2 className="text-xl font-semibold text-gray-600">Your watchlist is empty</h2>
                <p className="text-gray-500 mt-2">
                  Add items to your watchlist to keep track of things you're interested in
                </p>
                <Link to={createPageUrl("Home")}>
                  <Button className="mt-6">
                    <ShoppingBag className="mr-2 h-4 w-4" />
                    Browse Items
                  </Button>
                </Link>
              </div>
            )}
          </TabsContent>
          
          <TabsContent value="ended">
            <div className="text-center py-16 bg-white rounded-lg shadow-sm">
              <h2 className="text-xl font-semibold text-gray-600">No ended items</h2>
              <p className="text-gray-500 mt-2">
                When items you're watching end, they'll appear here
              </p>
            </div>
          </TabsContent>
        </Tabs>
      </main>
    </div>
  );
}