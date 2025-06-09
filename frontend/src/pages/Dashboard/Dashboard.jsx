import React, { useState, useEffect, useCallback } from 'react';
import Header from '../../components/Header/Header';
import AuthDialog from '../../components/AuthDialog/AuthDialog';
import HeroSection from '../../components/HeroSection/HeroSection';
import CategoryCard from '../../components/CategoryCard/CategoryCard';
import FeaturedSection from '../../components/FeaturedSection/FeaturedSection';
import MiniCart from '../../components/MiniCart/MiniCart';
import { productService } from '../../services/productService';
import { useAuth } from '../../contexts/AuthContext';
import './Dashboard.css';

// Material-UI imports
import {
  Box,
  Container,
  Typography,
  Grid,
  CircularProgress,
  Alert,
  Button
} from '@mui/material';

// Material-UI icons
import LaptopIcon from "@mui/icons-material/Laptop";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import HomeIcon from "@mui/icons-material/Home";
import MenuBookIcon from "@mui/icons-material/MenuBook";
import CheckroomIcon from "@mui/icons-material/Checkroom";
import CardGiftcardIcon from "@mui/icons-material/CardGiftcard";
import CategoryIcon from "@mui/icons-material/Category";

const Dashboard = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const { isAuthenticated, loading: authLoading } = useAuth();
  const [showMiniCart, setShowMiniCart] = useState(false);
  const [dataFetched, setDataFetched] = useState(false);

  // Icon mapping for categories
  const getCategoryIcon = (categoryName) => {
    const iconMap = {
      'Electronics': <LaptopIcon fontSize="small" />,
      'Clothing': <CheckroomIcon fontSize="small" />,
      'Fashion': <CheckroomIcon fontSize="small" />,
      'Home': <HomeIcon fontSize="small" />,
      'Books': <MenuBookIcon fontSize="small" />,
      'Sports': <ShoppingBagIcon fontSize="small" />,
      'Toys': <CardGiftcardIcon fontSize="small" />,
      'Collectibles': <CardGiftcardIcon fontSize="small" />
    };
    return iconMap[categoryName] || <CategoryIcon fontSize="small" />;
  };

  // Generate image URL for categories
  const getCategoryImage = (categoryName) => {
    const imageMap = {
      'Electronics': "https://images.unsplash.com/photo-1468495244123-6c6c332eeece?ixlib=rb-4.0.3&auto=format&fit=crop&w=2021&q=80",
      'Clothing': "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
      'Fashion': "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
      'Home': "https://images.unsplash.com/photo-1513694203232-719a280e022f?ixlib=rb-4.0.3&auto=format&fit=crop&w=2069&q=80",
      'Books': "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
      'Sports': "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
      'Toys': "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
      'Collectibles': "https://images.unsplash.com/photo-1599360889420-da1afaba9edc?ixlib=rb-4.0.3&auto=format&fit=crop&w=2069&q=80"
    };
    return imageMap[categoryName] || "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?ixlib=rb-4.0.3&auto=format&fit=crop&w=2340&q=80";
  };

  // More efficient version of discount fetching - batches products by store
  const fetchDiscountedPrices = useCallback(async (products) => {
    if (!products || products.length === 0) {
      return products;
    }

    // Group products by storeId for more efficient API calls
    const productsByStore = {};
    products.forEach(product => {
      if (product && product.storeId && product.id) {
        if (!productsByStore[product.storeId]) {
          productsByStore[product.storeId] = [];
        }
        productsByStore[product.storeId].push(product);
      }
    });

    // Updated products array that will contain products with discounts
    const updatedProducts = [...products];
    const productMap = {};
    
    // Create a map for easy lookup when updating
    products.forEach((product, index) => {
      if (product.id) {
        productMap[product.id] = index;
      }
    });

    try {
      // Process each store's products in parallel
      const storePromises = Object.entries(productsByStore).map(async ([storeId, storeProducts]) => {
        try {
          // Batch fetch discounted prices - ideally the backend would support this
          // This is a fallback to individual fetches
          await Promise.all(storeProducts.map(async (product) => {
            try {
              const response = await fetch(`http://localhost:8080/api/stores/${product.storeId}/products/${product.id}/discounted-price`);
              const apiResponse = await response.json();
              
              if (apiResponse.success && apiResponse.data !== undefined) {
                const discountPrice = apiResponse.data;
                // Only set discounted price if it's actually different from the original price
                if (discountPrice < product.price) {
                  const index = productMap[product.id];
                  if (index !== undefined) {
                    updatedProducts[index] = {
                      ...product,
                      hasDiscount: true,
                      discountedPrice: discountPrice
                    };
                  }
                }
              }
            } catch (error) {
              console.warn(`Could not fetch discounted price for product ${product.id}:`, error);
            }
          }));
        } catch (error) {
          console.error(`Error processing store ${storeId} discounts:`, error);
        }
      });

      await Promise.all(storePromises);
      return updatedProducts;
      
    } catch (error) {
      console.error('Error fetching discounted prices:', error);
      return products;
    }
  }, []);

  useEffect(() => {
    // Only fetch data if it hasn't been fetched yet
    if (!dataFetched) {
      const fetchData = async () => {
        try {
          setLoading(true);
          setError('');

          // Fetch products from the API
          const apiProducts = await productService.getAllProducts();
          
          // Fetch categories from the API
          const apiCategories = await productService.getCategories();

          // Only fetch discounted prices for a reasonable number of products to avoid API spam
          // You could limit this to featured products only
          const productsToProcess = apiProducts.slice(0, 20); // Process max 20 products
          const productsWithDiscounts = await fetchDiscountedPrices(productsToProcess);
          
          // Merge processed products back with the full list
          const productMap = {};
          productsWithDiscounts.forEach(product => {
            if (product.id) {
              productMap[product.id] = product;
            }
          });
          
          const mergedProducts = apiProducts.map(product => 
            productMap[product.id] ? productMap[product.id] : product
          );

          setProducts(mergedProducts);
          setCategories(apiCategories);
          setDataFetched(true);
        } catch (err) {
          console.error('Error fetching dashboard data:', err);
          setError(err.message || 'Failed to load dashboard data');
        } finally {
          setLoading(false);
        }
      };

      fetchData();
    }
  }, [dataFetched, fetchDiscountedPrices]);  // Only depend on dataFetched flag

  if (loading) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Header />
        <Container>
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
            <CircularProgress />
          </Box>
        </Container>
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Header />
        <Container>
          <Alert severity="error" sx={{ mt: 4 }}>
            {error}
            <Button
              onClick={() => window.location.reload()}
              sx={{ ml: 2 }}
              variant="outlined"
              size="small"
            >
              Retry
            </Button>
          </Alert>
        </Container>
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Header />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {/* Hero Section */}
        <HeroSection />

        {/* Categories Section */}
        <Box component="section" sx={{ mt: 8 }}>
          <Typography
            variant="h4"
            component="h2"
            sx={{ fontWeight: "bold", mb: 3 }}
          >
            Shop by Category
          </Typography>

          <Grid container spacing={3}>
            {categories.map((categoryName, idx) => (
              <Grid item xs={12} sm={6} md={4} key={idx}>
                <CategoryCard
                  name={categoryName}
                  icon={getCategoryIcon(categoryName)}
                  imageUrl={getCategoryImage(categoryName)}
                />
              </Grid>
            ))}
          </Grid>
        </Box>

        {/* Featured Sections */}
        <FeaturedSection
          title="Featured Items"
          subtitle="Handpicked deals just for you"
          limit={4}
          products={products.slice(0, 4)}
        />
        <FeaturedSection
          title="New Arrivals"
          subtitle="Just hit the marketplace"
          limit={4}
          products={products.slice(4, 8)}
        />

        {/* Ending Soon Section for all users */}
        <FeaturedSection
          title="Ending Soon"
          subtitle="Get them before they're gone"
          limit={4}
          products={products.slice(8, 12)}
        />
      </Container>

      {/* Dialogs & Overlays */}
      {showMiniCart && <MiniCart onClose={() => setShowMiniCart(false)} />}
      {/* Only show auth dialog when explicitly triggered, not on refresh */
      // showAuthDialog && !isAuthenticated && (
      //   <AuthDialog
      //     open={showAuthDialog}
      //     onClose={() => setShowAuthDialog(false)}
      //   />
      // )
      }
    </Box>
  );
};

export default Dashboard;
