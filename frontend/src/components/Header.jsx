import React, { useState, useEffect } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { createPageUrl } from "../utils";
import userService from "../services/userService";

import {
  AppBar,
  Toolbar,
  Container,
  Box,
  Typography,
  IconButton,
  Badge,
  Button,
  TextField,
  InputAdornment,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
} from "@mui/material";

import SearchIcon from "@mui/icons-material/Search";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import FavoriteIcon from "@mui/icons-material/Favorite";
import NotificationsIcon from "@mui/icons-material/Notifications";
import PersonIcon from "@mui/icons-material/Person";
import MenuIcon from "@mui/icons-material/Menu";

import AuthDialog from "./AuthDialog";

export default function Header() {
  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [authOpen, setAuthOpen] = useState(false);
  const [cart, setCart] = useState([]);

  useEffect(() => {
    (async () => {
      try {
        if (userService.isAuthenticated()) {
          const user = await userService.getProfile();
          setCart(user.cart ?? []);
        }
      } catch {
        /* ignore */
      }
    })();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    const query = new FormData(e.currentTarget).get("search")?.toString().trim();
    if (query) navigate(createPageUrl("SearchResults") + `?q=${encodeURIComponent(query)}`);
  };

  const navItems = [
    {
      label: "Cart",
      icon: <ShoppingCartIcon />,
      action: () => navigate(createPageUrl("Cart")),
      badge: cart.reduce((sum, i) => sum + i.quantity, 0),
    },
    {
      label: "Watchlist",
      icon: <FavoriteIcon />,
      action: () => navigate(createPageUrl("Watchlist")),
    },
    {
      label: "Notifications",
      icon: <NotificationsIcon />,
      action: () => navigate(createPageUrl("Home")), // placeholder
    },
  ];

  return (
    <>
      <AppBar
        position="sticky"
        color="default"
        elevation={0}
        sx={{
          backdropFilter: "blur(6px)",
          bgcolor: "rgba(255,255,255,0.8)",
          borderBottom: 1,
          borderColor: "divider",
        }}
      >
        <Container maxWidth="lg">
          <Toolbar disableGutters sx={{ py: 1 }}>
            {/* ——— Logo ——— */}
            <Box
              component={RouterLink}
              to={createPageUrl("Home")}
              sx={{ textDecoration: "none", flexShrink: 0, mr: 2 }}
            >
              <Typography
                variant="h6"
                fontWeight={800}
                sx={{
                  background: "linear-gradient(90deg,#2563eb 0%,#9333ea 100%)",
                  WebkitBackgroundClip: "text",
                  color: "transparent",
                }}
                noWrap
              >
                BGU‑Marketplace
              </Typography>
            </Box>

            {/* ——— Search (md+) ——— */}
            <Box
              component="form"
              onSubmit={handleSearch}
              sx={{ flexGrow: 1, mx: 3, display: { xs: "none", md: "flex" } }}
            >
              <TextField
                name="search"
                placeholder="Search for anything…"
                size="small"
                fullWidth
                variant="outlined"
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton type="submit" edge="end">
                        <SearchIcon fontSize="small" />
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
            </Box>

            {/* ——— Desktop actions ——— */}
            <Box sx={{ display: { xs: "none", md: "flex" }, alignItems: "center", gap: 2 }}>
              {navItems.map(({ label, icon, action, badge }) => (
                <IconButton key={label} onClick={action} size="large">
                  {badge !== undefined ? (
                    <Badge badgeContent={badge} color="error">
                      {icon}
                    </Badge>
                  ) : (
                    icon
                  )}
                </IconButton>
              ))}

              <Button
                variant="contained"
                color="primary"
                startIcon={<PersonIcon />}
                onClick={() => setAuthOpen(true)}
                sx={{ borderRadius: 6, fontWeight: 700, textTransform: "none" }}
              >
                Sign in
              </Button>
            </Box>

            {/* ——— Mobile hamburger ——— */}
            <IconButton
              onClick={() => setDrawerOpen(true)}
              sx={{ display: { xs: "flex", md: "none" }, ml: "auto" }}
              size="large"
            >
              <MenuIcon />
            </IconButton>
          </Toolbar>

          {/* ——— Mobile search (xs–sm) ——— */}
          <Box component="form" onSubmit={handleSearch} sx={{ px: 0, py: 1, display: { xs: "block", md: "none" } }}>
            <TextField
              name="search"
              placeholder="Search for anything…"
              size="small"
              fullWidth
              variant="outlined"
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton type="submit" edge="end" size="small">
                      <SearchIcon fontSize="small" />
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
          </Box>
        </Container>
      </AppBar>

      {/* ——— Mobile drawer ——— */}
      <Drawer anchor="right" open={drawerOpen} onClose={() => setDrawerOpen(false)}>
        <Box sx={{ width: 260, pt: 2 }} role="presentation">
          <List>
            {navItems.map(({ label, icon, action, badge }) => (
              <ListItemButton key={label} onClick={() => { action(); setDrawerOpen(false); }}>
                <ListItemIcon>
                  {badge !== undefined ? (
                    <Badge badgeContent={badge} color="error">
                      {icon}
                    </Badge>
                  ) : (
                    icon
                  )}
                </ListItemIcon>
                <ListItemText primary={label} />
              </ListItemButton>
            ))}
          </List>
          <Divider />
          <List>
            <ListItemButton onClick={() => { setAuthOpen(true); setDrawerOpen(false); }}>
              <ListItemIcon>
                <PersonIcon />
              </ListItemIcon>
              <ListItemText primary="Sign in" />
            </ListItemButton>
          </List>
        </Box>
      </Drawer>

      {/* ——— Auth dialog ——— */}
      <AuthDialog open={authOpen} onOpenChange={setAuthOpen} />
    </>
  );
}
