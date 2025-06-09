import React, { useEffect, useState, useCallback } from "react";
import { productService } from "../../services/productService";
import Header from "../../components/Header/Header";
import ProductCard from "../../components/ProductCard/ProductCard";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import MiniCart from "../../components/MiniCart/MiniCart";
import { Skeleton, Chip, Container } from "@mui/material";
import Box from "@mui/material/Box";
import './SearchResults.css';

export default function SearchResults() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [showMiniCart, setShowMiniCart] = useState(false);
  const urlParams = new URLSearchParams(window.location.search);
  const searchQuery = urlParams.get("q") || "";
  const categoryFilter = urlParams.get("category") || "";

  // Use useCallback to memoize the fetchProducts function
  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      let results;
      if (searchQuery) {
        results = await productService.searchProducts(searchQuery);
      } else if (categoryFilter) {
        results = await productService.getProductsByCategory(categoryFilter);
      } else {
        results = await productService.getAllProducts();
      }
      setProducts(results);
    } catch (error) {
      console.error("Error fetching products:", error);
    } finally {
      setLoading(false);
    }
  }, [searchQuery, categoryFilter]); // Include dependencies here

  // Call fetchProducts only when the function or its dependencies change
  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]); // fetchProducts includes searchQuery and categoryFilter as deps

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
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Header />
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <div className="mb-8">
          <h1 className="text-2xl font-bold">{getPageTitle()}</h1>
          <div className="flex items-center mt-2">
            <p className="text-gray-600">Showing {products.length} items</p>
            {categoryFilter && (
              <div className="inline-flex items-center gap-1 text-sm ml-2">
                <span>Filtering by:</span>
                <Chip
                  label={categoryFilter}
                  variant="outlined"
                  color="primary"
                  size="small"
                />
              </div>
            )}
          </div>
        </div>

        {loading ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' }, gap: 4, maxWidth: '1000px', width: '100%' }}>
              {Array(9)
                .fill(0)
                .map((_, index) => (
                  <Box
                    key={index}
                    sx={{
                      bgcolor: 'background.paper',
                      borderRadius: 2,
                      overflow: 'hidden',
                      boxShadow: 1
                    }}
                  >
                    <Skeleton variant="rectangular" height={200} />
                    <Box sx={{ p: 2, pt: 3 }}>
                      <Skeleton height={24} width="75%" sx={{ mb: 1 }} />
                      <Skeleton height={20} width="40%" sx={{ mb: 2 }} />
                      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                        <Skeleton height={16} width="30%" />
                        <Skeleton height={16} width="20%" />
                      </Box>
                    </Box>
                  </Box>
                ))}
            </Box>
          </Box>
        ) : products.length > 0 ? (
          <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' }, gap: 4, maxWidth: '1000px', width: '100%' }}>
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </Box>
          </Box>
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
      </Container>
      {/* Add conditional rendering for auth and cart components */}
      {showAuthDialog && <AuthDialog open={showAuthDialog} onClose={() => setShowAuthDialog(false)} />}
      {showMiniCart && <MiniCart onClose={() => setShowMiniCart(false)} />}
    </Box>
  );
}
