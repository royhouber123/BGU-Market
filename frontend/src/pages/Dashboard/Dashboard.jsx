import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import Header from '../../components/Header/Header';
import AuthDialog from '../../components/AuthDialog/AuthDialog';
import ProductCard from '../../components/ProductCard/ProductCard';
import HeroSection from '../../components/HeroSection/HeroSection';
import CategoryCard from '../../components/CategoryCard/CategoryCard';
import FeaturedSection from '../../components/FeaturedSection/FeaturedSection';
import MiniCart from '../../components/MiniCart/MiniCart';
import { storeService } from '../../services/storeService';
import { productService } from '../../services/productService';
import './Dashboard.css';

// Material-UI imports
import {
  Box,
  Container,
  Typography,
  Grid,
  Paper,
  Card,
  CardContent,
  CardActions,
  Button,
  Divider,
  CircularProgress,
  Alert,
  Chip
} from '@mui/material';

// Material-UI icons
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import StorefrontIcon from '@mui/icons-material/Storefront';
import InventoryIcon from '@mui/icons-material/Inventory';
import LaptopIcon from "@mui/icons-material/Laptop";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import HomeIcon from "@mui/icons-material/Home";
import MenuBookIcon from "@mui/icons-material/MenuBook";
import CheckroomIcon from "@mui/icons-material/Checkroom";
import CardGiftcardIcon from "@mui/icons-material/CardGiftcard";
import CategoryIcon from "@mui/icons-material/Category";

const Dashboard = () => {
  const { currentUser } = useAuth();
  const [stores, setStores] = useState([]);
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAuthDialog, setShowAuthDialog] = useState(false);
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

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError('');

        // Fetch stores and products from the API
        const { stores: apiStores, products: apiProducts } = await storeService.getAllStoresAndProducts();

        // Fetch categories from the API
        const apiCategories = await productService.getCategories();

        setStores(apiStores);
        setProducts(apiProducts);
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

  const handleCreateStore = async () => {
    if (!currentUser) {
      setShowAuthDialog(true);
      return;
    }

    try {
      // For now, we'll just show an alert. In a real app, you'd open a dialog to get store details
      const storeName = prompt('Enter store name:');
      if (storeName) {
        await storeService.createStore(storeName, currentUser.userName);
        // Refresh the data
        const { stores: apiStores, products: apiProducts } = await storeService.getAllStoresAndProducts();
        const apiCategories = await productService.getCategories();
        setStores(apiStores);
        setProducts(apiProducts);
        setCategories(apiCategories);
      }
    } catch (err) {
      console.error('Error creating store:', err);
      setError(err.message || 'Failed to create store');
    }
  };

  const handleAddProduct = async () => {
    if (!currentUser) {
      setShowAuthDialog(true);
      return;
    }

    try {
      // For now, we'll just show an alert. In a real app, you'd open a dialog to get product details
      alert('Add product functionality would open a form dialog here');
    } catch (err) {
      console.error('Error adding product:', err);
      setError(err.message || 'Failed to add product');
    }
  };

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

        {/* Management Sections - Only show if user is authenticated */}
        {currentUser && (
          <>
            {/* Dashboard Header */}
            <Box sx={{ textAlign: 'center', mt: 8, mb: 4 }}>
              <Typography variant="h4" component="h1" gutterBottom>
                Welcome to your Dashboard, {currentUser.userName}
              </Typography>
              <Typography variant="subtitle1" color="text.secondary">
                Manage your stores, products, and orders from one place
              </Typography>
            </Box>

            <Grid container spacing={3}>
              {/* Stores Section */}
              <Grid item xs={12}>
                <Paper sx={{ p: 3, borderRadius: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h5" component="h2" sx={{ display: 'flex', alignItems: 'center' }}>
                      <StorefrontIcon sx={{ mr: 1 }} /> Your Stores
                    </Typography>
                    <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreateStore}>
                      New Store
                    </Button>
                  </Box>
                  <Divider sx={{ mb: 3 }} />

                  {stores.length > 0 ? (
                    <Grid container spacing={2}>
                      {stores.map(store => (
                        <Grid item xs={12} sm={6} md={4} key={store.id}>
                          <Card variant="outlined" sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                            <CardContent sx={{ flexGrow: 1 }}>
                              <Typography variant="h6" component="h3" gutterBottom>
                                {store.name}
                              </Typography>
                              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                {store.description}
                              </Typography>
                              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                <Chip
                                  label={`${store.products} products`}
                                  size="small"
                                  color="primary"
                                  variant="outlined"
                                />
                                <Chip
                                  label={store.isActive ? 'Active' : 'Inactive'}
                                  size="small"
                                  color={store.isActive ? 'success' : 'default'}
                                  variant="outlined"
                                />
                              </Box>
                            </CardContent>
                            <CardActions>
                              <Button size="small" variant="contained" color="primary">View Store</Button>
                              <Button size="small" startIcon={<EditIcon />}>Edit</Button>
                            </CardActions>
                          </Card>
                        </Grid>
                      ))}
                      <Grid item xs={12} sm={6} md={4}>
                        <Card
                          variant="outlined"
                          sx={{
                            height: '100%',
                            display: 'flex',
                            flexDirection: 'column',
                            justifyContent: 'center',
                            alignItems: 'center',
                            p: 3,
                            bgcolor: 'action.hover',
                            cursor: 'pointer'
                          }}
                          onClick={handleCreateStore}
                        >
                          <AddIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                          <Button variant="contained" color="primary">Create New Store</Button>
                        </Card>
                      </Grid>
                    </Grid>
                  ) : (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                      <Typography variant="body1" gutterBottom>No stores available yet.</Typography>
                      <Button variant="contained" color="primary" sx={{ mt: 2 }} onClick={handleCreateStore}>
                        Create Your First Store
                      </Button>
                    </Box>
                  )}
                </Paper>
              </Grid>

              {/* Products Section */}
              <Grid item xs={12}>
                <Paper sx={{ p: 3, borderRadius: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                    <Typography variant="h5" component="h2" sx={{ display: 'flex', alignItems: 'center' }}>
                      <InventoryIcon sx={{ mr: 1 }} /> Your Products
                    </Typography>
                    <Button variant="contained" startIcon={<AddIcon />} onClick={handleAddProduct}>
                      New Product
                    </Button>
                  </Box>
                  <Divider sx={{ mb: 3 }} />

                  {products.length > 0 ? (
                    <Grid container spacing={2}>
                      {products.slice(0, 8).map(product => (
                        <Grid item xs={12} sm={6} md={3} key={product.id}>
                          <ProductCard product={product} />
                        </Grid>
                      ))}
                      <Grid item xs={12} sm={6} md={3}>
                        <Card
                          variant="outlined"
                          sx={{
                            height: '100%',
                            display: 'flex',
                            flexDirection: 'column',
                            justifyContent: 'center',
                            alignItems: 'center',
                            p: 3,
                            bgcolor: 'action.hover',
                            cursor: 'pointer'
                          }}
                          onClick={handleAddProduct}
                        >
                          <AddIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                          <Button variant="contained" color="primary">Add New Product</Button>
                        </Card>
                      </Grid>
                    </Grid>
                  ) : (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                      <Typography variant="body1" gutterBottom>No products available yet.</Typography>
                      <Button variant="contained" color="primary" sx={{ mt: 2 }} onClick={handleAddProduct}>
                        Add Your First Product
                      </Button>
                    </Box>
                  )}
                </Paper>
              </Grid>

              {/* Recent Activity Section */}
              <Grid item xs={12}>
                <Paper sx={{ p: 3, borderRadius: 2 }}>
                  <Typography variant="h5" component="h2" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                    <ShoppingCartIcon sx={{ mr: 1 }} /> Recent Activity
                  </Typography>
                  <Divider sx={{ mb: 3 }} />
                  <Typography variant="body1" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
                    No recent activity to display.
                  </Typography>
                </Paper>
              </Grid>
            </Grid>
          </>
        )}

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
      {showAuthDialog && (
        <AuthDialog
          open={showAuthDialog}
          onClose={() => setShowAuthDialog(false)}
        />
      )}
    </Box>
  );
};

export default Dashboard;
