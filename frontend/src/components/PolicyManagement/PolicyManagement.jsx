import React, { useState, useEffect, useCallback } from 'react';
import {
	Box,
	Typography,
	Card,
	CardContent,
	Grid,
	Button,
	Chip,
	Dialog,
	DialogTitle,
	DialogContent,
	DialogActions,
	TextField,
	FormControl,
	InputLabel,
	Select,
	MenuItem,
	List,
	ListItem,
	ListItemText,
	ListItemSecondaryAction,
	IconButton,
	Accordion,
	AccordionSummary,
	AccordionDetails,
	Alert
} from '@mui/material';
import {
	Add as AddIcon,
	Delete as DeleteIcon,
	Policy as PolicyIcon,
	ExpandMore as ExpandMoreIcon,
	LocalOffer as DiscountIcon,
	ShoppingCart as PurchaseIcon
} from '@mui/icons-material';
import { policyService } from '../../services/policyService';

const PolicyManagement = ({ store, currentUser, onUpdate, onPolicyUpdate }) => {
	const [purchasePolicies, setPurchasePolicies] = useState([]);
	const [discountPolicies, setDiscountPolicies] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	// Dialog states
	const [purchasePolicyDialog, setPurchasePolicyDialog] = useState(false);
	const [discountPolicyDialog, setDiscountPolicyDialog] = useState(false);

	// Form states
	const [newPurchasePolicy, setNewPurchasePolicy] = useState({ type: '', value: '' });
	const [newDiscountPolicy, setNewDiscountPolicy] = useState({
		type: 'PERCENTAGE',
		scope: 'STORE',
		scopeId: '',
		value: ''
	});

	const loadPolicies = useCallback(async () => {
		if (!store || !currentUser) return;

		setLoading(true);
		try {
			const [purchasePoliciesData, discountPoliciesData] = await Promise.all([
				policyService.getPurchasePolicies(store.id, currentUser.userName),
				policyService.getDiscountPolicies(store.id, currentUser.userName)
			]);

			setPurchasePolicies(purchasePoliciesData);
			setDiscountPolicies(discountPoliciesData);
			setError('');
		} catch (error) {
			console.error('Error loading policies:', error);
			setError('Failed to load policies: ' + error.message);
		} finally {
			setLoading(false);
		}
	}, [store, currentUser]);

	useEffect(() => {
		loadPolicies();
	}, [loadPolicies]);

	const handleAddPurchasePolicy = async () => {
		try {
			const policyData = policyService.createPurchasePolicy(newPurchasePolicy.type, newPurchasePolicy.value);
			await policyService.addPurchasePolicy(store.id, currentUser.userName, policyData);

			setPurchasePolicyDialog(false);
			setNewPurchasePolicy({ type: '', value: '' });
			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Purchase policy added successfully', variant: 'success' });
			onPolicyUpdate?.(); // Trigger product price refresh
		} catch (error) {
			console.error('Error adding purchase policy:', error);
			onUpdate?.({ title: 'Error', description: error.message, variant: 'destructive' });
		}
	};

	const handleRemovePurchasePolicy = async (policy) => {
		try {
			console.log('Attempting to remove purchase policy:', policy);
			console.log('Store ID:', store.id, 'User:', currentUser.userName);

			await policyService.removePurchasePolicy(store.id, currentUser.userName, policy);

			console.log('Purchase policy removed successfully');
			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Purchase policy removed successfully', variant: 'success' });
			onPolicyUpdate?.(); // Trigger product price refresh
		} catch (error) {
			console.error('Error removing purchase policy:', error);
			console.error('Error details:', {
				message: error.message,
				response: error.response?.data,
				status: error.response?.status
			});
			onUpdate?.({ title: 'Error', description: error.message, variant: 'destructive' });
		}
	};

	const handleAddDiscountPolicy = async () => {
		try {
			// Use the value directly for percentage - backend handles the conversion
			let processedValue = parseFloat(newDiscountPolicy.value);

			const policyData = policyService.createBasicDiscountPolicy(
				newDiscountPolicy.type,
				newDiscountPolicy.scope,
				newDiscountPolicy.scopeId || store.id,
				processedValue
			);
			await policyService.addDiscountPolicy(store.id, currentUser.userName, policyData);

			setDiscountPolicyDialog(false);
			setNewDiscountPolicy({ type: 'PERCENTAGE', scope: 'STORE', scopeId: '', value: '' });
			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Discount policy added successfully', variant: 'success' });
			onPolicyUpdate?.(); // Trigger product price refresh
		} catch (error) {
			console.error('Error adding discount policy:', error);
			onUpdate?.({ title: 'Error', description: error.message, variant: 'destructive' });
		}
	};

	const handleRemoveDiscountPolicy = async (policy) => {
		try {
			console.log('Attempting to remove discount policy:', policy);
			console.log('Store ID:', store.id, 'User:', currentUser.userName);

			await policyService.removeDiscountPolicy(store.id, currentUser.userName, policy);

			console.log('Discount policy removed successfully');
			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Discount policy removed successfully', variant: 'success' });
			onPolicyUpdate?.(); // Trigger product price refresh
		} catch (error) {
			console.error('Error removing discount policy:', error);
			console.error('Error details:', {
				message: error.message,
				response: error.response?.data,
				status: error.response?.status
			});
			onUpdate?.({ title: 'Error', description: error.message, variant: 'destructive' });
		}
	};

	const formatPolicyText = (policy) => {
		if (policy.type === 'MINITEMS') return `Minimum ${policy.value} items required`;
		if (policy.type === 'MAXITEMS') return `Maximum ${policy.value} items allowed`;
		if (policy.type === 'MINPRICE') return `Minimum total price: $${policy.value}`;
		if (policy.type === 'DEFAULT') return 'Default purchase policy (no restrictions)';
		return `${policy.type}: ${policy.value}`;
	};

	const formatDiscountText = (discount) => {
		const valueText = discount.type === 'PERCENTAGE' ? `${discount.value}%` : `$${discount.value}`;
		const scopeText = discount.scope === 'STORE' ? 'store-wide' :
			discount.scope === 'PRODUCT' ? `product ${discount.scopeId}` :
				discount.scope === 'CATEGORY' ? `category ${discount.scopeId}` : discount.scope;
		return `${valueText} ${discount.type.toLowerCase()} discount on ${scopeText}`;
	};

	if (loading) {
		return (
			<Card sx={{ mb: 4 }}>
				<CardContent>
					<Typography variant="h6">Loading policies...</Typography>
				</CardContent>
			</Card>
		);
	}

	return (
		<Card sx={{ mb: 4 }}>
			<CardContent>
				<Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
					<PolicyIcon sx={{ mr: 2, color: 'primary.main' }} />
					<Typography variant="h5" fontWeight="bold">
						Store Policies
					</Typography>
				</Box>

				{error && (
					<Alert severity="error" sx={{ mb: 3 }}>
						{error}
					</Alert>
				)}

				<Grid container spacing={3}>
					{/* Purchase Policies */}
					<Grid item xs={12} md={6}>
						<Accordion defaultExpanded>
							<AccordionSummary expandIcon={<ExpandMoreIcon />}>
								<Box sx={{ display: 'flex', alignItems: 'center' }}>
									<PurchaseIcon sx={{ mr: 1, color: 'primary.main' }} />
									<Typography variant="h6">Purchase Policies</Typography>
									<Chip
										size="small"
										label={purchasePolicies.length}
										color="primary"
										sx={{ ml: 2 }}
									/>
								</Box>
							</AccordionSummary>
							<AccordionDetails>
								<Box sx={{ mb: 2 }}>
									<Button
										variant="contained"
										startIcon={<AddIcon />}
										onClick={() => setPurchasePolicyDialog(true)}
										size="small"
									>
										Add Purchase Policy
									</Button>
								</Box>

								{purchasePolicies.length > 0 ? (
									<List dense>
										{purchasePolicies.map((policy, index) => (
											<ListItem key={index} divider>
												<ListItemText
													primary={formatPolicyText(policy)}
													secondary={`Type: ${policy.type}`}
												/>
												{policy.type !== 'DEFAULT' && (
													<ListItemSecondaryAction>
														<IconButton
															edge="end"
															color="error"
															onClick={() => handleRemovePurchasePolicy(policy)}
														>
															<DeleteIcon />
														</IconButton>
													</ListItemSecondaryAction>
												)}
											</ListItem>
										))}
									</List>
								) : (
									<Typography variant="body2" color="text.secondary">
										No purchase policies defined
									</Typography>
								)}
							</AccordionDetails>
						</Accordion>
					</Grid>

					{/* Discount Policies */}
					<Grid item xs={12} md={6}>
						<Accordion defaultExpanded>
							<AccordionSummary expandIcon={<ExpandMoreIcon />}>
								<Box sx={{ display: 'flex', alignItems: 'center' }}>
									<DiscountIcon sx={{ mr: 1, color: 'secondary.main' }} />
									<Typography variant="h6">Discount Policies</Typography>
									<Chip
										size="small"
										label={discountPolicies.length}
										color="secondary"
										sx={{ ml: 2 }}
									/>
								</Box>
							</AccordionSummary>
							<AccordionDetails>
								<Box sx={{ mb: 2 }}>
									<Button
										variant="contained"
										color="secondary"
										startIcon={<AddIcon />}
										onClick={() => setDiscountPolicyDialog(true)}
										size="small"
									>
										Add Discount Policy
									</Button>
								</Box>

								{discountPolicies.length > 0 ? (
									<List dense>
										{discountPolicies.map((discount, index) => (
											<ListItem key={index} divider>
												<ListItemText
													primary={formatDiscountText(discount)}
													secondary={`Type: ${discount.type}, Scope: ${discount.scope}`}
												/>
												<ListItemSecondaryAction>
													<IconButton
														edge="end"
														color="error"
														onClick={() => handleRemoveDiscountPolicy(discount)}
													>
														<DeleteIcon />
													</IconButton>
												</ListItemSecondaryAction>
											</ListItem>
										))}
									</List>
								) : (
									<Typography variant="body2" color="text.secondary">
										No discount policies defined
									</Typography>
								)}
							</AccordionDetails>
						</Accordion>
					</Grid>
				</Grid>
			</CardContent>

			{/* Add Purchase Policy Dialog */}
			<Dialog open={purchasePolicyDialog} onClose={() => setPurchasePolicyDialog(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Add Purchase Policy</DialogTitle>
				<DialogContent>
					<Box sx={{ pt: 1 }}>
						<FormControl fullWidth sx={{ mb: 3 }}>
							<InputLabel>Policy Type</InputLabel>
							<Select
								value={newPurchasePolicy.type}
								onChange={(e) => setNewPurchasePolicy(prev => ({ ...prev, type: e.target.value }))}
								label="Policy Type"
							>
								<MenuItem value="MINITEMS">Minimum Items</MenuItem>
								<MenuItem value="MAXITEMS">Maximum Items</MenuItem>
								<MenuItem value="MINPRICE">Minimum Price</MenuItem>
							</Select>
						</FormControl>

						<TextField
							fullWidth
							label="Value"
							type="number"
							value={newPurchasePolicy.value}
							onChange={(e) => setNewPurchasePolicy(prev => ({ ...prev, value: e.target.value }))}
							helperText={
								newPurchasePolicy.type === 'MINPRICE' ? 'Minimum total purchase amount in dollars' :
									newPurchasePolicy.type ? 'Number of items' : 'Select a policy type first'
							}
						/>
					</Box>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setPurchasePolicyDialog(false)}>Cancel</Button>
					<Button
						onClick={handleAddPurchasePolicy}
						variant="contained"
						disabled={!newPurchasePolicy.type || !newPurchasePolicy.value}
					>
						Add Policy
					</Button>
				</DialogActions>
			</Dialog>

			{/* Add Discount Policy Dialog */}
			<Dialog open={discountPolicyDialog} onClose={() => setDiscountPolicyDialog(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Add Discount Policy</DialogTitle>
				<DialogContent>
					<Box sx={{ pt: 1 }}>
						<FormControl fullWidth sx={{ mb: 3 }}>
							<InputLabel>Discount Type</InputLabel>
							<Select
								value={newDiscountPolicy.type}
								onChange={(e) => setNewDiscountPolicy(prev => ({ ...prev, type: e.target.value }))}
								label="Discount Type"
							>
								<MenuItem value="PERCENTAGE">Percentage</MenuItem>
								<MenuItem value="FIXED">Fixed Amount</MenuItem>
							</Select>
						</FormControl>

						<FormControl fullWidth sx={{ mb: 3 }}>
							<InputLabel>Scope</InputLabel>
							<Select
								value={newDiscountPolicy.scope}
								onChange={(e) => setNewDiscountPolicy(prev => ({ ...prev, scope: e.target.value }))}
								label="Scope"
							>
								<MenuItem value="STORE">Store-wide</MenuItem>
								<MenuItem value="PRODUCT">Specific Product</MenuItem>
								<MenuItem value="CATEGORY">Product Category</MenuItem>
							</Select>
						</FormControl>

						{(newDiscountPolicy.scope === 'PRODUCT' || newDiscountPolicy.scope === 'CATEGORY') && (
							<TextField
								fullWidth
								label={newDiscountPolicy.scope === 'PRODUCT' ? 'Product ID' : 'Category Name'}
								value={newDiscountPolicy.scopeId}
								onChange={(e) => setNewDiscountPolicy(prev => ({ ...prev, scopeId: e.target.value }))}
								sx={{ mb: 3 }}
								helperText={
									newDiscountPolicy.scope === 'PRODUCT' ? 'Enter the specific product ID' :
										'Enter the category name (e.g., Electronics, Clothing)'
								}
							/>
						)}

						<TextField
							fullWidth
							label={newDiscountPolicy.type === 'PERCENTAGE' ? 'Percentage' : 'Discount Amount'}
							type="number"
							value={newDiscountPolicy.value}
							onChange={(e) => setNewDiscountPolicy(prev => ({ ...prev, value: e.target.value }))}
							helperText={
								newDiscountPolicy.type === 'PERCENTAGE' ? 'Enter percentage (e.g., 15 for 15%)' :
									'Enter discount amount in dollars'
							}
							inputProps={
								newDiscountPolicy.type === 'PERCENTAGE' ? { min: 0, max: 100, step: 1 } :
									{ min: 0, step: 0.01 }
							}
						/>
					</Box>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setDiscountPolicyDialog(false)}>Cancel</Button>
					<Button
						onClick={handleAddDiscountPolicy}
						variant="contained"
						disabled={
							!newDiscountPolicy.type || !newDiscountPolicy.value ||
							((newDiscountPolicy.scope === 'PRODUCT' || newDiscountPolicy.scope === 'CATEGORY') && !newDiscountPolicy.scopeId)
						}
					>
						Add Discount
					</Button>
				</DialogActions>
			</Dialog>
		</Card>
	);
};

export default PolicyManagement; 