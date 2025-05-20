import React from "react";
import productService from "../services/productService";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { createPageUrl } from "../utils";

// Material-UI imports
import { 
  Typography, 
  Box, 
  Grid, 
  Container, 
  Link as MuiLink, 
  Skeleton as MuiSkeleton
} from "@mui/material";
import ArrowRightAltIcon from "@mui/icons-material/ArrowRightAlt";

import ProductCard from "./ProductCard";

export default function FeaturedSection({
  title,
  subtitle,
  filter = {},
  limit = 4,
  products: initialProducts = [],
}) {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  // Create a function to build the "View All" link based on filter criteria
  const createViewAllLink = () => {
    const baseUrl = createPageUrl("SearchResults");
    const params = new URLSearchParams();
    
    // Add any filter criteria to the URL
    if (filter) {
      if (filter.category) {
        params.append('category', filter.category);
      }
      if (filter.featured !== undefined) {
        params.append('featured', filter.featured);
      }
    }
    
    // Return the URL with query parameters
    return params.toString() ? `${baseUrl}?${params.toString()}` : baseUrl;
  };
  
  useEffect(() => {
    // If initialProducts are provided, use them
    if (initialProducts && initialProducts.length > 0) {
      setProducts(initialProducts);
      setLoading(false);
    } else {
      // Otherwise, fetch products based on filter
      const fetchProducts = async () => {
        try {
          setLoading(true);
          const fetchedProducts = await productService.filterProducts(
            filter,
            "-created_date",
            limit
          );
          setProducts(fetchedProducts);
        } catch (error) {
          console.error("Error fetching products:", error);
        } finally {
          setLoading(false);
        }
      };

      fetchProducts();
    }
  }, [filter, initialProducts]);

  return (
    <Box component="section" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
        <Box>
          <Typography variant="h4" component="h2" sx={{ fontWeight: 'bold' }}>
            {title}
          </Typography>
          {subtitle && (
            <Typography variant="body1" color="text.secondary" sx={{ mt: 0.5 }}>
              {subtitle}
            </Typography>
          )}
        </Box>
        <MuiLink
          component={Link}
          to={createViewAllLink()}
          sx={{ 
            display: 'flex', 
            alignItems: 'center', 
            color: 'primary.main',
            fontWeight: 500,
            textDecoration: 'none',
            '&:hover': { textDecoration: 'underline' } 
          }}
        >
          View all <ArrowRightAltIcon sx={{ ml: 0.5, fontSize: '1.25rem' }} />
        </MuiLink>
      </Box>

      <Grid container spacing={3}>
        {loading
          ? // Loading skeletons
            Array(limit)
              .fill(0)
              .map((_, index) => (
                <Grid item xs={12} sm={6} md={4} lg={3} key={index}>
                  <Box sx={{ 
                    bgcolor: 'white', 
                    borderRadius: 2, 
                    overflow: 'hidden', 
                    boxShadow: 1,
                    height: '100%'
                  }}>
                    <MuiSkeleton variant="rectangular" height={192} width="100%" />
                    <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                      <MuiSkeleton variant="text" width="75%" height={24} />
                      <MuiSkeleton variant="text" width="40%" height={20} />
                      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                        <MuiSkeleton variant="text" width="25%" height={16} />
                        <MuiSkeleton variant="text" width="25%" height={16} />
                      </Box>
                    </Box>
                  </Box>
                </Grid>
              ))
          : products.map((product) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
                <ProductCard product={product} />
              </Grid>
            ))}
      </Grid>
    </Box>
  );
}
