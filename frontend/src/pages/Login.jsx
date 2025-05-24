import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import Header from '../components/Header';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Check if there's a success message from registration
  useEffect(() => {
    if (location.state?.message) {
      setSuccess(location.state.message);
      // Clear the state after showing the message
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

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
    registerLink: {
      marginTop: '1.5rem',
      textAlign: 'center',
      fontSize: '0.9rem'
    },
    link: {
      color: '#4285f4',
      textDecoration: 'none'
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      // The AuthService will handle BCrypt password verification on the backend
      const success = await login(username, password);
      if (success) {
        navigate('/Home');
      } else {
        setError('Failed to log in. Please check your credentials.');
      }
    } catch (error) {
      setError('Failed to log in: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.heading}>Login to BGU Market</h2>
      {error && <div style={styles.errorMessage}>{error}</div>}
      {success && <div style={styles.successMessage}>{success}</div>}
      <form onSubmit={handleSubmit} style={styles.form}>
        <div style={styles.formGroup}>
          <label htmlFor="username" style={styles.label}>Username</label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            style={styles.input}
          />
        </div>
        <div style={styles.formGroup}>
          <label htmlFor="password" style={styles.label}>Password</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={styles.input}
          />
        </div>
        <button
          type="submit"
          style={loading ? { ...styles.button, ...styles.buttonDisabled } : styles.button}
          disabled={loading}
        >
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>
      <div style={styles.registerLink}>
        Don't have an account? <Link to="/register" style={styles.link}>Register here</Link>
      </div>
    </div>
  );
};

export default Login;
