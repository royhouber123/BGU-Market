import React, { useState, useEffect } from 'react';
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

  // Fetch discounted price for a single product
  const fetchDiscountedPrice = async (product) => {
    if (!product || !product.storeId || !product.id) {
      return product;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/stores/${product.storeId}/products/${product.id}/discounted-price`);
      const apiResponse = await response.json();

      if (apiResponse.success && apiResponse.data !== undefined) {
        const discountPrice = apiResponse.data;
        // Only set discounted price if it's actually different from the original price
        if (discountPrice < product.price) {
          return {
            ...product,
            hasDiscount: true,
            discountedPrice: discountPrice
          };
        }
      }
    } catch (error) {
      console.warn(`Could not fetch discounted price for product ${product.id}:`, error);
    }

    return product;
  };

  // Fetch discounted prices for multiple products
  const fetchDiscountedPrices = async (products) => {
    if (!products || products.length === 0) {
      return products;
    }

    try {
      const updatedProducts = await Promise.all(
        products.map(product => fetchDiscountedPrice(product))
      );
      return updatedProducts;
    } catch (error) {
      console.error('Error fetching discounted prices:', error);
      return products;
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError('');

        // Fetch products from the API
        const apiProducts = await productService.getAllProducts();

        // Fetch categories from the API
        const apiCategories = await productService.getCategories();

        // Fetch discounted prices for all products
        const productsWithDiscounts = await fetchDiscountedPrices(apiProducts);

        setProducts(productsWithDiscounts);
        setCategories(apiCategories);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError(err.message || 'Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

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
