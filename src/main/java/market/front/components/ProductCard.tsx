import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { createPageUrl } from "@/utils";
import { Heart, Clock } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { User } from "@/entities/User";
import { useToast } from "@/components/ui/use-toast";

export default function ProductCard({ product }) {
  const [isInWatchlist, setIsInWatchlist] = useState(false);
  const navigate = useNavigate();
  const { toast } = useToast();

  useEffect(() => {
    checkWatchlist();
  }, []);

  const checkWatchlist = async () => {
    try {
      const user = await User.me();
      const watchlist = user.watchlist || [];
      setIsInWatchlist(watchlist.includes(product.id));
    } catch (error) {
      // User not logged in or no watchlist
    }
  };

  const timeLeft = product.bid_end_date
    ? new Date(product.bid_end_date) - new Date()
    : null;

  const formatTimeLeft = () => {
    if (!timeLeft || timeLeft <= 0) return "Ended";

    const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
    const hours = Math.floor(
      (timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
    );

    if (days > 0) return `${days}d ${hours}h left`;

    const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
    if (hours > 0) return `${hours}h ${minutes}m left`;

    return `${minutes}m left`;
  };

  const handleProductClick = (e) => {
    e.preventDefault();
    navigate(createPageUrl("ProductDetail") + `?id=${product.id}`);
  };

  const toggleWatchlist = async (e) => {
    e.preventDefault();
    e.stopPropagation();

    try {
      const user = await User.me();
      let watchlist = user.watchlist || [];

      if (isInWatchlist) {
        // Remove from watchlist
        watchlist = watchlist.filter((id) => id !== product.id);
        toast({
          title: "Removed from watchlist",
          description: "Item has been removed from your watchlist",
        });
      } else {
        // Add to watchlist
        watchlist.push(product.id);
        toast({
          title: "Added to watchlist",
          description: "Item has been added to your watchlist",
        });
      }

      await User.updateMyUserData({ watchlist });
      setIsInWatchlist(!isInWatchlist);
    } catch (error) {
      toast({
        title: "Sign in required",
        description: "Please sign in to manage your watchlist",
        variant: "destructive",
      });
    }
  };

  return (
    <motion.div
      whileHover={{ y: -5 }}
      transition={{ duration: 0.2 }}
      className="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300"
    >
      <a href="#" onClick={handleProductClick}>
        <div className="relative">
          <img
            src={product.images[0]}
            alt={product.title}
            className="w-full h-48 object-cover"
          />
          <Button
            variant="ghost"
            size="icon"
            className={`absolute top-2 right-2 bg-white/80 rounded-full hover:bg-white ${
              isInWatchlist ? "text-red-500" : "text-gray-600"
            }`}
            onClick={toggleWatchlist}
          >
            <Heart size={18} className={isInWatchlist ? "fill-current" : ""} />
          </Button>

          {product.featured && (
            <Badge className="absolute top-2 left-2 bg-blue-600 hover:bg-blue-700">
              Featured
            </Badge>
          )}
        </div>

        <div className="p-4">
          <h3 className="text-lg font-medium line-clamp-1">{product.title}</h3>

          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-xl font-bold">
              ${product.price.toFixed(2)}
            </span>

            {product.shipping_cost === 0 ? (
              <span className="text-sm text-green-600">Free shipping</span>
            ) : (
              <span className="text-sm text-gray-500">
                +${product.shipping_cost?.toFixed(2)} shipping
              </span>
            )}
          </div>

          <div className="mt-3 flex items-center justify-between">
            <Badge
              variant="outline"
              className="capitalize cursor-pointer"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                navigate(
                  createPageUrl("SearchResults") +
                    `?category=${product.category}`
                );
              }}
            >
              {product.category}
            </Badge>

            {timeLeft && (
              <div className="flex items-center text-sm text-gray-500">
                <Clock size={14} className="mr-1" />
                {formatTimeLeft()}
              </div>
            )}
          </div>
        </div>
      </a>
    </motion.div>
  );
}
