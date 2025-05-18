import React, { useState } from "react";
import {
  Box,
  Container,
  Grid,
  Typography,
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
        />
        <FeaturedSection
          title="New Arrivals"
          subtitle="Just hit the marketplace"
          limit={4}
        />
        <FeaturedSection
          title="Ending Soon"
          subtitle="Get them before they're gone"
          limit={4}
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