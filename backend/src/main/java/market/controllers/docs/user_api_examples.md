# User API Endpoints - Curl Examples

Base URL: `http://localhost:8080/api/users`

## User Registration

### 1. Register Guest User
Register a guest user without password (temporary session).
```bash
curl -X POST "http://localhost:8080/api/users/register/guest" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "guest_user_123"
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Guest user registered successfully",
  "data": null
}
```

### 2. Register Regular User
Register a user with username and password.
```bash
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "regularuser",
    "password": "securepassword123"
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": null
}
```

## User Management

### 3. Delete User
Delete a user account (requires authentication).
```bash
curl -X DELETE "http://localhost:8080/api/users/testuser" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null
}
```

### 4. Get Current User Information
Get the current authenticated user's information.
```bash
curl -X GET "http://localhost:8080/api/users/me" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "User information retrieved",
  "data": {
    "username": "testuser",
    "id": "user123",
    "registrationDate": "2024-01-01T10:00:00Z"
  }
}
```

## Profile Management

### 5. Change Username
Update the current user's username.
```bash
curl -X PUT "http://localhost:8080/api/users/username" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "newUsername": "mynewusername"
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Username updated successfully",
  "data": true
}
```

### 6. Change Password
Update the current user's password.
```bash
curl -X PUT "http://localhost:8080/api/users/password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "newPassword": "mynewsecurepassword456"
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Password updated successfully",
  "data": true
}
```

## Shopping Cart Operations

### 7. Add Product to Cart
Add a product from a specific store to the user's shopping cart.
```bash
curl -X POST "http://localhost:8080/api/users/cart/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "storeId": "1",
    "productName": "Laptop",
    "quantity": 2
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Product added to cart successfully",
  "data": null
}
```

### 8. Remove Product from Cart
Remove a specific quantity of a product from the cart.
```bash
curl -X POST "http://localhost:8080/api/users/cart/remove" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "storeId": "1",
    "productName": "Laptop",
    "quantity": 1
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Product removed from cart successfully",
  "data": null
}
```

### 9. Clear Entire Cart
Remove all products from the user's shopping cart.
```bash
curl -X DELETE "http://localhost:8080/api/users/cart" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Cart cleared successfully",
  "data": null
}
```

### 10. Get Shopping Cart
Retrieve the current user's shopping cart contents.
```bash
curl -X GET "http://localhost:8080/api/users/cart" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "items": [
      {
        "storeId": "1",
        "productName": "Laptop",
        "quantity": 1,
        "price": 999.99
      },
      {
        "storeId": "2",
        "productName": "Mouse",
        "quantity": 2,
        "price": 25.50
      }
    ],
    "totalPrice": 1050.99
  }
}
```

## Complete User Workflow Example

### Step 1: Register a new user
```bash
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "shopper123",
    "password": "mypassword"
  }'
```

### Step 2: Login to get authentication token
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "shopper123",
    "password": "mypassword"
  }'
```

### Step 3: Use the token for authenticated operations
```bash
# Save token from login response
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Get user info
curl -X GET "http://localhost:8080/api/users/me" \
  -H "Authorization: Bearer $TOKEN"

# Add products to cart
curl -X POST "http://localhost:8080/api/users/cart/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "storeId": "1",
    "productName": "Laptop",
    "quantity": 1
  }'

# View cart
curl -X GET "http://localhost:8080/api/users/cart" \
  -H "Authorization: Bearer $TOKEN"

# Update username
curl -X PUT "http://localhost:8080/api/users/username" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "newUsername": "newshopper123"
  }'
```

## Error Response Examples

### Registration with Existing Username
```json
{
  "success": false,
  "message": "Username already exists",
  "data": null
}
```

### Unauthorized Access
```json
{
  "success": false,
  "message": "Authentication required",
  "data": null
}
```

### Invalid Product for Cart
```json
{
  "success": false,
  "message": "Product not found in store",
  "data": null
}
```

### Insufficient Quantity in Cart
```json
{
  "success": false,
  "message": "Not enough quantity in cart to remove",
  "data": null
}
```

## Notes

- **Authentication Required**: All endpoints except registration require a valid JWT token
- **Token Format**: Include JWT token in the Authorization header as `Bearer TOKEN` or just `TOKEN`
- **Guest vs Regular Users**: Guest users have limited functionality compared to registered users
- **Cart Persistence**: Shopping cart contents are preserved across sessions for registered users
- **Username Uniqueness**: Usernames must be unique across the system
- **Password Security**: Use strong passwords; the system may have password requirements
- **Error Handling**: All endpoints return standardized `ApiResponse<T>` format
- **Base URL**: Replace `localhost:8080` with your actual server address and port

## Integration with Store API

To add products to cart, you'll typically need to:

1. First browse stores and products using Store API endpoints
2. Find the store ID and product name you want
3. Use the cart endpoints to add/remove items
4. Proceed to checkout (using other payment/order APIs)

Example workflow:
```bash
# Get store info to find products
curl -X GET "http://localhost:8080/api/stores/info"

# Add found product to cart
curl -X POST "http://localhost:8080/api/users/cart/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "storeId": "1",
    "productName": "ProductFoundInStore",
    "quantity": 1
  }'
``` 