import React from "react";
import { productService } from "../../services/productService";
import { useEffect, useState, useCallback, useMemo } from "react";
import { Link } from "react-router-dom";
import { createPageUrl } from "../../utils";
import "./FeaturedSection.css";

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

import ProductCard from "../ProductCard/ProductCard";

export default function FeaturedSection({
    title,
    subtitle,
    filter = {},
    limit = 4,
    products: initialProducts = [],
}) {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [fetched, setFetched] = useState(false); // ✅ ADD THIS

    // ✅ FIXED: Memoize filter to prevent object recreation
    const memoizedFilter = useMemo(() => filter, [
        filter?.category,
        filter?.featured,
        // Add other filter properties as needed
    ]);

    // ✅ FIXED: Memoize initialProducts array
    const memoizedInitialProducts = useMemo(() => initialProducts, [
        initialProducts?.length,
        // Only depend on length, not the array reference
    ]);

    // Create a function to build the "View All" link based on filter criteria
    const createViewAllLink = useCallback(() => {
        const baseUrl = createPageUrl("SearchResults");
        const params = new URLSearchParams();

        // Add any filter criteria to the URL
        if (memoizedFilter) {
            if (memoizedFilter.category) {
                params.append('category', memoizedFilter.category);
            }
            if (memoizedFilter.featured !== undefined) {
                params.append('featured', memoizedFilter.featured);
            }
        }

        // Return the URL with query parameters
        return params.toString() ? `${baseUrl}?${params.toString()}` : baseUrl;
    }, [memoizedFilter]);

    // ✅ FIXED: Add guards and proper dependency management
    useEffect(() => {
        // If initialProducts are provided, use them
        if (memoizedInitialProducts && memoizedInitialProducts.length > 0) {
            if (!fetched) { // ✅ Only set once
                setProducts(memoizedInitialProducts);
                setLoading(false);
                setFetched(true);
            }
        } else {
            // Guard against multiple simultaneous calls
            if (fetched || loading) {
                return;
            }

            // Otherwise, fetch products based on filter
            const fetchProducts = async () => {
                try {
                    setLoading(true);
                    setFetched(true);
                    
                    const fetchedProducts = await productService.filterProducts(
                        memoizedFilter,
                        "-created_date",
                        limit
                    );
                    setProducts(fetchedProducts);
                } catch (error) {
                    console.error("Error fetching products:", error);
                    setFetched(false); // Allow retry on error
                } finally {
                    setLoading(false);
                }
            };

            fetchProducts();
        }
    }, [memoizedFilter, memoizedInitialProducts, limit, fetched, loading]); // ✅ FIXED: Use memoized values

    return (
        <Box component="section" className="featured-section">
            <Box className="featured-section-header">
                <Box>
                    <Typography variant="h4" component="h2" className="featured-section-title">
                        {title}
                    </Typography>
                    {subtitle && (
                        <Typography variant="body1" className="featured-section-subtitle">
                            {subtitle}
                        </Typography>
                    )}
                </Box>
                <MuiLink
                    component={Link}
                    to={createViewAllLink()}
                    className="featured-section-view-all"
                >
                    View all <ArrowRightAltIcon className="featured-section-view-all-icon" />
                </MuiLink>
            </Box>

            <Grid container spacing={3}>
                {loading
                    ? // Loading skeletons
                    Array(limit)
                        .fill(0)
                        .map((_, index) => (
                            <Grid item xs={12} sm={6} md={4} lg={3} key={index}>
                                <Box className="featured-section-skeleton-card">
                                    <MuiSkeleton variant="rectangular" height={192} width="100%" />
                                    <Box className="featured-section-skeleton-content">
                                        <MuiSkeleton variant="text" width="75%" height={24} />
                                        <MuiSkeleton variant="text" width="40%" height={20} />
                                        <Box className="featured-section-skeleton-actions">
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