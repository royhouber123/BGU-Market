import React, { useState } from "react";
import { useAuth } from "../../contexts/AuthContext";
import "./AuthDialog.css";

// Material-UI imports
import {
	Dialog,
	DialogActions,
	DialogContent,
	DialogContentText,
	DialogTitle,
	Button,
	TextField,
	Typography,
	Box,
	Divider,
	IconButton,
	Grid,
	Alert,
	CircularProgress,
} from "@mui/material";

// Material-UI Icons
import LoginIcon from "@mui/icons-material/Login";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import GoogleIcon from "@mui/icons-material/Google";

export default function AuthDialog({ open, onOpenChange, onClose }) {
	const { login, registerWithCart } = useAuth();
	const [isLogin, setIsLogin] = useState(true);
	const [formData, setFormData] = useState({
		username: "",
		password: "",
		fullName: "",
		email: "",
		confirmPassword: ""
	});
	const [error, setError] = useState("");
	const [success, setSuccess] = useState("");
	const [loading, setLoading] = useState(false);

	const handleInputChange = (e) => {
		setFormData({
			...formData,
			[e.target.name]: e.target.value
		});
	};

	const handleGoogleLogin = async () => {
		try {
			// TODO: Implement Google login when backend supports it
			setError("Google login not implemented yet");
		} catch (error) {
			console.error("Google login failed:", error);
			setError("Google login failed. Please try again.");
		}
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setError("");
		setSuccess("");
		setLoading(true);

		try {
			if (isLogin) {
				// Handle login
				const success = await login(formData.username, formData.password);
				if (success) {
					setSuccess("Login successful!");
					setTimeout(() => {
						// Use onOpenChange if provided, otherwise fall back to onClose
						if (onOpenChange) {
							onOpenChange(false);
						} else if (onClose) {
							onClose();
						}
					}, 1000);
				} else {
					setError("Invalid username or password");
				}
			} else {
				// Handle registration
				if (formData.password !== formData.confirmPassword) {
					setError("Passwords don't match");
					return;
				}

				if (formData.password.length < 6) {
					setError("Password must be at least 6 characters");
					return;
				}

				// Use the AuthContext method which handles guest cart transfer
				await registerWithCart({
					username: formData.username,
					password: formData.password,
					email: formData.email
				});

				setSuccess("Registration successful! Your cart items have been saved. Please log in to continue.");

				// Switch to login form after successful registration
				setTimeout(() => {
					setIsLogin(true);
					setFormData({
						username: formData.username, // Keep username filled
						password: "",
						fullName: "",
						email: "",
						confirmPassword: ""
					});
				}, 1500);
			}
		} catch (error) {
			console.error("Authentication failed:", error);
			setError(error.message || "Authentication failed. Please try again.");
		} finally {
			setLoading(false);
		}
	};

	const handleToggleForm = () => {
		setIsLogin(!isLogin);
		setError("");
		setSuccess("");
		// Reset form data when switching between login and register
		setFormData({
			username: "",
			password: "",
			fullName: "",
			email: "",
			confirmPassword: ""
		});
	};

	return (
		<Dialog open={open} onClose={() => {
			// Use onOpenChange if provided, otherwise fall back to onClose
			if (onOpenChange) {
				onOpenChange(false);
			} else if (onClose) {
				onClose();
			}
		}} maxWidth="sm" fullWidth>
			<DialogTitle className="auth-dialog-title">
				{!isLogin && (
					<IconButton
						edge="start"
						size="small"
						className="auth-dialog-back-btn"
						onClick={handleToggleForm}
					>
						<ArrowBackIcon fontSize="small" />
					</IconButton>
				)}
				{isLogin ? "Sign in to Marketplace" : "Create an Account"}
			</DialogTitle>
			<DialogContentText className="auth-dialog-content-text">
				{isLogin ? "Enter your credentials to continue" : "Fill in the information below to create your account"}
			</DialogContentText>
			<DialogContent>
				{/* Error/Success Messages */}
				{error && (
					<Alert severity="error" className="auth-dialog-alert">
						{error}
					</Alert>
				)}
				{success && (
					<Alert severity="success" className="auth-dialog-alert">
						{success}
					</Alert>
				)}

				<form onSubmit={handleSubmit} className="auth-dialog-form">
					{!isLogin && (
						<Grid container spacing={2} className="auth-dialog-register-grid">
							<Grid item xs={12} sm={6}>
								<TextField
									fullWidth
									label="Full Name"
									id="fullName"
									name="fullName"
									value={formData.fullName}
									onChange={handleInputChange}
									required
									variant="outlined"
									size="small"
									margin="normal"
								/>
							</Grid>
							<Grid item xs={12} sm={6}>
								<TextField
									fullWidth
									label="Email"
									id="email"
									name="email"
									type="email"
									value={formData.email}
									onChange={handleInputChange}
									required
									variant="outlined"
									size="small"
									margin="normal"
								/>
							</Grid>
						</Grid>
					)}

					<TextField
						fullWidth
						label="Username"
						id="username"
						name="username"
						value={formData.username}
						onChange={handleInputChange}
						required
						variant="outlined"
						size="small"
						margin="normal"
					/>

					<TextField
						fullWidth
						label="Password"
						id="password"
						name="password"
						type="password"
						value={formData.password}
						onChange={handleInputChange}
						required
						variant="outlined"
						size="small"
						margin="normal"
						helperText="Password is encrypted using BCrypt for maximum security"
					/>

					{!isLogin && (
						<TextField
							fullWidth
							label="Confirm Password"
							id="confirmPassword"
							name="confirmPassword"
							type="password"
							value={formData.confirmPassword}
							onChange={handleInputChange}
							required
							variant="outlined"
							size="small"
							margin="normal"
						/>
					)}

					<Button
						type="submit"
						variant="contained"
						fullWidth
						className="auth-dialog-submit-btn"
						startIcon={loading ? <CircularProgress size={20} color="inherit" /> : (isLogin ? <LoginIcon /> : <PersonAddIcon />)}
						disabled={loading}
					>
						{loading ? (isLogin ? "Signing In..." : "Creating Account...") : (isLogin ? "Sign In" : "Create Account")}
					</Button>
				</form>

				<Box className="auth-dialog-divider-container">
					<Divider className="auth-dialog-divider" />
					<Typography
						variant="caption"
						className="auth-dialog-divider-text"
					>
						Or continue with
					</Typography>
				</Box>

				<Button
					fullWidth
					onClick={handleGoogleLogin}
					variant="outlined"
					startIcon={<GoogleIcon />}
					disabled={loading}
				>
					Sign in with Google
				</Button>

			</DialogContent>
			<DialogActions className="auth-dialog-actions">
				<Typography variant="body2" className="auth-dialog-toggle-text">
					{isLogin ? (
						<>
							Don't have an account?{" "}
							<Button
								color="primary"
								onClick={handleToggleForm}
								size="small"
								className="auth-dialog-toggle-btn"
								disabled={loading}
							>
								Register
							</Button>
						</>
					) : (
						<>
							Already have an account?{" "}
							<Button
								color="primary"
								onClick={handleToggleForm}
								size="small"
								className="auth-dialog-toggle-btn"
								disabled={loading}
							>
								Sign in
							</Button>
						</>
					)}
				</Typography>
			</DialogActions>
		</Dialog>
	);
} 