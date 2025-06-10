import React, { useState, useEffect } from 'react';
import {
	Dialog,
	DialogTitle,
	DialogContent,
	DialogActions,
	TextField,
	Button,
	Box,
	Typography,
	Grid,
	InputAdornment,
	IconButton,
	FormControl,
	InputLabel,
	Select,
	MenuItem,
	Chip,
	Alert,
	Divider
} from '@mui/material';
import {
	Close as CloseIcon,
	ShoppingCart as ShoppingCartIcon,
	Gavel as GavelIcon,
	Schedule as ScheduleIcon,
	Casino as CasinoIcon,
	AccessTime as AccessTimeIcon,
	AttachMoney as AttachMoneyIcon
} from '@mui/icons-material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { storeService } from '../../services/storeService';

const AddProductDialog = ({ open, onClose, store, currentUser, onUpdate, purchaseType = 'REGULAR' }) => {
	const [formData, setFormData] = useState({
		productName: '',
		productCategory: '',
		productDescription: '',
		price: '',
		quantity: '',
		// Auction specific
		auctionEndTime: null,
		// Raffle specific
		raffleEndDate: null,
		maxParticipants: '',
		entryFee: ''
	});
	const [loading, setLoading] = useState(false);
	const [errors, setErrors] = useState({});

	// Reset form when dialog opens/closes or purchase type changes
	useEffect(() => {
		if (open) {
			setFormData({
				productName: '',
				productCategory: '',
				productDescription: '',
				price: '',
				quantity: '',
				auctionEndTime: null,
				raffleEndDate: null,
				maxParticipants: '',
				entryFee: ''
			});
			setErrors({});
		}
	}, [open, purchaseType]);

	const getPurchaseTypeInfo = () => {
		switch (purchaseType) {
			case 'BID':
				return {
					title: 'Add Bid Product',
					color: 'warning',
					icon: <GavelIcon />,
					description: 'Customers can submit bids for this product'
				};
			case 'AUCTION':
				return {
					title: 'Add Auction Product',
					color: 'info',
					icon: <ScheduleIcon />,
					description: 'Time-limited auction with highest bidder wins'
				};
			case 'RAFFLE':
				return {
					title: 'Add Raffle Product',
					color: 'secondary',
					icon: <CasinoIcon />,
					description: 'Random winner selection from participants'
				};
			default:
				return {
					title: 'Add Regular Product',
					color: 'primary',
					icon: <ShoppingCartIcon />,
					description: 'Standard purchase with fixed price'
				};
		}
	};

	const validateForm = () => {
		const newErrors = {};

		// Common validations
		if (!formData.productName.trim()) {
			newErrors.productName = 'Product name is required';
		}

		if (!formData.productCategory.trim()) {
			newErrors.productCategory = 'Product category is required';
		}

		// Price validation - not required for bid products
		if (purchaseType !== 'BID' && (!formData.price || isNaN(parseFloat(formData.price)) || parseFloat(formData.price) <= 0)) {
			newErrors.price = 'Price must be a valid positive number';
		}

		if (!formData.quantity || isNaN(parseInt(formData.quantity)) || parseInt(formData.quantity) <= 0) {
			newErrors.quantity = 'Quantity must be a valid positive integer';
		}

		// Purchase type specific validations
		switch (purchaseType) {
			case 'AUCTION':
				if (!formData.auctionEndTime) {
					newErrors.auctionEndTime = 'Auction end time is required';
				} else if (new Date(formData.auctionEndTime) <= new Date()) {
					newErrors.auctionEndTime = 'Auction end time must be in the future';
				}
				break;

			case 'RAFFLE':
				if (!formData.raffleEndDate) {
					newErrors.raffleEndDate = 'Raffle end date is required';
				} else if (new Date(formData.raffleEndDate) <= new Date()) {
					newErrors.raffleEndDate = 'Raffle end date must be in the future';
				}
				if (formData.maxParticipants && (isNaN(parseInt(formData.maxParticipants)) || parseInt(formData.maxParticipants) <= 0)) {
					newErrors.maxParticipants = 'Max participants must be a valid positive integer';
				}
				if (formData.entryFee && (isNaN(parseFloat(formData.entryFee)) || parseFloat(formData.entryFee) < 0)) {
					newErrors.entryFee = 'Entry fee must be a valid non-negative number';
				}
				break;
		}

		setErrors(newErrors);
		return Object.keys(newErrors).length === 0;
	};

	const handleInputChange = (field, value) => {
		setFormData(prev => ({
			...prev,
			[field]: value
		}));

		// Clear error for this field when user starts typing
		if (errors[field]) {
			setErrors(prev => ({
				...prev,
				[field]: undefined
			}));
		}
	};

	const generateProductId = () => {
		// Simple product ID generation - you may want to make this more sophisticated
		return `${store.id}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
	};

	const handleSave = async () => {
		if (!validateForm()) {
			return;
		}

		setLoading(true);

		try {
			const productId = generateProductId();

			// For bid products, use 0 as price since they don't have fixed prices
			const productPrice = purchaseType === 'BID' ? 0 : parseFloat(formData.price);

			// Call the add listing API
			const result = await storeService.addListing(
				currentUser.userName,
				store.id,
				productId,
				formData.productName,
				formData.productCategory,
				formData.productDescription,
				parseInt(formData.quantity),
				productPrice,
				purchaseType
			);

			if (result.success) {
				onUpdate?.({
					title: 'Success',
					description: `${getPurchaseTypeInfo().title.replace('Add ', '')} created successfully`,
					variant: 'success'
				});
				onClose();
			} else {
				throw new Error(result.message || 'Failed to add product');
			}

		} catch (error) {
			console.error('Error adding product:', error);
			onUpdate?.({
				title: 'Error',
				description: error.message || 'Failed to add product',
				variant: 'destructive'
			});
		} finally {
			setLoading(false);
		}
	};

	const handleClose = () => {
		setFormData({
			productName: '',
			productCategory: '',
			productDescription: '',
			price: '',
			quantity: '',
			auctionEndTime: null,
			raffleEndDate: null,
			maxParticipants: '',
			entryFee: ''
		});
		setErrors({});
		onClose();
	};

	const typeInfo = getPurchaseTypeInfo();

	return (
		<Dialog
			open={open}
			onClose={handleClose}
			maxWidth="md"
			fullWidth
			PaperProps={{
				sx: { maxHeight: '90vh' }
			}}
		>
			<DialogTitle sx={{ pb: 1 }}>
				<Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
					<Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
						{typeInfo.icon}
						<Typography variant="h6" component="span">
							{typeInfo.title}
						</Typography>
						<Chip
							label={purchaseType}
							color={typeInfo.color}
							size="small"
							variant="filled"
						/>
					</Box>
					<IconButton onClick={handleClose} size="small">
						<CloseIcon />
					</IconButton>
				</Box>
				<Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
					{typeInfo.description}
				</Typography>
			</DialogTitle>

			<DialogContent sx={{ pt: 2 }}>
				<Grid container spacing={3}>
					{/* Basic Product Information */}
					<Grid item xs={12}>
						<Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
							<ShoppingCartIcon />
							Basic Information
						</Typography>
					</Grid>

					<Grid item xs={12} sm={6}>
						<TextField
							fullWidth
							label="Product Name"
							value={formData.productName}
							onChange={(e) => handleInputChange('productName', e.target.value)}
							error={!!errors.productName}
							helperText={errors.productName}
							required
						/>
					</Grid>

					<Grid item xs={12} sm={6}>
						<TextField
							fullWidth
							label="Category"
							value={formData.productCategory}
							onChange={(e) => handleInputChange('productCategory', e.target.value)}
							error={!!errors.productCategory}
							helperText={errors.productCategory}
							required
						/>
					</Grid>

					<Grid item xs={12}>
						<TextField
							fullWidth
							label="Description"
							multiline
							rows={3}
							value={formData.productDescription}
							onChange={(e) => handleInputChange('productDescription', e.target.value)}
							error={!!errors.productDescription}
							helperText={errors.productDescription}
						/>
					</Grid>

					{/* Price field - only show for non-bid products */}
					{purchaseType !== 'BID' && (
						<Grid item xs={12} sm={6}>
							<TextField
								fullWidth
								label="Price"
								type="number"
								value={formData.price}
								onChange={(e) => handleInputChange('price', e.target.value)}
								error={!!errors.price}
								helperText={errors.price}
								required
								InputProps={{
									startAdornment: <InputAdornment position="start">$</InputAdornment>,
								}}
							/>
						</Grid>
					)}

					{/* Alert for bid products explaining no price needed */}
					{purchaseType === 'BID' && (
						<Grid item xs={12}>
							<Alert severity="info" sx={{ mt: 1 }}>
								<Typography variant="body2">
									<strong>Bid Product:</strong> No fixed price or bid settings required. Customers will submit their own price offers which you can review and approve.
								</Typography>
							</Alert>
						</Grid>
					)}

					<Grid item xs={12} sm={purchaseType === 'BID' ? 12 : 6}>
						<TextField
							fullWidth
							label="Quantity"
							type="number"
							value={formData.quantity}
							onChange={(e) => handleInputChange('quantity', e.target.value)}
							error={!!errors.quantity}
							helperText={errors.quantity}
							required
						/>
					</Grid>

					{/* Purchase Type Specific Fields */}
					{(purchaseType === 'AUCTION' || purchaseType === 'RAFFLE') && (
						<>
							<Grid item xs={12}>
								<Divider sx={{ my: 2 }} />
								<Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
									{typeInfo.icon}
									{purchaseType} Specific Settings
								</Typography>
							</Grid>

							{/* Auction Fields */}
							{purchaseType === 'AUCTION' && (
								<>
									<Grid item xs={12}>
										<LocalizationProvider dateAdapter={AdapterDateFns}>
											<DateTimePicker
												label="Auction End Time"
												value={formData.auctionEndTime}
												onChange={(newValue) => handleInputChange('auctionEndTime', newValue)}
												renderInput={(params) => (
													<TextField
														{...params}
														fullWidth
														required
														error={!!errors.auctionEndTime}
														helperText={errors.auctionEndTime || 'Set when the auction will end'}
													/>
												)}
												minDateTime={new Date()}
											/>
										</LocalizationProvider>
									</Grid>
									<Grid item xs={12}>
										<Alert severity="info" sx={{ mt: 1 }}>
											<Typography variant="body2">
												<strong>Note:</strong> This price will be the starting bid amount. The auction will automatically close at the specified time and the highest bidder wins.
											</Typography>
										</Alert>
									</Grid>
								</>
							)}

							{/* Raffle Fields */}
							{purchaseType === 'RAFFLE' && (
								<>
									<Grid item xs={12} sm={6}>
										<LocalizationProvider dateAdapter={AdapterDateFns}>
											<DateTimePicker
												label="Raffle End Date"
												value={formData.raffleEndDate}
												onChange={(newValue) => handleInputChange('raffleEndDate', newValue)}
												renderInput={(params) => (
													<TextField
														{...params}
														fullWidth
														required
														error={!!errors.raffleEndDate}
														helperText={errors.raffleEndDate || 'When to draw the winner'}
													/>
												)}
												minDateTime={new Date()}
											/>
										</LocalizationProvider>
									</Grid>
									<Grid item xs={12} sm={6}>
										<TextField
											fullWidth
											label="Max Participants"
											type="number"
											value={formData.maxParticipants}
											onChange={(e) => handleInputChange('maxParticipants', e.target.value)}
											error={!!errors.maxParticipants}
											helperText={errors.maxParticipants || 'Optional: Limit number of entries'}
										/>
									</Grid>
									<Grid item xs={12} sm={6}>
										<TextField
											fullWidth
											label="Entry Fee"
											type="number"
											value={formData.entryFee}
											onChange={(e) => handleInputChange('entryFee', e.target.value)}
											error={!!errors.entryFee}
											helperText={errors.entryFee || 'Optional: Cost to enter raffle'}
											InputProps={{
												startAdornment: <InputAdornment position="start">$</InputAdornment>,
											}}
										/>
									</Grid>
									<Grid item xs={12}>
										<Alert severity="info">
											<Typography variant="body2">
												<strong>Note:</strong> The listed price is the retail value of the item. Participants pay the entry fee (if set) for a chance to win.
											</Typography>
										</Alert>
									</Grid>
								</>
							)}
						</>
					)}
				</Grid>
			</DialogContent>

			<DialogActions sx={{ p: 3, pt: 1 }}>
				<Button
					onClick={handleClose}
					disabled={loading}
				>
					Cancel
				</Button>
				<Button
					onClick={handleSave}
					variant="contained"
					disabled={loading}
					sx={{ minWidth: 120 }}
				>
					{loading ? 'Adding...' : `Add ${purchaseType} Product`}
				</Button>
			</DialogActions>
		</Dialog>
	);
};

export default AddProductDialog; 