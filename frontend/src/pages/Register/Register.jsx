import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import Header from '../../components/Header/Header';
import ApiTest from '../../components/ApiTest/ApiTest';
import './Register.css';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    email: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { registerWithCart } = useAuth();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validate passwords match
    if (formData.password !== formData.confirmPassword) {
      return setError('Passwords do not match');
    }

    // Validate password strength
    if (formData.password.length < 6) {
      return setError('Password must be at least 6 characters');
    }

    setLoading(true);

    try {
      // Use the AuthContext method which handles guest cart transfer
      const response = await registerWithCart({
        username: formData.username,
        password: formData.password,
        email: formData.email
      });

      // Registration successful, show success message
      setSuccess(response.message || 'Registration successful! Your cart items have been saved. Please log in to continue.');

      // Clear form
      setFormData({
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      });

      // Redirect to login page after 3 seconds
      setTimeout(() => {
        navigate('/login', { state: { message: 'Registration successful! Your cart items have been saved. Please log in.' } });
      }, 3000);

    } catch (error) {
      setError(error.message || 'Failed to register account');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Header />

      {/* Temporary API Test Component */}
      <ApiTest />

      <div className="register-container">
        <h2 className="register-heading">Create an Account</h2>
        {error && <div className="register-error-message">{error}</div>}
        {success && <div className="register-success-message">{success}</div>}
        <form onSubmit={handleSubmit} className="register-form">
          <div className="register-form-group">
            <label htmlFor="username" className="register-label">Username</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
              className="register-input"
            />
          </div>

          <div className="register-form-group">
            <label htmlFor="email" className="register-label">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              className="register-input"
            />
            <small className="register-small">Email is for display purposes only</small>
          </div>

          <div className="register-form-group">
            <label htmlFor="password" className="register-label">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              className="register-input"
            />
            <small className="register-small">Password must be at least 6 characters long</small>
          </div>

          <div className="register-form-group">
            <label htmlFor="confirmPassword" className="register-label">Confirm Password</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              className="register-input"
            />
          </div>

          <button
            type="submit"
            className="register-button"
            disabled={loading}
          >
            {loading ? 'Registering...' : 'Register'}
          </button>
        </form>
        <div className="register-login-link">
          Already have an account? <Link to="/login" className="register-link">Log in here</Link>
        </div>
      </div>
    </>
  );
};

export default Register;
