import React, { useState, useEffect, useCallback } from "react";
import { Link as RouterLink, useNavigate } from "react-router-dom";
import { createPageUrl } from "../../utils";
import userService from "../../services/userService";
import { storeService } from "../../services/storeService";
import { productService } from "../../services/productService";
import purchaseService from "../../services/purchaseService";
import { useAuth } from "../../contexts/AuthContext";
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
import EditIcon from "@mui/icons-material/Edit";
import SaveIcon from "@mui/icons-material/Save";
import CancelIcon from "@mui/icons-material/Cancel";
import PersonIcon from "@mui/icons-material/Person";
import StoreIcon from "@mui/icons-material/Store";
import ShoppingBagIcon from "@mui/icons-material/ShoppingBag";
import AddIcon from "@mui/icons-material/Add";
import VisibilityIcon from "@mui/icons-material/Visibility";
import InventoryIcon from "@mui/icons-material/Inventory";
import Header from "../../components/Header/Header";
import AuthDialog from "../../components/AuthDialog/AuthDialog";
import './Profile.css';

export default function Profile() {
	const { currentUser } = useAuth();
	const navigate = useNavigate();
	const [activeTab, setActiveTab] = useState(0);
	const [loading, setLoading] = useState(true);
	const [userProfile, setUserProfile] = useState(null);
	const [userStores, setUserStores] = useState([]);
	const [purchaseHistory, setPurchaseHistory] = useState([]);
	const [isEditing, setIsEditing] = useState(false);
	const [editedProfile, setEditedProfile] = useState({});
	const [authOpen, setAuthOpen] = useState(false);
	const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });

	// Stable toast function that doesn't cause re-renders
	const toast = useCallback(({ title, description, variant }) => {
		setSnackbar({
			open: true,
			message: description || title,
			severity: variant === "destructive" ? "error" : "success"
		});
	}, []);

	const loadUserManagedStores = useCallback(async () => {
		try {
			console.log('Loading user managed stores for:', currentUser.userName);
			// Get all stores first
			const { stores } = await storeService.getAllStoresAndProducts();
			console.log('Retrieved stores:', stores);

			// Check which stores the user owns or manages
			const managedStores = [];
			for (const store of stores) {
				try {
					console.log(`Checking ownership/management for store ${store.id} (${store.name})`);
					// Check if user is owner
					const isOwner = await storeService.isOwner(store.id, currentUser.userName);
					console.log(`Is owner of ${store.name}:`, isOwner);
					if (isOwner) {
						managedStores.push({
							...store,
							role: 'Owner'
						});
						continue;
					}

					// Check if user is manager
					const isManager = await storeService.isManager(store.id, currentUser.userName);
					console.log(`Is manager of ${store.name}:`, isManager);
					if (isManager) {
						managedStores.push({
							...store,
							role: 'Manager'
						});
					}
				} catch (error) {
					console.warn(`Failed to check role for store ${store.id}:`, error);
				}
			}

			console.log('Final managed stores:', managedStores);
			setUserStores(managedStores);
		} catch (error) {
			console.error("Error loading managed stores:", error);
			setUserStores([]);
		}
	}, [currentUser]);

	const loadPurchaseHistory = useCallback(async () => {
		try {
			console.log('Loading purchase history for user:', currentUser.userName);
			// The backend endpoint extracts username from token, so we just need to call with any userId
			// The actual userId parameter is ignored by the backend
			const history = await purchaseService.getPurchaseHistory(currentUser.id || currentUser.userName || 1);
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
							return {
								title: productDetails?.productName || productDetails?.title || `Product ${product.productId}`,
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
			console.log("No purchase history found or service unavailable:", error);
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

	useEffect(() => {
		if (!currentUser) {
			setAuthOpen(true);
			return;
		}
		loadProfileData();
	}, [currentUser, loadProfileData]);

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
				founderId: currentUser.userName,
				description: "New store"
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
				{authOpen && <AuthDialog open={authOpen} onClose={() => setAuthOpen(false)} />}
			</Box>
		);
	}

	return (
		<Box sx={{ minHeight: "100vh", bgcolor: "background.default" }}>
			<Header />
			<Container maxWidth="lg" sx={{ py: 6 }}>
				<Typography variant="h4" fontWeight={700} mb={3}>My Profile</Typography>

				<Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)} sx={{ mb: 3 }}>
					<Tab
						icon={<PersonIcon />}
						label="Personal Details"
						iconPosition="start"
					/>
					<Tab
						icon={<StoreIcon />}
						label={`My Stores (${userStores.length})`}
						iconPosition="start"
					/>
					<Tab
						icon={<ShoppingBagIcon />}
						label={`Purchase History (${purchaseHistory.length})`}
						iconPosition="start"
					/>
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
						) : userStores.length > 0 ? (
							<Grid container spacing={3}>
								{userStores.map((store) => (
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
														label={store.role || "Member"}
														color={store.role === "Owner" ? "primary" : "default"}
														variant="outlined"
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
													startIcon={<VisibilityIcon />}
													onClick={() => navigate(`/store/${store.name}`)}
												>
													View Store
												</Button>
												<Button
													size="small"
													startIcon={<InventoryIcon />}
													onClick={() => navigate(createPageUrl("Dashboard"))}
												>
													Manage
												</Button>
											</CardActions>
										</Card>
									</Grid>
								))}
							</Grid>
						) : (
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
						) : purchaseHistory.length > 0 ? (
							<Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
								{purchaseHistory.map((purchase, index) => (
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
											<Chip
												label={purchase.status || "Completed"}
												color="success"
												size="small"
											/>
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
														<Box>
															<Typography variant="body2" fontWeight="medium">
																{item.title}
															</Typography>
															<Typography variant="caption" color="text.secondary">
																Qty: {item.quantity} × ${item.price}
															</Typography>
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
													${purchase.total || "0.00"}
												</Typography>
												<Button
													size="small"
													variant="outlined"
													sx={{ mt: 1 }}
													onClick={() => navigate(createPageUrl("OrderConfirmation"))}
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
		</Box>
	);
} 