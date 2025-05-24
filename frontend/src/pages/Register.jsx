import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import Header from '../components/Header';
import ApiTest from '../components/ApiTest';

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

  const styles = {
    container: {
      maxWidth: '400px',
      margin: '2rem auto',
      padding: '2rem',
      borderRadius: '8px',
      boxShadow: '0 2px 10px rgba(0, 0, 0, 0.1)',
      backgroundColor: '#fff'
    },
    heading: {
      textAlign: 'center',
      marginBottom: '1.5rem',
      color: '#333'
    },
    form: {
      display: 'flex',
      flexDirection: 'column'
    },
    formGroup: {
      marginBottom: '1rem'
    },
    label: {
      display: 'block',
      marginBottom: '0.5rem',
      fontWeight: 500
    },
    input: {
      width: '100%',
      padding: '0.75rem',
      border: '1px solid #ddd',
      borderRadius: '4px',
      fontSize: '1rem'
    },
    small: {
      display: 'block',
      marginTop: '0.25rem',
      fontSize: '0.8rem',
      color: '#666'
    },
    button: {
      padding: '0.75rem 1rem',
      marginTop: '1rem',
      border: 'none',
      borderRadius: '4px',
      backgroundColor: '#4285f4',
      color: 'white',
      fontSize: '1rem',
      cursor: 'pointer',
      transition: 'background-color 0.3s'
    },
    buttonDisabled: {
      backgroundColor: '#a4c2f4',
      cursor: 'not-allowed'
    },
    errorMessage: {
      color: '#d93025',
      marginBottom: '1rem',
      padding: '0.5rem',
      backgroundColor: '#fde7e9',
      borderRadius: '4px',
      textAlign: 'center'
    },
    successMessage: {
      color: '#0f5132',
      marginBottom: '1rem',
      padding: '0.5rem',
      backgroundColor: '#d1edff',
      borderRadius: '4px',
      textAlign: 'center'
    },
    loginLink: {
      marginTop: '1.5rem',
      textAlign: 'center',
      fontSize: '0.9rem'
    },
    link: {
      color: '#4285f4',
      textDecoration: 'none'
    }
  };

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
      // The backend will handle password hashing with BCrypt
      const response = await authService.register({
        username: formData.username,
        password: formData.password,
        email: formData.email
      });

      // Registration successful, show success message
      setSuccess(response.message || 'Registration successful!');

      // Clear form
      setFormData({
        username: '',
        password: '',
        confirmPassword: '',
        email: ''
      });

      // Redirect to login after 2 seconds
      setTimeout(() => {
        navigate('/login', { state: { message: 'Registration successful! Please log in.' } });
      }, 2000);

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

      <div style={styles.container}>
        <h2 style={styles.heading}>Create an Account</h2>
        {error && <div style={styles.errorMessage}>{error}</div>}
        {success && <div style={styles.successMessage}>{success}</div>}
        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label htmlFor="username" style={styles.label}>Username</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
              style={styles.input}
            />
          </div>

          <div style={styles.formGroup}>
            <label htmlFor="email" style={styles.label}>Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              style={styles.input}
            />
            <small style={styles.small}>Email is for display purposes only</small>
          </div>

          <div style={styles.formGroup}>
            <label htmlFor="password" style={styles.label}>Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              style={styles.input}
            />
            <small style={styles.small}>Password must be at least 6 characters long</small>
          </div>

          <div style={styles.formGroup}>
            <label htmlFor="confirmPassword" style={styles.label}>Confirm Password</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              style={styles.input}
            />
          </div>

          <button
            type="submit"
            style={loading ? { ...styles.button, ...styles.buttonDisabled } : styles.button}
            disabled={loading}
          >
            {loading ? 'Registering...' : 'Register'}
          </button>
        </form>
        <div style={styles.loginLink}>
          Already have an account? <Link to="/login" style={styles.link}>Log in here</Link>
        </div>
      </div>
    </>
  );
};

export default Register;
