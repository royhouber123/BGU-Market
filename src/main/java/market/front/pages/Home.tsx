
import React, { useState, useEffect } from "react";
import { Product } from "@/entities/Product";
import { Laptop, ShoppingBag, Car, Home as HomeIcon, BookOpen, Camera, Watch, Music, Shirt, Gift } from "lucide-react";

import Header from "../components/Header";
import HeroSection from "../components/HeroSection";
import CategoryCard from "../components/CategoryCard";
import FeaturedSection from "../components/FeaturedSection";

export default function Home() {
  const categories = [
    { 
      name: "Electronics", 
      icon: <Laptop size={20} />, 
      imageUrl: "https://images.unsplash.com/photo-1468495244123-6c6c332eeece?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2021&q=80" 
    },
    { 
      name: "Fashion", 
      icon: <Shirt size={20} />, 
      imageUrl: "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80" 
    },
    { 
      name: "Home", 
      icon: <HomeIcon size={20} />, 
      imageUrl: "https://images.unsplash.com/photo-1513694203232-719a280e022f?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2069&q=80" 
    },
    { 
      name: "Collectibles", 
      icon: <Gift size={20} />, 
      imageUrl: "https://images.unsplash.com/photo-1599360889420-da1afaba9edc?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2069&q=80" 
    },
    { 
      name: "Toys", 
      icon: <Gift size={20} />, 
      imageUrl: "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80" 
    },
    { 
      name: "Sports", 
      icon: <ShoppingBag size={20} />, 
      imageUrl: "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80" 
    },
    { 
      name: "Books", 
      icon: <BookOpen size={20} />, 
      imageUrl: "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80" 
    },
    { 
      name: "Jewelry", 
      icon: <Watch size={20} />, 
      imageUrl: "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80" 
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <main className="container mx-auto px-4">
        <HeroSection />
        
        {/* Categories Section */}
        <section className="py-8">
          <h2 className="text-2xl font-bold mb-6">Shop by Category</h2>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
            {categories.map((category, index) => (
              <CategoryCard 
                key={index} 
                name={category.name} 
                icon={category.icon} 
                imageUrl={category.imageUrl} 
              />
            ))}
          </div>
        </section>
        
        {/* Featured Products */}
        <FeaturedSection 
          title="Featured Items" 
          subtitle="Handpicked deals just for you" 
          filter={{ featured: true }} 
          limit={4} 
        />
        
        {/* New Arrivals */}
        <FeaturedSection 
          title="New Arrivals" 
          subtitle="Just hit the marketplace" 
          limit={4} 
        />
        
        {/* Ending Soon */}
        <FeaturedSection 
          title="Ending Soon" 
          subtitle="Get them before they're gone" 
          limit={4} 
        />
      </main>
      
      {/* Footer */}
      <footer className="bg-gray-800 text-white mt-16 py-12">
        <div className="container mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div>
              <h3 className="text-lg font-semibold mb-4">BGU-Marketplace</h3>
              <p className="text-gray-400">Find unique items from thousands of sellers around the world.</p>
            </div>
            <div>
              <h3 className="text-lg font-semibold mb-4">Buy</h3>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-white transition-colors">Registration</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Buyer Protection</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Bidding & Buying</a></li>
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-semibold mb-4">Sell</h3>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-white transition-colors">Start Selling</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Learn to Sell</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Seller Fees</a></li>
              </ul>
            </div>
            <div>
              <h3 className="text-lg font-semibold mb-4">About</h3>
              <ul className="space-y-2 text-gray-400">
                <li><a href="#" className="hover:text-white transition-colors">Company Info</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Policies</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Contact Us</a></li>
              </ul>
            </div>
          </div>
          <div className="mt-8 pt-8 border-t border-gray-700 text-center">
            <p className="text-gray-400">&copy; {new Date().getFullYear()} BGU-Marketplace. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
