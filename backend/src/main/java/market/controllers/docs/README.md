# BGU Market API Documentation

This directory contains comprehensive curl examples for all API endpoints in the BGU Market application.

## Available API Documentation

### 📁 Controller APIs

- **[Auth API Examples](auth_api_examples.md)** - Authentication endpoints
  - User login/logout
  - Token validation
  - Authentication workflows

- **[User API Examples](user_api_examples.md)** - User management endpoints
  - User registration (guest and regular)
  - Profile management (username, password changes)
  - Shopping cart operations

- **[Store API Examples](store_api_examples.md)** - Store management endpoints
  - Store creation and management
  - Owner and manager operations
  - Product listing management
  - Store policies and discounts

## Quick Start Guide

### 1. Authentication Flow
```bash
# Register a new user
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'

# Login to get token
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'

# Use token for authenticated requests
curl -X GET "http://localhost:8080/api/users/me" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### 2. Basic Store Operations
```bash
# Create a store (requires authentication)
curl -X POST "http://localhost:8080/api/stores/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{"storeName": "MyStore", "founderId": "testuser"}'

# Add a product to the store
curl -X POST "http://localhost:8080/api/stores/listings/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "userName": "testuser",
    "storeID": "1",
    "productId": "PROD001",
    "productName": "Test Product",
    "productCategory": "Electronics",
    "productDescription": "A test product",
    "quantity": 10,
    "price": 99.99
  }'
```

### 3. Shopping Cart Operations
```bash
# Add product to cart
curl -X POST "http://localhost:8080/api/users/cart/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "storeId": "1",
    "productName": "Test Product",
    "quantity": 2
  }'

# View cart contents
curl -X GET "http://localhost:8080/api/users/cart" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## API Response Format

All APIs return responses in the following standardized format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data (can be null, object, array, etc.)
  }
}
```

### Success Response Example
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": null
}
```

### Error Response Example
```json
{
  "success": false,
  "message": "Username already exists",
  "data": null
}
```

## Authentication

Most endpoints require authentication using JWT tokens:

1. **Register** a user using `/api/users/register`
2. **Login** using `/api/auth/login` to get a JWT token
3. **Include** the token in subsequent requests using the `Authorization` header:
   ```bash
   -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
   ```

## Base URLs

- **Development**: `http://localhost:8080`
- **Production**: Update with your actual server URL

## Common HTTP Status Codes

- `200 OK` - Request successful
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required or invalid
- `403 Forbidden` - Access denied
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Testing Tools

### Using curl (Command Line)
All examples in this documentation use curl commands that can be run directly from the terminal.

### Using Postman
You can import these curl commands into Postman:
1. Open Postman
2. Click "Import" 
3. Select "Raw text"
4. Paste any curl command from the documentation
5. Postman will automatically parse it

### Using Browser Dev Tools
For GET requests, you can also test directly in browser console:
```javascript
fetch('http://localhost:8080/api/stores/info')
  .then(response => response.json())
  .then(data => console.log(data));
```

## Environment Variables

For easier testing, you can set environment variables:

```bash
# Set base URL
export API_BASE_URL="http://localhost:8080"

# Set token after login
export JWT_TOKEN="your_jwt_token_here"

# Use in curl commands
curl -X GET "$API_BASE_URL/api/users/me" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## File Structure

```
backend/src/main/java/market/controllers/docs/
├── README.md                 # This file - overview and navigation
├── auth_api_examples.md      # Authentication API examples
├── user_api_examples.md      # User management API examples
└── store_api_examples.md     # Store management API examples
```

## Contributing

When adding new API endpoints:

1. Update the corresponding controller documentation file
2. Follow the existing format with:
   - Clear endpoint descriptions
   - Complete curl examples
   - Request/response examples
   - Error scenarios
3. Update this README if new API categories are added

## Support

For issues or questions about the APIs:
1. Check the relevant documentation file first
2. Review error responses and status codes
3. Ensure authentication tokens are valid and included
4. Verify request payload structure matches the examples 