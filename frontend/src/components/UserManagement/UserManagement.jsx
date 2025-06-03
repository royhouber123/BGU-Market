import React, { useState, useEffect, useCallback } from "react";
import {
	Box,
	Typography,
	Card,
	CardContent,
	CardActions,
	Grid,
	Button,
	Chip,
	Alert,
	Dialog,
	DialogTitle,
	DialogContent,
	DialogActions,
	TextField,
	FormControl,
	InputLabel,
	Select,
	MenuItem,
	FormControlLabel,
	Checkbox,
	Avatar,
	Divider,
	Tooltip,
	List,
	ListItem,
	ListItemAvatar,
	ListItemText,
	ListItemSecondaryAction,
	IconButton,
	Paper
} from "@mui/material";
import {
	Add as AddIcon,
	Person as PersonIcon,
	SupervisorAccount as SupervisorAccountIcon,
	AdminPanelSettings as AdminPanelSettingsIcon,
	Edit as EditIcon,
	Delete as DeleteIcon,
	Security as SecurityIcon,
	Close as CloseIcon,
	Check as CheckIcon
} from "@mui/icons-material";
import { storeService } from "../../services/storeService";
import { useStorePermissions, PERMISSIONS } from "../../hooks/useStorePermissions";

const PERMISSION_NAMES = {
	0: "View Only",
	1: "Edit Products",
	2: "Edit Policies",
	3: "Bid Approval"
};

const PERMISSION_DESCRIPTIONS = {
	0: "Can view store information and products",
	1: "Can add, edit, and remove products",
	2: "Can create and modify store policies",
	3: "Can approve or reject customer bids"
};

export default function UserManagement({ store, currentUser, onUpdate }) {
	const [users, setUsers] = useState([]);
	const [loading, setLoading] = useState(true);
	const [addUserDialog, setAddUserDialog] = useState(false);
	const [editPermissionsDialog, setEditPermissionsDialog] = useState(false);
	const [selectedUser, setSelectedUser] = useState(null);
	const [newUserData, setNewUserData] = useState({
		userId: "",
		role: "MANAGER"
	});
	const [managerPermissions, setManagerPermissions] = useState([]);
	const [validatingUser, setValidatingUser] = useState(false);

	// Get current user's permissions in this store
	const { permissions: userPermissions, canManageUsers, isOwnerOrFounder, role: currentUserRole } = useStorePermissions(store?.id);

	const loadStoreUsers = useCallback(async () => {
		if (!store?.id || !currentUser?.userName) return;

		setLoading(true);
		try {
			const usersData = await storeService.getStoreUsers(store.id, currentUser.userName);
			setUsers(usersData.users || []);
		} catch (error) {
			console.error("Error loading store users:", error);
			onUpdate?.({
				title: "Error",
				description: error.message,
				variant: "destructive"
			});
		} finally {
			setLoading(false);
		}
	}, [store?.id, currentUser?.userName]);

	useEffect(() => {
		loadStoreUsers();
	}, [loadStoreUsers]);

	const validateUserExists = async (userId) => {
		try {
			// Call backend to validate user existence
			const response = await storeService.validateUserExists(userId);
			return response.exists;
		} catch (error) {
			console.error("Error validating user:", error);
			return false;
		}
	};

	const handleAddUser = async () => {
		if (!newUserData.userId.trim()) {
			onUpdate?.({
				title: "Error",
				description: "Please enter a user ID",
				variant: "destructive"
			});
			return;
		}

		// Check if current user has permission to add users
		if (!canManageUsers && !isOwnerOrFounder) {
			onUpdate?.({
				title: "Error",
				description: "You don't have permission to add users to this store",
				variant: "destructive"
			});
			return;
		}

		setValidatingUser(true);

		try {
			// Validate that the user exists in the system
			const userExists = await validateUserExists(newUserData.userId.trim());
			if (!userExists) {
				onUpdate?.({
					title: "Error",
					description: `User '${newUserData.userId.trim()}' does not exist in the system`,
					variant: "destructive"
				});
				setValidatingUser(false);
				return;
			}

			// Check if user is already in the store
			const existingUser = users.find(user => user.id === newUserData.userId.trim());
			if (existingUser) {
				onUpdate?.({
					title: "Error",
					description: `User '${newUserData.userId.trim()}' is already a ${existingUser.role.toLowerCase()} in this store`,
					variant: "destructive"
				});
				setValidatingUser(false);
				return;
			}

			if (newUserData.role === "OWNER") {
				await storeService.addAdditionalStoreOwner(
					currentUser.userName,
					newUserData.userId.trim(),
					store.id
				);
			} else {
				await storeService.addNewManager(
					currentUser.userName,
					newUserData.userId.trim(),
					store.id
				);
			}

			onUpdate?.({
				title: "Success",
				description: `${newUserData.role === "OWNER" ? "Owner" : "Manager"} added successfully`,
				variant: "success"
			});

			setAddUserDialog(false);
			setNewUserData({ userId: "", role: "MANAGER" });
			loadStoreUsers();
		} catch (error) {
			onUpdate?.({
				title: "Error",
				description: error.message,
				variant: "destructive"
			});
		} finally {
			setValidatingUser(false);
		}
	};

	const handleRemoveUser = async (user) => {
		// Check if current user has permission to remove users
		if (!canManageUsers && !isOwnerOrFounder) {
			onUpdate?.({
				title: "Error",
				description: "You don't have permission to remove users from this store",
				variant: "destructive"
			});
			return;
		}

		if (window.confirm(`Are you sure you want to remove ${user.id} from the store?`)) {
			try {
				if (user.role === "OWNER") {
					await storeService.removeOwner(currentUser.userName, user.id, store.id);
				} else if (user.role === "MANAGER") {
					await storeService.removeManager(currentUser.userName, user.id, store.id);
				}

				onUpdate?.({
					title: "Success",
					description: `${user.role === "OWNER" ? "Owner" : "Manager"} removed successfully`,
					variant: "success"
				});

				loadStoreUsers();
			} catch (error) {
				onUpdate?.({
					title: "Error",
					description: error.message,
					variant: "destructive"
				});
			}
		}
	};

	const handleEditPermissions = (user) => {
		if (user.role !== "MANAGER") return;

		// Check if current user has permission to edit permissions
		if (!canManageUsers && !isOwnerOrFounder) {
			onUpdate?.({
				title: "Error",
				description: "You don't have permission to edit user permissions",
				variant: "destructive"
			});
			return;
		}

		setSelectedUser(user);
		setManagerPermissions(user.permissions || []);
		setEditPermissionsDialog(true);
	};

	const handlePermissionChange = (permissionId, checked) => {
		if (checked) {
			setManagerPermissions(prev => [...prev, permissionId]);
		} else {
			setManagerPermissions(prev => prev.filter(p => p !== permissionId));
		}
	};

	const handleSavePermissions = async () => {
		if (!selectedUser) return;

		try {
			const currentPermissions = selectedUser.permissions || [];
			const newPermissions = managerPermissions;

			// Add new permissions
			for (const permission of newPermissions) {
				if (!currentPermissions.includes(permission)) {
					await storeService.addPermissionToManager(
						selectedUser.id,
						currentUser.userName,
						permission,
						store.id
					);
				}
			}

			// Remove old permissions
			for (const permission of currentPermissions) {
				if (!newPermissions.includes(permission)) {
					await storeService.removePermissionFromManager(
						selectedUser.id,
						permission,
						currentUser.userName,
						store.id
					);
				}
			}

			onUpdate?.({
				title: "Success",
				description: "Permissions updated successfully",
				variant: "success"
			});

			setEditPermissionsDialog(false);
			setSelectedUser(null);
			loadStoreUsers();
		} catch (error) {
			onUpdate?.({
				title: "Error",
				description: error.message,
				variant: "destructive"
			});
		}
	};

	const getRoleIcon = (role) => {
		switch (role) {
			case "FOUNDER":
				return <AdminPanelSettingsIcon color="success" />;
			case "OWNER":
				return <SupervisorAccountIcon color="primary" />;
			case "MANAGER":
				return <PersonIcon color="warning" />;
			default:
				return <PersonIcon />;
		}
	};

	const getRoleColor = (role) => {
		switch (role) {
			case "FOUNDER":
				return "success";
			case "OWNER":
				return "primary";
			case "MANAGER":
				return "warning";
			default:
				return "default";
		}
	};

	if (loading) {
		return (
			<Box sx={{ textAlign: "center", py: 4 }}>
				<Typography>Loading store users...</Typography>
			</Box>
		);
	}

	// Check if current user has permission to view user management
	if (!canManageUsers && !isOwnerOrFounder) {
		return (
			<Paper sx={{ p: 3, mb: 4, borderRadius: 2 }}>
				<Alert severity="warning">
					You don't have permission to manage users in this store.
				</Alert>
			</Paper>
		);
	}

	return (
		<Paper sx={{ p: 3, mb: 4, borderRadius: 2 }}>
			<Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", mb: 3 }}>
				<Typography variant="h6" sx={{ display: "flex", alignItems: "center" }}>
					<SecurityIcon sx={{ mr: 1 }} />
					User Management ({users.length} users)
				</Typography>
				{(canManageUsers || isOwnerOrFounder) && (
					<Button
						variant="contained"
						startIcon={<AddIcon />}
						onClick={() => setAddUserDialog(true)}
					>
						Add User
					</Button>
				)}
			</Box>

			{users.length === 0 ? (
				<Alert severity="info">
					No users found in this store.
				</Alert>
			) : (
				<Grid container spacing={3}>
					{users.map((user) => (
						<Grid item xs={12} sm={6} md={4} key={user.id}>
							<Card sx={{ height: "100%" }}>
								<CardContent>
									<Box sx={{ display: "flex", alignItems: "center", mb: 2 }}>
										<Avatar sx={{ mr: 2, bgcolor: `${getRoleColor(user.role)}.main` }}>
											{getRoleIcon(user.role)}
										</Avatar>
										<Box sx={{ flex: 1 }}>
											<Typography variant="h6" sx={{ wordBreak: "break-word" }}>
												{user.id}
											</Typography>
											<Chip
												size="small"
												label={user.role}
												color={getRoleColor(user.role)}
												variant="filled"
											/>
										</Box>
									</Box>

									{user.appointerID && (
										<Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
											Appointed by: {user.appointerID}
										</Typography>
									)}

									{user.role === "MANAGER" && (
										<Box sx={{ mt: 2 }}>
											<Typography variant="subtitle2" gutterBottom>
												Permissions:
											</Typography>
											{user.permissions && user.permissions.length > 0 ? (
												<Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.5 }}>
													{user.permissions.map((permission) => (
														<Chip
															key={permission}
															size="small"
															label={PERMISSION_NAMES[permission]}
															variant="outlined"
															color="secondary"
														/>
													))}
												</Box>
											) : (
												<Typography variant="body2" color="text.secondary">
													No permissions assigned
												</Typography>
											)}
										</Box>
									)}

									{(user.role === "OWNER" || user.role === "FOUNDER") && (
										<Box sx={{ mt: 2 }}>
											<Typography variant="subtitle2" gutterBottom>
												Permissions:
											</Typography>
											<Chip size="small" label="All Permissions" color="success" variant="filled" />
										</Box>
									)}
								</CardContent>

								<CardActions sx={{ justifyContent: "flex-end", pt: 0 }}>
									{user.role === "MANAGER" && (canManageUsers || isOwnerOrFounder) && (
										<Button
											size="small"
											startIcon={<EditIcon />}
											onClick={() => handleEditPermissions(user)}
										>
											Edit Permissions
										</Button>
									)}
									{user.canRemove && (canManageUsers || isOwnerOrFounder) && (
										<Button
											size="small"
											color="error"
											startIcon={<DeleteIcon />}
											onClick={() => handleRemoveUser(user)}
										>
											Remove
										</Button>
									)}
									{!user.canRemove && user.role === "FOUNDER" && (
										<Tooltip title="Founder cannot be removed">
											<span>
												<Button size="small" disabled>
													Protected
												</Button>
											</span>
										</Tooltip>
									)}
								</CardActions>
							</Card>
						</Grid>
					))}
				</Grid>
			)}

			{/* Add User Dialog */}
			<Dialog open={addUserDialog} onClose={() => setAddUserDialog(false)} maxWidth="sm" fullWidth>
				<DialogTitle>Add New User</DialogTitle>
				<DialogContent>
					<Box sx={{ pt: 2 }}>
						<TextField
							fullWidth
							label="User ID"
							value={newUserData.userId}
							onChange={(e) => setNewUserData(prev => ({ ...prev, userId: e.target.value }))}
							sx={{ mb: 3 }}
							helperText="Enter the username of the user you want to add"
							disabled={validatingUser}
						/>

						<FormControl fullWidth disabled={validatingUser}>
							<InputLabel>Role</InputLabel>
							<Select
								value={newUserData.role}
								label="Role"
								onChange={(e) => setNewUserData(prev => ({ ...prev, role: e.target.value }))}
							>
								<MenuItem value="MANAGER">Manager</MenuItem>
								{(isOwnerOrFounder || currentUserRole === "FOUNDER") && (
									<MenuItem value="OWNER">Owner</MenuItem>
								)}
							</Select>
						</FormControl>

						<Alert severity="info" sx={{ mt: 2 }}>
							<Typography variant="body2">
								<strong>Owner:</strong> Can manage products, policies, users, and approve bids<br />
								<strong>Manager:</strong> Can be granted specific permissions by owners
							</Typography>
						</Alert>

						{validatingUser && (
							<Alert severity="info" sx={{ mt: 2 }}>
								<Typography variant="body2">
									Validating user existence...
								</Typography>
							</Alert>
						)}
					</Box>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setAddUserDialog(false)} disabled={validatingUser}>
						Cancel
					</Button>
					<Button
						onClick={handleAddUser}
						variant="contained"
						disabled={validatingUser}
					>
						{validatingUser ? "Validating..." : "Add User"}
					</Button>
				</DialogActions>
			</Dialog>

			{/* Edit Permissions Dialog */}
			<Dialog
				open={editPermissionsDialog}
				onClose={() => setEditPermissionsDialog(false)}
				maxWidth="md"
				fullWidth
			>
				<DialogTitle>
					Edit Permissions for {selectedUser?.id}
				</DialogTitle>
				<DialogContent>
					<Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
						Select the permissions you want to grant to this manager:
					</Typography>

					<List>
						{Object.entries(PERMISSION_NAMES).map(([permissionId, name]) => {
							const id = parseInt(permissionId);
							return (
								<ListItem key={id} sx={{ pl: 0 }}>
									<FormControlLabel
										control={
											<Checkbox
												checked={managerPermissions.includes(id)}
												onChange={(e) => handlePermissionChange(id, e.target.checked)}
											/>
										}
										label={
											<Box>
												<Typography variant="subtitle2">{name}</Typography>
												<Typography variant="body2" color="text.secondary">
													{PERMISSION_DESCRIPTIONS[id]}
												</Typography>
											</Box>
										}
										sx={{ width: "100%" }}
									/>
								</ListItem>
							);
						})}
					</List>
				</DialogContent>
				<DialogActions>
					<Button onClick={() => setEditPermissionsDialog(false)}>Cancel</Button>
					<Button onClick={handleSavePermissions} variant="contained">Save Permissions</Button>
				</DialogActions>
			</Dialog>
		</Paper>
	);
} 