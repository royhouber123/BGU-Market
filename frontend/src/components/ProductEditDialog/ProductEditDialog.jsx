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
	IconButton
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { storeService } from '../../services/storeService';

const ProductEditDialog = ({ open, onClose, product, store, currentUser, onUpdate }) => {
	const [formData, setFormData] = useState({
		title: '',
		description: '',
		price: '',
		quantity: '',
		category: ''
	});
	const [loading, setLoading] = useState(false);
	const [errors, setErrors] = useState({});

	// Initialize form data when product changes
	useEffect(() => {
		if (product) {
			setFormData({
				title: product.title || '',
				description: product.description || '',
				price: product.price?.toString() || '',
				quantity: product.quantity?.toString() || '',
				category: product.category || ''
			});
		}
	}, [product]);

	const validateForm = () => {
		const newErrors = {};

		if (!formData.title.trim()) {
			newErrors.title = 'Product name is required';
		}

		if (!formData.price || isNaN(parseFloat(formData.price)) || parseFloat(formData.price) < 0) {
			newErrors.price = 'Price must be a valid non-negative number';
		}

		if (!formData.quantity || isNaN(parseInt(formData.quantity)) || parseInt(formData.quantity) < 0) {
			newErrors.quantity = 'Quantity must be a valid non-negative integer';
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

	const handleSave = async () => {
		if (!validateForm()) {
			return;
		}

		setLoading(true);

		try {
			const userName = currentUser.userName;
			const storeID = store.id;
			const listingId = product.id;

			// Check what fields have changed and update them
			const updates = [];

			if (formData.title !== product.title) {
				updates.push(
					storeService.editListingName(userName, storeID, listingId, formData.title)
				);
			}

			if (formData.description !== product.description) {
				updates.push(
					storeService.editListingDescription(userName, storeID, listingId, formData.description)
				);
			}

			if (parseFloat(formData.price) !== product.price) {
				updates.push(
					storeService.editListingPrice(userName, storeID, listingId, parseFloat(formData.price))
				);
			}

			if (parseInt(formData.quantity) !== product.quantity) {
				updates.push(
					storeService.editListingQuantity(userName, storeID, listingId, parseInt(formData.quantity))
				);
			}

			if (formData.category !== product.category) {
				updates.push(
					storeService.editListingCategory(userName, storeID, listingId, formData.category)
				);
			}

			// Execute all updates
			if (updates.length > 0) {
				await Promise.all(updates);
				onUpdate?.({
					title: 'Success',
					description: 'Product updated successfully',
					variant: 'success'
				});
			} else {
				onUpdate?.({
					title: 'Info',
					description: 'No changes detected',
					variant: 'info'
				});
			}

			onClose();
		} catch (error) {
			console.error('Error updating product:', error);
			onUpdate?.({
				title: 'Error',
				description: error.message || 'Failed to update product',
				variant: 'destructive'
			});
		} finally {
			setLoading(false);
		}
	};

	const handleClose = () => {
		setFormData({
			title: '',
			description: '',
			price: '',
			quantity: '',
			category: ''
		});
		setErrors({});
		onClose();
	};

	if (!product) return null;

	return (
		<Dialog
			open={open}
			onClose={handleClose}
			maxWidth="md"
			fullWidth
			PaperProps={{
				sx: { borderRadius: 2 }
			}}
		>
			<DialogTitle sx={{ pb: 1 }}>
				<Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
					<Typography variant="h6" fontWeight="bold">
						Edit Product
					</Typography>
					<IconButton onClick={handleClose} size="small">
						<CloseIcon />
					</IconButton>
				</Box>
			</DialogTitle>

			<DialogContent sx={{ pt: 2 }}>
				<Grid container spacing={3}>
					<Grid item xs={12}>
						<TextField
							fullWidth
							label="Product Name"
							value={formData.title}
							onChange={(e) => handleInputChange('title', e.target.value)}
							error={!!errors.title}
							helperText={errors.title}
							variant="outlined"
						/>
					</Grid>

					<Grid item xs={12}>
						<TextField
							fullWidth
							label="Description"
							value={formData.description}
							onChange={(e) => handleInputChange('description', e.target.value)}
							multiline
							rows={3}
							variant="outlined"
						/>
					</Grid>

					<Grid item xs={12} sm={6}>
						<TextField
							fullWidth
							label="Price"
							value={formData.price}
							onChange={(e) => handleInputChange('price', e.target.value)}
							error={!!errors.price}
							helperText={errors.price}
							type="number"
							inputProps={{
								min: 0,
								step: 0.01
							}}
							InputProps={{
								startAdornment: <InputAdornment position="start">$</InputAdornment>,
							}}
							variant="outlined"
						/>
					</Grid>

					<Grid item xs={12} sm={6}>
						<TextField
							fullWidth
							label="Quantity"
							value={formData.quantity}
							onChange={(e) => handleInputChange('quantity', e.target.value)}
							error={!!errors.quantity}
							helperText={errors.quantity}
							type="number"
							inputProps={{
								min: 0
							}}
							variant="outlined"
						/>
					</Grid>

					<Grid item xs={12}>
						<TextField
							fullWidth
							label="Category"
							value={formData.category}
							onChange={(e) => handleInputChange('category', e.target.value)}
							variant="outlined"
							placeholder="e.g., Electronics, Clothing, Books"
						/>
					</Grid>
				</Grid>
			</DialogContent>

			<DialogActions sx={{ p: 3, pt: 2 }}>
				<Button onClick={handleClose} variant="outlined">
					Cancel
				</Button>
				<Button
					onClick={handleSave}
					variant="contained"
					disabled={loading}
				>
					{loading ? 'Saving...' : 'Save Changes'}
				</Button>
			</DialogActions>
		</Dialog>
	);
};

export default ProductEditDialog; 