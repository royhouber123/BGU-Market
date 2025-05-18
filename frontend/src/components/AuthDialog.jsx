import React, { useState } from "react";
import userService from "../services/userService";

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
} from "@mui/material";

// Material-UI Icons
import LoginIcon from "@mui/icons-material/Login";
import PersonAddIcon from "@mui/icons-material/PersonAdd";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import GoogleIcon from "@mui/icons-material/Google";

export default function AuthDialog({ open, onOpenChange }) {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    fullName: "",
    email: "",
    confirmPassword: ""
  });

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleGoogleLogin = async () => {
    try {
      await userService.googleLogin();  // This will redirect to Google login
    } catch (error) {
      console.error("Login failed:", error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      if (isLogin) {
        // Handle login with BCrypt verification on backend
        await userService.login(formData.username, formData.password);
        onOpenChange(false); // Close dialog after successful login
      } else {
        // Handle registration
        if (formData.password !== formData.confirmPassword) {
          // Show error
          alert("Passwords don't match");
          return;
        }
        
        // Register new user - password will be encrypted with BCrypt on backend
        await userService.register({
          username: formData.username,
          password: formData.password,
          fullName: formData.fullName,
          email: formData.email
        });
        onOpenChange(false); // Close dialog after successful registration
      }
    } catch (error) {
      console.error("Authentication failed:", error);
      alert(error.response?.data?.message || "Authentication failed. Please try again.");
    }
  };

  const handleToggleForm = () => {
    setIsLogin(!isLogin);
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
    <Dialog open={open} onClose={() => onOpenChange(false)} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', pb: 0 }}>
        {!isLogin && (
          <IconButton 
            edge="start" 
            size="small" 
            sx={{ mr: 1 }} 
            onClick={handleToggleForm}
          >
            <ArrowBackIcon fontSize="small" />
          </IconButton>
        )}
        {isLogin ? "Sign in to Marketplace" : "Create an Account"}
      </DialogTitle>
      <DialogContentText sx={{ px: 3, pt: 1 }}>
        {isLogin ? "Enter your credentials to continue" : "Fill in the information below to create your account"}
      </DialogContentText>
      <DialogContent>

        <form onSubmit={handleSubmit} style={{ marginTop: 2 }}>
          {!isLogin && (
            <Grid container spacing={2} sx={{ mb: 2 }}>
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
            sx={{ mt: 2 }}
            startIcon={isLogin ? <LoginIcon /> : <PersonAddIcon />}
          >
            {isLogin ? "Sign In" : "Create Account"}
          </Button>
        </form>

        <Box sx={{ position: 'relative', my: 3 }}>
          <Divider sx={{ my: 2 }} />
          <Typography 
            variant="caption" 
            sx={{ 
              position: 'absolute', 
              top: '50%', 
              left: '50%', 
              transform: 'translate(-50%, -50%)', 
              bgcolor: 'background.paper', 
              px: 1,
              color: 'text.secondary',
              textTransform: 'uppercase'
            }}
          >
            Or continue with
          </Typography>
        </Box>

        <Button
          fullWidth
          onClick={handleGoogleLogin}
          variant="outlined"
          startIcon={<GoogleIcon />}
        >
          Sign in with Google
        </Button>

      </DialogContent>
      <DialogActions sx={{ justifyContent: 'center', pb: 3, flexDirection: 'column' }}>
        <Typography variant="body2" sx={{ mb: 1 }}>
          {isLogin ? (
            <>
              Don't have an account?{" "}
              <Button 
                color="primary" 
                onClick={handleToggleForm} 
                size="small"
                sx={{ textTransform: 'none', fontWeight: 'medium', p: 0, minWidth: 'auto', verticalAlign: 'baseline' }}
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
                sx={{ textTransform: 'none', fontWeight: 'medium', p: 0, minWidth: 'auto', verticalAlign: 'baseline' }}
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
