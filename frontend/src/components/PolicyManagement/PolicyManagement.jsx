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
	Alert,
	Stepper,
	Step,
	StepLabel,
	StepContent,
	Divider,
	Radio,
	RadioGroup,
	FormControlLabel,
	FormLabel,
	Stack,
	Tooltip,
	Paper
} from '@mui/material';
import {
	Add as AddIcon,
	Delete as DeleteIcon,
	Policy as PolicyIcon,
	ExpandMore as ExpandMoreIcon,
	LocalOffer as DiscountIcon,
	ShoppingCart as PurchaseIcon,
	RuleFolder as ConditionIcon,
	MergeType as CompositeIcon,
	Percent as PercentIcon,
	AttachMoney as FixedIcon,
	Info as InfoIcon
} from '@mui/icons-material';
import { policyService } from '../../services/policyService';
import SubDiscountCreator from './SubDiscountCreator';

const PolicyManagement = ({ store, currentUser, onUpdate, onPolicyUpdate }) => {
	const [purchasePolicies, setPurchasePolicies] = useState([]);
	const [discountPolicies, setDiscountPolicies] = useState([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	// Dialog states
	const [purchasePolicyDialog, setPurchasePolicyDialog] = useState(false);
	const [discountPolicyDialog, setDiscountPolicyDialog] = useState(false);

	// Discount dialog state management
	const [discountType, setDiscountType] = useState('BASIC'); // BASIC, CONDITIONAL, COMPOSITE
	const [activeStep, setActiveStep] = useState(0);

	// Form states
	const [newPurchasePolicy, setNewPurchasePolicy] = useState({ type: '', value: '' });

	// For backward compatibility
	const [newDiscountPolicy, setNewDiscountPolicy] = useState({
		type: 'PERCENTAGE',
		scope: 'STORE',
		scopeId: '',
		value: ''
	});

	// Basic discount state
	const [basicDiscount, setBasicDiscount] = useState({
		type: 'PERCENTAGE',
		scope: 'STORE',
		scopeId: '',
		value: ''
	});

	// Conditional discount state
	const [conditionalDiscount, setConditionalDiscount] = useState({
		baseDiscount: null,
		conditionType: 'BASKET_TOTAL_AT_LEAST',
		conditionValue: '',
		conditionTarget: 'STORE'
	});

	// Enhanced Composite discount state
	const [compositeDiscount, setCompositeDiscount] = useState({
		subDiscounts: [],
		combinationType: 'SUM', // SUM or MAXIMUM
		showSubDiscountCreator: false
	});

	// Temporary discount for composites
	const [tempDiscount, setTempDiscount] = useState(null);
	const [tempDiscountType, setTempDiscountType] = useState('BASIC');

	const loadPolicies = useCallback(async () => {
		if (!store?.id || !currentUser || !currentUser.userName) {
			console.warn('Missing required data for loading policies:', {
				storeId: store?.id,
				currentUser: !!currentUser,
				userName: currentUser?.userName
			});
			return;
		}

		setLoading(true);
		try {
			// Load purchase policies
			const purchasePoliciesData = await policyService.getPurchasePolicies(store.id, currentUser.userName);
			setPurchasePolicies(purchasePoliciesData);

			// Load discount policies
			const discountPoliciesData = await policyService.getDiscountPolicies(store.id, currentUser.userName);
			setDiscountPolicies(discountPoliciesData);

		} catch (error) {
			console.error('Error loading policies:', error);
			setError('Failed to load policies: ' + error.message);
		} finally {
			setLoading(false);
		}
	}, [store?.id, currentUser]);

	useEffect(() => {
		loadPolicies();
	}, [loadPolicies]);

	const handleAddPurchasePolicy = async () => {
		if (!currentUser?.userName) {
			onUpdate?.({ title: 'Error', description: 'User authentication required', variant: 'destructive' });
			return;
		}

		try {
			await policyService.addPurchasePolicy(store.id, currentUser.userName, newPurchasePolicy);
			setPurchasePolicyDialog(false);
			setNewPurchasePolicy({ type: '', value: '' });
			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Purchase policy added successfully', variant: 'success' });
		} catch (error) {
			console.error('Error adding purchase policy:', error);
			onUpdate?.({ title: 'Error', description: error.message, variant: 'destructive' });
		}
	};

	const handleRemovePurchasePolicy = async (policy) => {
		if (!currentUser?.userName) {
			onUpdate?.({ title: 'Error', description: 'User authentication required', variant: 'destructive' });
			return;
		}

		try {
			await policyService.removePurchasePolicy(store.id, currentUser.userName, policy);
			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Purchase policy removed successfully', variant: 'success' });
		} catch (error) {
			console.error('Error removing purchase policy:', error);
			onUpdate?.({ title: 'Error', description: error.message, variant: 'destructive' });
		}
	};

	const handleAddSubDiscount = (subDiscount) => {
		setCompositeDiscount(prev => ({
			...prev,
			subDiscounts: [...prev.subDiscounts, subDiscount]
		}));
	};

	const handleRemoveSubDiscount = (index) => {
		setCompositeDiscount(prev => ({
			...prev,
			subDiscounts: prev.subDiscounts.filter((_, i) => i !== index)
		}));
	};

	const handleToggleSubDiscountCreator = () => {
		setCompositeDiscount(prev => ({
			...prev,
			showSubDiscountCreator: !prev.showSubDiscountCreator
		}));
	};

	// Enhanced discount policy addition
	const handleAddDiscountPolicyByType = async () => {
		if (!currentUser?.userName) {
			onUpdate?.({ title: 'Error', description: 'User authentication required', variant: 'destructive' });
			return;
		}

		try {
			let policyData;

			// Create the appropriate discount policy based on type
			switch (discountType) {
				case 'BASIC':
					// Basic percentage/fixed discount
					const processedValue = parseFloat(basicDiscount.value);
					policyData = policyService.createBasicDiscountPolicy(
						basicDiscount.type,
						basicDiscount.scope,
						basicDiscount.scopeId || store.id,
						processedValue
					);
					break;

				case 'CONDITIONAL':
					// First create the base discount
					if (!conditionalDiscount.baseDiscount) {
						throw new Error('Base discount is required for conditional discount');
					}

					// Create the conditional discount with the condition
					policyData = policyService.createConditionalDiscountPolicy(
						conditionalDiscount.baseDiscount,
						conditionalDiscount.conditionType,
						parseFloat(conditionalDiscount.conditionValue),
						conditionalDiscount.conditionTarget
					);
					break;

				case 'COMPOSITE':
					// Validate that we have sub-discounts
					if (compositeDiscount.subDiscounts.length < 2) {
						throw new Error('Composite discount requires at least 2 sub-discounts');
					}

					// Validate each sub-discount
					compositeDiscount.subDiscounts.forEach((subDiscount, index) => {
						try {
							policyService.validateDiscountPolicy(subDiscount);
						} catch (error) {
							throw new Error(`Sub-discount ${index + 1}: ${error.message}`);
						}
					});

					console.log('Creating composite discount with sub-discounts:', compositeDiscount.subDiscounts);

					// Create the enhanced composite discount
					policyData = policyService.createEnhancedCompositeDiscount(
						compositeDiscount.subDiscounts,
						compositeDiscount.combinationType,
					);

					console.log('Generated composite policy data:', JSON.stringify(policyData, null, 2));
					break;

				default:
					throw new Error('Invalid discount type selected');
			}

			// Add the policy to the store
			await policyService.addDiscountPolicy(store.id, currentUser.userName, policyData);

			// Success - reset form and reload policies
			setDiscountPolicyDialog(false);
			setDiscountType('BASIC');
			setActiveStep(0);
			setBasicDiscount({ type: 'PERCENTAGE', scope: 'STORE', scopeId: '', value: '' });
			setConditionalDiscount({
				baseDiscount: null,
				conditionType: 'BASKET_TOTAL_AT_LEAST',
				conditionValue: '',
				conditionTarget: 'STORE'
			});
			setCompositeDiscount({
				subDiscounts: [],
				combinationType: 'SUM',
				showSubDiscountCreator: false
			});

			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Discount policy added successfully', variant: 'success' });
			onPolicyUpdate?.(); // Trigger product price refresh
		} catch (error) {
			console.error('Error adding discount policy:', error);

			// Provide more specific error messages
			let errorMessage = error.message;
			let errorTitle = 'Error';

			if (error.message.includes('Policy already exists') || error.message.includes('already exists')) {
				errorTitle = 'Duplicate Policy';
				errorMessage = 'A similar discount policy already exists for this store. Please remove the existing policy first or modify your discount parameters.';
			} else if (error.message.includes('Failed to add discount: Policy already exists')) {
				errorTitle = 'Duplicate Policy';
				errorMessage = 'This exact discount policy already exists. Please check the existing policies below or create a different discount.';
			} else if (error.message.includes('Sub-discount')) {
				errorTitle = 'Invalid Sub-Discount';
				errorMessage = `Composite discount creation failed: ${error.message}`;
			} else if (error.message.includes('Composite discount requires')) {
				errorTitle = 'Insufficient Sub-Discounts';
				errorMessage = error.message + '. Please add at least 2 sub-discounts before creating the composite discount.';
			} else if (error.message.includes('condition') || error.message.includes('Conditional')) {
				errorTitle = 'Condition Error';
				errorMessage = `Conditional discount error: ${error.message}`;
			}

			onUpdate?.({
				title: errorTitle,
				description: errorMessage,
				variant: 'destructive'
			});
		}
	};

	// Enhanced format discount text function
	const formatDiscountText = (discount) => {
		// Handle different discount types
		if (discount.policyType === 'CONDITIONAL') {
			// Extract condition info
			let conditionText = '';
			if (discount.condition) {
				switch (discount.condition.type) {
					case 'BASKET_TOTAL_AT_LEAST':
						conditionText = `minimum purchase of $${discount.condition.params?.minTotal || discount.condition.value}`;
						break;
					case 'PRODUCT_QUANTITY_AT_LEAST':
						conditionText = `minimum ${discount.condition.params?.minQuantity || discount.condition.value} of product ${discount.condition.params?.productId || discount.condition.targetId}`;
						break;
					case 'PRODUCT_CATEGORY_CONTAINS':
						conditionText = `minimum ${discount.condition.params?.minQuantity || discount.condition.value} items from ${discount.condition.params?.category || discount.condition.targetId}`;
						break;
					default:
						conditionText = `${discount.condition.type}: ${discount.condition.value || JSON.stringify(discount.condition.params)}`;
				}
			}

			// Format the base discount part
			const baseDiscountText = discount.baseDiscount ? formatDiscountText(discount.baseDiscount) :
				`${discount.value}${discount.type === 'PERCENTAGE' ? '%' : '$'} discount`;
			return `${baseDiscountText} if ${conditionText}`;
		} else if (discount.policyType === 'COMPOSITE' || discount.type === 'COMPOSITE') {
			// Enhanced composite discount formatting
			const subCount = discount.subDiscounts?.length || 0;
			const combType = discount.combinationType === 'SUM' ? 'Sum' : 'Maximum';

			if (subCount === 0) {
				return `${combType} of composite discounts (empty)`;
			} else if (subCount <= 2) {
				// Show details for small composites
				const subTexts = discount.subDiscounts?.map(sub => {
					const value = sub.type === 'PERCENTAGE' ? `${sub.value}%` : `$${sub.value}`;
					return `${value} ${sub.scope?.toLowerCase() || 'discount'}`;
				}).join(' and ') || 'multiple discounts';
				return `${combType} of (${subTexts})`;
			} else {
				// Summarize for large composites
				return `${combType} of ${subCount} sub-discounts`;
			}
		} else {
			// Handle standard percentage or fixed discount
			const valueText = discount.type === 'PERCENTAGE' ? `${discount.value}%` : `$${discount.value}`;
			const scopeText = discount.scope === 'STORE' ? 'store-wide' :
				discount.scope === 'PRODUCT' ? `product ${discount.scopeId}` :
					discount.scope === 'CATEGORY' ? `category ${discount.scopeId}` : discount.scope;
			return `${valueText} ${discount.type?.toLowerCase() || 'discount'} on ${scopeText}`;
		}
	};

	// Keep backward compatibility with the old function
	const handleAddDiscountPolicy = async () => {
		try {
			// Use the value directly for percentage - backend handles the conversion
			let processedValue = parseFloat(basicDiscount.value);

			const policyData = policyService.createBasicDiscountPolicy(
				basicDiscount.type,
				basicDiscount.scope,
				basicDiscount.scopeId || store.id,
				processedValue
			);
			await policyService.addDiscountPolicy(store.id, currentUser.userName, policyData);

			setDiscountPolicyDialog(false);
			setBasicDiscount({ type: 'PERCENTAGE', scope: 'STORE', scopeId: '', value: '' });
			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Discount policy added successfully', variant: 'success' });
			onPolicyUpdate?.(); // Trigger product price refresh
		} catch (error) {
			console.error('Error adding discount policy:', error);
			onUpdate?.({ title: 'Error', description: error.message, variant: 'destructive' });
		}
	};

	const handleRemoveDiscountPolicy = async (policy) => {
		if (!currentUser?.userName) {
			onUpdate?.({ title: 'Error', description: 'User authentication required', variant: 'destructive' });
			return;
		}

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

			{/* Enhanced Add Discount Policy Dialog */}
			<Dialog
				open={discountPolicyDialog}
				onClose={() => setDiscountPolicyDialog(false)}
				maxWidth="md"
				fullWidth
				PaperProps={{ sx: { minHeight: '600px' } }}
			>
				<DialogTitle>
					<Box sx={{ display: 'flex', alignItems: 'center' }}>
						<DiscountIcon sx={{ mr: 1 }} />
						Add Discount Policy
					</Box>
				</DialogTitle>
				<DialogContent>
					<Box sx={{ pt: 1 }}>
						{/* Discount Type Selector */}
						<FormControl fullWidth sx={{ mb: 3 }}>
							<InputLabel>Discount Type</InputLabel>
							<Select
								value={discountType}
								onChange={(e) => {
									setDiscountType(e.target.value);
									setActiveStep(0);
									// Reset states when changing type
									if (e.target.value === 'COMPOSITE') {
										setCompositeDiscount(prev => ({ ...prev, showSubDiscountCreator: false }));
									}
								}}
								label="Discount Type"
							>
								<MenuItem value="BASIC">
									<Box sx={{ display: 'flex', alignItems: 'center' }}>
										<PercentIcon sx={{ mr: 1 }} />
										<Box>
											<Typography variant="body2">Basic Discount</Typography>
											<Typography variant="caption" color="text.secondary">
												Simple percentage or fixed amount discount
											</Typography>
										</Box>
									</Box>
								</MenuItem>
								<MenuItem value="CONDITIONAL">
									<Box sx={{ display: 'flex', alignItems: 'center' }}>
										<ConditionIcon sx={{ mr: 1 }} />
										<Box>
											<Typography variant="body2">Conditional Discount</Typography>
											<Typography variant="caption" color="text.secondary">
												Discount that applies when conditions are met
											</Typography>
										</Box>
									</Box>
								</MenuItem>
								<MenuItem value="COMPOSITE">
									<Box sx={{ display: 'flex', alignItems: 'center' }}>
										<CompositeIcon sx={{ mr: 1 }} />
										<Box>
											<Typography variant="body2">Composite Discount</Typography>
											<Typography variant="caption" color="text.secondary">
												Combine multiple discounts with sum or maximum logic
											</Typography>
										</Box>
									</Box>
								</MenuItem>
							</Select>
						</FormControl>

						{/* Progress indicator for multi-step processes */}
						{(discountType === 'CONDITIONAL' || discountType === 'COMPOSITE') && (
							<Box sx={{ mb: 3 }}>
								{discountType === 'CONDITIONAL' ? (
									<Stepper activeStep={activeStep} sx={{ mb: 3 }}>
										<Step>
											<StepLabel>Base Discount</StepLabel>
										</Step>
										<Step>
											<StepLabel>Condition</StepLabel>
										</Step>
									</Stepper>
								) : (
									<Box sx={{ mb: 2 }}>
										<Typography variant="subtitle2" color="primary">
											Composite Discount Builder
										</Typography>
										<Typography variant="body2" color="text.secondary">
											Add multiple sub-discounts and choose how to combine them
										</Typography>
									</Box>
								)}
							</Box>
						)}

						{/* Basic Discount Form */}
						{(discountType === 'BASIC' || (discountType === 'CONDITIONAL' && activeStep === 0)) && (
							<Paper sx={{ p: 3, mb: 3, bgcolor: 'grey.50' }}>
								<Typography variant="subtitle2" gutterBottom>
									{discountType === 'BASIC' ? 'Configure Discount' : 'Configure Base Discount'}
								</Typography>

								<FormControl fullWidth sx={{ mb: 3 }}>
									<InputLabel>Discount Type</InputLabel>
									<Select
										value={discountType === 'BASIC' ? basicDiscount.type : tempDiscountType}
										onChange={(e) => {
											if (discountType === 'BASIC') {
												setBasicDiscount(prev => ({ ...prev, type: e.target.value }));
											} else {
												setTempDiscountType(e.target.value);
											}
										}}
										label="Discount Type"
									>
										<MenuItem value="PERCENTAGE">
											<Box sx={{ display: 'flex', alignItems: 'center' }}>
												<PercentIcon sx={{ mr: 1 }} />
												Percentage Discount
											</Box>
										</MenuItem>
										<MenuItem value="FIXED">
											<Box sx={{ display: 'flex', alignItems: 'center' }}>
												<FixedIcon sx={{ mr: 1 }} />
												Fixed Amount Discount
											</Box>
										</MenuItem>
									</Select>
								</FormControl>

								<FormControl fullWidth sx={{ mb: 3 }}>
									<InputLabel>Scope</InputLabel>
									<Select
										value={discountType === 'BASIC' ? basicDiscount.scope : tempDiscount?.scope || 'STORE'}
										onChange={(e) => {
											if (discountType === 'BASIC') {
												setBasicDiscount(prev => ({ ...prev, scope: e.target.value }));
											} else if (tempDiscount) {
												setTempDiscount(prev => ({ ...prev, scope: e.target.value }));
											}
										}}
										label="Scope"
									>
										<MenuItem value="STORE">Store-wide</MenuItem>
										<MenuItem value="PRODUCT">Specific Product</MenuItem>
										<MenuItem value="CATEGORY">Product Category</MenuItem>
									</Select>
								</FormControl>

								{((discountType === 'BASIC' && (basicDiscount.scope === 'PRODUCT' || basicDiscount.scope === 'CATEGORY')) ||
									(discountType === 'CONDITIONAL' && tempDiscount && (tempDiscount.scope === 'PRODUCT' || tempDiscount.scope === 'CATEGORY'))) && (
										<TextField
											fullWidth
											label={discountType === 'BASIC' ?
												(basicDiscount.scope === 'PRODUCT' ? 'Product ID' : 'Category Name') :
												(tempDiscount.scope === 'PRODUCT' ? 'Product ID' : 'Category Name')}
											value={discountType === 'BASIC' ? basicDiscount.scopeId : tempDiscount?.scopeId || ''}
											onChange={(e) => {
												if (discountType === 'BASIC') {
													setBasicDiscount(prev => ({ ...prev, scopeId: e.target.value }));
												} else if (tempDiscount) {
													setTempDiscount(prev => ({ ...prev, scopeId: e.target.value }));
												}
											}}
											sx={{ mb: 3 }}
											helperText={discountType === 'BASIC' ?
												(basicDiscount.scope === 'PRODUCT' ? 'Enter the specific product ID' : 'Enter the category name') :
												(tempDiscount?.scope === 'PRODUCT' ? 'Enter the specific product ID' : 'Enter the category name')}
										/>
									)}

								<TextField
									fullWidth
									label={(discountType === 'BASIC' ? basicDiscount.type : tempDiscountType) === 'PERCENTAGE' ? 'Percentage' : 'Discount Amount'}
									type="number"
									value={discountType === 'BASIC' ? basicDiscount.value : tempDiscount?.value || ''}
									onChange={(e) => {
										if (discountType === 'BASIC') {
											setBasicDiscount(prev => ({ ...prev, value: e.target.value }));
										} else if (tempDiscount) {
											setTempDiscount(prev => ({ ...prev, value: e.target.value }));
										} else {
											// Create a new temp discount
											setTempDiscount({
												type: tempDiscountType,
												scope: 'STORE',
												scopeId: '',
												value: e.target.value
											});
										}
									}}
									helperText={(discountType === 'BASIC' ? basicDiscount.type : tempDiscountType) === 'PERCENTAGE' ?
										'Enter percentage (e.g., 15 for 15%)' : 'Enter discount amount in dollars'}
									inputProps={(discountType === 'BASIC' ? basicDiscount.type : tempDiscountType) === 'PERCENTAGE' ?
										{ min: 0, max: 100, step: 1 } :
										{ min: 0, step: 0.01 }
									}
								/>

								{discountType === 'CONDITIONAL' && activeStep === 0 && (
									<Box sx={{ mt: 2 }}>
										<Button
											variant="contained"
											disabled={!tempDiscount || !tempDiscount.value}
											onClick={() => {
												const baseDto = policyService.createBasicDiscountPolicy(
													tempDiscount.type || 'PERCENTAGE',
													tempDiscount.scope || 'STORE',
													tempDiscount.scopeId || null,
													parseFloat(tempDiscount.value)
												);
												setConditionalDiscount(prev => ({ ...prev, baseDiscount: baseDto }));
												setActiveStep(1);
											}}
										>
											Next: Add Condition
										</Button>
									</Box>
								)}
							</Paper>
						)}

						{/* Condition Form for Conditional Discounts */}
						{discountType === 'CONDITIONAL' && activeStep === 1 && (
							<Paper sx={{ p: 3, mb: 3, bgcolor: 'warning.light', bgcolor: 'rgba(255, 243, 224, 0.5)' }}>
								<Typography variant="subtitle2" gutterBottom>
									Configure Discount Condition
								</Typography>

								<FormControl fullWidth sx={{ mb: 3 }}>
									<InputLabel>Condition Type</InputLabel>
									<Select
										value={conditionalDiscount.conditionType}
										onChange={(e) => setConditionalDiscount(prev => ({ ...prev, conditionType: e.target.value }))}
										label="Condition Type"
									>
										<MenuItem value="BASKET_TOTAL_AT_LEAST">Basket total at least ($)</MenuItem>
										<MenuItem value="PRODUCT_QUANTITY_AT_LEAST">Product quantity at least</MenuItem>
										<MenuItem value="CATEGORY_QUANTITY_AT_LEAST">Category quantity at least</MenuItem>
									</Select>
								</FormControl>

								<TextField
									fullWidth
									label={conditionalDiscount.conditionType === 'BASKET_TOTAL_AT_LEAST' ? 'Minimum Basket Total ($)' : 'Minimum Quantity'}
									type="number"
									value={conditionalDiscount.conditionValue}
									onChange={(e) => setConditionalDiscount(prev => ({ ...prev, conditionValue: e.target.value }))}
									sx={{ mb: 3 }}
								/>

								{(conditionalDiscount.conditionType === 'PRODUCT_QUANTITY_AT_LEAST' || conditionalDiscount.conditionType === 'CATEGORY_QUANTITY_AT_LEAST') && (
									<TextField
										fullWidth
										label={conditionalDiscount.conditionType === 'PRODUCT_QUANTITY_AT_LEAST' ? 'Product ID' : 'Category'}
										value={conditionalDiscount.conditionTarget}
										onChange={(e) => setConditionalDiscount(prev => ({ ...prev, conditionTarget: e.target.value }))}
										sx={{ mb: 3 }}
									/>
								)}

								<Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
									<Button variant="outlined" onClick={() => setActiveStep(0)}>Back</Button>
								</Box>
							</Paper>
						)}

						{/* Enhanced Composite Discount Form */}
						{discountType === 'COMPOSITE' && (
							<Box>
								{/* Combination Type Selector */}
								<Paper sx={{ p: 3, mb: 3, bgcolor: 'primary.light', bgcolor: 'rgba(224, 247, 255, 0.5)' }}>
									<Typography variant="subtitle2" gutterBottom>
										Combination Method
									</Typography>
									<FormControl component="fieldset" sx={{ mb: 2 }}>
										<FormLabel>How should sub-discounts be combined?</FormLabel>
										<RadioGroup
											row
											value={compositeDiscount.combinationType}
											onChange={(e) => setCompositeDiscount(prev => ({ ...prev, combinationType: e.target.value }))}
											sx={{ mt: 1 }}
										>
											<FormControlLabel
												value="SUM"
												control={<Radio />}
												label={
													<Box>
														<Typography variant="body2">Sum All</Typography>
														<Typography variant="caption" color="text.secondary">
															Add all discounts together
														</Typography>
													</Box>
												}
											/>
											<FormControlLabel
												value="MAXIMUM"
												control={<Radio />}
												label={
													<Box>
														<Typography variant="body2">Maximum Only</Typography>
														<Typography variant="caption" color="text.secondary">
															Apply only the largest discount
														</Typography>
													</Box>
												}
											/>
										</RadioGroup>
									</FormControl>
								</Paper>

								{/* Sub-Discounts Management */}
								<Paper sx={{ p: 3, mb: 3 }}>
									<Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
										<Typography variant="subtitle2">
											Sub-Discounts ({compositeDiscount.subDiscounts.length})
										</Typography>
										<Button
											variant="outlined"
											startIcon={<AddIcon />}
											onClick={handleToggleSubDiscountCreator}
											size="small"
										>
											{compositeDiscount.showSubDiscountCreator ? 'Hide Creator' : 'Add Sub-Discount'}
										</Button>
									</Box>

									{compositeDiscount.subDiscounts.length === 0 && !compositeDiscount.showSubDiscountCreator && (
										<Alert severity="info" sx={{ mb: 2 }}>
											<Typography variant="body2">
												Add at least 2 sub-discounts to create a composite discount.
											</Typography>
										</Alert>
									)}

									{/* Existing Sub-Discounts List */}
									{compositeDiscount.subDiscounts.length > 0 && (
										<List dense sx={{ mb: 2 }}>
											{compositeDiscount.subDiscounts.map((subDiscount, index) => (
												<ListItem
													key={index}
													sx={{
														border: '1px solid',
														borderColor: 'divider',
														borderRadius: 1,
														mb: 1,
														bgcolor: 'background.paper'
													}}
												>
													<ListItemText
														primary={subDiscount.metadata?.displayName || `Sub-discount ${index + 1}`}
														secondary={
															<span style={{ marginTop: '0.5rem', display: 'inline-block' }}>
																<Chip
																	size="small"
																	label={subDiscount.type}
																	color="primary"
																	variant="outlined"
																	sx={{ mr: 1 }}
																/>
																{subDiscount.scope && (
																	<Chip
																		size="small"
																		label={subDiscount.scope}
																		color="secondary"
																		variant="outlined"
																	/>
																)}
															</span>
														}
													/>
													<ListItemSecondaryAction>
														<IconButton
															edge="end"
															color="error"
															onClick={() => handleRemoveSubDiscount(index)}
															size="small"
														>
															<DeleteIcon />
														</IconButton>
													</ListItemSecondaryAction>
												</ListItem>
											))}
										</List>
									)}

									{/* Sub-Discount Creator */}
									{compositeDiscount.showSubDiscountCreator && (
										<SubDiscountCreator
											onAddSubDiscount={handleAddSubDiscount}
											existingSubDiscounts={compositeDiscount.subDiscounts}
											onClose={handleToggleSubDiscountCreator}
										/>
									)}

									{/* Composite Preview */}
									{compositeDiscount.subDiscounts.length >= 2 && (
										<Alert severity="success" sx={{ mt: 2 }}>
											<Typography variant="body2">
												<strong>Preview:</strong> This composite discount will {compositeDiscount.combinationType === 'SUM' ? 'sum' : 'apply the maximum of'} {compositeDiscount.subDiscounts.length} sub-discounts.
											</Typography>
										</Alert>
									)}
								</Paper>
							</Box>
						)}
					</Box>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setDiscountPolicyDialog(false)}>Cancel</Button>
					<Button
						onClick={handleAddDiscountPolicyByType}
						variant="contained"
						disabled={
							(discountType === 'BASIC' && (!basicDiscount.value)) ||
							(discountType === 'COMPOSITE' && compositeDiscount.subDiscounts.length < 2) ||
							(discountType === 'CONDITIONAL' && (activeStep !== 1 || !conditionalDiscount.conditionValue))
						}
					>
						Add {discountType} Discount
					</Button>
				</DialogActions>
			</Dialog>
		</Card>
	);
};

export default PolicyManagement; 