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
	ConfirmationNumber as CouponIcon,
	RuleFolder as ConditionIcon,
	MergeType as CompositeIcon,
	Percent as PercentIcon,
	AttachMoney as FixedIcon,
	Info as InfoIcon
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

	// Composite discount state
	const [compositeDiscount, setCompositeDiscount] = useState({
		subDiscounts: [],
		combinationType: 'SUM' // SUM or MAXIMUM
	});
	
	// Temporary discount for composites
	const [tempDiscount, setTempDiscount] = useState(null);
	const [tempDiscountType, setTempDiscountType] = useState('BASIC');

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

	const handleAddDiscountPolicyByType = async () => {
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
					
					// Create the composite discount
					policyData = policyService.createCompositeDiscountPolicy(
						compositeDiscount.subDiscounts,
						compositeDiscount.combinationType
					);
					break;
					
				default:
					throw new Error('Unknown discount type');
			}

			// Add the policy to the store
			await policyService.addDiscountPolicy(store.id, currentUser.userName, policyData);

			// Reset state and close dialog
			setDiscountPolicyDialog(false);
			setDiscountType('BASIC');
			setActiveStep(0);
			
			// Reset all form states
			setBasicDiscount({ type: 'PERCENTAGE', scope: 'STORE', scopeId: '', value: '' });
			setConditionalDiscount({ baseDiscount: null, conditionType: 'BASKET_TOTAL_AT_LEAST', conditionValue: '', conditionTarget: null });
			setCompositeDiscount({ subDiscounts: [], combinationType: 'SUM' });
			setTempDiscount(null);
			setTempDiscountType('BASIC');
			
			// Reload policies and notify user
			loadPolicies();
			onUpdate?.({ title: 'Success', description: 'Discount policy added successfully', variant: 'success' });
			onPolicyUpdate?.(); // Trigger product price refresh
		} catch (error) {
			console.error('Error adding discount policy:', error);
			onUpdate?.({ title: 'Error', description: error.message, variant: 'destructive' });
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
		// Handle different discount types
		if (discount.policyType === 'CONDITIONAL') {
			// Extract condition info
			let conditionText = '';
			if (discount.condition) {
				switch(discount.condition.type) {
					case 'MIN_PRICE':
						conditionText = `minimum purchase of $${discount.condition.value}`;
						break;
					case 'MIN_ITEMS':
						conditionText = `minimum ${discount.condition.value} items`;
						break;
					case 'MAX_PRICE':
						conditionText = `maximum purchase of $${discount.condition.value}`;
						break;
					case 'MAX_ITEMS':
						conditionText = `maximum ${discount.condition.value} items`;
						break;
					default:
						conditionText = `${discount.condition.type}: ${discount.condition.value}`;
				}
			}
			
			// Format the base discount part
			const baseDiscountText = discount.baseDiscount ? formatDiscountText(discount.baseDiscount) : 'discount';
			return `${baseDiscountText} if ${conditionText}`;
		} else if (discount.policyType === 'COMPOSITE') {
			// Format each sub-discount
			const subDiscountsText = discount.subDiscounts?.map(formatDiscountText).join(' and ') || 'multiple discounts';
			return `${discount.combinationType === 'SUM' ? 'Sum' : 'Maximum'} of (${subDiscountsText})`;
		} else {
			// Handle standard percentage or fixed discount
			const valueText = discount.type === 'PERCENTAGE' ? `${discount.value}%` : `$${discount.value}`;
			const scopeText = discount.scope === 'STORE' ? 'store-wide' :
				discount.scope === 'PRODUCT' ? `product ${discount.scopeId}` :
					discount.scope === 'CATEGORY' ? `category ${discount.scopeId}` : discount.scope;
			return `${valueText} ${discount.type.toLowerCase()} discount on ${scopeText}`;
		}
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
						{/* Discount Type Selector */}
						<FormControl fullWidth sx={{ mb: 3 }}>
							<InputLabel>Discount Type</InputLabel>
							<Select
								value={discountType}
								onChange={(e) => setDiscountType(e.target.value)}
								label="Discount Type"
							>
								<MenuItem value="BASIC">Basic Discount</MenuItem>
								<MenuItem value="CONDITIONAL">Conditional Discount</MenuItem>
								<MenuItem value="COMPOSITE">Composite Discount</MenuItem>
							</Select>
						</FormControl>

						{/* Stepper for complex discount types */}
						{(discountType === 'CONDITIONAL' || discountType === 'COMPOSITE') && (
							<Stepper activeStep={activeStep} sx={{ mb: 3 }}>
								{discountType === 'CONDITIONAL' ? (
									<>
										<Step>
											<StepLabel>Base Discount</StepLabel>
										</Step>
										<Step>
											<StepLabel>Condition</StepLabel>
										</Step>
									</>
								) : (
									<>
										<Step>
											<StepLabel>Add Sub-Discounts</StepLabel>
										</Step>
										<Step>
											<StepLabel>Set Combination Type</StepLabel>
										</Step>
									</>
								)}
							</Stepper>
						)}

						{/* Basic Discount Form */}
						{(discountType === 'BASIC' || (discountType === 'CONDITIONAL' && activeStep === 0)) && (
							<>
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
										<MenuItem value="PERCENTAGE">Percentage</MenuItem>
										<MenuItem value="FIXED">Fixed Amount</MenuItem>
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
					</>
				)}


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
							Next
						</Button>
					</Box>
				)}

				{/* Condition Form */}
				{discountType === 'CONDITIONAL' && activeStep === 1 && (
					<>
						<FormControl fullWidth sx={{ mb: 3 }}>
							<InputLabel>Condition Type</InputLabel>
							<Select
								value={conditionalDiscount.conditionType}
								onChange={(e)=> setConditionalDiscount(prev=>({...prev, conditionType: e.target.value }))}
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
							onChange={(e)=> setConditionalDiscount(prev=>({...prev, conditionValue: e.target.value }))}
							sx={{ mb: 3 }}
						/>

						{(conditionalDiscount.conditionType === 'PRODUCT_QUANTITY_AT_LEAST' || conditionalDiscount.conditionType === 'CATEGORY_QUANTITY_AT_LEAST') && (
							<TextField
								fullWidth
								label={conditionalDiscount.conditionType === 'PRODUCT_QUANTITY_AT_LEAST' ? 'Product ID' : 'Category'}
								value={conditionalDiscount.conditionTarget}
								onChange={(e)=> setConditionalDiscount(prev=>({...prev, conditionTarget: e.target.value }))}
								sx={{ mb: 3 }}
							/>
						)}

						<Box sx={{ mt: 2 }}>
							<Button variant="outlined" onClick={()=> setActiveStep(0)}>Back</Button>
						</Box>
					</>
				)}

				{/* Composite Discount Form */}
				{discountType === 'COMPOSITE' && (
					<>
						{activeStep === 0 && (
							<>
								<Typography variant="subtitle1" sx={{ mb: 1 }}>Create Sub-Discount</Typography>
								<TextField
									fullWidth
									label="Sub-Discount Percentage"
									type="number"
									value={tempDiscount?.value || ''}
									onChange={(e) => setTempDiscount({ type: 'PERCENTAGE', scope: 'STORE', scopeId: '', value: e.target.value })}
									helperText="Enter percentage (e.g., 10 for 10%)"
									sx={{ mb: 2 }}
								/>
								<Button
									variant="outlined"
									disabled={!tempDiscount?.value}
									onClick={() => {
										const sub = policyService.createBasicDiscountPolicy('PERCENTAGE','STORE',null,parseFloat(tempDiscount.value));
										setCompositeDiscount(prev => ({ ...prev, subDiscounts: [...prev.subDiscounts, sub] }));
										setTempDiscount(null);
									}}
								>
									Add Sub-Discount
								</Button>

								{compositeDiscount.subDiscounts.length > 0 && (
									<List dense sx={{ mt: 2 }}>
										{compositeDiscount.subDiscounts.map((sd, idx) => (
											<ListItem key={idx} secondaryAction={
												<IconButton edge="end" color="error" onClick={() => {
													setCompositeDiscount(prev => ({ ...prev, subDiscounts: prev.subDiscounts.filter((_, i) => i !== idx) }));
												}}>
													<DeleteIcon />
												</IconButton> }>
												<ListItemText primary={`Sub-Discount ${idx+1}: ${sd.value}%`} />
											</ListItem>
										))}
									</List>
								)}
							</>
						)}

						{activeStep === 1 && (
							<FormControl component="fieldset" sx={{ mb: 3 }}>
								<FormLabel>Combination Type</FormLabel>
								<RadioGroup row value={compositeDiscount.combinationType} onChange={(e)=> setCompositeDiscount(prev=>({...prev, combinationType: e.target.value }))}>
									<FormControlLabel value="SUM" control={<Radio />} label="Sum" />
									<FormControlLabel value="MAXIMUM" control={<Radio />} label="Maximum" />
								</RadioGroup>
							</FormControl>
						)}

						{/* Step Navigation */}
						<Box sx={{ mt: 2 }}>
							{activeStep === 0 && (
								<Button variant="contained" disabled={compositeDiscount.subDiscounts.length < 2} onClick={() => setActiveStep(1)}>
									Next
								</Button>
							)}
							{activeStep === 1 && (
								<Button variant="outlined" onClick={() => setActiveStep(0)}>
									Back
								</Button>
							)}
						</Box>
					</>
				)}
			</Box>
			</DialogContent>
			<DialogActions>
				<Button onClick={() => setDiscountPolicyDialog(false)}>Cancel</Button>
				<Button
					onClick={handleAddDiscountPolicyByType}
					variant="contained"
					disabled={
	(discountType==='COMPOSITE' && (activeStep!==1 || compositeDiscount.subDiscounts.length<2)) ||
	(discountType==='CONDITIONAL' && (activeStep!==1 || !conditionalDiscount.conditionValue))
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