import React, { useState, useEffect } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { createPageUrl } from "../../utils";
import { useAuth } from "../../contexts/AuthContext";
import userService from "../../services/userService";
import "./Header.css";

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
import LogoutIcon from "@mui/icons-material/Logout";
import MenuIcon from "@mui/icons-material/Menu";

import AuthDialog from "../AuthDialog/AuthDialog";

export default function Header() {
	const navigate = useNavigate();
	const { currentUser, isAuthenticated, logout, cart } = useAuth();
	const [drawerOpen, setDrawerOpen] = useState(false);
	const [authOpen, setAuthOpen] = useState(false);

	const handleSearch = (e) => {
		e.preventDefault();
		const query = new FormData(e.currentTarget).get("search")?.toString().trim();
		if (query) navigate(createPageUrl("SearchResults") + `?q=${encodeURIComponent(query)}`);
	};

	const handleLogout = async () => {
		try {
			await logout();
			navigate('/'); // Redirect to home after logout
		} catch (error) {
			console.error('Logout failed:', error);
		}
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
			action: () => navigate(createPageUrl("Dashboard")), // placeholder
		},
	];

	return (
		<>
			<AppBar
				position="sticky"
				color="default"
				elevation={0}
				className="header-appbar"
			>
				<Container maxWidth="lg">
					<Toolbar disableGutters className="header-toolbar">
						{/* ——— Logo ——— */}
						<Box
							component={RouterLink}
							to={createPageUrl("Dashboard")}
							className="header-logo-link"
						>
							<Typography
								variant="h6"
								fontWeight={800}
								className="header-logo-text"
								noWrap
							>
								BGU‑Marketplace
							</Typography>
						</Box>

						{/* ——— Search (md+) ——— */}
						<Box
							component="form"
							onSubmit={handleSearch}
							className="header-search-desktop header-hide-mobile"
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
						<Box className="header-desktop-actions header-hide-mobile">
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

							{/* Authentication Button */}
							{isAuthenticated && currentUser ? (
								<Box className="header-user-info">
									<Typography variant="body2" className="header-user-greeting">
										Hello, {currentUser.userName || 'User'}
									</Typography>
									<Button
										variant="outlined"
										color="primary"
										startIcon={<LogoutIcon />}
										onClick={handleLogout}
										className="header-auth-button"
									>
										Logout
									</Button>
								</Box>
							) : (
								<Button
									variant="contained"
									color="primary"
									startIcon={<PersonIcon />}
									onClick={() => setAuthOpen(true)}
									className="header-auth-button"
								>
									Sign in
								</Button>
							)}
						</Box>

						{/* ——— Mobile hamburger ——— */}
						<IconButton
							onClick={() => setDrawerOpen(true)}
							className="header-mobile-menu header-hide-desktop"
							size="large"
						>
							<MenuIcon />
						</IconButton>
					</Toolbar>

					{/* ——— Mobile search (xs–sm) ——— */}
					<Box
						component="form"
						onSubmit={handleSearch}
						className="header-search-mobile header-show-mobile"
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
				<Box className="header-drawer-content" role="presentation">
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
						{isAuthenticated && currentUser ? (
							<>
								<ListItemButton disabled>
									<ListItemIcon>
										<PersonIcon />
									</ListItemIcon>
									<ListItemText primary={`Hello, ${currentUser.userName || 'User'}`} />
								</ListItemButton>
								<ListItemButton onClick={() => { handleLogout(); setDrawerOpen(false); }}>
									<ListItemIcon>
										<LogoutIcon />
									</ListItemIcon>
									<ListItemText primary="Logout" />
								</ListItemButton>
							</>
						) : (
							<ListItemButton onClick={() => { setAuthOpen(true); setDrawerOpen(false); }}>
								<ListItemIcon>
									<PersonIcon />
								</ListItemIcon>
								<ListItemText primary="Sign in" />
							</ListItemButton>
						)}
					</List>
				</Box>
			</Drawer>

			{/* ——— Auth dialog ——— */}
			<AuthDialog open={authOpen} onOpenChange={setAuthOpen} />
		</>
	);
} 