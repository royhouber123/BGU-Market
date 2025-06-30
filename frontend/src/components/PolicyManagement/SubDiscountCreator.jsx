import React, { useState } from 'react';
import {
	Box,
	Typography,
	Card,
	CardContent,
	CardActions,
	Button,
	TextField,
	FormControl,
	InputLabel,
	Select,
	MenuItem,
	Stepper,
	Step,
	StepLabel,
	StepContent,
	Chip,
	Divider,
	Alert,
	FormHelperText,
	IconButton,
	Tooltip,
	Stack
} from '@mui/material';
import {
	Percent as PercentIcon,
	AttachMoney as MoneyIcon,
	RuleFolder as ConditionalIcon,
	Add as AddIcon,
	Delete as DeleteIcon,
	Info as InfoIcon
} from '@mui/icons-material';
import { policyService } from '../../services/policyService';

const SubDiscountCreator = ({ onAddSubDiscount, existingSubDiscounts = [], onClose }) => {
	const [discountType, setDiscountType] = useState('PERCENTAGE');
	const [activeStep, setActiveStep] = useState(0);

	// Basic discount fields
	const [discountData, setDiscountData] = useState({
		type: 'PERCENTAGE',
		scope: 'STORE',
		scopeId: '',
		value: ''
	});

	// For conditional discounts, track the base discount type separately
	const [baseDiscountType, setBaseDiscountType] = useState('PERCENTAGE');

	// Conditional discount specific
	const [conditionData, setConditionData] = useState({
		type: 'BASKET_TOTAL_AT_LEAST',
		params: {}
	});

	const [errors, setErrors] = useState({});

	const discountTypes = policyService.getAvailableDiscountTypes();
	const targetTypes = policyService.getAvailableTargetTypes();
	const conditionTypes = policyService.getAvailableConditionTypes();

	const validateForm = () => {
		const newErrors = {};

		// Basic validation
		if (!discountData.value) {
			newErrors.value = 'Discount value is required';
		} else if (parseFloat(discountData.value) <= 0) {
			newErrors.value = 'Discount value must be positive';
		} else if (discountType === 'PERCENTAGE' && parseFloat(discountData.value) > 100) {
			newErrors.value = 'Percentage cannot be greater than 100';
		}

		// Scope-specific validation
		if ((discountData.scope === 'PRODUCT' || discountData.scope === 'CATEGORY') && !discountData.scopeId) {
			newErrors.scopeId = `${discountData.scope === 'PRODUCT' ? 'Product ID' : 'Category name'} is required`;
		}

		// Conditional validation
		if (discountType === 'CONDITIONAL') {
			switch (conditionData.type) {
				case 'BASKET_TOTAL_AT_LEAST':
					if (!conditionData.params.minTotal) {
						newErrors.conditionValue = 'Minimum total is required';
					} else if (parseFloat(conditionData.params.minTotal) <= 0) {
						newErrors.conditionValue = 'Minimum total must be positive';
					}
					break;
				case 'PRODUCT_QUANTITY_AT_LEAST':
					if (!conditionData.params.productId) {
						newErrors.conditionTarget = 'Product ID is required';
					}
					if (!conditionData.params.minQuantity) {
						newErrors.conditionValue = 'Minimum quantity is required';
					} else if (parseInt(conditionData.params.minQuantity) <= 0) {
						newErrors.conditionValue = 'Minimum quantity must be positive';
					}
					break;
				case 'PRODUCT_CATEGORY_CONTAINS':
					if (!conditionData.params.category) {
						newErrors.conditionTarget = 'Category is required';
					}
					if (!conditionData.params.minQuantity) {
						newErrors.conditionValue = 'Minimum quantity is required';
					} else if (parseInt(conditionData.params.minQuantity) <= 0) {
						newErrors.conditionValue = 'Minimum quantity must be positive';
					}
					break;
				default:
					newErrors.conditionValue = 'Invalid condition type selected';
			}

			// Additional validation: ensure condition params object exists and has the right structure
			if (!conditionData.params || Object.keys(conditionData.params).length === 0) {
				newErrors.conditionValue = 'Condition parameters are missing';
			}
		}

		setErrors(newErrors);
		return Object.keys(newErrors).length === 0;
	};

	const handleDiscountDataChange = (field, value) => {
		setDiscountData(prev => ({ ...prev, [field]: value }));
		// Clear related errors
		if (errors[field]) {
			setErrors(prev => ({ ...prev, [field]: undefined }));
		}
	};

	const handleConditionDataChange = (field, value) => {
		if (field === 'type') {
			setConditionData({ type: value, params: {} });
		} else {
			setConditionData(prev => ({
				...prev,
				params: { ...prev.params, [field]: value }
			}));
		}
		// Clear related errors
		if (errors.conditionValue || errors.conditionTarget) {
			setErrors(prev => ({
				...prev,
				conditionValue: undefined,
				conditionTarget: undefined
			}));
		}
	};

	const handleNext = () => {
		if (discountType === 'CONDITIONAL' && activeStep === 0) {
			// Validate basic discount info before moving to condition step
			const basicErrors = {};
			if (!discountData.value) basicErrors.value = 'Discount value is required';
			if ((discountData.scope === 'PRODUCT' || discountData.scope === 'CATEGORY') && !discountData.scopeId) {
				basicErrors.scopeId = `${discountData.scope === 'PRODUCT' ? 'Product ID' : 'Category name'} is required`;
			}

			if (Object.keys(basicErrors).length === 0) {
				setActiveStep(1);
			} else {
				setErrors(basicErrors);
			}
		}
	};

	const handleBack = () => {
		setActiveStep(0);
	};

	const handleAddDiscount = () => {
		if (!validateForm()) return;

		try {
			let subDiscount;

			switch (discountType) {
				case 'PERCENTAGE':
				case 'FIXED':
					subDiscount = policyService.createTargetedDiscountPolicy(
						discountType,
						discountData.scope,
						discountData.scopeId || null,
						discountData.value
					);
					break;

				case 'CONDITIONAL':
					// Create the base discount first
					const baseDiscount = policyService.createTargetedDiscountPolicy(
						baseDiscountType, // Use the actual selected type (PERCENTAGE or FIXED)
						discountData.scope,
						discountData.scopeId || null,
						discountData.value
					);

					// Create the condition object with proper structure and data types
					const conditionParams = { ...conditionData.params };

					// Ensure numeric values are properly converted
					if (conditionParams.minTotal) {
						conditionParams.minTotal = parseFloat(conditionParams.minTotal);
					}
					if (conditionParams.minQuantity) {
						conditionParams.minQuantity = parseInt(conditionParams.minQuantity);
					}

					const conditionDTO = {
						type: conditionData.type,
						params: conditionParams,
						subConditions: [],
						logic: null
					};

					// Create the conditional discount with the correct structure
					subDiscount = {
						type: 'CONDITIONAL',
						scope: discountData.scope,
						scopeId: discountData.scopeId || null,
						value: parseFloat(discountData.value),
						couponCode: null,
						condition: conditionDTO,
						subDiscounts: [baseDiscount],
						combinationType: null
					};
					break;

				default:
					throw new Error('Unsupported discount type');
			}

			// Add metadata for display
			subDiscount.metadata = {
				displayName: getDiscountDisplayName(subDiscount),
				createdAt: new Date().toISOString()
			};

			onAddSubDiscount(subDiscount);

			// Reset form
			setDiscountData({
				type: 'PERCENTAGE',
				scope: 'STORE',
				scopeId: '',
				value: ''
			});
			setBaseDiscountType('PERCENTAGE');
			setConditionData({
				type: 'BASKET_TOTAL_AT_LEAST',
				params: {}
			});
			setActiveStep(0);
			setErrors({});

		} catch (error) {
			setErrors({ general: error.message });
		}
	};

	const getDiscountDisplayName = (discount) => {
		switch (discount.type) {
			case 'PERCENTAGE':
				return `${discount.value}% off ${discount.scope.toLowerCase()}${discount.scopeId ? ` (${discount.scopeId})` : ''}`;
			case 'FIXED':
				return `$${discount.value} off ${discount.scope.toLowerCase()}${discount.scopeId ? ` (${discount.scopeId})` : ''}`;
			case 'CONDITIONAL':
				// Build condition description
				let conditionText = '';
				if (discount.condition) {
					switch (discount.condition.type) {
						case 'BASKET_TOTAL_AT_LEAST':
							conditionText = `when basket ≥ $${discount.condition.params?.minTotal || 0}`;
							break;
						case 'PRODUCT_QUANTITY_AT_LEAST':
							conditionText = `when product ${discount.condition.params?.productId || 'ID'} qty ≥ ${discount.condition.params?.minQuantity || 0}`;
							break;
						case 'PRODUCT_CATEGORY_CONTAINS':
							conditionText = `when category '${discount.condition.params?.category || 'unknown'}' qty ≥ ${discount.condition.params?.minQuantity || 0}`;
							break;
						default:
							conditionText = `when condition met`;
					}
				}
				return `${discount.value}% off ${discount.scope.toLowerCase()} ${conditionText}`;
			default:
				return 'Custom discount';
		}
	};

	const getDiscountIcon = (type) => {
		switch (type) {
			case 'PERCENTAGE': return <PercentIcon />;
			case 'FIXED': return <MoneyIcon />;
			case 'CONDITIONAL': return <ConditionalIcon />;
			default: return <InfoIcon />;
		}
	};

	const renderBasicDiscountForm = () => (
		<Box>
			<FormControl fullWidth sx={{ mb: 3 }}>
				<InputLabel>Discount Type</InputLabel>
				<Select
					value={discountType}
					onChange={(e) => {
						setDiscountType(e.target.value);
						// Don't set discountData.type for conditional discounts
						if (e.target.value !== 'CONDITIONAL') {
							setDiscountData(prev => ({ ...prev, type: e.target.value }));
						}
						setActiveStep(0);
					}}
					label="Discount Type"
				>
					{discountTypes.map(type => (
						<MenuItem key={type.value} value={type.value}>
							<Box sx={{ display: 'flex', alignItems: 'center' }}>
								{getDiscountIcon(type.value)}
								<Box sx={{ ml: 1 }}>
									<Typography variant="body2">{type.label}</Typography>
									<Typography variant="caption" color="text.secondary">
										{type.description}
									</Typography>
								</Box>
							</Box>
						</MenuItem>
					))}
				</Select>
			</FormControl>

			{/* Show base discount type selector for conditional discounts */}
			{discountType === 'CONDITIONAL' && (
				<FormControl fullWidth sx={{ mb: 3 }}>
					<InputLabel>Base Discount Type</InputLabel>
					<Select
						value={baseDiscountType}
						onChange={(e) => setBaseDiscountType(e.target.value)}
						label="Base Discount Type"
					>
						<MenuItem value="PERCENTAGE">
							<Box sx={{ display: 'flex', alignItems: 'center' }}>
								<PercentIcon sx={{ mr: 1 }} />
								Percentage Discount
							</Box>
						</MenuItem>
						<MenuItem value="FIXED">
							<Box sx={{ display: 'flex', alignItems: 'center' }}>
								<MoneyIcon sx={{ mr: 1 }} />
								Fixed Amount Discount
							</Box>
						</MenuItem>
					</Select>
				</FormControl>
			)}

			<FormControl fullWidth sx={{ mb: 3 }}>
				<InputLabel>Target Scope</InputLabel>
				<Select
					value={discountData.scope}
					onChange={(e) => handleDiscountDataChange('scope', e.target.value)}
					label="Target Scope"
				>
					{targetTypes.map(target => (
						<MenuItem key={target.value} value={target.value}>
							<Box>
								<Typography variant="body2">{target.label}</Typography>
								<Typography variant="caption" color="text.secondary">
									{target.description}
								</Typography>
							</Box>
						</MenuItem>
					))}
				</Select>
			</FormControl>

			{(discountData.scope === 'PRODUCT' || discountData.scope === 'CATEGORY') && (
				<TextField
					fullWidth
					label={discountData.scope === 'PRODUCT' ? 'Product ID' : 'Category Name'}
					value={discountData.scopeId}
					onChange={(e) => handleDiscountDataChange('scopeId', e.target.value)}
					error={!!errors.scopeId}
					helperText={errors.scopeId || (discountData.scope === 'PRODUCT' ? 'Enter the specific product ID' : 'Enter the category name')}
					sx={{ mb: 3 }}
				/>
			)}

			<TextField
				fullWidth
				label={
					discountType === 'CONDITIONAL'
						? (baseDiscountType === 'PERCENTAGE' ? 'Percentage' : 'Discount Amount ($)')
						: (discountType === 'PERCENTAGE' ? 'Percentage' : 'Discount Amount ($)')
				}
				type="number"
				value={discountData.value}
				onChange={(e) => handleDiscountDataChange('value', e.target.value)}
				error={!!errors.value}
				helperText={
					errors.value ||
					(discountType === 'CONDITIONAL'
						? (baseDiscountType === 'PERCENTAGE' ? 'Enter percentage (e.g., 15 for 15%)' : 'Enter discount amount in dollars')
						: (discountType === 'PERCENTAGE' ? 'Enter percentage (e.g., 15 for 15%)' : 'Enter discount amount in dollars')
					)
				}
				inputProps={
					(discountType === 'CONDITIONAL' ? baseDiscountType : discountType) === 'PERCENTAGE' ?
						{ min: 0, max: 100, step: 1 } :
						{ min: 0, step: 0.01 }
				}
				sx={{ mb: 3 }}
			/>
		</Box>
	);

	const renderConditionalForm = () => (
		<Box>
			<FormControl fullWidth sx={{ mb: 3 }}>
				<InputLabel>Condition Type</InputLabel>
				<Select
					value={conditionData.type}
					onChange={(e) => handleConditionDataChange('type', e.target.value)}
					label="Condition Type"
				>
					{conditionTypes.map(condition => (
						<MenuItem key={condition.value} value={condition.value}>
							<Box>
								<Typography variant="body2">{condition.label}</Typography>
								<Typography variant="caption" color="text.secondary">
									{condition.description}
								</Typography>
							</Box>
						</MenuItem>
					))}
				</Select>
			</FormControl>

			{conditionData.type === 'BASKET_TOTAL_AT_LEAST' && (
				<TextField
					fullWidth
					label="Minimum Basket Total ($)"
					type="number"
					value={conditionData.params.minTotal || ''}
					onChange={(e) => handleConditionDataChange('minTotal', e.target.value)}
					error={!!errors.conditionValue}
					helperText={errors.conditionValue || 'Enter minimum basket total required'}
					inputProps={{ min: 0, step: 0.01 }}
					sx={{ mb: 3 }}
				/>
			)}

			{conditionData.type === 'PRODUCT_QUANTITY_AT_LEAST' && (
				<>
					<TextField
						fullWidth
						label="Product ID"
						value={conditionData.params.productId || ''}
						onChange={(e) => handleConditionDataChange('productId', e.target.value)}
						error={!!errors.conditionTarget}
						helperText={errors.conditionTarget || 'Enter the specific product ID'}
						sx={{ mb: 3 }}
					/>
					<TextField
						fullWidth
						label="Minimum Quantity"
						type="number"
						value={conditionData.params.minQuantity || ''}
						onChange={(e) => handleConditionDataChange('minQuantity', e.target.value)}
						error={!!errors.conditionValue}
						helperText={errors.conditionValue || 'Enter minimum quantity required'}
						inputProps={{ min: 1, step: 1 }}
						sx={{ mb: 3 }}
					/>
				</>
			)}

			{conditionData.type === 'PRODUCT_CATEGORY_CONTAINS' && (
				<>
					<TextField
						fullWidth
						label="Category"
						value={conditionData.params.category || ''}
						onChange={(e) => handleConditionDataChange('category', e.target.value)}
						error={!!errors.conditionTarget}
						helperText={errors.conditionTarget || 'Enter category name'}
						sx={{ mb: 3 }}
					/>
					<TextField
						fullWidth
						label="Minimum Quantity"
						type="number"
						value={conditionData.params.minQuantity || ''}
						onChange={(e) => handleConditionDataChange('minQuantity', e.target.value)}
						error={!!errors.conditionValue}
						helperText={errors.conditionValue || 'Enter minimum quantity from category'}
						inputProps={{ min: 1, step: 1 }}
						sx={{ mb: 3 }}
					/>
				</>
			)}
		</Box>
	);

	return (
		<Card sx={{ mb: 3 }}>
			<CardContent>
				<Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center' }}>
					<AddIcon sx={{ mr: 1 }} />
					Add Sub-Discount to Composite
				</Typography>

				{errors.general && (
					<Alert severity="error" sx={{ mb: 3 }}>
						{errors.general}
					</Alert>
				)}

				{discountType === 'CONDITIONAL' ? (
					<Stepper activeStep={activeStep} orientation="vertical" sx={{ mb: 3 }}>
						<Step>
							<StepLabel>Configure Base Discount</StepLabel>
							<StepContent>
								{renderBasicDiscountForm()}
								<Box sx={{ mt: 2 }}>
									<Button variant="contained" onClick={handleNext}>
										Next: Add Condition
									</Button>
								</Box>
							</StepContent>
						</Step>
						<Step>
							<StepLabel>Set Discount Condition</StepLabel>
							<StepContent>
								{renderConditionalForm()}
								<Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
									<Button onClick={handleBack}>
										Back
									</Button>
									<Button variant="contained" onClick={handleAddDiscount}>
										Add Conditional Discount
									</Button>
								</Box>
							</StepContent>
						</Step>
					</Stepper>
				) : (
					<>
						{renderBasicDiscountForm()}
						<Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
							<Button variant="contained" onClick={handleAddDiscount}>
								Add {discountType} Discount
							</Button>
						</Box>
					</>
				)}

				{existingSubDiscounts.length > 0 && (
					<Box sx={{ mt: 3 }}>
						<Divider sx={{ mb: 2 }} />
						<Typography variant="subtitle2" gutterBottom>
							Added Sub-Discounts ({existingSubDiscounts.length})
						</Typography>
						<Stack spacing={1}>
							{existingSubDiscounts.map((discount, index) => (
								<Chip
									key={index}
									label={discount.metadata?.displayName || `Discount ${index + 1}`}
									color="primary"
									variant="outlined"
									icon={getDiscountIcon(discount.type)}
								/>
							))}
						</Stack>
					</Box>
				)}
			</CardContent>
		</Card>
	);
};

export default SubDiscountCreator; 