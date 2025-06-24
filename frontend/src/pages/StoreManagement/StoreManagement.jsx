import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { useStorePermissions, PERMISSIONS } from "../../hooks/useStorePermissions";
import { storeService } from "../../services/storeService";
import purchaseService from "../../services/purchaseService";
import { productService } from "../../services/productService";
import userService from "../../services/userService";
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
	Tooltip,
	Divider,
	List,
	ListItem,
	ListItemText,
	CircularProgress,
	CardMedia
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
	People as PeopleIcon,
	Receipt as ReceiptIcon,
	Refresh as RefreshIcon
} from "@mui/icons-material";
import Header from "../../components/Header/Header";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import PolicyManagement from "../../components/PolicyManagement/PolicyManagement";
import ProductEditDialog from "../../components/ProductEditDialog/ProductEditDialog";
import AddProductDialog from "../../components/AddProductDialog/AddProductDialog";
import BidManagementDialog from "../../components/BidManagementDialog/BidManagementDialog";
import UserManagement from "../../components/UserManagement/UserManagement";
import './StoreManagement.css';
import { fetchDiscountedPrice, getEffectivePrice, hasDiscount, calculateSavings, formatPrice } from "../../utils/priceUtils";

// Create a proper React component for the product card
const ProductCard = ({ product, pendingBidCounts, storePermissions, handleViewProduct, handleManageBids, handleEditProduct }) => {
	const [discountedPrice, setDiscountedPrice] = useState(null);
	const [loading, setLoading] = useState(true);
	// Add auction status states
	const [auctionStatus, setAuctionStatus] = useState(null);
	const [timeLeft, setTimeLeft] = useState(null);
	const [auctionEnded, setAuctionEnded] = useState(false);
	const [loadingAuctionStatus, setLoadingAuctionStatus] = useState(false);

	// Fetch real-time discounted price
	useEffect(() => {
		const fetchPrice = async () => {
			setLoading(true);
			const discount = await fetchDiscountedPrice(product);
			setDiscountedPrice(discount);
			setLoading(false);
		};
		fetchPrice();
	}, [product.id, product.storeId]);

	// Fetch auction status for auction products
	useEffect(() => {
		if (product.purchaseType === 'AUCTION') {
			fetchAuctionStatus();
		}
	}, [product.id, product.storeId, product.purchaseType]);

	// Add auction timer countdown
	useEffect(() => {
		let interval = null;

		if (product?.purchaseType === 'AUCTION' && auctionStatus && !auctionEnded) {
			interval = setInterval(() => {
				const now = Date.now();
				const timeLeftMs = auctionStatus.timeLeftMillis - (now - auctionStatus.fetchedAt);

				if (timeLeftMs <= 0) {
					setTimeLeft(0);
					setAuctionEnded(true);
					clearInterval(interval);
				} else {
					setTimeLeft(timeLeftMs);
				}
			}, 1000);
		}

		return () => {
			if (interval) clearInterval(interval);
		};
	}, [auctionStatus, auctionEnded, product]);

	// Fetch auction status function
	const fetchAuctionStatus = async () => {
		if (!product || product.purchaseType !== 'AUCTION') return;

		setLoadingAuctionStatus(true);
		try {
			// Using placeholder userId since server uses JWT token
			const response = await fetch(`http://localhost:8080/api/purchases/auction/status/0/${product.storeId}/${String(product.id)}`, {
				headers: {
					'Authorization': `Bearer ${userService.getToken()}`
				}
			});

			if (response.ok) {
				const result = await response.json();
				if (result.success && result.data) {
					const statusData = {
						...result.data,
						fetchedAt: Date.now()
					};
					setAuctionStatus(statusData);
					setTimeLeft(statusData.timeLeftMillis);
					setAuctionEnded(statusData.timeLeftMillis <= 0);
				}
			}
		} catch (error) {
			console.warn(`Could not fetch auction status for product ${product.id}:`, error);
		} finally {
			setLoadingAuctionStatus(false);
		}
	};

	// Helper function to format time left
	const formatTimeLeft = (timeInMs) => {
		if (!timeInMs || timeInMs <= 0) return "Ended";

		const totalSeconds = Math.floor(timeInMs / 1000);
		const days = Math.floor(totalSeconds / (24 * 3600));
		const hours = Math.floor((totalSeconds % (24 * 3600)) / 3600);
		const minutes = Math.floor((totalSeconds % 3600) / 60);

		if (days > 0) {
			return `${days}d ${hours}h`;
		} else if (hours > 0) {
			return `${hours}h ${minutes}m`;
		} else {
			return `${minutes}m`;
		}
	};

	// Helper function to get auction status color
	const getAuctionStatusColor = (timeInMs) => {
		if (!timeInMs || timeInMs <= 0) return 'error';
		if (timeInMs < 3600000) return 'warning'; // Less than 1 hour
		return 'success';
	};

	const currentHasDiscount = hasDiscount(product, discountedPrice);
	const effectivePrice = getEffectivePrice(product, discountedPrice);
	const savings = calculateSavings(product, discountedPrice);

	// Calculate pending bids for bid products
	const pendingBids = product.purchaseType === 'BID' && storePermissions.canApproveBids ?
		(pendingBidCounts[product.id] || 0) : 0;

	return (
		<Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
			<Card
				sx={{
					height: "100%",
					display: "flex",
					flexDirection: "column",
					boxShadow: 2,
					'&:hover': { boxShadow: 4 },
					position: 'relative'
				}}
			>
				{/* Pending Bid Indicator */}
				{pendingBids > 0 && (
					<Box
						sx={{
							position: 'absolute',
							top: 8,
							right: 8,
							zIndex: 1,
							animation: 'pulse 2s infinite',
							'@keyframes pulse': {
								'0%': { opacity: 1, transform: 'scale(1)' },
								'50%': { opacity: 0.8, transform: 'scale(1.1)' },
								'100%': { opacity: 1, transform: 'scale(1)' }
							}
						}}
					>
						<Chip
							size="small"
							label={pendingBids}
							color="error"
							icon={<GavelIcon />}
							sx={{ fontWeight: 'bold', bgcolor: 'error.main', color: 'white' }}
						/>
					</Box>
				)}

				{/* Auction Status Indicator */}
				{product.purchaseType === 'AUCTION' && auctionStatus && (
					<Box
						sx={{
							position: 'absolute',
							top: 8,
							left: 8,
							zIndex: 1
						}}
					>
						<Chip
							size="small"
							label={auctionEnded ? "ENDED" : formatTimeLeft(timeLeft)}
							color={getAuctionStatusColor(timeLeft)}
							icon={<TimerIcon />}
							sx={{
								fontWeight: 'bold',
								animation: !auctionEnded && timeLeft < 3600000 ? 'pulse 2s infinite' : 'none',
								'@keyframes pulse': {
									'0%': { opacity: 1, transform: 'scale(1)' },
									'50%': { opacity: 0.8, transform: 'scale(1.05)' },
									'100%': { opacity: 1, transform: 'scale(1)' }
								}
							}}
						/>
					</Box>
				)}

				<CardMedia
					component="img"
					height="160"
					image={product.images?.[0] || "https://via.placeholder.com/300x160"}
					alt={product.title}
					sx={{
						objectFit: "cover",
						cursor: "pointer",
						'&:hover': { opacity: 0.8 }
					}}
					onClick={() => handleViewProduct(product.id)}
				/>
				<CardContent sx={{ flex: 1 }}>
					<Typography variant="h6" component="h3" fontWeight="bold" gutterBottom>
						{product.title}
					</Typography>
					<Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
						{product.description || "No description available"}
					</Typography>

					{/* Price Display - Updated to use real-time pricing */}
					<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
						{product.purchaseType === 'BID' ? (
							<Box sx={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
								<Typography variant="h6" color="warning.main" fontWeight="bold">
									Bid Product
								</Typography>
								<Typography variant="caption" color="warning.dark">
									Accepts bids - no fixed price
								</Typography>
								{product.minBidAmount && (
									<Typography variant="caption" color="text.secondary">
										Min bid: ${formatPrice(product.minBidAmount)}
									</Typography>
								)}
							</Box>
						) : product.purchaseType === 'AUCTION' ? (
							<Box sx={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
								{auctionStatus ? (
									<>
										<Typography variant="h6" color="info.main" fontWeight="bold">
											${(auctionStatus.currentMaxOffer || auctionStatus.startingPrice || product.price).toFixed(2)}
										</Typography>
										<Typography variant="caption" color="info.dark">
											{auctionStatus.currentMaxOffer > auctionStatus.startingPrice
												? "Current highest bid"
												: "Starting price"}
										</Typography>
										{auctionStatus.startingPrice && auctionStatus.currentMaxOffer > auctionStatus.startingPrice && (
											<Typography variant="caption" color="success.main">
												+${(auctionStatus.currentMaxOffer - auctionStatus.startingPrice).toFixed(2)} above start
											</Typography>
										)}
									</>
								) : loadingAuctionStatus ? (
									<>
										<Typography variant="h6" color="info.main" fontWeight="bold">
											${formatPrice(product.price)}
										</Typography>
										<Typography variant="caption" color="text.secondary">
											Loading auction status...
										</Typography>
									</>
								) : (
									<>
										<Typography variant="h6" color="info.main" fontWeight="bold">
											${formatPrice(product.price)}
										</Typography>
										<Typography variant="caption" color="text.secondary">
											Starting price
										</Typography>
									</>
								)}
							</Box>
						) : loading ? (
							<Box>
								<Typography variant="h6" color="primary" fontWeight="bold">
									${formatPrice(product.price)}
								</Typography>
								<Typography variant="caption" color="text.secondary">
									Checking for discounts...
								</Typography>
							</Box>
						) : currentHasDiscount ? (
							<Box sx={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
								<Typography variant="body2" color="text.secondary" sx={{ textDecoration: "line-through" }}>
									${formatPrice(product.price)}
								</Typography>
								<Typography variant="h6" color="primary" fontWeight="bold">
									${formatPrice(effectivePrice)}
								</Typography>
								<Typography variant="caption" color="success.main">
									Save ${formatPrice(savings)}
								</Typography>
							</Box>
						) : (
							<Box>
								<Typography variant="h6" color="primary" fontWeight="bold">
									${formatPrice(effectivePrice)}
								</Typography>
								{product.purchaseType === 'AUCTION' && (
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
						<Box sx={{ mt: 1, p: 1, bgcolor: auctionEnded ? 'grey.100' : 'info.light', borderRadius: 1 }}>
							<Typography variant="caption" color={auctionEnded ? 'text.secondary' : 'info.dark'} sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
								<TimerIcon fontSize="small" />
								{auctionEnded ? 'Auction Ended' : 'Live Auction'}
								{auctionStatus && timeLeft !== null && !auctionEnded && (
									<Typography component="span" sx={{ ml: 1, fontWeight: 'bold' }}>
										- {formatTimeLeft(timeLeft)} left
									</Typography>
								)}
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

export default function StoreManagement() {
	const { storeId } = useParams();
	const navigate = useNavigate();
	const { currentUser, isAuthenticated, loading: authLoading } = useAuth();
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

	// Add auction status tracking
	const [auctionStatusCounts, setAuctionStatusCounts] = useState({
		active: 0,
		ended: 0,
		total: 0
	});

	// Store purchase history state
	const [purchaseHistory, setPurchaseHistory] = useState([]);
	const [loadingPurchaseHistory, setLoadingPurchaseHistory] = useState(false);
	const [purchaseHistoryError, setPurchaseHistoryError] = useState(null);

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
			const rejectedCounts = {};

			await Promise.all(
				bidProducts.map(async (product) => {
					try {
						const bids = await purchaseService.getProductBids(store.id, product.id);
						counts[product.id] = bids?.length || 0;
						pendingCounts[product.id] = bids?.filter(bid => !bid.isApproved && !bid.isRejected)?.length || 0;
						rejectedCounts[product.id] = bids?.filter(bid => bid.isRejected)?.length || 0;
					} catch (error) {
						console.warn(`Could not fetch bids for product ${product.id}:`, error);
						counts[product.id] = 0;
						pendingCounts[product.id] = 0;
						rejectedCounts[product.id] = 0;
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

			// Store rejected counts for display (you can add this state if needed)
			// setRejectedBidCounts(rejectedCounts);
		} catch (error) {
			console.error("Error loading bid counts:", error);
		}
	}, [store?.id, bidProducts, storePermissions.canApproveBids, pendingBidCounts, lastBidCheckTime, toast]);

	// Function to load auction status counts for all auction products
	const loadAuctionStatusCounts = useCallback(async () => {
		if (!store?.id || auctionProducts.length === 0) return;

		try {
			let activeCount = 0;
			let endedCount = 0;

			await Promise.all(
				auctionProducts.map(async (product) => {
					try {
						const response = await fetch(`http://localhost:8080/api/purchases/auction/status/0/${store.id}/${String(product.id)}`, {
							headers: {
								'Authorization': `Bearer ${userService.getToken()}`
							}
						});

						if (response.ok) {
							const result = await response.json();
							if (result.success && result.data) {
								if (result.data.timeLeftMillis <= 0) {
									endedCount++;
								} else {
									activeCount++;
								}
							}
						}
					} catch (error) {
						console.warn(`Could not fetch auction status for product ${product.id}:`, error);
					}
				})
			);

			setAuctionStatusCounts({
				active: activeCount,
				ended: endedCount,
				total: auctionProducts.length
			});
		} catch (error) {
			console.error("Error loading auction status counts:", error);
		}
	}, [store?.id, auctionProducts, userService]);

	// Polling effect for bid updates
	useEffect(() => {
		if (!storePermissions.canApproveBids || bidProducts.length === 0) return;

		// Initial load
		loadBidCounts();

		// Set up polling every 30 seconds
		const interval = setInterval(loadBidCounts, 30000);

		return () => clearInterval(interval);
	}, [loadBidCounts, storePermissions.canApproveBids, bidProducts.length]);

	// Polling effect for auction status updates
	useEffect(() => {
		if (auctionProducts.length === 0) return;

		// Initial load
		loadAuctionStatusCounts();

		// Set up polling every 30 seconds
		const interval = setInterval(loadAuctionStatusCounts, 30000);

		return () => clearInterval(interval);
	}, [loadAuctionStatusCounts, auctionProducts.length]);

	// Load purchase history when tab is selected
	useEffect(() => {
		if (activeTab === 5 && storeId) {
			loadPurchaseHistory();
		}
	}, [activeTab, storeId]);

	// Function to load purchase history
	const loadPurchaseHistory = async () => {
		if (!storeId) return;

		setLoadingPurchaseHistory(true);
		setPurchaseHistoryError(null);

		try {
			const history = await purchaseService.getStorePurchaseHistory(storeId);
			setPurchaseHistory(history);
		} catch (error) {
			console.error('Error loading purchase history:', error);
			setPurchaseHistoryError('Failed to load purchase history. ' + (error.message || ''));
			toast('Failed to load purchase history', 'error');
		} finally {
			setLoadingPurchaseHistory(false);
		}
	};

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
		return (
			<ProductCard
				key={product.id}
				product={product}
				pendingBidCounts={pendingBidCounts}
				storePermissions={storePermissions}
				handleViewProduct={handleViewProduct}
				handleManageBids={handleManageBids}
				handleEditProduct={handleEditProduct}
			/>
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

						{/* Special indicator for auction products with active auctions */}
						{purchaseType === 'AUCTION' && auctionStatusCounts.active > 0 && (
							<Box sx={{ ml: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
								<Chip
									size="small"
									label={`${auctionStatusCounts.active} LIVE`}
									color="info"
									variant="filled"
									icon={<TimerIcon />}
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
								<Typography variant="body2" color="info.main" sx={{ fontWeight: 'bold' }}>
									🔥 Active Auctions
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
								{purchaseType === 'AUCTION' && auctionStatusCounts.total > 0 && (
									<>
										<Chip
											size="small"
											label={`Live: ${auctionStatusCounts.active}`}
											color="info"
											variant="outlined"
										/>
										<Chip
											size="small"
											label={`Ended: ${auctionStatusCounts.ended}`}
											color="warning"
											variant="outlined"
										/>
									</>
								)}
								{purchaseType !== 'BID' && purchaseType !== 'AUCTION' && (
									<Chip
										size="small"
										label={`Total Value: $${products.reduce((sum, p) => sum + (p.hasDiscount ? p.discountedPrice : p.price), 0).toFixed(2)}`}
										color="primary"
										variant="outlined"
									/>
								)}
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
									{purchaseType === 'AUCTION' && "Create auction products that automatically start accepting bids"}
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
				{authOpen && !isAuthenticated && <AuthDialog open={authOpen} onClose={() => setAuthOpen(false)} />}
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
						<Tab
							icon={<ReceiptIcon />}
							label="Purchase History"
							iconPosition="start"
						/>
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

					{/* Purchase History Tab */}
					{activeTab === 5 && (
						<Box sx={{ mb: 4 }}>
							<Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", mb: 3 }}>
								<Typography variant="h6" sx={{ display: "flex", alignItems: "center" }}>
									<ReceiptIcon sx={{ mr: 1, color: "primary.main" }} />
									Store Purchase History
								</Typography>
								<Button
									variant="outlined"
									size="small"
									startIcon={<RefreshIcon />}
									onClick={loadPurchaseHistory}
									disabled={loadingPurchaseHistory}
								>
									Refresh
								</Button>
							</Box>

							{loadingPurchaseHistory ? (
								<Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
									<CircularProgress />
								</Box>
							) : purchaseHistoryError ? (
								<Alert severity="error" sx={{ mb: 3 }}>
									{purchaseHistoryError}
								</Alert>
							) : purchaseHistory.length === 0 ? (
								<Box sx={{ textAlign: "center", py: 6, bgcolor: "grey.50", borderRadius: 2 }}>
									<ReceiptIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
									<Typography variant="h6" color="text.secondary" gutterBottom>
										No purchase history found
									</Typography>
									<Typography variant="body2" color="text.disabled">
										There are no completed purchases for this store yet
									</Typography>
								</Box>
							) : (
								<>
									<Typography variant="subtitle2" sx={{ mb: 2 }}>
										Found {purchaseHistory.length} purchase records
									</Typography>
									<Grid container spacing={2}>
										{purchaseHistory.map((purchase) => (
											<Grid item xs={12} key={purchase.purchaseId}>
												<Paper
													sx={{
														p: 2,
														borderRadius: 2,
														border: "1px solid",
														borderColor: "divider"
													}}
													elevation={0}
												>
													<Box sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}>
														<Box>
															<Typography variant="subtitle1" fontWeight="bold">
																Purchase #{purchase.purchaseId}
															</Typography>
															<Typography variant="body2" color="text.secondary">
																{new Date(purchase.timestamp).toLocaleString()}
															</Typography>
														</Box>
														<Box sx={{ textAlign: "right" }}>
															<Typography variant="h6" color="primary.main" fontWeight="bold">
																${purchase.totalPrice.toFixed(2)}
															</Typography>
															<Chip
																size="small"
																label="Completed"
																color="success"
															/>
														</Box>
													</Box>

													<Divider sx={{ my: 2 }} />

													<Typography variant="subtitle2" gutterBottom>
														Items Purchased:
													</Typography>
													<List dense disablePadding>
														{purchase.products?.map((item, index) => (
															<ListItem
																key={index}
																disablePadding
																sx={{ py: 0.5 }}
															>
																<ListItemText
																	primary={`Product ID: ${item.productId}`}
																	secondary={
																		<>
																			Quantity: {item.quantity} × ${item.unitPrice.toFixed(2)}
																		</>
																	}
																/>
																<Typography variant="subtitle2">
																	${(item.unitPrice * item.quantity).toFixed(2)}
																</Typography>
															</ListItem>
														))}
													</List>

													<Divider sx={{ my: 2 }} />

													<Box sx={{ display: "flex", justifyContent: "space-between" }}>
														<Box>
															<Typography variant="body2">
																<strong>Buyer:</strong> User #{purchase.userId}
															</Typography>
															{purchase.contactInfo && (
																<Typography variant="body2">
																	<strong>Payment Method:</strong> {purchase.contactInfo}
																</Typography>
															)}
														</Box>
														{purchase.shippingAddress && (
															<Typography variant="body2">
																<strong>Shipping Address:</strong> {purchase.shippingAddress}
															</Typography>
														)}
													</Box>
												</Paper>
											</Grid>
										))}
									</Grid>
								</>
							)}
						</Box>
					)}
				</Box>
			</Container>

			{authOpen && !isAuthenticated && <AuthDialog open={authOpen} onClose={() => setAuthOpen(false)} />}

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