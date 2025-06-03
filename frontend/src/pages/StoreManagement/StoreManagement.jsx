import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { useStorePermissions, PERMISSIONS } from "../../hooks/useStorePermissions";
import { storeService } from "../../services/storeService";
import { productService } from "../../services/productService";
import purchaseService from "../../services/purchaseService";
import {
	Box,
	Container,
	Typography,
	Card,
	CardContent,
	CardActions,
	Grid,
	Button,
	Chip,
	Skeleton,
	Alert,
	Paper,
	Avatar,
	Snackbar,
	Tabs,
	Tab,
	Badge,
	Tooltip
} from "@mui/material";
import {
	ArrowBack as ArrowBackIcon,
	Edit as EditIcon,
	Add as AddIcon,
	Visibility as VisibilityIcon,
	Store as StoreIcon,
	ShoppingCart as ShoppingCartIcon,
	Gavel as GavelIcon,
	Schedule as ScheduleIcon,
	Casino as CasinoIcon,
	LocalOffer as LocalOfferIcon,
	Timer as TimerIcon,
	TrendingUp as TrendingUpIcon,
	Security as SecurityIcon,
	People as PeopleIcon
} from "@mui/icons-material";
import Header from "../../components/Header/Header";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import PolicyManagement from "../../components/PolicyManagement/PolicyManagement";
import ProductEditDialog from "../../components/ProductEditDialog/ProductEditDialog";
import AddProductDialog from "../../components/AddProductDialog/AddProductDialog";
import BidManagementDialog from "../../components/BidManagementDialog/BidManagementDialog";
import UserManagement from "../../components/UserManagement/UserManagement";
import './StoreManagement.css';

export default function StoreManagement() {
	const { storeId } = useParams();
	const navigate = useNavigate();
	const { currentUser } = useAuth();
	const storePermissions = useStorePermissions(storeId);
	const [store, setStore] = useState(null);
	const [products, setProducts] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState("");
	const [authOpen, setAuthOpen] = useState(false);
	const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });
	const [editProductDialog, setEditProductDialog] = useState(false);
	const [selectedProduct, setSelectedProduct] = useState(null);
	const [activeTab, setActiveTab] = useState(0);
	const [addProductDialog, setAddProductDialog] = useState(false);
	const [addProductPurchaseType, setAddProductPurchaseType] = useState('REGULAR');
	const [bidManagementDialog, setBidManagementDialog] = useState(false);
	const [selectedBidProduct, setSelectedBidProduct] = useState(null);

	// New state for bid tracking
	const [bidCounts, setBidCounts] = useState({});
	const [pendingBidCounts, setPendingBidCounts] = useState({});
	const [lastBidCheckTime, setLastBidCheckTime] = useState(Date.now());

	// Separate products by purchase type from actual server data
	const regularProducts = products.filter(p => !p.purchaseType || p.purchaseType === 'REGULAR');
	const bidProducts = products.filter(p => p.purchaseType === 'BID');
	const auctionProducts = products.filter(p => p.purchaseType === 'AUCTION');
	const raffleProducts = products.filter(p => p.purchaseType === 'RAFFLE');

	const toast = useCallback(({ title, description, variant }) => {
		setSnackbar({
			open: true,
			message: description || title,
			severity: variant === "destructive" ? "error" : variant === "info" ? "info" : "success"
		});
	}, []);

	// Function to load bid counts for all bid products
	const loadBidCounts = useCallback(async () => {
		if (!store?.id || !storePermissions.canApproveBids) return;

		try {
			const counts = {};
			const pendingCounts = {};

			await Promise.all(
				bidProducts.map(async (product) => {
					try {
						const bids = await purchaseService.getProductBids(store.id, product.id);
						counts[product.id] = bids?.length || 0;
						pendingCounts[product.id] = bids?.filter(bid => !bid.isApproved && !bid.isRejected)?.length || 0;
					} catch (error) {
						console.warn(`Could not fetch bids for product ${product.id}:`, error);
						counts[product.id] = 0;
						pendingCounts[product.id] = 0;
					}
				})
			);

			setBidCounts(counts);

			// Check if there are new pending bids since last check
			const newPendingBids = Object.entries(pendingCounts).some(([productId, count]) => {
				return count > (pendingBidCounts[productId] || 0);
			});

			if (newPendingBids && Date.now() - lastBidCheckTime > 5000) { // Only show notification if it's been more than 5 seconds
				const totalNewBids = Object.values(pendingCounts).reduce((sum, count) => sum + count, 0) -
					Object.values(pendingBidCounts).reduce((sum, count) => sum + count, 0);

				if (totalNewBids > 0) {
					toast({
						title: "New Bids Received",
						description: `You have ${totalNewBids} new bid${totalNewBids > 1 ? 's' : ''} to review`,
						variant: "info"
					});
				}
			}

			setPendingBidCounts(pendingCounts);
			setLastBidCheckTime(Date.now());
		} catch (error) {
			console.error("Error loading bid counts:", error);
		}
	}, [store?.id, bidProducts, storePermissions.canApproveBids, pendingBidCounts, lastBidCheckTime, toast]);

	// Polling effect for bid updates
	useEffect(() => {
		if (!storePermissions.canApproveBids || bidProducts.length === 0) return;

		// Initial load
		loadBidCounts();

		// Set up polling every 30 seconds
		const interval = setInterval(loadBidCounts, 30000);

		return () => clearInterval(interval);
	}, [loadBidCounts, storePermissions.canApproveBids, bidProducts.length]);

	const loadProductsWithDiscounts = useCallback(async (storeIdToUse = null) => {
		try {
			const targetStoreId = storeIdToUse || store?.id;
			if (!targetStoreId) return;

			// Use the new service function that includes discounted prices
			const storeProducts = await productService.getStoreProductsWithDiscounts(targetStoreId);

			// Set products directly from server
			setProducts(storeProducts);
		} catch (error) {
			console.error("Error loading products with discounts:", error);
			// Fallback to regular products if discount API fails
			try {
				const targetStoreId = storeIdToUse || store?.id;
				if (!targetStoreId) return;

				const { products: allProducts } = await storeService.getAllStoresAndProducts();
				const storeProducts = allProducts.filter(product =>
					String(product.storeId) === String(targetStoreId) ||
					product.storeId === targetStoreId ||
					product.storeId === parseInt(targetStoreId)
				);

				setProducts(storeProducts);
			} catch (fallbackError) {
				console.error("Error loading products (fallback):", fallbackError);
			}
		}
	}, [store?.id]);

	const handlePolicyUpdate = useCallback(() => {
		// Refresh product prices when policies change
		loadProductsWithDiscounts();
	}, [loadProductsWithDiscounts]);

	const loadStoreData = useCallback(async () => {
		if (!currentUser || !storeId) return;

		setLoading(true);
		try {
			// Get all stores and find the one we're managing
			const { stores } = await storeService.getAllStoresAndProducts();

			// Find store by ID (handle both string and number comparison)
			const currentStore = stores.find(s =>
				String(s.id) === String(storeId) ||
				s.id === storeId ||
				s.id === parseInt(storeId)
			);

			if (!currentStore) {
				setError("Store not found");
				return;
			}

			// Check if user has permission to manage this store
			const [isFounder, isOwner, isManager] = await Promise.all([
				storeService.isFounder(currentStore.id, currentUser.userName),
				storeService.isOwner(currentStore.id, currentUser.userName),
				storeService.isManager(currentStore.id, currentUser.userName)
			]);

			if (!isFounder && !isOwner && !isManager) {
				setError("You don't have permission to manage this store");
				return;
			}

			// Set store data
			setStore({
				...currentStore,
				role: isFounder ? 'Founder' : isOwner ? 'Owner' : 'Manager'
			});

			// Load products with discounted prices
			await loadProductsWithDiscounts(currentStore.id);

		} catch (error) {
			console.error("Error loading store data:", error);
			setError("Failed to load store data: " + error.message);
		} finally {
			setLoading(false);
		}
	}, [currentUser, storeId, loadProductsWithDiscounts]);

	useEffect(() => {
		if (!currentUser) {
			setAuthOpen(true);
			return;
		}
		loadStoreData();
	}, [currentUser, loadStoreData]);

	const handleAddProduct = () => {
		// General add product for main button - defaults to regular
		setAddProductPurchaseType('REGULAR');
		setAddProductDialog(true);
	};

	const handleAddProductByType = (purchaseType) => {
		setAddProductPurchaseType(purchaseType);
		setAddProductDialog(true);
	};

	const handleProductDialogClose = () => {
		setAddProductDialog(false);
		// Refresh products after adding
		loadProductsWithDiscounts();
	};

	const handleEditProduct = (productId) => {
		const product = products.find(p => p.id === productId);
		if (product) {
			setSelectedProduct(product);
			setEditProductDialog(true);
		}
	};

	const handleViewProduct = (productId) => {
		navigate(`/product/${productId}`);
	};

	const handleManageBids = (productId) => {
		const product = products.find(p => p.id === productId);
		if (product && product.purchaseType === 'BID') {
			setSelectedBidProduct(product);
			setBidManagementDialog(true);
		}
	};

	const handleBidManagementClose = () => {
		setBidManagementDialog(false);
		setSelectedBidProduct(null);
		// Refresh bid counts after managing bids
		if (storePermissions.canApproveBids) {
			loadBidCounts();
		}
	};

	const handleProductEditClose = () => {
		setEditProductDialog(false);
		setSelectedProduct(null);
	};

	const handleProductUpdate = (updateInfo) => {
		toast(updateInfo);
		// Refresh products after update
		loadProductsWithDiscounts();
	};

	const getPurchaseTypeInfo = (purchaseType) => {
		switch (purchaseType) {
			case 'BID':
				return {
					color: 'warning',
					icon: <GavelIcon />,
					label: 'Bid Product',
					description: 'Customers can submit bids for this product'
				};
			case 'AUCTION':
				return {
					color: 'info',
					icon: <ScheduleIcon />,
					label: 'Auction Product',
					description: 'Time-limited auction with highest bidder wins'
				};
			case 'RAFFLE':
				return {
					color: 'secondary',
					icon: <CasinoIcon />,
					label: 'Raffle Product',
					description: 'Random winner selection from participants'
				};
			default:
				return {
					color: 'primary',
					icon: <ShoppingCartIcon />,
					label: 'Regular Product',
					description: 'Standard purchase with fixed price'
				};
		}
	};

	const renderProductCard = (product) => {
		const purchaseInfo = getPurchaseTypeInfo(product.purchaseType);
		const totalBids = bidCounts[product.id] || 0;
		const pendingBids = pendingBidCounts[product.id] || 0;

		return (
			<Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
				<Card sx={{ height: "100%", display: "flex", flexDirection: "column", position: "relative" }}>
					{/* Purchase Type Badge */}
					<Box sx={{ position: "absolute", top: 8, right: 8, zIndex: 1 }}>
						<Tooltip title={purchaseInfo.description}>
							<Chip
								icon={purchaseInfo.icon}
								label={purchaseInfo.label}
								color={purchaseInfo.color}
								size="small"
								variant="filled"
								sx={{ fontWeight: 'bold' }}
							/>
						</Tooltip>
					</Box>

					{/* Bid Count Badge for BID products */}
					{product.purchaseType === 'BID' && storePermissions.canApproveBids && (
						<Box sx={{ position: "absolute", top: 8, left: 8, zIndex: 1 }}>
							{pendingBids > 0 ? (
								<Tooltip title={`${pendingBids} pending bid${pendingBids > 1 ? 's' : ''}, ${totalBids} total`}>
									<Badge badgeContent={pendingBids} color="warning" max={99}>
										<Chip
											icon={<GavelIcon />}
											label={`${totalBids} bid${totalBids !== 1 ? 's' : ''}`}
											color="warning"
											size="small"
											variant="filled"
											sx={{
												fontWeight: 'bold',
												animation: pendingBids > 0 ? 'pulse 2s infinite' : 'none',
												'@keyframes pulse': {
													'0%': { opacity: 1 },
													'50%': { opacity: 0.7 },
													'100%': { opacity: 1 }
												}
											}}
										/>
									</Badge>
								</Tooltip>
							) : totalBids > 0 ? (
								<Tooltip title={`${totalBids} total bid${totalBids > 1 ? 's' : ''}`}>
									<Chip
										icon={<GavelIcon />}
										label={`${totalBids} bid${totalBids !== 1 ? 's' : ''}`}
										color="success"
										size="small"
										variant="outlined"
										sx={{ fontWeight: 'bold' }}
									/>
								</Tooltip>
							) : null}
						</Box>
					)}

					<Box
						component="img"
						src={product.images?.[0] || "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5vIEltYWdlPC90ZXh0Pjwvc3ZnPg=="}
						alt={product.title}
						sx={{
							width: "100%",
							height: 200,
							objectFit: "cover"
						}}
						onError={(e) => {
							if (!e.target.src.includes('data:image/svg+xml')) {
								e.target.src = "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5vIEltYWdlPC90ZXh0Pjwvc3ZnPg==";
							}
						}}
					/>
					<CardContent sx={{ flex: 1 }}>
						<Typography variant="h6" component="h3" fontWeight="bold" gutterBottom>
							{product.title}
						</Typography>
						<Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
							{product.description || "No description available"}
						</Typography>

						{/* Price Display */}
						<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
							{product.hasDiscount ? (
								<Box sx={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
									<Typography variant="body2" color="text.secondary" sx={{ textDecoration: "line-through" }}>
										${product.price?.toFixed(2)}
									</Typography>
									<Typography variant="h6" color="primary" fontWeight="bold">
										${product.discountedPrice?.toFixed(2)}
									</Typography>
									<Typography variant="caption" color="success.main">
										Save ${(product.price - product.discountedPrice)?.toFixed(2)}
									</Typography>
								</Box>
							) : (
								<Box>
									<Typography variant="h6" color="primary" fontWeight="bold">
										${product.price?.toFixed(2)}
									</Typography>
									{product.purchaseType === 'BID' && (
										<Typography variant="caption" color="text.secondary">
											Starting price
										</Typography>
									)}
								</Box>
							)}
							<Chip
								size="small"
								label={product.status === 'active' ? 'Active' : 'Inactive'}
								color={product.status === 'active' ? 'success' : 'default'}
							/>
						</Box>

						{/* Product Details */}
						<Box sx={{ display: "flex", gap: 1, mb: 1, flexWrap: "wrap" }}>
							{product.category && (
								<Chip size="small" label={product.category} variant="outlined" />
							)}
							<Chip size="small" label={`Qty: ${product.quantity || 0}`} variant="outlined" />
						</Box>

						{/* Purchase Type Specific Info */}
						{product.purchaseType === 'AUCTION' && (
							<Box sx={{ mt: 1, p: 1, bgcolor: 'info.light', borderRadius: 1 }}>
								<Typography variant="caption" color="info.dark" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
									<TimerIcon fontSize="small" />
									Auction Item - Time Limited
								</Typography>
							</Box>
						)}
						{product.purchaseType === 'BID' && (
							<Box sx={{ mt: 1, p: 1, bgcolor: 'warning.light', borderRadius: 1 }}>
								<Typography variant="caption" color="warning.dark" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
									<TrendingUpIcon fontSize="small" />
									Negotiable Price - Accepts Bids
								</Typography>
							</Box>
						)}
						{product.purchaseType === 'RAFFLE' && (
							<Box sx={{ mt: 1, p: 1, bgcolor: 'secondary.light', borderRadius: 1 }}>
								<Typography variant="caption" color="secondary.dark" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
									<LocalOfferIcon fontSize="small" />
									Raffle Entry - Random Selection
								</Typography>
							</Box>
						)}
					</CardContent>
					<CardActions sx={{ justifyContent: "flex-end", p: 2 }}>
						<Button
							size="small"
							startIcon={<VisibilityIcon />}
							onClick={() => handleViewProduct(product.id)}
						>
							View
						</Button>
						{product.purchaseType === 'BID' && storePermissions.canApproveBids && (
							<Button
								size="small"
								startIcon={<GavelIcon />}
								onClick={() => handleManageBids(product.id)}
								color="warning"
								variant={pendingBids > 0 ? "contained" : "outlined"}
								sx={{
									animation: pendingBids > 0 ? 'glow 2s infinite' : 'none',
									'@keyframes glow': {
										'0%': { boxShadow: '0 0 0 0 rgba(255, 152, 0, 0.4)' },
										'50%': { boxShadow: '0 0 0 8px rgba(255, 152, 0, 0)' },
										'100%': { boxShadow: '0 0 0 0 rgba(255, 152, 0, 0)' }
									}
								}}
							>
								{pendingBids > 0 ? `Review ${pendingBids} Bid${pendingBids > 1 ? 's' : ''}` : 'Manage Bids'}
							</Button>
						)}
						{storePermissions.canEditProducts && (
							<Button
								size="small"
								startIcon={<EditIcon />}
								onClick={() => handleEditProduct(product.id)}
							>
								Edit
							</Button>
						)}
						{!storePermissions.canEditProducts && !storePermissions.canApproveBids && (
							<Typography variant="caption" color="text.disabled" sx={{ ml: 1 }}>
								View only
							</Typography>
						)}
					</CardActions>
				</Card>
			</Grid>
		);
	};

	const renderProductSection = (products, title, icon, emptyMessage, purchaseType) => {
		// Calculate pending bids for this section
		const sectionPendingBids = purchaseType === 'BID' && storePermissions.canApproveBids ?
			Object.values(pendingBidCounts).reduce((sum, count) => sum + count, 0) : 0;

		return (
			<Box sx={{ mb: 4 }}>
				<Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", mb: 3 }}>
					<Box sx={{ display: "flex", alignItems: "center" }}>
						<Typography variant="h6" sx={{ display: "flex", alignItems: "center" }}>
							{icon}
							{title} ({products.length})
						</Typography>

						{/* Special indicator for bid products with pending bids */}
						{purchaseType === 'BID' && sectionPendingBids > 0 && (
							<Box sx={{ ml: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
								<Chip
									size="small"
									label={`${sectionPendingBids} NEEDS REVIEW`}
									color="error"
									variant="filled"
									icon={<GavelIcon />}
									sx={{
										fontWeight: 'bold',
										animation: 'pulse 2s infinite',
										'@keyframes pulse': {
											'0%': { opacity: 1, transform: 'scale(1)' },
											'50%': { opacity: 0.8, transform: 'scale(1.05)' },
											'100%': { opacity: 1, transform: 'scale(1)' }
										}
									}}
								/>
								<Typography variant="body2" color="error.main" sx={{ fontWeight: 'bold' }}>
									⚠️ Action Required
								</Typography>
							</Box>
						)}
					</Box>

					{/* Purchase Type Specific Stats and Add Button */}
					<Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
						{products.length > 0 && (
							<>
								<Chip
									size="small"
									label={`Active: ${products.filter(p => p.status === 'active').length}`}
									color="success"
									variant="outlined"
								/>
								<Chip
									size="small"
									label={`Total Value: $${products.reduce((sum, p) => sum + (p.hasDiscount ? p.discountedPrice : p.price), 0).toFixed(2)}`}
									color="primary"
									variant="outlined"
								/>
							</>
						)}

						{/* Add Product Button for this specific type */}
						{storePermissions.canEditProducts && (
							<Button
								variant="contained"
								size="small"
								startIcon={<AddIcon />}
								onClick={() => handleAddProductByType(purchaseType)}
								sx={{ ml: 1 }}
							>
								Add {purchaseType === 'REGULAR' ? '' : purchaseType} Product
							</Button>
						)}
					</Box>
				</Box>

				{products.length > 0 ? (
					<Grid container spacing={3}>
						{products.map(product => renderProductCard(product))}
					</Grid>
				) : (
					<Box sx={{ textAlign: "center", py: 6, bgcolor: "grey.50", borderRadius: 2 }}>
						<Box sx={{ mb: 2 }}>
							{React.cloneElement(icon, { sx: { fontSize: 48, color: "text.disabled" } })}
						</Box>
						<Typography variant="h6" color="text.secondary" gutterBottom>
							{emptyMessage}
						</Typography>
						{storePermissions.canEditProducts ? (
							<>
								<Typography variant="body2" color="text.disabled" sx={{ mb: 3 }}>
									{purchaseType === 'REGULAR' && "Add your first regular product to start selling"}
									{purchaseType === 'BID' && "Enable bid functionality for negotiable pricing"}
									{purchaseType === 'AUCTION' && "Create time-limited auctions for competitive bidding"}
									{purchaseType === 'RAFFLE' && "Set up raffle entries for exciting giveaways"}
								</Typography>

								{/* Add Product Button in empty state */}
								<Button
									variant="contained"
									startIcon={<AddIcon />}
									onClick={() => handleAddProductByType(purchaseType)}
								>
									Add {purchaseType === 'REGULAR' ? 'First' : purchaseType} Product
								</Button>
							</>
						) : (
							<Typography variant="body2" color="text.disabled" sx={{ mb: 3 }}>
								{storePermissions.hasAnyRole
									? "You need 'Edit Products' permission to add products to this store."
									: "You don't have access to manage products in this store."
								}
							</Typography>
						)}
					</Box>
				)}
			</Box>
		);
	};

	if (!currentUser) {
		return (
			<Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
				<Header />
				<Container maxWidth="lg" sx={{ py: 6 }}>
					<Box sx={{ textAlign: "center", py: 8 }}>
						<StoreIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
						<Typography variant="h6" mb={1}>Please sign in to manage stores</Typography>
						<Typography variant="body2" color="text.secondary">You need to be logged in to access store management</Typography>
					</Box>
				</Container>
				{authOpen && <AuthDialog open={authOpen} onClose={() => setAuthOpen(false)} />}
			</Box>
		);
	}

	if (loading) {
		return (
			<Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
				<Header />
				<Container maxWidth="lg" sx={{ py: 6 }}>
					<Box sx={{ display: "flex", alignItems: "center", mb: 4 }}>
						<Skeleton variant="circular" width={40} height={40} sx={{ mr: 2 }} />
						<Skeleton variant="text" width="200px" height={40} />
					</Box>
					<Skeleton variant="rectangular" height={200} sx={{ mb: 3, borderRadius: 2 }} />
					<Grid container spacing={3}>
						{[1, 2, 3, 4].map((i) => (
							<Grid item xs={12} sm={6} md={4} lg={3} key={i}>
								<Skeleton variant="rectangular" height={300} sx={{ borderRadius: 2 }} />
							</Grid>
						))}
					</Grid>
				</Container>
			</Box>
		);
	}

	if (error) {
		return (
			<Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
				<Header />
				<Container maxWidth="lg" sx={{ py: 6 }}>
					<Button
						startIcon={<ArrowBackIcon />}
						onClick={() => navigate("/profile")}
						sx={{ mb: 3 }}
					>
						Back to Profile
					</Button>
					<Alert severity="error" sx={{ mt: 4 }}>
						{error}
						<Button
							onClick={() => navigate("/profile")}
							sx={{ ml: 2 }}
							variant="outlined"
							size="small"
						>
							Go Back
						</Button>
					</Alert>
				</Container>
			</Box>
		);
	}

	return (
		<Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
			<Header />
			<Container maxWidth="lg" sx={{ py: 6 }}>
				{/* Back Button */}
				<Button
					startIcon={<ArrowBackIcon />}
					onClick={() => navigate("/profile")}
					sx={{ mb: 3 }}
				>
					Back to Profile
				</Button>

				{/* Store Header */}
				<Paper sx={{ p: 4, mb: 4, borderRadius: 2 }}>
					<Box sx={{ display: "flex", alignItems: "center", mb: 3 }}>
						<Avatar
							sx={{ width: 60, height: 60, mr: 3, bgcolor: "primary.main" }}
						>
							<StoreIcon />
						</Avatar>
						<Box sx={{ flex: 1 }}>
							<Typography variant="h4" fontWeight="bold">
								{store?.name}
							</Typography>
							<Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
								{store?.description || "No description available"}
							</Typography>
							<Box sx={{ display: "flex", gap: 1, alignItems: "center", flexWrap: "wrap" }}>
								<Chip
									size="small"
									label={store?.isActive ? "Active" : "Inactive"}
									color={store?.isActive ? "success" : "default"}
								/>
								<Chip
									size="small"
									label={`Your Role: ${storePermissions.role}`}
									color={storePermissions.role === 'FOUNDER' ? 'success' :
										storePermissions.role === 'OWNER' ? 'primary' :
											storePermissions.role === 'MANAGER' ? 'warning' : 'default'}
									variant="filled"
									icon={<SecurityIcon />}
								/>
								<Chip
									size="small"
									label={`${products.length} Total Products`}
									variant="outlined"
								/>
								<Chip
									size="small"
									label={`${regularProducts.length} Regular`}
									variant="outlined"
									color="primary"
								/>
								<Chip
									size="small"
									label={`${bidProducts.length} Bid`}
									variant="outlined"
									color="warning"
								/>
								{/* Bid Statistics for managers with bid approval permission */}
								{storePermissions.canApproveBids && bidProducts.length > 0 && (
									<>
										<Chip
											size="small"
											label={`${Object.values(bidCounts).reduce((sum, count) => sum + count, 0)} Total Bids`}
											variant="outlined"
											color="warning"
											icon={<GavelIcon />}
										/>
										{Object.values(pendingBidCounts).reduce((sum, count) => sum + count, 0) > 0 && (
											<Chip
												size="small"
												label={`${Object.values(pendingBidCounts).reduce((sum, count) => sum + count, 0)} Pending Review`}
												color="error"
												variant="filled"
												icon={<GavelIcon />}
												sx={{
													animation: 'pulse 2s infinite',
													'@keyframes pulse': {
														'0%': { opacity: 1 },
														'50%': { opacity: 0.8 },
														'100%': { opacity: 1 }
													}
												}}
											/>
										)}
									</>
								)}
								<Chip
									size="small"
									label={`${auctionProducts.length} Auction`}
									variant="outlined"
									color="info"
								/>
								<Chip
									size="small"
									label={`${raffleProducts.length} Raffle`}
									variant="outlined"
									color="secondary"
								/>
							</Box>
						</Box>
						<Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
							{storePermissions.canEditProducts && (
								<Button
									variant="contained"
									startIcon={<AddIcon />}
									onClick={handleAddProduct}
								>
									Add Product
								</Button>
							)}
							{/* Refresh Bids Button */}
							{storePermissions.canApproveBids && bidProducts.length > 0 && (
								<Button
									variant="outlined"
									size="small"
									startIcon={<GavelIcon />}
									onClick={loadBidCounts}
									color="warning"
									sx={{ minWidth: 'auto' }}
								>
									Refresh Bids
								</Button>
							)}
						</Box>
						{!storePermissions.hasAnyRole && (
							<Alert severity="info" sx={{ ml: 2, maxWidth: 300 }}>
								<Typography variant="body2">
									You don't have management access to this store.
								</Typography>
							</Alert>
						)}
					</Box>

					{/* Permission Details */}
					{storePermissions.hasAnyRole && (
						<Box sx={{ mt: 2, p: 2, bgcolor: "grey.50", borderRadius: 1 }}>
							<Typography variant="subtitle2" fontWeight="bold" gutterBottom>
								Your Permissions:
							</Typography>
							<Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
								<Chip
									size="small"
									label="View Store"
									color="default"
									variant="outlined"
								/>
								{storePermissions.canEditProducts && (
									<Chip
										size="small"
										label="Edit Products"
										color="primary"
										variant="filled"
									/>
								)}
								{storePermissions.canEditPolicies && (
									<Chip
										size="small"
										label="Edit Policies"
										color="secondary"
										variant="filled"
									/>
								)}
								{storePermissions.canApproveBids && (
									<Chip
										size="small"
										label="Approve Bids"
										color="warning"
										variant="filled"
									/>
								)}
								{storePermissions.canManageUsers && (
									<Chip
										size="small"
										label="Manage Users"
										color="success"
										variant="filled"
									/>
								)}
							</Box>
						</Box>
					)}
				</Paper>

				{/* Store Policies Section */}
				{storePermissions.canEditPolicies ? (
					<PolicyManagement
						store={store}
						currentUser={currentUser}
						onUpdate={toast}
						onPolicyUpdate={handlePolicyUpdate}
					/>
				) : storePermissions.hasAnyRole ? (
					<Paper sx={{ p: 3, mb: 4, borderRadius: 2 }}>
						<Box sx={{ textAlign: "center", py: 2 }}>
							<SecurityIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
							<Typography variant="h6" color="text.secondary" gutterBottom>
								Policy Management Access Required
							</Typography>
							<Typography variant="body2" color="text.disabled">
								You need the "Edit Policies" permission to manage store policies.
							</Typography>
							{storePermissions.role === 'MANAGER' && (
								<Typography variant="body2" color="text.disabled" sx={{ mt: 1 }}>
									Ask a store owner to grant you the EDIT_POLICIES permission.
								</Typography>
							)}
						</Box>
					</Paper>
				) : null}

				{/* Products Section with Tabs */}
				<Box>
					<Typography variant="h5" fontWeight="bold" mb={3}>
						Product Management by Purchase Type
					</Typography>

					<Tabs
						value={activeTab}
						onChange={(_, v) => setActiveTab(v)}
						sx={{ mb: 3 }}
						variant="scrollable"
						scrollButtons="auto"
					>
						<Tab
							icon={<Badge badgeContent={regularProducts.length} color="primary"><ShoppingCartIcon /></Badge>}
							label="Regular Products"
							iconPosition="start"
						/>
						<Tab
							icon={
								<Box sx={{ position: 'relative' }}>
									<Badge badgeContent={bidProducts.length} color="warning">
										<GavelIcon />
									</Badge>
									{/* Show pending bids indicator */}
									{storePermissions.canApproveBids && Object.values(pendingBidCounts).reduce((sum, count) => sum + count, 0) > 0 && (
										<Box
											sx={{
												position: 'absolute',
												top: -8,
												right: -8,
												width: 12,
												height: 12,
												bgcolor: 'error.main',
												borderRadius: '50%',
												animation: 'pulse 2s infinite',
												'@keyframes pulse': {
													'0%': { opacity: 1, transform: 'scale(1)' },
													'50%': { opacity: 0.7, transform: 'scale(1.2)' },
													'100%': { opacity: 1, transform: 'scale(1)' }
												}
											}}
										/>
									)}
								</Box>
							}
							label={
								<Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
									<span>Bid Products</span>
									{storePermissions.canApproveBids && Object.values(pendingBidCounts).reduce((sum, count) => sum + count, 0) > 0 && (
										<Chip
											size="small"
											label={`${Object.values(pendingBidCounts).reduce((sum, count) => sum + count, 0)} pending`}
											color="error"
											variant="filled"
											sx={{ height: 20, fontSize: '0.7rem' }}
										/>
									)}
								</Box>
							}
							iconPosition="start"
						/>
						<Tab
							icon={<Badge badgeContent={auctionProducts.length} color="info"><ScheduleIcon /></Badge>}
							label="Auction Products"
							iconPosition="start"
						/>
						<Tab
							icon={<Badge badgeContent={raffleProducts.length} color="secondary"><CasinoIcon /></Badge>}
							label="Raffle Products"
							iconPosition="start"
						/>
						{storePermissions.canManageUsers && (
							<Tab
								icon={<PeopleIcon />}
								label="User Management"
								iconPosition="start"
							/>
						)}
					</Tabs>

					{/* Regular Products Tab */}
					{activeTab === 0 && renderProductSection(
						regularProducts,
						"Regular Products",
						<ShoppingCartIcon sx={{ mr: 1, color: "primary.main" }} />,
						"No regular products found",
						"REGULAR"
					)}

					{/* Bid Products Tab */}
					{activeTab === 1 && renderProductSection(
						bidProducts,
						"Bid Products",
						<GavelIcon sx={{ mr: 1, color: "warning.main" }} />,
						"No bid products found",
						"BID"
					)}

					{/* Auction Products Tab */}
					{activeTab === 2 && renderProductSection(
						auctionProducts,
						"Auction Products",
						<ScheduleIcon sx={{ mr: 1, color: "info.main" }} />,
						"No auction products found",
						"AUCTION"
					)}

					{/* Raffle Products Tab */}
					{activeTab === 3 && renderProductSection(
						raffleProducts,
						"Raffle Products",
						<CasinoIcon sx={{ mr: 1, color: "secondary.main" }} />,
						"No raffle products found",
						"RAFFLE"
					)}

					{/* User Management Tab */}
					{storePermissions.canManageUsers && activeTab === 4 && (
						<UserManagement
							store={store}
							currentUser={currentUser}
							onUpdate={toast}
						/>
					)}
				</Box>
			</Container>

			{authOpen && <AuthDialog open={authOpen} onClose={() => setAuthOpen(false)} />}

			<Snackbar
				open={snackbar.open}
				autoHideDuration={6000}
				onClose={() => setSnackbar((s) => ({ ...s, open: false }))}
				anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
			>
				<Alert
					onClose={() => setSnackbar((s) => ({ ...s, open: false }))}
					severity={snackbar.severity}
					variant="filled"
				>
					{snackbar.message}
				</Alert>
			</Snackbar>

			{editProductDialog && (
				<ProductEditDialog
					open={editProductDialog}
					onClose={handleProductEditClose}
					product={selectedProduct}
					store={store}
					currentUser={currentUser}
					onUpdate={handleProductUpdate}
				/>
			)}

			{addProductDialog && (
				<AddProductDialog
					open={addProductDialog}
					onClose={handleProductDialogClose}
					store={store}
					currentUser={currentUser}
					onUpdate={toast}
					purchaseType={addProductPurchaseType}
				/>
			)}

			{bidManagementDialog && (
				<BidManagementDialog
					open={bidManagementDialog}
					onClose={handleBidManagementClose}
					product={selectedBidProduct}
					store={store}
					onUpdate={toast}
				/>
			)}
		</Box>
	);
} 