import React, { useState, useEffect, useCallback } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { createPageUrl } from "../../utils";
import userService from "../../services/userService";
import { storeService } from "../../services/storeService";
import { productService } from "../../services/productService";
import purchaseService from "../../services/purchaseService";
import adminService from "../../services/adminService";
import { useAuth } from "../../contexts/AuthContext";
import { fetchDiscountedPrice, getEffectivePrice, hasDiscount, calculateSavings, formatPrice } from "../../utils/priceUtils";
import {
	Box,
	Container,
	Typography,
	Tabs,
	Tab,
	Skeleton,
	Chip,
	Button,
	Snackbar,
	Alert,
	Card,
	CardContent,
	CardActions,
	Grid,
	Paper,
	TextField,
	Divider,
	Avatar
} from "@mui/material";
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import TableContainer from '@mui/material/TableContainer';
import Table from '@mui/material/Table';
import TableHead from '@mui/material/TableHead';
import TableBody from '@mui/material/TableBody';
import TableRow from '@mui/material/TableRow';
import TableCell from '@mui/material/TableCell';
import EditIcon from "@mui/icons-material/Edit";
import SaveIcon from "@mui/icons-material/Save";
import CancelIcon from "@mui/icons-material/Cancel";
import PersonIcon from "@mui/icons-material/Person";
import StoreIcon from "@mui/icons-material/Store";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import AddIcon from "@mui/icons-material/Add";
import InventoryIcon from "@mui/icons-material/Inventory";
import Header from "../../components/Header/Header";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import './Profile.css';

export default function Profile() {
	const { currentUser, isAuthenticated, loading: authLoading } = useAuth();
	const navigate = useNavigate();
	const [activeTab, setActiveTab] = useState(0);
	const [loading, setLoading] = useState(true);
	const [userProfile, setUserProfile] = useState(null);
	const [foundedStores, setFoundedStores] = useState([]);
	const [ownedStores, setOwnedStores] = useState([]);
	const [managedStores, setManagedStores] = useState([]);
	const [purchaseHistory, setPurchaseHistory] = useState([]);
	const [purchaseHistoryWithPrices, setPurchaseHistoryWithPrices] = useState([]);
	const [loadingPurchasePrices, setLoadingPurchasePrices] = useState(false);
	const [isEditing, setIsEditing] = useState(false);
	const [editedProfile, setEditedProfile] = useState({});
	const [authOpen, setAuthOpen] = useState(false);
	const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });
	const [isAdmin, setIsAdmin] = useState(false);
	const [allStores, setAllStores] = useState([]);
	const [allUsers, setAllUsers] = useState([]);
	const [suspendedUsers, setSuspendedUsers] = useState([]);
	const [adminLoading, setAdminLoading] = useState(false);
	const [adminTabValue, setAdminTabValue] = useState(0);

	// Stable toast function that doesn't cause re-renders
	const toast = useCallback(({ title, description, variant }) => {
		setSnackbar({
			open: true,
			message: description || title,
			severity: variant === "destructive" ? "error" : "success"
		});
	}, []);

	const checkAdminStatus = useCallback(async () => {
		try {
			const isAdminResult = await adminService.isAdmin();
			setIsAdmin(isAdminResult);
		} catch (error) {
			console.error("Error checking admin status:", error);
			setIsAdmin(false);
		}
	}, []);

	// Check admin status separately from regular data loading
	useEffect(() => {
		if (currentUser) {
			checkAdminStatus();
		}
	}, [currentUser, checkAdminStatus]);

	const loadUserManagedStores = useCallback(async () => {
		try {
			console.log('Loading user managed stores for:', currentUser.userName);
			// Get all stores first
			const { stores } = await storeService.getAllStoresAndProducts();
			console.log('Retrieved stores:', stores);

			// Categorize stores by role
			const founded = [];
			const owned = [];
			const managed = [];

			for (const store of stores) {
				try {
					console.log(`Checking roles for store ${store.id} (${store.name})`);

					// Check if user is founder
					const isFounder = await storeService.isFounder(store.id, currentUser.userName);
					console.log(`Is founder of ${store.name}:`, isFounder);
					if (isFounder) {
						founded.push({
							...store,
							role: 'Founder'
						});
						continue; // If founder, skip checking other roles
					}

					// Check if user is owner
					const isOwner = await storeService.isOwner(store.id, currentUser.userName);
					console.log(`Is owner of ${store.name}:`, isOwner);
					if (isOwner) {
						owned.push({
							...store,
							role: 'Owner'
						});
						continue; // If owner, skip checking manager role
					}

					// Check if user is manager
					const isManager = await storeService.isManager(store.id, currentUser.userName);
					console.log(`Is manager of ${store.name}:`, isManager);
					if (isManager) {
						managed.push({
							...store,
							role: 'Manager'
						});
					}
				} catch (error) {
					console.warn(`Failed to check role for store ${store.id}:`, error);
				}
			}

			console.log('Founded stores:', founded);
			console.log('Owned stores:', owned);
			console.log('Managed stores:', managed);

			setFoundedStores(founded);
			setOwnedStores(owned);
			setManagedStores(managed);

			// Note: Removed backward compatibility userStores state as it's no longer needed
		} catch (error) {
			console.error("Error loading managed stores:", error);
			setFoundedStores([]);
			setOwnedStores([]);
			setManagedStores([]);
		}
	}, [currentUser]);

	const fetchPurchaseHistoryPrices = useCallback(async () => {
		if (purchaseHistory.length === 0) {
			setPurchaseHistoryWithPrices([]);
			return;
		}

		setLoadingPurchasePrices(true);
		try {
			// For purchase history, just use the actual prices that were paid
			// No need to calculate discounts on completed purchases
			const historyWithPrices = purchaseHistory.map((purchase) => {
				const itemsWithPrices = purchase.items.map((item) => ({
					...item,
					originalPrice: item.price,
					discountedPrice: null, // No discounts on completed purchases
					effectivePrice: item.price, // Use the actual paid price
					hasDiscount: false,
					savings: 0
				}));

				return {
					...purchase,
					items: itemsWithPrices,
					currentTotal: purchase.total, // Use the actual total that was paid
					originalTotal: purchase.total,
					totalSavings: 0,
					hasSavingsAvailable: false
				};
			});

			setPurchaseHistoryWithPrices(historyWithPrices);
		} catch (error) {
			console.error("Error processing purchase history:", error);
			// Fallback to original purchase history
			setPurchaseHistoryWithPrices(purchaseHistory.map(purchase => ({
				...purchase,
				items: purchase.items.map(item => ({
					...item,
					originalPrice: item.price,
					discountedPrice: null,
					effectivePrice: item.price,
					hasDiscount: false,
					savings: 0
				})),
				currentTotal: purchase.total,
				originalTotal: purchase.total,
				totalSavings: 0,
				hasSavingsAvailable: false
			})));
		}
		setLoadingPurchasePrices(false);
	}, [purchaseHistory]);

	const loadPurchaseHistory = useCallback(async () => {
		try {
			console.log('Loading purchase history for user:', currentUser.userName);
			// The backend endpoint extracts username from token, so we just need to call with any numeric userId
			// The actual userId parameter is ignored by the backend, but it must be numeric for the API
			const userIdParam = currentUser.id || currentUser.userId || 1; // Use actual ID when available, fallback to 1
			const history = await purchaseService.getPurchaseHistory(userIdParam);
			console.log('Retrieved purchase history:', history);

			// Transform the backend Purchase objects to frontend format
			const transformedHistory = await Promise.all(history.map(async (purchase, index) => {
				console.log(`Processing purchase ${index}:`, purchase);
				// Try to get product details for each purchased product
				const itemsWithDetails = await Promise.all(
					purchase.products?.map(async (product) => {
						try {
							console.log(`Fetching details for product ${product.productId}`);
							// Try to get actual product details from the listing
							const productDetails = await productService.getListing(product.productId);
							console.log(`Product details for ${product.productId}:`, productDetails);

							// Handle the productService.getListing response format correctly
							// The productService transforms backend 'productName' to frontend 'title'
							const productTitle = productDetails?.title || productDetails?.productName || `Product ${product.productId}`;

							return {
								title: productTitle,
								quantity: product.quantity,
								price: product.unitPrice,
								image: productDetails?.images?.[0] || "https://via.placeholder.com/40",
								productId: product.productId,
								storeId: product.storeId
							};
						} catch (error) {
							console.warn(`Failed to fetch details for product ${product.productId}:`, error);
							return {
								title: `Product ${product.productId}`,
								quantity: product.quantity,
								price: product.unitPrice,
								image: "https://via.placeholder.com/40",
								productId: product.productId,
								storeId: product.storeId
							};
						}
					}) || []
				);

				return {
					id: `purchase-${index}`,
					createdAt: purchase.timestamp,
					status: "Completed",
					total: purchase.totalPrice,
					shippingAddress: purchase.shippingAddress,
					contactInfo: purchase.contactInfo,
					items: itemsWithDetails
				};
			}));

			console.log('Transformed purchase history:', transformedHistory);
			setPurchaseHistory(transformedHistory);
		} catch (error) {
			console.error("Error loading purchase history:", error);
			// Set empty array but don't hide the error completely
			setPurchaseHistory([]);
		}
	}, [currentUser]);

	const loadProfileData = useCallback(async () => {
		if (!currentUser) return;

		setLoading(true);
		try {
			// Load user profile
			const profile = await userService.getProfile();
			setUserProfile(profile);
			setEditedProfile(profile);

			// Load user's managed stores (owner/manager) from server
			await loadUserManagedStores();

			// Load purchase history from server
			await loadPurchaseHistory();

		} catch (error) {
			console.error("Error loading profile data:", error);
			toast({ title: "Error", description: "Failed to load profile data", variant: "destructive" });
		} finally {
			setLoading(false);
		}
	}, [currentUser, toast, loadUserManagedStores, loadPurchaseHistory]);

	const loadAllStores = useCallback(async () => {
		if (!isAdmin) return;
		setAdminLoading(true);
		try {
			const { stores } = await storeService.getAllStoresAndProducts();
			setAllStores(stores);
		} catch (error) {
			console.error("Error loading all stores:", error);
			toast({ title: "Error", description: "Failed to load stores", variant: "destructive" });
		} finally {
			setAdminLoading(false);
		}
	}, [isAdmin, toast]);

	const loadSuspendedUsers = useCallback(async () => {
		if (!isAdmin) return;
		try {
			const suspendedUserIds = await adminService.getSuspendedUsers();
			console.log("admin suspended users: ", suspendedUserIds);
			setSuspendedUsers(suspendedUserIds);
			console.log('Loaded suspended users:', suspendedUserIds);
		} catch (error) {
			console.error("Error loading suspended users:", error);
			toast({ title: "Error", description: "Failed to load suspended users", variant: "destructive" });
		}
	}, [isAdmin, toast]);

	const loadAllUsers = useCallback(async () => {
		if (!isAdmin) return;
		setAdminLoading(true);
		try {
			const { users } = await adminService.getAllUsers();
			console.log("admin users: ", users);
			setAllUsers(users);
			console.log("all users: ", allUsers);
		} catch (error) {
			console.error("Error loading all users:", error);
			toast({ title: "Error", description: "Failed to load users", variant: "destructive" });
		} finally {
			setAdminLoading(false);
		}
		loadSuspendedUsers();
		loadProfileData();
	}, [currentUser, loadProfileData, loadSuspendedUsers]);

	// Load tab-specific data when a tab is selected
	useEffect(() => {
		if (!currentUser) return;

		// Clear loading state initially
		setLoading(true);

		// Load data specific to the selected tab
		const loadTabData = async () => {
			try {
				console.log(`Loading data for tab ${activeTab}`);

				// Load different data based on active tab
				switch (activeTab) {
					case 0: // Personal Details tab
						// Load user profile data only
						const profile = await userService.getProfile();
						setUserProfile(profile);
						setEditedProfile(profile);
						break;

					case 1: // My Stores tab
						// Load only store data
						await loadUserManagedStores();
						break;

					case 2: // Purchase History tab
						// Load only purchase history
						await loadPurchaseHistory();
						break;

					case 3: // Admin tab
						if (isAdmin) {
							// Load suspended users first, so we can highlight them in the users list
							await loadSuspendedUsers().catch(error => {
								console.error("Error loading suspended users:", error);
							});

							// Continue with loading other data even if suspended users fails
							await loadAllStores();
							await loadAllUsers();
						}
						break;
				}
			} catch (error) {
				console.error(`Error loading data for tab ${activeTab}:`, error);
				toast({ title: "Error", description: `Failed to load data for this tab`, variant: "destructive" });
			} finally {
				setLoading(false);
			}
		};

		// Execute the data loading function
		loadTabData();

	}, [activeTab, currentUser, isAdmin, loadUserManagedStores, loadPurchaseHistory, loadSuspendedUsers, loadAllStores, loadAllUsers, toast]);

	useEffect(() => {
		if (purchaseHistory.length > 0) {
			fetchPurchaseHistoryPrices();
		} else {
			setPurchaseHistoryWithPrices([]);
		}
	}, [purchaseHistory, fetchPurchaseHistoryPrices]);

	const handleCloseStore = async (storeId, storeName) => {
		try {
			await adminService.closeStore(storeId);
			toast({ title: "Success", description: `Store "${storeName}" has been closed` });
			loadAllStores(); // Refresh the store list
		} catch (error) {
			console.error("Error closing store:", error);
			toast({ title: "Error", description: `Failed to close store: ${error.message || "Unknown error"}`, variant: "destructive" });
		}
	};

	const handleSuspendUser = async (userId, isPermanent = false) => {
		let hours;

		if (isPermanent) {
			// Permanent suspension (0 hours)
			hours = 0;
		} else {
			// Temporary suspension - ask for duration
			const suspensionHours = window.prompt("Enter suspension duration in hours:", "24");

			if (suspensionHours === null) return; // User cancelled

			hours = parseInt(suspensionHours, 10);
			if (isNaN(hours) || hours < 0) {
				setSnackbar({
					open: true,
					message: "Please enter a valid non-negative number for suspension hours",
					severity: "error"
				});
				return;
			}

		}

		const durationText = hours === 0 ? "permanently" : `for ${hours} hours`;

		try {
			console.log("Suspension duration:", hours);
			await adminService.suspendUser(userId, hours);
			toast({ title: "Success", description: `User "${userId}" has been suspended ${durationText}` });
			loadAllUsers(); // Refresh the user list
			loadSuspendedUsers(); // Refresh the suspended users list
		} catch (error) {
			console.error("Error suspending user:", error);
			toast({
				title: "Error",
				description: `Failed to suspend user: ${error.message || "Unknown error"}`,
				variant: "destructive"
			});
		}
	};

	const handleUnsuspendUser = async (username) => {
		try {
			console.log("Unsuspending user:", username);
			await adminService.unsuspendUser(username);
			toast({ title: "Success", description: `User "${username}" has been unsuspended` });
			loadAllUsers(); // Refresh the user list
			loadSuspendedUsers(); // Refresh the suspended users list
		} catch (error) {
			console.error("Error unsuspending user:", error);
			toast({ title: "Error", description: `Failed to unsuspend user: ${error.message || "Unknown error"}`, variant: "destructive" });
		}
	};

	const handleSaveProfile = async () => {
		try {
			await userService.updateUserData(editedProfile);
			setUserProfile(editedProfile);
			setIsEditing(false);
			toast({ title: "Success", description: "Profile updated successfully" });
		} catch (error) {
			console.error("Error updating profile:", error);
			toast({ title: "Error", description: "Failed to update profile", variant: "destructive" });
		}
	};

	const handleCancelEdit = () => {
		setEditedProfile(userProfile);
		setIsEditing(false);
	};

	const handleCreateStore = () => {
		// Navigate to store creation or show dialog
		const storeName = prompt('Enter store name:');
		if (storeName) {
			createNewStore(storeName);
		}
	};

	const createNewStore = async (storeName) => {
		try {
			await storeService.createStore({
				storeName: storeName,
				founderId: currentUser.userName
				// description: "New store"
			});
			toast({ title: "Success", description: "Store created successfully" });
			loadUserManagedStores(); // Refresh managed stores
		} catch (error) {
			console.error("Error creating store:", error);
			toast({ title: "Error", description: "Failed to create store", variant: "destructive" });
		}
	};

	if (!currentUser) {
		return (
			<Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
				<Header />
				<Container maxWidth="lg" sx={{ py: 6 }}>
					<Box sx={{ textAlign: "center", py: 8 }}>
						<PersonIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
						<Typography variant="h6" mb={1}>Please sign in to view your profile</Typography>
						<Typography variant="body2" color="text.secondary">You need to be logged in to access your profile page</Typography>
					</Box>
				</Container>
				{/* Only show auth dialog when explicitly triggered, not on refresh */}
				{authOpen && !isAuthenticated && <AuthDialog open={authOpen} onClose={() => setAuthOpen(false)} />}
			</Box>
		);
	}

	return (
		<Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
			<Header />
			<Container maxWidth="lg" sx={{ py: 6 }}>
				<Typography variant="h4" fontWeight={700} mb={3}>My Profile</Typography>

				<Tabs
					value={activeTab}
					onChange={(_, v) => {
						setActiveTab(v);
						// No need to do anything else here - the useEffect will handle data loading
					}}
					sx={{ mb: 3 }}>
					<Tab
						icon={<PersonIcon />}
						label="Personal Details"
						iconPosition="start"
					/>
					<Tab
						icon={<StoreIcon />}
						label={`My Stores (${foundedStores.length + ownedStores.length + managedStores.length})`}
						iconPosition="start"
					/>
					<Tab
						icon={<ShoppingBagIcon />}
						label={`Purchase History (${purchaseHistory.length})`}
						iconPosition="start"
					/>
					{isAdmin && (
						<Tab
							icon={<AdminPanelSettingsIcon />}
							label="Admin Management"
							iconPosition="start"
						/>
					)}
				</Tabs>

				{/* Personal Details Tab */}
				{activeTab === 0 && (
					<Paper sx={{ p: 4, borderRadius: 2 }}>
						{loading ? (
							<Box>
								<Skeleton variant="circular" width={80} height={80} sx={{ mb: 2 }} />
								<Skeleton variant="text" width="60%" height={40} sx={{ mb: 2 }} />
								<Skeleton variant="text" width="40%" height={30} sx={{ mb: 1 }} />
								<Skeleton variant="text" width="50%" height={30} sx={{ mb: 1 }} />
								<Skeleton variant="text" width="45%" height={30} />
							</Box>
						) : (
							<Box>
								<Box sx={{ display: "flex", alignItems: "center", mb: 4 }}>
									<Avatar
										sx={{ width: 80, height: 80, mr: 3, bgcolor: "primary.main" }}
									>
										{currentUser.userName ? currentUser.userName.charAt(0).toUpperCase() : "U"}
									</Avatar>
									<Box sx={{ flex: 1 }}>
										<Typography variant="h5" fontWeight="bold">
											{userProfile?.firstName && userProfile?.lastName
												? `${userProfile.firstName} ${userProfile.lastName}`
												: currentUser.userName
											}
										</Typography>
										<Typography variant="body2" color="text.secondary">
											Member since {new Date(userProfile?.createdAt || Date.now()).toLocaleDateString()}
										</Typography>
									</Box>
									<Button
										variant={isEditing ? "outlined" : "contained"}
										startIcon={isEditing ? <CancelIcon /> : <EditIcon />}
										onClick={isEditing ? handleCancelEdit : () => setIsEditing(true)}
										sx={{ mr: 1 }}
									>
										{isEditing ? "Cancel" : "Edit"}
									</Button>
									{isEditing && (
										<Button
											variant="contained"
											startIcon={<SaveIcon />}
											onClick={handleSaveProfile}
										>
											Save
										</Button>
									)}
								</Box>

								<Grid container spacing={3}>
									<Grid item xs={12} md={6}>
										<TextField
											fullWidth
											label="Username"
											value={userProfile?.userName || currentUser.userName}
											disabled
											sx={{ mb: 2 }}
										/>
										<TextField
											fullWidth
											label="First Name"
											value={isEditing ? (editedProfile.firstName || "") : (userProfile?.firstName || "")}
											onChange={(e) => setEditedProfile(prev => ({ ...prev, firstName: e.target.value }))}
											disabled={!isEditing}
											sx={{ mb: 2 }}
										/>
										<TextField
											fullWidth
											label="Last Name"
											value={isEditing ? (editedProfile.lastName || "") : (userProfile?.lastName || "")}
											onChange={(e) => setEditedProfile(prev => ({ ...prev, lastName: e.target.value }))}
											disabled={!isEditing}
										/>
									</Grid>
									<Grid item xs={12} md={6}>
										<TextField
											fullWidth
											label="Email"
											value={isEditing ? (editedProfile.email || "") : (userProfile?.email || "")}
											onChange={(e) => setEditedProfile(prev => ({ ...prev, email: e.target.value }))}
											disabled={!isEditing}
											sx={{ mb: 2 }}
										/>
										<TextField
											fullWidth
											label="Phone"
											value={isEditing ? (editedProfile.phone || "") : (userProfile?.phone || "")}
											onChange={(e) => setEditedProfile(prev => ({ ...prev, phone: e.target.value }))}
											disabled={!isEditing}
											sx={{ mb: 2 }}
										/>
										<TextField
											fullWidth
											label="Address"
											value={isEditing ? (editedProfile.address || "") : (userProfile?.address || "")}
											onChange={(e) => setEditedProfile(prev => ({ ...prev, address: e.target.value }))}
											disabled={!isEditing}
											multiline
											rows={2}
										/>
									</Grid>
								</Grid>
							</Box>
						)}
					</Paper>
				)}

				{/* My Stores Tab */}
				{activeTab === 1 && (
					<Box>
						<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 3 }}>
							<Typography variant="h6">My Stores</Typography>
							<Button
								variant="contained"
								startIcon={<AddIcon />}
								onClick={handleCreateStore}
							>
								Create Store
							</Button>
						</Box>

						{loading ? (
							<Grid container spacing={3}>
								{[1, 2, 3].map((i) => (
									<Grid item xs={12} md={6} lg={4} key={i}>
										<Skeleton variant="rectangular" height={200} sx={{ borderRadius: 2 }} />
									</Grid>
								))}
							</Grid>
						) : (
							<>
								{/* Founded Stores Section */}
								{foundedStores.length > 0 && (
									<Box sx={{ mb: 4 }}>
										<Typography variant="h6" sx={{ mb: 2, display: "flex", alignItems: "center" }}>
											<StoreIcon sx={{ mr: 1, color: "primary.main" }} />
											Stores I Founded ({foundedStores.length})
										</Typography>
										<Grid container spacing={3}>
											{foundedStores.map((store) => (
												<Grid item xs={12} md={6} lg={4} key={store.id}>
													<Card sx={{ height: "100%", display: "flex", flexDirection: "column" }}>
														<CardContent sx={{ flex: 1 }}>
															<Box sx={{ display: "flex", alignItems: "center", mb: 2 }}>
																<StoreIcon sx={{ mr: 1, color: "primary.main" }} />
																<Typography variant="h6" component="h3" fontWeight="bold">
																	{store.name}
																</Typography>
															</Box>
															<Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
																{store.description}
															</Typography>
															<Box sx={{ display: "flex", gap: 1, mb: 2 }}>
																<Chip
																	size="small"
																	label={store.isActive ? "Active" : "Inactive"}
																	color={store.isActive ? "success" : "default"}
																/>
																<Chip
																	size="small"
																	label="Founder"
																	color="primary"
																	variant="filled"
																/>
																<Chip
																	size="small"
																	label={`${store.totalProducts || 0} Products`}
																	variant="outlined"
																/>
															</Box>
														</CardContent>
														<CardActions sx={{ justifyContent: "flex-end" }}>
															<Button
																size="small"
																startIcon={<InventoryIcon />}
																onClick={() => navigate(`/store/${store.id}/manage`)}
															>
																Manage
															</Button>
														</CardActions>
													</Card>
												</Grid>
											))}
										</Grid>
									</Box>
								)}

								{/* Owned Stores Section */}
								{ownedStores.length > 0 && (
									<Box sx={{ mb: 4 }}>
										<Typography variant="h6" sx={{ mb: 2, display: "flex", alignItems: "center" }}>
											<StoreIcon sx={{ mr: 1, color: "secondary.main" }} />
											Stores I Own ({ownedStores.length})
										</Typography>
										<Grid container spacing={3}>
											{ownedStores.map((store) => (
												<Grid item xs={12} md={6} lg={4} key={store.id}>
													<Card sx={{ height: "100%", display: "flex", flexDirection: "column" }}>
														<CardContent sx={{ flex: 1 }}>
															<Box sx={{ display: "flex", alignItems: "center", mb: 2 }}>
																<StoreIcon sx={{ mr: 1, color: "secondary.main" }} />
																<Typography variant="h6" component="h3" fontWeight="bold">
																	{store.name}
																</Typography>
															</Box>
															<Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
																{store.description}
															</Typography>
															<Box sx={{ display: "flex", gap: 1, mb: 2 }}>
																<Chip
																	size="small"
																	label={store.isActive ? "Active" : "Inactive"}
																	color={store.isActive ? "success" : "default"}
																/>
																<Chip
																	size="small"
																	label="Owner"
																	color="secondary"
																	variant="filled"
																/>
																<Chip
																	size="small"
																	label={`${store.totalProducts || 0} Products`}
																	variant="outlined"
																/>
															</Box>
														</CardContent>
														<CardActions sx={{ justifyContent: "flex-end" }}>
															<Button
																size="small"
																startIcon={<InventoryIcon />}
																onClick={() => navigate(`/store/${store.id}/manage`)}
															>
																Manage
															</Button>
														</CardActions>
													</Card>
												</Grid>
											))}
										</Grid>
									</Box>
								)}

								{/* Managed Stores Section */}
								{managedStores.length > 0 && (
									<Box sx={{ mb: 4 }}>
										<Typography variant="h6" sx={{ mb: 2, display: "flex", alignItems: "center" }}>
											<StoreIcon sx={{ mr: 1, color: "warning.main" }} />
											Stores I Manage ({managedStores.length})
										</Typography>
										<Grid container spacing={3}>
											{managedStores.map((store) => (
												<Grid item xs={12} md={6} lg={4} key={store.id}>
													<Card sx={{ height: "100%", display: "flex", flexDirection: "column" }}>
														<CardContent sx={{ flex: 1 }}>
															<Box sx={{ display: "flex", alignItems: "center", mb: 2 }}>
																<StoreIcon sx={{ mr: 1, color: "warning.main" }} />
																<Typography variant="h6" component="h3" fontWeight="bold">
																	{store.name}
																</Typography>
															</Box>
															<Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
																{store.description}
															</Typography>
															<Box sx={{ display: "flex", gap: 1, mb: 2 }}>
																<Chip
																	size="small"
																	label={store.isActive ? "Active" : "Inactive"}
																	color={store.isActive ? "success" : "default"}
																/>
																<Chip
																	size="small"
																	label="Manager"
																	color="warning"
																	variant="filled"
																/>
																<Chip
																	size="small"
																	label={`${store.totalProducts || 0} Products`}
																	variant="outlined"
																/>
															</Box>
														</CardContent>
														<CardActions sx={{ justifyContent: "flex-end" }}>
															<Button
																size="small"
																startIcon={<InventoryIcon />}
																onClick={() => navigate(`/store/${store.id}/manage`)}
															>
																Manage
															</Button>
														</CardActions>
													</Card>
												</Grid>
											))}
										</Grid>
									</Box>
								)}

								{/* No Stores Message */}
								{foundedStores.length === 0 && ownedStores.length === 0 && managedStores.length === 0 && (
									<Box sx={{ textAlign: "center", py: 8 }}>
										<StoreIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
										<Typography variant="h6" mb={1}>No stores yet</Typography>
										<Typography variant="body2" color="text.secondary" mb={3}>
											Create your first store to start selling products
										</Typography>
										<Button
											variant="contained"
											startIcon={<AddIcon />}
											onClick={handleCreateStore}
										>
											Create Your First Store
										</Button>
									</Box>
								)}
							</>
						)}
					</Box>
				)}

				{/* Purchase History Tab */}
				{activeTab === 2 && (
					<Box>
						<Typography variant="h6" mb={3}>Purchase History</Typography>

						{loading ? (
							<Box>
								{[1, 2, 3].map((i) => (
									<Skeleton key={i} variant="rectangular" height={100} sx={{ borderRadius: 2, mb: 2 }} />
								))}
							</Box>
						) : (purchaseHistoryWithPrices.length > 0 || purchaseHistory.length > 0) ? (
							<Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
								{(purchaseHistoryWithPrices.length > 0 ? purchaseHistoryWithPrices : purchaseHistory).map((purchase, index) => (
									<Paper key={purchase.id || index} sx={{ p: 3, borderRadius: 2 }}>
										<Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "start", mb: 2 }}>
											<Box>
												<Typography variant="h6" fontWeight="bold">
													Order #{purchase.id || `ORD-${index + 1}`}
												</Typography>
												<Typography variant="body2" color="text.secondary">
													{new Date(purchase.createdAt || Date.now()).toLocaleDateString()}
												</Typography>
											</Box>
											<Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
												<Chip
													label={purchase.status || "Completed"}
													color="success"
													size="small"
												/>
											</Box>
										</Box>
										<Divider sx={{ mb: 2 }} />
										<Grid container spacing={2}>
											<Grid item xs={12} md={8}>
												<Typography variant="subtitle2" color="text.secondary" gutterBottom>
													Items:
												</Typography>
												{purchase.items && purchase.items.length > 0 ? purchase.items.map((item, itemIndex) => (
													<Box key={itemIndex} sx={{ display: "flex", alignItems: "center", mb: 1 }}>
														<Box
															component="img"
															src={item.image || "https://via.placeholder.com/40"}
															alt={item.title}
															sx={{ width: 40, height: 40, borderRadius: 1, mr: 2 }}
														/>
														<Box sx={{ flexGrow: 1 }}>
															<Typography variant="body2" fontWeight="medium">
																{item.title}
															</Typography>
															<Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
																<Typography variant="caption" color="text.secondary">
																	Qty: {item.quantity}
																</Typography>
																<Typography variant="caption" color="text.secondary">
																	×
																</Typography>
																<Typography variant="caption" fontWeight="medium">
																	${formatPrice(item.effectivePrice || item.price)}
																</Typography>
															</Box>
														</Box>
													</Box>
												)) : (
													<Typography variant="body2" color="text.secondary">
														Order details not available
													</Typography>
												)}
											</Grid>
											<Grid item xs={12} md={4} sx={{ textAlign: { md: "right" } }}>
												<Typography variant="h6" fontWeight="bold" color="primary">
													${formatPrice(purchase.total || 0)}
												</Typography>
												<Button
													size="small"
													variant="outlined"
													sx={{ mt: 1 }}
													onClick={() => navigate(createPageUrl("OrderConfirmation"), {
														state: {
															orderInfo: {
																total: purchase.total,
																paymentMethod: purchase.contactInfo || "Payment processed",
																items: purchase.items || [],
																shippingAddress: purchase.shippingAddress || "",
																orderDate: purchase.createdAt,
																orderNumber: `BGU-${String(purchase.id || 'UNKNOWN').padStart(6, '0')}`,
																isPastPurchase: true,
																subtotal: purchase.total,
																tax: 0, // Tax info not available in purchase history
																shipping: 0, // Shipping info not available in purchase history  
																totalSavings: 0
															}
														}
													})}
												>
													View Details
												</Button>
											</Grid>
										</Grid>
									</Paper>
								))}
							</Box>
						) : (
							<Box sx={{ textAlign: "center", py: 8 }}>
								<ShoppingBagIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
								<Typography variant="h6" mb={1}>No purchases yet</Typography>
								<Typography variant="body2" color="text.secondary" mb={3}>
									Your purchase history will appear here once you make your first order
								</Typography>
								<Button
									component={RouterLink}
									to={createPageUrl("Dashboard")}
									variant="contained"
								>
									Start Shopping
								</Button>
							</Box>
						)}
					</Box>
				)}

				{/* Admin Management Tab */}
				{activeTab === 3 && isAdmin && (
					<Box>
						<Tabs value={adminTabValue} onChange={(_, v) => setAdminTabValue(v)} sx={{ mb: 3 }}>
							<Tab label="Manage All Stores" />
							<Tab label="Manage All Users" />
							<Tab label={`Suspended Users (${suspendedUsers.length})`} />
						</Tabs>

						{/* Manage All Stores */}
						{adminTabValue === 0 && (
							<Box>
								<Typography variant="h6" mb={3}>All Stores</Typography>
								{adminLoading ? (
									<Box>
										{[1, 2, 3].map((i) => (
											<Skeleton key={i} variant="rectangular" height={100} sx={{ borderRadius: 2, mb: 2 }} />
										))}
									</Box>
								) : allStores.length > 0 ? (
									<TableContainer component={Paper} sx={{ borderRadius: 2 }}>
										<Table>
											<TableHead>
												<TableRow>
													<TableCell>Store Name</TableCell>
													<TableCell>Status</TableCell>
													<TableCell>Actions</TableCell>
												</TableRow>
											</TableHead>
											<TableBody>
												{allStores.map((store) => (
													<TableRow key={store.id}>
														<TableCell>{store.name}</TableCell>
														<TableCell>
															<Chip
																size="small"
																label={store.isActive ? "Active" : "Inactive"}
																color={store.isActive ? "success" : "default"}
															/>
														</TableCell>
														<TableCell>
															<Button
																size="small"
																color="error"
																disabled={!store.isActive}
																onClick={() => handleCloseStore(store.id, store.name)}
															>
																Close Store
															</Button>
														</TableCell>
													</TableRow>
												))}
											</TableBody>
										</Table>
									</TableContainer>
								) : (
									<Box sx={{ textAlign: "center", py: 8 }}>
										<StoreIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
										<Typography variant="h6" mb={1}>No stores found</Typography>
										<Typography variant="body2" color="text.secondary">
											There are no stores in the system yet
										</Typography>
									</Box>
								)}
							</Box>
						)}

						{/* Manage All Users */}
						{adminTabValue === 1 && (
							<Box>
								<Typography variant="h6" mb={3}>All Users</Typography>
								{adminLoading ? (
									<Box>
										{[1, 2, 3].map((i) => (
											<Skeleton key={i} variant="rectangular" height={100} sx={{ borderRadius: 2, mb: 2 }} />
										))}
									</Box>
								) : Object.values(allUsers).length > 0 ? (
									<TableContainer component={Paper} sx={{ borderRadius: 2 }}>
										<Table>
											<TableHead>
												<TableRow>
													<TableCell>Username</TableCell>
													<TableCell>Admin</TableCell>
													<TableCell>Status</TableCell>
													<TableCell>Roles</TableCell>
													<TableCell>Actions</TableCell>
												</TableRow>
											</TableHead>
											<TableBody>
												{Object.values(allUsers).map((user) => (
													<TableRow key={user.username}>
														<TableCell>{user.username}</TableCell>
														<TableCell>
															<Chip
																size="small"
																label={user.isAdmin ? "Admin" : "User"}
																color={user.isAdmin ? "primary" : "default"}
																variant={user.isAdmin ? "filled" : "outlined"}
															/>
														</TableCell>
														<TableCell>
															<Chip
																size="small"
																label={suspendedUsers.some(id => id.toLowerCase() === user.username.toLowerCase()) ? "Suspended" : "Active"}
																color={suspendedUsers.some(id => id.toLowerCase() === user.username.toLowerCase()) ? "error" : "success"}
															/>
														</TableCell>
														<TableCell>
															{user.roles && Object.keys(user.roles).length > 0
																? Object.entries(user.roles).map(([storeId, roles]) => `${roles.join(", ")} (Store ${storeId})`).join("; ")
																: "No roles"}
														</TableCell>
														<TableCell>
															{suspendedUsers.some(id => id.toLowerCase() === user.username.toLowerCase()) ? (
																<Button
																	size="small"
																	color="success"
																	disabled={user.isAdmin || user.username === currentUser.userName}
																	onClick={() => handleUnsuspendUser(user.username)}
																	sx={{ mr: 1 }}
																>
																	Unsuspend
																</Button>
															) : (
																<>
																	<Button
																		size="small"
																		color="warning"
																		disabled={user.isAdmin || user.username === currentUser.userName}
																		onClick={() => handleSuspendUser(user.username, false)}
																		sx={{ mr: 1 }}
																	>
																		Suspend
																	</Button>
																</>
															)}
														</TableCell>
													</TableRow>
												))}
											</TableBody>
										</Table>
									</TableContainer>
								) : (
									<Box sx={{ textAlign: "center", py: 8 }}>
										<PersonIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
										<Typography variant="h6" mb={1}>No users found</Typography>
										<Typography variant="body2" color="text.secondary">
											There are no users in the system yet
										</Typography>
									</Box>
								)}
							</Box>
						)}

						{/* Suspended Users */}
						{adminTabValue === 2 && (
							<Box>
								<Typography variant="h6" mb={3}>Suspended Users</Typography>
								{adminLoading ? (
									<Box>
										{[1, 2, 3].map((i) => (
											<Skeleton key={i} variant="rectangular" height={100} sx={{ borderRadius: 2, mb: 2 }} />
										))}
									</Box>
								) : suspendedUsers.length > 0 ? (
									<TableContainer component={Paper} sx={{ borderRadius: 2 }}>
										<Table>
											<TableHead>
												<TableRow>
													<TableCell>Username</TableCell>
													<TableCell>Admin</TableCell>
													<TableCell>Roles</TableCell>
													<TableCell>Actions</TableCell>
												</TableRow>
											</TableHead>
											<TableBody>
												{suspendedUsers.length > 0 && Object.values(allUsers).filter(user =>
													suspendedUsers.some(id => id.toLowerCase() === user.username.toLowerCase())
												).map((user) => (
													<TableRow key={user.username}>
														<TableCell>{user.username}</TableCell>
														<TableCell>
															<Chip
																size="small"
																label={user.isAdmin ? "Admin" : "User"}
																color={user.isAdmin ? "primary" : "default"}
																variant={user.isAdmin ? "filled" : "outlined"}
															/>
														</TableCell>
														<TableCell>
															{user.roles && Object.keys(user.roles).length > 0
																? Object.entries(user.roles).map(([storeId, roles]) => `${roles.join(", ")} (Store ${storeId})`).join("; ")
																: "No roles"}
														</TableCell>
														<TableCell>
															<Button
																size="small"
																color="success"
																disabled={user.isAdmin || user.username === currentUser.userName}
																onClick={() => handleUnsuspendUser(user.username)}
															>
																Unsuspend User
															</Button>
														</TableCell>
													</TableRow>
												))}
											</TableBody>
										</Table>
									</TableContainer>
								) : (
									<Box sx={{ textAlign: "center", py: 8 }}>
										<PersonIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
										<Typography variant="h6" mb={1}>No suspended users</Typography>
										<Typography variant="body2" color="text.secondary">
											There are no suspended users in the system at this time
										</Typography>
									</Box>
								)}
							</Box>
						)}
					</Box>
				)}
			</Container>

			{/* Only show auth dialog when explicitly triggered, not on refresh */}
			{authOpen && !isAuthenticated && <AuthDialog open={authOpen} onOpenChange={setAuthOpen} />}

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
		</Box>
	);
}