# Auth API Endpoints - Curl Examples

Base URL: `http://localhost:8080/api/auth`

## Authentication Operations

### 1. User Login
Authenticate a user with username and password.
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "testuser"
  }
}
```

### 2. Validate Token
Validate if a JWT token is still valid.
```bash
curl -X POST "http://localhost:8080/api/auth/validate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Alternative format (without Bearer prefix):**
```bash
curl -X POST "http://localhost:8080/api/auth/validate" \
  -H "Content-Type: application/json" \
  -H "Authorization: YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Token is valid",
  "data": null
}
```

### 3. User Logout
Logout and invalidate the current JWT token.
```bash
curl -X POST "http://localhost:8080/api/auth/logout" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

## Complete Authentication Flow Example

### Step 1: Register a new user first (using User API)
```bash
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "securepassword123"
  }'
```

### Step 2: Login with the registered user
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "securepassword123"
  }'
```

### Step 3: Save the token from login response and use it for subsequent requests
```bash
# Example with token saved to variable
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Validate the token
curl -X POST "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer $TOKEN"

# Make authenticated requests to other APIs
curl -X GET "http://localhost:8080/api/users/me" \
  -H "Authorization: Bearer $TOKEN"

# Logout when done
curl -X POST "http://localhost:8080/api/auth/logout" \
  -H "Authorization: Bearer $TOKEN"
```

## Error Response Examples

### Invalid Login Credentials
```json
{
  "success": false,
  "message": "Invalid username or password",
  "data": null
}
```

### Invalid Token
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "data": null
}
```

### Missing Authorization Header
```json
{
  "success": false,
  "message": "Authorization header is required",
  "data": null
}
```

## Notes

- All endpoints return responses in the standard `ApiResponse<T>` format
- JWT tokens should be included in the `Authorization` header for protected endpoints
- Tokens can be prefixed with "Bearer " but it's optional (the controller handles both formats)
- Replace `localhost:8080` with your actual server address and port
- Store the JWT token securely and include it in subsequent API calls
- Tokens expire after a certain time (check your JWT configuration)
- Always logout to invalidate tokens when the session is complete

## Authentication Headers

For all protected endpoints in other controllers (Users, Stores), include the JWT token:

```bash
-H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

or

```bash
-H "Authorization: YOUR_JWT_TOKEN_HERE"
``` 