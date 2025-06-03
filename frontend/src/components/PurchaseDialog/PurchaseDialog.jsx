import React from 'react';
import {
	Dialog,
	DialogTitle,
	DialogContent,
	DialogActions,
	Button,
	Typography,
	Box,
	Alert,
	IconButton
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import CloseIcon from '@mui/icons-material/Close';

const PurchaseDialog = ({
	open,
	onClose,
	success,
	title,
	message,
	details,
	onContinue
}) => {
	const handleContinue = () => {
		onClose();
		if (onContinue) {
			onContinue();
		}
	};

	return (
		<Dialog
			open={open}
			onClose={onClose}
			maxWidth="sm"
			fullWidth
			PaperProps={{
				sx: { borderRadius: 2 }
			}}
		>
			<DialogTitle>
				<Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
					<Box sx={{ display: 'flex', alignItems: 'center' }}>
						{success ? (
							<CheckCircleIcon sx={{ color: 'success.main', mr: 1, fontSize: 28 }} />
						) : (
							<ErrorIcon sx={{ color: 'error.main', mr: 1, fontSize: 28 }} />
						)}
						<Typography variant="h6" component="div">
							{title}
						</Typography>
					</Box>
					<IconButton
						aria-label="close"
						onClick={onClose}
						sx={{ color: 'grey.500' }}
					>
						<CloseIcon />
					</IconButton>
				</Box>
			</DialogTitle>

			<DialogContent>
				<Alert
					severity={success ? 'success' : 'error'}
					variant="outlined"
					sx={{ mb: 2 }}
				>
					<Typography variant="body1">
						{message}
					</Typography>
				</Alert>

				{details && (
					<Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
						<Typography variant="body2" color="textSecondary">
							{details}
						</Typography>
					</Box>
				)}
			</DialogContent>

			<DialogActions sx={{ p: 3, pt: 1 }}>
				{success ? (
					<>
						<Button onClick={onClose} color="primary" variant="outlined">
							Stay Here
						</Button>
						<Button
							onClick={handleContinue}
							color="primary"
							variant="contained"
							autoFocus
						>
							Continue Shopping
						</Button>
					</>
				) : (
					<Button
						onClick={onClose}
						color="primary"
						variant="contained"
						autoFocus
					>
						Try Again
					</Button>
				)}
			</DialogActions>
		</Dialog>
	);
};

export default PurchaseDialog; 