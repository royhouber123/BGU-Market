import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import Header from '../components/Header';
import AuthDialog from '../components/AuthDialog.jsx';
import ProductCard from '../components/ProductCard.jsx';

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

const Dashboard = () => {
  const { currentUser } = useAuth();
  const [stores, setStores] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAuthDialog, setShowAuthDialog] = useState(false);
  const [showMiniCart, setShowMiniCart] = useState(false);

  useEffect(() => {
    // Simulating API call with timeout
    const fetchData = () => {
      setTimeout(() => {
        // Example data
        const mockStores = [
          { id: 1, name: 'Electronics Hub', description: 'All your electronic needs', products: 12 },
          { id: 2, name: 'Fashion Outlet', description: 'Latest trends in fashion', products: 8 }
        ];
        
        const mockProducts = [
          { id: 101, title: 'Wireless Headphones', price: 99.99, status: 'active' },
          { id: 102, title: 'Smartphone Case', price: 19.99, status: 'active' },
          { id: 103, title: 'Laptop Sleeve', price: 29.99, status: 'pending' },
          { id: 104, title: 'Bluetooth Speaker', price: 79.99, status: 'active' }
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
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <Header />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {/* Dashboard Header */}
        <Box sx={{ textAlign: 'center', mb: 4 }}>
          <Typography variant="h4" component="h1" gutterBottom>
            Welcome to your Dashboard, {currentUser?.userName || 'User'}
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
                <Button variant="contained" startIcon={<AddIcon />}>
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
                          <Chip 
                            label={`${store.products} products`} 
                            size="small" 
                            color="primary" 
                            variant="outlined"
                          />
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
                        bgcolor: 'action.hover'
                      }}
                    >
                      <AddIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                      <Button variant="contained" color="primary">Create New Store</Button>
                    </Card>
                  </Grid>
                </Grid>
              ) : (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" gutterBottom>You don't have any stores yet.</Typography>
                  <Button variant="contained" color="primary" sx={{ mt: 2 }}>
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
                <Button variant="contained" startIcon={<AddIcon />}>
                  New Product
                </Button>
              </Box>
              <Divider sx={{ mb: 3 }} />
              
              {products.length > 0 ? (
                <Grid container spacing={2}>
                  {products.map(product => (
                    <Grid item xs={12} sm={6} md={3} key={product.id}>
                      <ProductCard product={{
                        id: product.id,
                        title: product.title,
                        price: product.price,
                        images: ["https://source.unsplash.com/random/300x200?sig=" + product.id],
                        category: "Electronics",
                        shipping_cost: 0,
                        featured: product.status === 'active'
                      }} />
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
                        bgcolor: 'action.hover'
                      }}
                    >
                      <AddIcon sx={{ fontSize: 48, color: 'text.secondary', mb: 2 }} />
                      <Button variant="contained" color="primary">Add New Product</Button>
                    </Card>
                  </Grid>
                </Grid>
              ) : (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" gutterBottom>You don't have any products listed yet.</Typography>
                  <Button variant="contained" color="primary" sx={{ mt: 2 }}>
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
      </Container>
    </Box>
  );
};

export default Dashboard;
