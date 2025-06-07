import React, { useState } from 'react';
import {
	Dialog,
	DialogTitle,
	DialogContent,
	DialogActions,
	TextField,
	Button,
	Box,
	Typography,
	Alert,
	FormControl,
	InputLabel,
	Select,
	MenuItem,
	InputAdornment
} from '@mui/material';
import {
	Timer as TimerIcon,
	Gavel as GavelIcon,
	AttachMoney as AttachMoneyIcon
} from '@mui/icons-material';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import purchaseService from '../../services/purchaseService';

const StartAuctionDialog = ({ open, onClose, product, store, onUpdate }) => {
	const [formData, setFormData] = useState({
		productName: product?.title || '',
		productCategory: product?.category || '',
		productDescription: product?.description || '',
		startingPrice: product?.price || 0,
		endDateTime: new Date(Date.now() + 24 * 60 * 60 * 1000) // Default to 24 hours from now
	});
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState('');

	const handleChange = (field, value) => {
		setFormData(prev => ({
			...prev,
			[field]: value
		}));
		setError('');
	};

	const handleSubmit = async () => {
		try {
			// Validation
			if (!formData.productName.trim()) {
				setError('Product name is required');
				return;
			}
			if (!formData.productCategory.trim()) {
				setError('Product category is required');
				return;
			}
			if (!formData.productDescription.trim()) {
				setError('Product description is required');
				return;
			}
			if (formData.startingPrice <= 0) {
				setError('Starting price must be greater than 0');
				return;
			}
			if (formData.endDateTime <= new Date()) {
				setError('End date must be in the future');
				return;
			}

			setLoading(true);
			setError('');

			const endTimeMillis = formData.endDateTime.getTime();

			await purchaseService.openAuction(
				store.id,
				product.id,
				formData.productName,
				formData.productCategory,
				formData.productDescription,
				formData.startingPrice,
				endTimeMillis
			);

			onUpdate?.({
				title: 'Auction Started',
				description: `Auction for "${formData.productName}" has been successfully started!`,
				variant: 'success'
			});

			onClose();
		} catch (err) {
			console.error('Error starting auction:', err);
			setError(err.message || 'Failed to start auction');
		} finally {
			setLoading(false);
		}
	};

	const getDurationFromNow = () => {
		if (!formData.endDateTime) return '';
		const now = new Date();
		const diff = formData.endDateTime.getTime() - now.getTime();
		if (diff <= 0) return 'Already past';

		const hours = Math.floor(diff / (1000 * 60 * 60));
		const days = Math.floor(hours / 24);

		if (days > 0) {
			return `${days} day${days > 1 ? 's' : ''} ${hours % 24} hour${hours % 24 !== 1 ? 's' : ''}`;
		}
		return `${hours} hour${hours !== 1 ? 's' : ''}`;
	};

	return (
		<Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
			<DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
				<GavelIcon color="primary" />
				Start Auction for Product
			</DialogTitle>
			<DialogContent>
				<Box sx={{ pt: 2 }}>
					{error && (
						<Alert severity="error" sx={{ mb: 3 }}>
							{error}
						</Alert>
					)}

					<Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
						Configure the auction details for this product. Once started, users will be able to place bids until the auction ends.
					</Typography>

					<Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
						<TextField
							label="Product Name"
							value={formData.productName}
							onChange={(e) => handleChange('productName', e.target.value)}
							fullWidth
							required
							helperText="The name that will be displayed in the auction"
						/>

						<TextField
							label="Product Category"
							value={formData.productCategory}
							onChange={(e) => handleChange('productCategory', e.target.value)}
							fullWidth
							required
							helperText="Category to help users find your auction"
						/>

						<TextField
							label="Product Description"
							value={formData.productDescription}
							onChange={(e) => handleChange('productDescription', e.target.value)}
							fullWidth
							multiline
							rows={3}
							required
							helperText="Detailed description of the product being auctioned"
						/>

						<TextField
							label="Starting Price"
							type="number"
							value={formData.startingPrice}
							onChange={(e) => handleChange('startingPrice', parseFloat(e.target.value) || 0)}
							fullWidth
							required
							InputProps={{
								startAdornment: <InputAdornment position="start"><AttachMoneyIcon /></InputAdornment>
							}}
							helperText="Minimum bid amount to start the auction"
						/>

						<LocalizationProvider dateAdapter={AdapterDateFns}>
							<DateTimePicker
								label="Auction End Date & Time"
								value={formData.endDateTime}
								onChange={(newValue) => handleChange('endDateTime', newValue)}
								renderInput={(params) => (
									<TextField
										{...params}
										fullWidth
										required
										helperText={`Duration: ${getDurationFromNow()}`}
										InputProps={{
											...params.InputProps,
											startAdornment: <InputAdornment position="start"><TimerIcon /></InputAdornment>
										}}
									/>
								)}
								minDateTime={new Date()}
							/>
						</LocalizationProvider>

						{/* Auction Summary */}
						<Box sx={{ p: 2, bgcolor: 'primary.light', borderRadius: 1, mt: 2 }}>
							<Typography variant="subtitle2" fontWeight="bold" gutterBottom>
								Auction Summary
							</Typography>
							<Typography variant="body2" color="text.secondary">
								• Product: {formData.productName || 'Not set'}
							</Typography>
							<Typography variant="body2" color="text.secondary">
								• Category: {formData.productCategory || 'Not set'}
							</Typography>
							<Typography variant="body2" color="text.secondary">
								• Starting Price: ${formData.startingPrice}
							</Typography>
							<Typography variant="body2" color="text.secondary">
								• Duration: {getDurationFromNow()}
							</Typography>
						</Box>
					</Box>
				</Box>
			</DialogContent>
			<DialogActions sx={{ p: 3 }}>
				<Button onClick={onClose} disabled={loading}>
					Cancel
				</Button>
				<Button
					onClick={handleSubmit}
					variant="contained"
					disabled={loading || !formData.productName || !formData.productCategory ||
						!formData.productDescription || formData.startingPrice <= 0}
					startIcon={<GavelIcon />}
				>
					{loading ? 'Starting Auction...' : 'Start Auction'}
				</Button>
			</DialogActions>
		</Dialog>
	);
};

export default StartAuctionDialog; 