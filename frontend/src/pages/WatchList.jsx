import React, { useState, useEffect } from "react";
import { Link as RouterLink } from "react-router-dom";
import { createPageUrl } from "../utils";
import userService from "../services/userService";
import productService from "../services/productService";
import {
  Box,
  Container,
  Typography,
  Tabs,
  Tab,
  Skeleton,
  Chip,
  IconButton,
  Button,
  Snackbar,
  Alert,
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Grid,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import FavoriteIcon from "@mui/icons-material/Favorite";
import Header from "../components/Header.jsx";
import AuthDialog from "../components/AuthDialog.jsx";
import MiniCart from "../components/MiniCart.jsx";

export default function Watchlist() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState(0);
  const [authOpen, setAuthOpen] = useState(false);
  const [miniCartOpen, setMiniCartOpen] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });

  const toast = ({ title, description, variant }) => setSnackbar({ open: true, message: description || title, severity: variant === "destructive" ? "error" : "success" });

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        if (!userService.isAuthenticated()) return;
        const user = await userService.getProfile();
        const watch = user.watchlist ?? [];
        if (!watch.length) return;
        const products = await productService.getAllProducts();
        setItems(
          watch.map((w) => {
            const p = products.find((prod) => prod.id === w.productId);
            return { ...w, ...p };
          }),
        );
      } catch (e) { console.error(e); }
      finally { setLoading(false); }
    })();
  }, []);

  const remove = async (pid) => {
    try {
      const user = await userService.getProfile();
      const updated = (user.watchlist || []).filter((i) => i.productId !== pid);
      await userService.updateUserData({ watchlist: updated });
      setItems((prev) => prev.filter((i) => i.productId !== pid));
      toast({ title: "Removed", description: "Item removed from watchlist" });
    } catch {
      toast({ title: "Error", description: "Could not update watchlist", variant: "destructive" });
    }
  };

  const addToCart = async (product) => {
    try {
      if (!userService.isAuthenticated()) { setAuthOpen(true); return; }
      const user = await userService.getProfile();
      const cart = user.cart ?? [];
      const idx = cart.findIndex((c) => c.productId === product.id);
      if (idx > -1) cart[idx].quantity += 1; else cart.push({ productId: product.id, title: product.title, price: product.price, image: product.images?.[0] || "", quantity: 1 });
      await userService.updateUserData({ cart });
      setMiniCartOpen(true);
      toast({ title: "Added", description: "Item added to cart" });
    } catch {
      toast({ title: "Error", description: "Could not add to cart", variant: "destructive" });
    }
  };

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
      <Header />
      <Container maxWidth="lg" sx={{ py: 6 }}>
        <Typography variant="h4" fontWeight={700} mb={3}>My Watchlist</Typography>

        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
          <Tab label={`Active (${items.length})`} />
          <Tab label="Ended (0)" />
        </Tabs>

        {tab === 0 && (
          loading ? (
            <Grid container spacing={3}>{Array.from({ length: 3 }).map((_, i) => (<Grid item xs={12} md={6} key={i}><Skeleton variant="rectangular" height={140} sx={{ borderRadius: 2 }} /></Grid>))}</Grid>
          ) : items.length ? (
            <Grid container spacing={3}> {items.map((item) => (
              <Grid item xs={12} md={6} key={item.productId}>
                <Card sx={{ display: "flex", gap: 2, p: 2 }}>
                  <CardMedia component="img" image={item.images?.[0] || "https://via.placeholder.com/120"} alt={item.title} sx={{ width: 120, height: 120, borderRadius: 1 }} />
                  <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
                    <CardContent sx={{ flex: 1, p: 0 }}>
                      <Typography component={RouterLink} to={`/product?id=${item.productId}`} variant="subtitle1" fontWeight={600} sx={{ textDecoration: "none", color: "text.primary", '&:hover': { textDecoration: "underline" } }}>{item.title}</Typography>
                      <Box sx={{ mt: 1, display: "flex", gap: 1, flexWrap: "wrap" }}>
                        <Chip size="small" color="primary" label={`$${item.price?.toFixed(2) || "0.00"}`} />
                        {item.category && <Chip size="small" variant="outlined" label={item.category} />}
                      </Box>
                    </CardContent>
                    <CardActions sx={{ justifyContent: "flex-end", px: 0, pb: 0 }}>
                      <IconButton color="error" onClick={() => remove(item.productId)}><DeleteIcon /></IconButton>
                      <Button size="small" variant="contained" startIcon={<ShoppingCartIcon />} onClick={() => addToCart(item)}>Add to Cart</Button>
                    </CardActions>
                  </Box>
                </Card>
              </Grid>
            ))} </Grid>
          ) : (
            <Box sx={{ textAlign: "center", py: 8 }}>
              <FavoriteIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
              <Typography variant="h6" mb={1}>Your watchlist is empty</Typography>
              <Typography variant="body2" color="text.secondary">Items you're watching will appear here</Typography>
              <Button component={RouterLink} to={createPageUrl("Home")} variant="contained" sx={{ mt: 2 }}>Browse Products</Button>
            </Box>
          )
        )}

        {tab === 1 && (
          <Box sx={{ py: 8, textAlign: "center" }}>
            <Typography variant="h6" mb={1}>No ended items</Typography>
            <Typography variant="body2" color="text.secondary">When items you're watching end, they'll appear here</Typography>
          </Box>
        )}
      </Container>

      {authOpen && <AuthDialog open={authOpen} onClose={() => setAuthOpen(false)} />}
      {miniCartOpen && <MiniCart onClose={() => setMiniCartOpen(false)} />}

      <Snackbar open={snackbar.open} autoHideDuration={6000} onClose={() => setSnackbar((s) => ({ ...s, open: false }))} anchorOrigin={{ vertical: "bottom", horizontal: "right" }}>
        <Alert onClose={() => setSnackbar((s) => ({ ...s, open: false }))} severity={snackbar.severity} variant="filled">{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
}
