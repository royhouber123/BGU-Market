import React, { useEffect, useState } from "react";
import { Product } from "@/entities/Product";
import Header from "../components/Header";
import ProductCard from "../components/ProductCard";
import { Skeleton } from "@/components/ui/skeleton";
import { Badge } from "@/components/ui/badge";

export default function SearchResults() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const urlParams = new URLSearchParams(window.location.search);
  const searchQuery = urlParams.get("q") || "";
  const categoryFilter = urlParams.get("category") || "";

  useEffect(() => {
    fetchProducts();
  }, [searchQuery, categoryFilter]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const allProducts = await Product.list();
      let filteredProducts = allProducts;

      // Filter by search query if present
      if (searchQuery) {
        filteredProducts = filteredProducts.filter((product) =>
          product.title.toLowerCase().includes(searchQuery.toLowerCase())
        );
      }

      // Filter by category if present
      if (categoryFilter) {
        filteredProducts = filteredProducts.filter(
          (product) =>
            product.category.toLowerCase() === categoryFilter.toLowerCase()
        );
      }

      setProducts(filteredProducts);
    } catch (error) {
      console.error("Error fetching products:", error);
    }
    setLoading(false);
  };

  const getPageTitle = () => {
    if (categoryFilter) {
      return categoryFilter.charAt(0).toUpperCase() + categoryFilter.slice(1);
    }
    if (searchQuery) {
      return `Results for "${searchQuery}"`;
    }
    return "All Products";
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-2xl font-bold">{getPageTitle()}</h1>
          <div className="flex items-center mt-2">
            <p className="text-gray-600">Showing {products.length} items</p>
            {categoryFilter && (
              <Badge className="ml-4 bg-blue-100 text-blue-800 hover:bg-blue-200">
                Category: {categoryFilter}
              </Badge>
            )}
          </div>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {Array(8)
              .fill(0)
              .map((_, index) => (
                <div
                  key={index}
                  className="bg-white rounded-lg overflow-hidden shadow-md"
                >
                  <Skeleton className="w-full h-48" />
                  <div className="p-4 space-y-3">
                    <Skeleton className="h-6 w-3/4" />
                    <Skeleton className="h-5 w-1/3" />
                    <div className="flex justify-between">
                      <Skeleton className="h-4 w-1/4" />
                      <Skeleton className="h-4 w-1/4" />
                    </div>
                  </div>
                </div>
              ))}
          </div>
        ) : products.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        ) : (
          <div className="text-center py-16">
            <h2 className="text-xl font-semibold text-gray-600">
              No results found
            </h2>
            <p className="text-gray-500 mt-2">
              Try different keywords or browse categories instead
            </p>
          </div>
        )}
      </main>
    </div>
  );
}
