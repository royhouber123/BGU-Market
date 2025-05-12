import React, { useState, useEffect } from "react";
import { Product } from "@/entities/Product";
import { User } from "@/entities/User";
import Header from "../components/Header";
import { Button } from "@/components/ui/button";
import {
  Heart,
  Share,
  ShoppingCart,
  Check,
  ChevronRight,
  Building,
  Star,
  Shield,
  Truck,
} from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";
import { format } from "date-fns";
import { useToast } from "@/components/ui/use-toast";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";

export default function ProductDetail() {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [mainImage, setMainImage] = useState("");
  const [isInCart, setIsInCart] = useState(false);
  const [isInWatchlist, setIsInWatchlist] = useState(false);
  const { toast } = useToast();

  const urlParams = new URLSearchParams(window.location.search);
  const productId = urlParams.get("id");

  useEffect(() => {
    if (productId) {
      fetchProduct();
      checkUserLists();
    }
  }, [productId]);

  const fetchProduct = async () => {
    try {
      // In a real app, you would fetch a single product by ID
      // For this example, we'll fetch all and find the one we want
      const products = await Product.list();
      const foundProduct = products.find((p) => p.id === productId);

      if (foundProduct) {
        setProduct(foundProduct);
        setMainImage(foundProduct.images[0]);
      }
    } catch (error) {
      console.error("Error fetching product:", error);
    } finally {
      setLoading(false);
    }
  };

  const checkUserLists = async () => {
    try {
      const user = await User.me();

      // Check if product is in cart
      const cartItem = (user.cart || []).find(
        (item) => item.productId === productId
      );
      setIsInCart(!!cartItem);

      // Check if product is in watchlist
      const isWatched = (user.watchlist || []).includes(productId);
      setIsInWatchlist(isWatched);
    } catch (error) {
      console.log("User not logged in or lists empty");
    }
  };

  const addToCart = async () => {
    try {
      const user = await User.me();
      const cart = user.cart || [];

      // Check if product is already in cart
      const existingItemIndex = cart.findIndex(
        (item) => item.productId === productId
      );

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
          quantity: 1,
        });
      }

      await User.updateMyUserData({ cart });
      setIsInCart(true);

      toast({
        title: "Added to cart",
        description: "Item has been added to your cart",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Please sign in to add items to your cart",
        variant: "destructive",
      });
    }
  };

  const toggleWatchlist = async () => {
    try {
      const user = await User.me();
      let watchlist = user.watchlist || [];

      if (isInWatchlist) {
        // Remove from watchlist
        watchlist = watchlist.filter((id) => id !== productId);
        toast({
          title: "Removed from watchlist",
          description: "Item has been removed from your watchlist",
        });
      } else {
        // Add to watchlist
        watchlist.push(productId);
        toast({
          title: "Added to watchlist",
          description: "Item has been added to your watchlist",
        });
      }

      await User.updateMyUserData({ watchlist });
      setIsInWatchlist(!isInWatchlist);
    } catch (error) {
      toast({
        title: "Error",
        description: "Please sign in to manage your watchlist",
        variant: "destructive",
      });
    }
  };

  const changeMainImage = (image) => {
    setMainImage(image);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-8">
          <div className="grid md:grid-cols-2 gap-8">
            <Skeleton className="aspect-square rounded-lg" />
            <div className="space-y-4">
              <Skeleton className="h-8 w-3/4" />
              <Skeleton className="h-6 w-1/3" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          </div>
        </main>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-8 text-center">
          <h1 className="text-2xl font-bold text-gray-700">
            Product not found
          </h1>
          <p className="mt-4 text-gray-500">
            The product you're looking for doesn't exist or has been removed.
          </p>
        </main>
      </div>
    );
  }

  // Calculate time left for bidding
  const timeLeft = product.bid_end_date
    ? new Date(product.bid_end_date) - new Date()
    : null;

  const formatTimeLeft = () => {
    if (!timeLeft || timeLeft <= 0) return "Auction ended";

    const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
    const hours = Math.floor(
      (timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
    );
    const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));

    return `${days}d ${hours}h ${minutes}m left`;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow-sm p-6 md:p-8">
          <div className="grid md:grid-cols-2 gap-8">
            {/* Product Images */}
            <div>
              <div className="aspect-square rounded-lg overflow-hidden bg-gray-100">
                <img
                  src={mainImage}
                  alt={product.title}
                  className="w-full h-full object-contain p-4"
                />
              </div>

              {product.images.length > 1 && (
                <div className="grid grid-cols-5 gap-2 mt-4">
                  {product.images.map((image, index) => (
                    <button
                      key={index}
                      className={`aspect-square rounded border ${
                        mainImage === image
                          ? "border-blue-500"
                          : "border-gray-200"
                      } overflow-hidden`}
                      onClick={() => changeMainImage(image)}
                    >
                      <img
                        src={image}
                        alt={`${product.title} - view ${index + 1}`}
                        className="w-full h-full object-cover"
                      />
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Product Info */}
            <div>
              <h1 className="text-2xl md:text-3xl font-bold mb-2">
                {product.title}
              </h1>

              {timeLeft > 0 && (
                <div className="text-sm text-blue-600 mb-4">
                  {formatTimeLeft()}
                </div>
              )}

              <div className="flex items-center gap-2 mb-4">
                <Badge className="capitalize bg-gray-100 text-gray-800 hover:bg-gray-200">
                  {product.condition.replace("_", " ")}
                </Badge>
                <Badge className="capitalize bg-gray-100 text-gray-800 hover:bg-gray-200">
                  {product.category}
                </Badge>
              </div>

              <div className="text-3xl font-bold mb-2">
                ${product.price.toFixed(2)}
              </div>

              {product.shipping_cost === 0 ? (
                <p className="text-green-600 mb-4">Free shipping</p>
              ) : (
                <p className="text-gray-600 mb-4">
                  +${product.shipping_cost?.toFixed(2)} shipping
                </p>
              )}

              <p className="text-gray-600 mb-6">{product.description}</p>

              <div className="flex flex-col sm:flex-row gap-3 mb-6">
                <Button className="flex-1 gap-2" size="lg" onClick={addToCart}>
                  {isInCart ? (
                    <>
                      <Check className="w-5 h-5" /> Added to Cart
                    </>
                  ) : (
                    <>
                      <ShoppingCart className="w-5 h-5" /> Add to Cart
                    </>
                  )}
                </Button>

                <Button
                  variant={isInWatchlist ? "default" : "outline"}
                  className={`gap-2 ${
                    isInWatchlist ? "bg-red-500 hover:bg-red-600" : ""
                  }`}
                  onClick={toggleWatchlist}
                >
                  <Heart className="w-5 h-5" />
                  {isInWatchlist ? "Watching" : "Add to Watchlist"}
                </Button>
              </div>

              <div className="space-y-3 mt-6">
                <div className="flex items-center">
                  <Building className="w-4 h-4 text-gray-500 mr-2" />
                  <p className="text-sm text-gray-600">
                    Sold by:{" "}
                    <span className="font-medium">
                      {product.seller || "MarketPlace Seller"}
                    </span>
                  </p>
                </div>
                <div className="flex items-center">
                  <Star className="w-4 h-4 text-yellow-500 mr-2" />
                  <p className="text-sm text-gray-600">
                    Top Rated Plus with fast shipping and excellent service
                  </p>
                </div>
                <div className="flex items-center">
                  <Shield className="w-4 h-4 text-green-500 mr-2" />
                  <p className="text-sm text-gray-600">Money back guarantee</p>
                </div>
                <div className="flex items-center">
                  <Truck className="w-4 h-4 text-blue-500 mr-2" />
                  <p className="text-sm text-gray-600">
                    Estimated delivery: 3-5 business days
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Product Details Tabs */}
          <div className="mt-12">
            <Tabs defaultValue="details">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="details">Product Details</TabsTrigger>
                <TabsTrigger value="shipping">Shipping & Returns</TabsTrigger>
                <TabsTrigger value="seller">Seller Information</TabsTrigger>
              </TabsList>
              <TabsContent value="details" className="p-4">
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold">Specifications</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-600">Condition</span>
                        <span className="font-medium capitalize">
                          {product.condition.replace("_", " ")}
                        </span>
                      </div>
                      <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-600">Category</span>
                        <span className="font-medium capitalize">
                          {product.category}
                        </span>
                      </div>
                      <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-600">Listed</span>
                        <span className="font-medium">
                          {format(
                            new Date(product.created_date),
                            "MMM d, yyyy"
                          )}
                        </span>
                      </div>
                    </div>
                    <div className="space-y-2">
                      <div className="flex justify-between border-b pb-2">
                        <span className="text-gray-600">Item Number</span>
                        <span className="font-medium">
                          {product.id.substring(0, 8)}
                        </span>
                      </div>
                      {product.bid_end_date && (
                        <div className="flex justify-between border-b pb-2">
                          <span className="text-gray-600">Auction Ends</span>
                          <span className="font-medium">
                            {format(
                              new Date(product.bid_end_date),
                              "MMM d, yyyy HH:mm"
                            )}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </TabsContent>
              <TabsContent value="shipping" className="p-4">
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold">
                    Shipping Information
                  </h3>
                  <p>Standard shipping delivered within 3-5 business days.</p>
                  <div className="mt-4">
                    <h4 className="font-medium mb-2">Return Policy</h4>
                    <p className="text-gray-600">
                      This item can be returned within 30 days of delivery.
                      Items must be in original condition, with tags, and in
                      original packaging.
                    </p>
                  </div>
                </div>
              </TabsContent>
              <TabsContent value="seller" className="p-4">
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold">About the Seller</h3>
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center">
                      <Building className="w-6 h-6 text-gray-500" />
                    </div>
                    <div>
                      <h4 className="font-medium">
                        {product.seller || "MarketPlace Seller"}
                      </h4>
                      <div className="flex items-center mt-1">
                        <div className="flex">
                          {[1, 2, 3, 4, 5].map((star) => (
                            <Star
                              key={star}
                              className="w-4 h-4 text-yellow-400 fill-yellow-400"
                            />
                          ))}
                        </div>
                        <span className="text-sm text-gray-600 ml-1">
                          (4.9/5, 243 ratings)
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="mt-4">
                    <p className="text-gray-600">
                      Top Rated Plus seller with excellent track record of
                      customer satisfaction.
                    </p>
                    <Button
                      variant="link"
                      className="px-0 mt-2 text-blue-600 hover:text-blue-800 flex items-center"
                    >
                      See all items from this seller
                      <ChevronRight className="w-4 h-4 ml-1" />
                    </Button>
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          </div>
        </div>
      </main>
    </div>
  );
}
