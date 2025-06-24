import React, { useState, useEffect } from "react";
import {
	Dialog,
	DialogTitle,
	DialogContent,
	DialogActions,
	Button,
	Table,
	TableBody,
	TableCell,
	TableContainer,
	TableHead,
	TableRow,
	Paper,
	Chip,
	Typography,
	Box,
	TextField,
	IconButton,
	Tooltip,
	Alert,
	CircularProgress,
	Stack
} from "@mui/material";
import {
	CheckCircle as ApproveIcon,
	Cancel as RejectIcon,
	MonetizationOn as CounterIcon,
	Close as CloseIcon,
	Gavel as BidIcon,
	Security as SecurityIcon
} from "@mui/icons-material";
import purchaseService from "../../services/purchaseService";
import { storeService } from "../../services/storeService";
import { useAuth } from "../../contexts/AuthContext";

export default function BidManagementDialog({ open, onClose, product, store, onUpdate }) {
	const { currentUser } = useAuth();
	const [bids, setBids] = useState([]);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState("");
	const [permissionError, setPermissionError] = useState(false);
	// Store counter amounts keyed by unique bid ID/key instead of user ID
	const [counterBidAmounts, setCounterBidAmounts] = useState({});
	// Track the bid currently being processed (approve/reject/counter) by its unique key
	const [processingBid, setProcessingBid] = useState(null);
	const [rejectConfirmOpen, setRejectConfirmOpen] = useState(false);
	const [bidToReject, setBidToReject] = useState(null);

	useEffect(() => {
		if (open && product && store) {
			loadBids();
		}
	}, [open, product, store]);

	const loadBids = async () => {
		setLoading(true);
		setError("");
		setPermissionError(false);
		try {
			const bidsData = await purchaseService.getProductBids(store.id, product.id);
			setBids(bidsData);
		} catch (error) {
			console.error("Error loading bids:", error);
			if (error.message?.includes("permission") || error.message?.includes("403") || error.message?.includes("Forbidden")) {
				setPermissionError(true);
				setError("You don't have permission to view bids for this store. You need the 'BID_APPROVAL' permission to manage bids.");
			} else {
				setError("Failed to load bids: " + error.message);
			}
		} finally {
			setLoading(false);
		}
	};

	// Accept a bid – takes the full bid object for a unique reference
	const handleApproveBid = async (bid) => {
		const { userId: bidderUsername } = bid; // existing API still expects username
		const bidKey = bid.id;
		console.log('handleApproveBid called with bidderUsername:', bidderUsername);
		console.log('Current bids array:', bids);
		console.log('Bid being approved belongs to:', bidderUsername);

		setProcessingBid(bidKey);
		try {
			await purchaseService.approveBid(store.id, product.id, bidderUsername);
			console.log('Successfully approved bid for:', bidderUsername);
			onUpdate({
				title: "Bid Approved",
				description: `Successfully approved bid from ${bidderUsername}`,
				variant: "success"
			});
			loadBids(); // Refresh bids
		} catch (error) {
			console.error("Error approving bid:", error);
			onUpdate({
				title: "Error",
				description: "Failed to approve bid: " + error.message,
				variant: "destructive"
			});
		} finally {
			setProcessingBid(null);
		}
	};

	const handleRejectBid = async (bid) => {
		setBidToReject(bid);
		setRejectConfirmOpen(true);
	};

	const confirmRejectBid = async () => {
		if (!bidToReject) return;

		const bidKey = bidToReject.id;
		setProcessingBid(bidKey);
		
		try {
			await purchaseService.rejectBid(store.id, product.id, bidToReject.userId);
			onUpdate({
				title: "Bid Rejected",
				description: `Successfully rejected bid from ${bidToReject}. This bid can no longer be approved or modified.`,
				variant: "success"
			});
			loadBids(); // Refresh bids
		} catch (error) {
			console.error("Error rejecting bid:", error);
			onUpdate({
				title: "Error",
				description: "Failed to reject bid: " + error.message,
				variant: "destructive"
			});
		} finally {
			setProcessingBid(null);
			setRejectConfirmOpen(false);
			setBidToReject(null);
		}
	};

	const cancelRejectBid = () => {
		setRejectConfirmOpen(false);
		setBidToReject(null);
	};

	const handleCounterBid = async (bid) => {
		const bidderUsername = bid.userId;
		const bidKey = bid.id;
		const counterAmount = counterBidAmounts[bidKey];
		if (!counterAmount || parseFloat(counterAmount) <= 0) {
			onUpdate({
				title: "Error",
				description: "Please enter a valid counter bid amount",
				variant: "destructive"
			});
			return;
		}

		setProcessingBid(bidKey);
		try {
			await purchaseService.proposeCounterBid(store.id, product.id, bidderUsername, parseFloat(counterAmount));
			onUpdate({
				title: "Counter Bid Proposed",
				description: `Successfully proposed counter bid of $${counterAmount} to ${bidderUsername}`,
				variant: "success"
			});
			setCounterBidAmounts(prev => ({ ...prev, [bidKey]: "" })); // Clear the input
			loadBids(); // Refresh bids
		} catch (error) {
			console.error("Error proposing counter bid:", error);
			onUpdate({
				title: "Error",
				description: "Failed to propose counter bid: " + error.message,
				variant: "destructive"
			});
		} finally {
			setProcessingBid(null);
		}
	};

	const handleCounterAmountChange = (bidKey, value) => {
		setCounterBidAmounts(prev => ({ ...prev, [bidKey]: value }));
	};

	// Ensure mutually exclusive status representation
	const getStatusChip = (bid) => {
		if (bid.isRejected) {
			return <Chip label="Rejected" color="error" size="small" variant="filled" sx={{ fontWeight: 'bold' }} />;
		}
		if (bid.isApproved) {
			return <Chip label="Approved" color="success" size="small" variant="filled" sx={{ fontWeight: 'bold' }} />;
		}
		if (bid.counterOffered) {
			return <Chip label={`Counter: $${bid.counterOfferAmount?.toFixed(2)}`} color="warning" size="small" variant="filled" />;
		}
		return <Chip label="Pending" color="default" size="small" variant="outlined" />;
	};

	const canTakeAction = (bid) => {
		return !bid.isApproved && !bid.isRejected;
	};

	const getActionMessage = (bid) => {
		if (bid.isRejected) {
			return "This bid has been rejected and cannot be modified";
		}
		if (bid.isApproved) {
			return "This bid has been approved and purchase is complete";
		}
		return "Available actions";
	};

	return (
		<Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
			<DialogTitle sx={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
				<Box sx={{ display: "flex", alignItems: "center" }}>
					<BidIcon sx={{ mr: 1 }} />
					Bid Management - {product?.title}
				</Box>
				<IconButton onClick={onClose}>
					<CloseIcon />
				</IconButton>
			</DialogTitle>

			<DialogContent>
				{error && !permissionError && (
					<Alert severity="error" sx={{ mb: 2 }}>
						{error}
					</Alert>
				)}

				{permissionError && (
					<Box sx={{ textAlign: "center", py: 4 }}>
						<SecurityIcon sx={{ fontSize: 64, color: "warning.main", mb: 2 }} />
						<Typography variant="h6" color="text.primary" gutterBottom>
							Permission Required
						</Typography>
						<Typography variant="body1" color="text.secondary" sx={{ mb: 3, maxWidth: 500, mx: "auto" }}>
							To manage bids for this store, you need the <strong>BID_APPROVAL</strong> permission.
							This permission allows you to view, approve, reject, and counter-offer on customer bids.
						</Typography>

						<Alert severity="info" sx={{ mb: 3, textAlign: "left" }}>
							<Typography variant="body2">
								<strong>Current User Role:</strong> {store?.role || 'Unknown'}
							</Typography>
							<Typography variant="body2" sx={{ mt: 1 }}>
								<strong>Required:</strong> Store Owner OR Manager with BID_APPROVAL permission
							</Typography>
						</Alert>

						{store?.role === 'Owner' || store?.role === 'Founder' ? (
							<Box>
								<Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
									As a store {store.role.toLowerCase()}, you should have bid approval access. If you're seeing this error,
									there might be a temporary issue. Try refreshing the page or contact support.
								</Typography>
								<Button
									variant="outlined"
									onClick={loadBids}
									disabled={loading}
									startIcon={loading && <CircularProgress size={16} />}
								>
									{loading ? "Checking..." : "Retry Loading Bids"}
								</Button>
							</Box>
						) : (
							<Box>
								<Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
									Please ask a store owner to grant you the BID_APPROVAL permission.
								</Typography>
								<Alert severity="info" sx={{ textAlign: "left", mb: 2 }}>
									<Typography variant="body2">
										<strong>How to get BID_APPROVAL permission:</strong>
									</Typography>
									<Typography variant="body2" component="div" sx={{ mt: 1 }}>
										1. Contact a store owner or founder<br />
										2. Ask them to go to Store Management<br />
										3. They can grant you the "BID_APPROVAL" permission<br />
										4. Once granted, return here to manage bids
									</Typography>
								</Alert>
								<Button
									variant="outlined"
									onClick={loadBids}
									sx={{ mr: 1 }}
									disabled={loading}
									startIcon={loading && <CircularProgress size={16} />}
								>
									{loading ? "Checking..." : "Check Permissions Again"}
								</Button>
							</Box>
						)}
					</Box>
				)}

				{!permissionError && (
					<>
						{loading ? (
							<Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
								<CircularProgress />
							</Box>
						) : bids.length === 0 ? (
							<Box sx={{ textAlign: "center", py: 4 }}>
								<BidIcon sx={{ fontSize: 48, color: "text.disabled", mb: 2 }} />
								<Typography variant="h6" color="text.secondary">
									No bids submitted yet
								</Typography>
								<Typography variant="body2" color="text.disabled">
									When customers submit bids for this product, they will appear here.
								</Typography>
							</Box>
						) : (
							<TableContainer component={Paper} variant="outlined">
								<Table>
									<TableHead>
										<TableRow>
											<TableCell>Bidder</TableCell>
											<TableCell align="right">Bid Amount</TableCell>
											<TableCell>Status</TableCell>
											<TableCell>Contact Info</TableCell>
											<TableCell>Shipping Address</TableCell>
											<TableCell align="center">Actions</TableCell>
										</TableRow>
									</TableHead>
									<TableBody>
										{bids.map((bid, index) => {
											// Unique identifiers per bid row
											const currentBidUserId = bid.userId;
											const currentBidKey = bid.id;

											return (
												<TableRow
													key={currentBidKey}
													hover={canTakeAction(bid)}
													sx={{
														bgcolor: bid.isRejected ? 'error.light' :
															bid.isApproved ? 'success.light' :
																'transparent',
														opacity: bid.isRejected ? 0.7 : 1,
														'& .MuiTableCell-root': {
															borderBottom: bid.isRejected ? '1px solid rgba(211, 47, 47, 0.3)' :
																bid.isApproved ? '1px solid rgba(46, 125, 50, 0.3)' :
																	undefined
														}
													}}
												>
													<TableCell>
														<Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
															<Typography variant="body2" fontWeight="bold">
																{currentBidUserId}
															</Typography>
															{bid.isRejected && !bid.isApproved && (
																<Tooltip title="This bid has been rejected">
																	<Box sx={{ display: 'flex', alignItems: 'center' }}>
																		<Typography variant="caption" color="error.main" sx={{ fontSize: '0.7rem' }}>
																			❌ REJECTED
																		</Typography>
																	</Box>
																</Tooltip>
															)}
															{bid.isApproved && !bid.isRejected && (
																<Tooltip title="This bid has been approved and purchase completed">
																	<Box sx={{ display: 'flex', alignItems: 'center' }}>
																		<Typography variant="caption" color="success.main" sx={{ fontSize: '0.7rem' }}>
																			✅ APPROVED
																		</Typography>
																	</Box>
																</Tooltip>
															)}
														</Box>
													</TableCell>
													<TableCell align="right">
														<Typography variant="body2" fontWeight="bold" color="primary">
															${bid.bidAmount?.toFixed(2)}
														</Typography>
													</TableCell>
													<TableCell>
														{getStatusChip(bid)}
													</TableCell>
													<TableCell>
														<Typography variant="body2" sx={{ maxWidth: 150, overflow: "hidden", textOverflow: "ellipsis" }}>
															{bid.contactInfo}
														</Typography>
													</TableCell>
													<TableCell>
														<Typography variant="body2" sx={{ maxWidth: 150, overflow: "hidden", textOverflow: "ellipsis" }}>
															{bid.shippingAddress}
														</Typography>
													</TableCell>
													<TableCell align="center">
														{canTakeAction(bid) ? (
															<Stack direction="row" spacing={1} justifyContent="center">
																{/* Approve Button */}
																<Tooltip title={`Approve bid from ${currentBidUserId}`}>
																	<IconButton
																		color="success"
																		size="small"
																		onClick={() => {
																			console.log('Approving bid id:', currentBidKey);
																			handleApproveBid(bid);
																		}}
																		disabled={processingBid === currentBidKey}
																	>
																		{processingBid === currentBidKey ? (
																			<CircularProgress size={16} />
																		) : (
																			<ApproveIcon />
																		)}
																	</IconButton>
																</Tooltip>

																{/* Reject Button */}
																<Tooltip title={`Reject bid from ${currentBidUserId}`}>
																	<IconButton
																		color="error"
																		size="small"
																		onClick={() => {
																			console.log('Rejecting bid for user:', currentBidUserId);
																			handleRejectBid(bid);
																		}}
																		disabled={processingBid === currentBidKey}
																	>
																		{processingBid === currentBidKey ? (
																			<CircularProgress size={16} />
																		) : (
																			<RejectIcon />
																		)}
																	</IconButton>
																</Tooltip>

																{/* Counter Bid Section */}
																<Box sx={{ display: "flex", alignItems: "center", gap: 0.5 }}>
																	<TextField
																		size="small"
																		type="number"
																		placeholder="Counter amount"
																		value={counterBidAmounts[currentBidKey] || ""}
																		onChange={(e) => handleCounterAmountChange(currentBidKey, e.target.value)}
																		sx={{ width: 120 }}
																		inputProps={{ min: 0, step: 0.01 }}
																	/>
																	<Tooltip title={`Propose counter bid to ${currentBidUserId}`}>
																		<IconButton
																			color="warning"
																			size="small"
																			onClick={() => {
																				console.log('Counter bid for bid id:', currentBidKey);
																				handleCounterBid(bid);
																			}}
																			disabled={processingBid === currentBidKey || !counterBidAmounts[currentBidKey]}
																		>
																			{processingBid === currentBidKey ? (
																				<CircularProgress size={16} />
																			) : (
																				<CounterIcon />
																			)}
																		</IconButton>
																	</Tooltip>
																</Box>
															</Stack>
														) : (
															<Typography variant="body2" color="text.disabled">
																{getActionMessage(bid)}
															</Typography>
														)}
													</TableCell>
												</TableRow>
											);
										})}
									</TableBody>
								</Table>
							</TableContainer>
						)}
					</>
				)}
			</DialogContent>

			<DialogActions>
				<Button onClick={onClose} variant="outlined">
					Close
				</Button>
				<Button onClick={loadBids} disabled={loading} startIcon={loading && <CircularProgress size={16} />}>
					Refresh Bids
				</Button>
			</DialogActions>

			{/* Reject Confirmation Dialog */}
			<Dialog
				open={rejectConfirmOpen}
				onClose={cancelRejectBid}
				maxWidth="sm"
				fullWidth
			>
				<DialogTitle sx={{ color: 'error.main' }}>
					⚠️ Confirm Bid Rejection
				</DialogTitle>
				<DialogContent>
					<Alert severity="warning" sx={{ mb: 2 }}>
						<Typography variant="body2" sx={{ fontWeight: 'bold' }}>
							This action cannot be undone!
						</Typography>
					</Alert>
					<Typography variant="body1" sx={{ mb: 2 }}>
						Are you sure you want to reject the bid from <strong>{bidToReject?.userId}</strong>?
					</Typography>
					<Typography variant="body2" color="text.secondary">
						Once rejected:
					</Typography>
					<Box component="ul" sx={{ mt: 1, mb: 2, pl: 2 }}>
						<Typography component="li" variant="body2" color="text.secondary">
							The bid will be permanently declined
						</Typography>
						<Typography component="li" variant="body2" color="text.secondary">
							No one can approve or modify this bid anymore
						</Typography>
						<Typography component="li" variant="body2" color="text.secondary">
							The bidder will need to submit a new bid if they want to try again
						</Typography>
					</Box>
				</DialogContent>
				<DialogActions>
					<Button onClick={cancelRejectBid} variant="outlined">
						Cancel
					</Button>
					<Button
						onClick={confirmRejectBid}
						variant="contained"
						color="error"
						disabled={processingBid === bidToReject?.id}
						startIcon={processingBid === bidToReject?.id ? <CircularProgress size={16} /> : null}
					>
						{processingBid === bidToReject?.id ? "Rejecting..." : "Reject Bid"}
					</Button>
				</DialogActions>
			</Dialog>
		</Dialog>
	);
} 