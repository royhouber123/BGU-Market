import React, { useState } from 'react';
import { authService } from '../services/authService';
import { Box, Button, TextField, Typography, Alert, Paper } from '@mui/material';

const ApiTest = () => {
	const [username, setUsername] = useState('testuser123');
	const [password, setPassword] = useState('testpass');
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

	const testBackendDirect = async () => {
		setLoading(true);
		setResult('Testing direct backend call...');
		try {
			const response = await fetch('http://localhost:8080/api/users/register', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ username: username + '_direct', password })
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
		<Paper sx={{ p: 3, m: 2, maxWidth: 600 }}>
			<Typography variant="h6" gutterBottom>
				API Integration Test
			</Typography>

			<Box sx={{ mb: 2 }}>
				<TextField
					label="Username"
					value={username}
					onChange={(e) => setUsername(e.target.value)}
					sx={{ mr: 1, mb: 1 }}
				/>
				<TextField
					label="Password"
					value={password}
					onChange={(e) => setPassword(e.target.value)}
					sx={{ mb: 1 }}
				/>
			</Box>

			<Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
				<Button
					variant="contained"
					onClick={testRegister}
					disabled={loading}
					size="small"
				>
					Test Register
				</Button>
				<Button
					variant="contained"
					onClick={testLogin}
					disabled={loading}
					size="small"
				>
					Test Login
				</Button>
				<Button
					variant="contained"
					onClick={testGetUser}
					disabled={loading}
					size="small"
				>
					Test Get User
				</Button>
				<Button
					variant="outlined"
					onClick={testBackendDirect}
					disabled={loading}
					size="small"
				>
					Test Direct
				</Button>
			</Box>

			{result && (
				<Alert
					severity={result.includes('✅') ? 'success' : 'error'}
					sx={{ mt: 2 }}
				>
					<pre style={{ margin: 0, fontSize: '12px', whiteSpace: 'pre-wrap' }}>
						{result}
					</pre>
				</Alert>
			)}
		</Paper>
	);
};

export default ApiTest; 