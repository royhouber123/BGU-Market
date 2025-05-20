import React, { useState, useEffect } from "react";
import {
  Box,
  Container,
  Grid,
  Typography,
  CircularProgress,
  Alert,
} from "@mui/material";

// Material‑UI icons
import LaptopIcon from "@mui/icons-material/Laptop";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import HomeIcon from "@mui/icons-material/Home";
import MenuBookIcon from "@mui/icons-material/MenuBook";
import CheckroomIcon from "@mui/icons-material/Checkroom";
import CardGiftcardIcon from "@mui/icons-material/CardGiftcard";

// Components
import Header from "../components/Header.jsx";
import HeroSection from "../components/HeroSection.jsx";
import CategoryCard from "../components/CategoryCard.jsx";
import FeaturedSection from "../components/FeaturedSection.jsx";
import MiniCart from "../components/MiniCart.jsx";
import AuthDialog from "../components/AuthDialog.jsx";

export default function Home() {
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [showMiniCart, setShowMiniCart] = useState(false);
  const [stores, setStores] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const categories = [
    {
      name: "Electronics",
      icon: <LaptopIcon fontSize="small" />,
      imageUrl:
        "https://images.unsplash.com/photo-1468495244123-6c6c332eeece?ixlib=rb-4.0.3&auto=format&fit=crop&w=2021&q=80",
    },
    {
      name: "Fashion",
      icon: <CheckroomIcon fontSize="small" />,
      imageUrl:
        "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
    },
    {
      name: "Home",
      icon: <HomeIcon fontSize="small" />,
      imageUrl:
        "https://images.unsplash.com/photo-1513694203232-719a280e022f?ixlib=rb-4.0.3&auto=format&fit=crop&w=2069&q=80",
    },
    {
      name: "Collectibles",
      icon: <CardGiftcardIcon fontSize="small" />,
      imageUrl:
        "https://images.unsplash.com/photo-1599360889420-da1afaba9edc?ixlib=rb-4.0.3&auto=format&fit=crop&w=2069&q=80",
    },
    {
      name: "Toys",
      icon: <CardGiftcardIcon fontSize="small" />,
      imageUrl:
        "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
    },
    {
      name: "Sports",
      icon: <ShoppingBagIcon fontSize="small" />,
      imageUrl:
        "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
    },
    {
      name: "Books",
      icon: <MenuBookIcon fontSize="small" />,
      imageUrl:
        "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?ixlib=rb-4.0.3&auto=format&fit=crop&w=2070&q=80",
    },
  ];

  useEffect(() => {
    // USING MOCK DATA FOR NOW
    // TODO: Replace with actual API calls when backend is ready
    // Future backend integration:
    // 1. Replace this with axios or fetch calls to the Spring Boot endpoints
    // 2. Example API call: 
    // axios.get('/api/products')
    //   .then(response => setProducts(response.data))
    //   .catch(err => setError(err.message))

    const fetchData = () => {
      setTimeout(() => {
        // Mock stores data
        const mockStores = [
          { id: 1, name: 'Electronics Hub', description: 'All your electronic needs', products: 12 },
          { id: 2, name: 'Fashion Outlet', description: 'Latest trends in fashion', products: 8 }
        ];
        
        // Mock products data
        const mockProducts = [
          { 
            id: 101, 
            title: 'Wireless Headphones', 
            price: 99.99, 
            status: 'active',
            images: ["https://source.unsplash.com/random/300x200?sig=101"],
            category: "Electronics",
            shipping_cost: 0,
            featured: true
          },
          { 
            id: 102, 
            title: 'Smartphone Case', 
            price: 19.99, 
            status: 'active',
            images: ["https://source.unsplash.com/random/300x200?sig=102"],
            category: "Electronics",
            shipping_cost: 0,
            featured: true
          },
          { 
            id: 103, 
            title: 'Laptop Sleeve', 
            price: 29.99, 
            status: 'pending',
            images: ["https://source.unsplash.com/random/300x200?sig=103"],
            category: "Electronics",
            shipping_cost: 0,
            featured: false
          },
          { 
            id: 104, 
            title: 'Bluetooth Speaker', 
            price: 79.99, 
            status: 'active',
            images: ["https://source.unsplash.com/random/300x200?sig=104"],
            category: "Electronics",
            shipping_cost: 0,
            featured: true
          }
        ];
        
        setStores(mockStores);
        setProducts(mockProducts);
        setLoading(false);
      }, 1500); // Simulate loading delay
    };
    
    fetchData();
    
    return () => {
      // Cleanup if needed
    };
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
          <Alert severity="error" sx={{ mt: 4 }}>{error}</Alert>
        </Container>
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <Header />

      <Container maxWidth="lg" component="main" sx={{ py: 4 }}>
        <HeroSection />

        {/* Categories Section */}
        <Box component="section" sx={{ mt: 12 }}>
          <Typography
            variant="h4"
            component="h2"
            sx={{ fontWeight: "bold", mb: 3 }}
          >
            Shop by Category
          </Typography>

          <Grid container spacing={3}>
            {categories.map((category, idx) => (
              <Grid item xs={12} sm={6} md={4} key={idx}>
                <CategoryCard
                  name={category.name}
                  icon={category.icon}
                  imageUrl={category.imageUrl}
                />
              </Grid>
            ))}
          </Grid>
        </Box>

        {/* Featured Sections */}
        <FeaturedSection
          title="Featured Items"
          subtitle="Handpicked deals just for you"
          filter={{ featured: true }}
          limit={4}
          products={products.filter(p => p.featured)}
        />
        <FeaturedSection
          title="New Arrivals"
          subtitle="Just hit the marketplace"
          limit={4}
          products={products}
        />
        <FeaturedSection
          title="Ending Soon"
          subtitle="Get them before they're gone"
          limit={4}
          products={products.slice(0, 2)}
        />
      </Container>

      {/* Dialogs & Overlays */}
      {showMiniCart && <MiniCart onClose={() => setShowMiniCart(false)} />}
      {showAuthDialog && (
        <AuthDialog
          open={showAuthDialog}
          onClose={() => setShowAuthDialog(false)}
        />
      )}
    </Box>
  );
}