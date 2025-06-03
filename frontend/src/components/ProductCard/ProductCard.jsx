import React, { useState, useEffect, useCallback } from "react";
import { createPageUrl } from "../../utils";
import { useNavigate } from "react-router-dom";
import userService from "../../services/userService";
import "./ProductCard.css";

// Material-UI imports
import { Card, CardContent, CardMedia, Typography, Chip, IconButton, Box, Snackbar, Alert } from "@mui/material";
import FavoriteIcon from "@mui/icons-material/Favorite";
import AccessTimeIcon from "@mui/icons-material/AccessTime";

export default function ProductCard({ product }) {
	const [isInWatchlist, setIsInWatchlist] = useState(false);
	const navigate = useNavigate();
	const [openToast, setOpenToast] = useState(false);
	const [toastMessage, setToastMessage] = useState({ severity: "success", title: "", description: "" });

	const checkWatchlist = useCallback(async () => {
		try {
			if (userService.isAuthenticated()) {
				const user = await userService.getProfile();
				const watchlist = user.watchlist || [];
				setIsInWatchlist(watchlist.includes(product.id));
			}
		} catch (error) {
			// User not logged in or no watchlist
		}
	}, [product.id]);

	useEffect(() => {
		checkWatchlist();
	}, [checkWatchlist]);

	const timeLeft = product.bid_end_date
		? new Date(product.bid_end_date) - new Date()
		: null;

	const formatTimeLeft = () => {
		if (!timeLeft || timeLeft <= 0) return "Ended";

		const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));
		const hours = Math.floor(
			(timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
		);

		if (days > 0) return `${days}d ${hours}h left`;

		const minutes = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));
		if (hours > 0) return `${hours}h ${minutes}m left`;

		return `${minutes}m left`;
	};

	const handleProductClick = (e) => {
		e.preventDefault();
		navigate(`/product/${product.id}`);
	};

	const toggleWatchlist = async (e) => {
		e.preventDefault();
		e.stopPropagation();

		try {
			if (!userService.isAuthenticated()) {
				setToastMessage({
					severity: "error",
					title: "Sign in required",
					description: "Please sign in to manage your watchlist",
				});
				setOpenToast(true);
				return;
			}

			if (isInWatchlist) {
				// Remove from watchlist
				await userService.removeFromWatchlist(product.id);
				setToastMessage({
					severity: "success",
					title: "Removed from watchlist",
					description: "Item has been removed from your watchlist",
				});
				setOpenToast(true);
			} else {
				// Add to watchlist
				await userService.addToWatchlist(product.id);
				setToastMessage({
					severity: "success",
					title: "Added to watchlist",
					description: "Item has been added to your watchlist",
				});
				setOpenToast(true);
			}

			setIsInWatchlist(!isInWatchlist);
		} catch (error) {
			setToastMessage({
				severity: "error",
				title: "Error",
				description: "There was an error updating your watchlist",
			});
			setOpenToast(true);
		}
	};

	const handleCloseToast = () => {
		setOpenToast(false);
	};

	return (
		<Box className="product-card-container">
			<Card className="product-card">
				<Box
					className="product-card-media-container"
					onClick={handleProductClick}
					component="a"
					href="#"
				>
					<CardMedia
						component="img"
						height="192"
						image={product.images[0]}
						alt={product.title}
						className="product-card-media"
					/>
					<IconButton
						size="small"
						onClick={toggleWatchlist}
						className={`product-card-favorite-btn ${isInWatchlist ? 'in-watchlist' : ''}`}
					>
						<FavoriteIcon
							fontSize="small"
							sx={{ fill: isInWatchlist ? 'currentColor' : 'none' }}
						/>
					</IconButton>

					{product.featured && (
						<Chip
							label="Featured"
							color="primary"
							size="small"
							className="product-card-featured-chip"
						/>
					)}
				</Box>

				<CardContent
					className="product-card-content"
					onClick={handleProductClick}
					component="a"
					href="#"
				>
					<Typography variant="h6" component="h3" noWrap>
						{product.title}
					</Typography>

					<Box className="product-card-price-container">
						{product.hasDiscount ? (
							<Box sx={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
								<Typography variant="body2" color="text.secondary" sx={{ textDecoration: "line-through" }}>
									${product.price?.toFixed(2)}
								</Typography>
								<Typography variant="h6" component="span" fontWeight="bold" color="primary">
									${product.discountedPrice?.toFixed(2)}
								</Typography>
								<Typography variant="caption" color="success.main">
									Save ${(product.price - product.discountedPrice)?.toFixed(2)}
								</Typography>
							</Box>
						) : (
							<Typography variant="h6" component="span" fontWeight="bold">
								${product.price?.toFixed(2)}
							</Typography>
						)}

						{product.shipping_cost === 0 ? (
							<Typography variant="body2" color="success.main">
								Free shipping
							</Typography>
						) : (
							<Typography variant="body2" color="text.secondary">
								+${product.shipping_cost?.toFixed(2)} shipping
							</Typography>
						)}
					</Box>

					<Box className="product-card-actions">
						<Chip
							label={product.category}
							variant="outlined"
							size="small"
							className="product-card-category-chip"
							onClick={(e) => {
								e.preventDefault();
								e.stopPropagation();
								navigate(
									createPageUrl("SearchResults") +
									`?category=${product.category}`
								);
							}}
						/>

						{timeLeft && (
							<Box className="product-card-time-left">
								<AccessTimeIcon className="product-card-time-icon" />
								<Typography variant="body2">
									{formatTimeLeft()}
								</Typography>
							</Box>
						)}
					</Box>
				</CardContent>
			</Card>

			<Snackbar open={openToast} autoHideDuration={6000} onClose={handleCloseToast}>
				<Alert onClose={handleCloseToast} severity={toastMessage.severity} sx={{ width: '100%' }}>
					<Typography variant="subtitle2">{toastMessage.title}</Typography>
					<Typography variant="body2">{toastMessage.description}</Typography>
				</Alert>
			</Snackbar>
		</Box>
	);
} 