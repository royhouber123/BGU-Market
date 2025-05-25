import React, { useState } from 'react';
import { authService } from '../../services/authService';
import { storeService } from '../../services/storeService';
import { productService } from '../../services/productService';
import { Box, Button, TextField, Typography, Alert, Paper } from '@mui/material';
import './ApiTest.css';

const ApiTest = () => {
	const [username, setUsername] = useState('');
	const [password, setPassword] = useState('');
	const [result, setResult] = useState('');
	const [loading, setLoading] = useState(false);

	const testRegister = async () => {
		setLoading(true);
		setResult('Testing registration...');
		try {
			const response = await authService.register({ username, password });
			setResult(`✅ Registration success: ${JSON.stringify(response, null, 2)}`);
		} catch (error) {
			setResult(`❌ Registration failed: ${error.message}`);
			console.error('Registration test error:', error);
		} finally {
			setLoading(false);
		}
	};

	const testLogin = async () => {
		setLoading(true);
		setResult('Testing login...');
		try {
			const response = await authService.login(username, password);
			setResult(`✅ Login success: ${JSON.stringify(response, null, 2)}`);
		} catch (error) {
			setResult(`❌ Login failed: ${error.message}`);
			console.error('Login test error:', error);
		} finally {
			setLoading(false);
		}
	};

	const testGetUser = async () => {
		setLoading(true);
		setResult('Testing get user...');
		try {
			const response = await authService.getCurrentUser();
			setResult(`✅ Get user success: ${JSON.stringify(response, null, 2)}`);
		} catch (error) {
			setResult(`❌ Get user failed: ${error.message}`);
			console.error('Get user test error:', error);
		} finally {
			setLoading(false);
		}
	};

	const testStoresAndProducts = async () => {
		setLoading(true);
		setResult('Testing stores and products...');
		try {
			const response = await storeService.getAllStoresAndProducts();
			setResult(`✅ Stores and products success: ${JSON.stringify(response, null, 2)}`);
		} catch (error) {
			setResult(`❌ Stores and products failed: ${error.message}`);
			console.error('Stores and products test error:', error);
		} finally {
			setLoading(false);
		}
	};

	const testSearchProducts = async () => {
		setLoading(true);
		setResult('Testing product search...');
		try {
			const response = await productService.searchProducts('laptop');
			setResult(`✅ Product search success: Found ${response.length} products`);
		} catch (error) {
			setResult(`❌ Product search failed: ${error.message}`);
			console.error('Product search test error:', error);
		} finally {
			setLoading(false);
		}
	};

	const testBackendDirect = async () => {
		setLoading(true);
		setResult('Testing direct backend call...');
		try {
			const response = await fetch('http://localhost:8080/api/stores/info', {
				method: 'GET',
				headers: { 'Content-Type': 'application/json' }
			});
			const data = await response.json();
			setResult(`✅ Direct backend call: ${JSON.stringify(data, null, 2)}`);
		} catch (error) {
			setResult(`❌ Direct backend failed: ${error.message}`);
		} finally {
			setLoading(false);
		}
	};

	return (
		<Paper className="api-test-container">
			<Typography variant="h5" gutterBottom>
				API Test Component
			</Typography>

			<Box className="api-test-inputs">
				<TextField
					className="api-test-username"
					label="Username"
					value={username}
					onChange={(e) => setUsername(e.target.value)}
					variant="outlined"
					size="small"
					placeholder="Enter username"
				/>
				<TextField
					className="api-test-password"
					label="Password"
					type="password"
					value={password}
					onChange={(e) => setPassword(e.target.value)}
					variant="outlined"
					size="small"
					placeholder="Enter password"
				/>
			</Box>

			<Box className="api-test-buttons">
				<Button
					variant="contained"
					onClick={testRegister}
					disabled={loading || !username || !password}
				>
					Test Register
				</Button>
				<Button
					variant="contained"
					onClick={testLogin}
					disabled={loading || !username || !password}
				>
					Test Login
				</Button>
				<Button
					variant="contained"
					onClick={testGetUser}
					disabled={loading}
				>
					Test Get User
				</Button>
				<Button
					variant="contained"
					onClick={testStoresAndProducts}
					disabled={loading}
				>
					Test Stores & Products
				</Button>
				<Button
					variant="contained"
					onClick={testSearchProducts}
					disabled={loading}
				>
					Test Search Products
				</Button>
				<Button
					variant="contained"
					onClick={testBackendDirect}
					disabled={loading}
				>
					Test Direct Backend
				</Button>
			</Box>

			{result && (
				<Alert
					severity={result.includes('✅') ? 'success' : 'error'}
					className="api-test-result"
				>
					<pre className="api-test-result-text">{result}</pre>
				</Alert>
			)}
		</Paper>
	);
};

export default ApiTest; 