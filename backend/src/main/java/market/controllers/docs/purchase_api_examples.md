# Purchase API Endpoints - Curl Examples

Base URL: `http://localhost:8080/api/purchases`

## Important: Parameter Types

**All ID parameters (userId, storeId, productId) must be integers, not strings.**
- ✅ Correct: `"userId": 1`, `/user/1`
- ❌ Incorrect: `"userId": "1"`, `/user/admin`

## Purchase Operations

### 1. Execute Purchase
Execute a regular purchase from user's shopping cart.
```bash
curl -X POST "http://localhost:8080/api/purchases/execute" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "userId": 1,
    "paymentDetails": "Credit Card: **** **** **** 1234",
    "shippingAddress": "123 Main St, City, State 12345"
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Purchase completed successfully",
  "data": "Purchase ID: PUR_20241205_001"
}
```

## Auction Operations

### 2. Submit Auction Offer
Submit an offer for an auction item.
```bash
curl -X POST "http://localhost:8080/api/purchases/auction/offer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "userId": 1,
    "storeId": 5,
    "productId": 10,
    "offerAmount": 150.00
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Auction offer submitted successfully",
  "data": null
}
```

### 3. Get Auction Status
Get the current status of an auction.
```bash
curl -X GET "http://localhost:8080/api/purchases/auction/status/1/5/10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Auction status retrieved",
  "data": {
    "isActive": true,
    "currentHighestBid": 175.00,
    "status": "ACTIVE",
    "endTime": "2024-12-10T15:30:00"
  }
}
```

## Bid Operations

### 4. Submit Bid
Submit a bid for a product.
```bash
curl -X POST "http://localhost:8080/api/purchases/bid/submit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "userId": 1,
    "storeId": 5,
    "productId": 15,
    "bidAmount": 85.00,
    "quantity": 2
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Bid submitted successfully",
  "data": null
}
```

### 5. Get Bid Status
Get the status of a specific bid.
```bash
curl -X GET "http://localhost:8080/api/purchases/bid/status/5/15/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Bid status retrieved",
  "data": "PENDING_APPROVAL"
}
```

### 6. Accept Counter Offer
Accept a counter offer from the store.
```bash
curl -X POST "http://localhost:8080/api/purchases/bid/counter/accept" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "storeId": 5,
    "productId": 15,
    "userId": 1
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Counter offer accepted successfully",
  "data": null
}
```

### 7. Decline Counter Offer
Decline a counter offer from the store.
```bash
curl -X POST "http://localhost:8080/api/purchases/bid/counter/decline" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "storeId": 5,
    "productId": 15,
    "userId": 1
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Counter offer declined successfully",
  "data": null
}
```

## Purchase History

### 8. Get User Purchase History
Retrieve purchase history for a specific user.
```bash
curl -X GET "http://localhost:8080/api/purchases/user/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Purchase history retrieved",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "storeId": 5,
      "productId": 10,
      "quantity": 2,
      "totalPrice": 199.98,
      "purchaseDate": "2024-12-05T10:30:00",
      "status": "COMPLETED"
    },
    {
      "id": 2,
      "userId": 1,
      "storeId": 3,
      "productId": 7,
      "quantity": 1,
      "totalPrice": 49.99,
      "purchaseDate": "2024-12-03T14:15:00",
      "status": "SHIPPED"
    }
  ]
}
```

### 9. Get Store Purchase History
Retrieve purchase history for a specific store.
```bash
curl -X GET "http://localhost:8080/api/purchases/store/5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Store purchase history retrieved",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "storeId": 5,
      "productId": 10,
      "quantity": 2,
      "totalPrice": 199.98,
      "purchaseDate": "2024-12-05T10:30:00",
      "status": "COMPLETED"
    },
    {
      "id": 3,
      "userId": 2,
      "storeId": 5,
      "productId": 12,
      "quantity": 1,
      "totalPrice": 79.99,
      "purchaseDate": "2024-12-04T16:45:00",
      "status": "PROCESSING"
    }
  ]
}
```

## Complete Purchase Flow Example

### Step 1: Add items to cart (using User API)
```bash
curl -X POST "http://localhost:8080/api/users/cart/add" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "storeId": 5,
    "productName": "Gaming Laptop",
    "quantity": 1
  }'
```

### Step 2: Execute the purchase
```bash
curl -X POST "http://localhost:8080/api/purchases/execute" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "userId": 1,
    "paymentDetails": "Credit Card: **** **** **** 1234",
    "shippingAddress": "123 Main St, City, State 12345"
  }'
```

### Step 3: Check purchase history
```bash
curl -X GET "http://localhost:8080/api/purchases/user/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Auction Flow Example

### Step 1: Submit an auction offer
```bash
curl -X POST "http://localhost:8080/api/purchases/auction/offer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "userId": 1,
    "storeId": 5,
    "productId": 10,
    "offerAmount": 150.00
  }'
```

### Step 2: Monitor auction status
```bash
curl -X GET "http://localhost:8080/api/purchases/auction/status/1/5/10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Step 3: Continue monitoring until auction ends or you're outbid
```bash
# Check periodically
curl -X GET "http://localhost:8080/api/purchases/auction/status/1/5/10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## Bid Flow Example

### Step 1: Submit a bid
```bash
curl -X POST "http://localhost:8080/api/purchases/bid/submit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "userId": 1,
    "storeId": 5,
    "productId": 15,
    "bidAmount": 85.00,
    "quantity": 2
  }'
```

### Step 2: Check bid status
```bash
curl -X GET "http://localhost:8080/api/purchases/bid/status/5/15/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Step 3: Handle counter offers (if received)
```bash
# Accept counter offer
curl -X POST "http://localhost:8080/api/purchases/bid/counter/accept" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "storeId": 5,
    "productId": 15,
    "userId": 1
  }'

# OR decline counter offer
curl -X POST "http://localhost:8080/api/purchases/bid/counter/decline" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "storeId": 5,
    "productId": 15,
    "userId": 1
  }'
```

## Error Response Examples

### Purchase Failed - Insufficient Inventory
```json
{
  "success": false,
  "message": "Purchase failed: Insufficient inventory for product",
  "data": null
}
```

### Invalid Bid Amount
```json
{
  "success": false,
  "message": "Bid submission failed: Bid amount must be positive",
  "data": null
}
```

### Auction Not Found
```json
{
  "success": false,
  "message": "Failed to get auction status: Auction not found",
  "data": null
}
```

### Unauthorized Access
```json
{
  "success": false,
  "message": "Access denied: You don't have permission to view this purchase history",
  "data": null
}
```

### Parameter Type Mismatch (Common Error)
```json
{
  "success": false,
  "message": "Failed to convert value of type 'java.lang.String' to required type 'int'",
  "data": null
}
```

## Testing Environment Variables

For easier testing, set these environment variables:

```bash
# Set your JWT token after login
export JWT_TOKEN="your_jwt_token_here"

# Set user ID for testing (INTEGER - no quotes in usage)
export USER_ID=1

# Set store ID for testing (INTEGER - no quotes in usage)
export STORE_ID=5

# Set product ID for testing (INTEGER - no quotes in usage)
export PRODUCT_ID=10

# Use in curl commands
curl -X GET "http://localhost:8080/api/purchases/user/$USER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Parameter Requirements

### Required Integer Parameters:
- **userId**: Must be a positive integer representing an existing user
- **storeId**: Must be a positive integer representing an existing store
- **productId**: Must be a positive integer representing an existing product

### Required Numeric Parameters:
- **offerAmount**: Must be a positive decimal number
- **bidAmount**: Must be a positive decimal number
- **quantity**: Must be a positive integer

### String Parameters:
- **paymentDetails**: String containing payment information
- **shippingAddress**: String containing shipping address

## Notes

- **CRITICAL**: All ID parameters (userId, storeId, productId) must be integers, not strings
- All endpoints require authentication using JWT tokens
- Replace `YOUR_JWT_TOKEN_HERE` with your actual JWT token obtained from login
- User IDs, Store IDs, and Product IDs should be valid existing entities
- Payment details and shipping addresses are currently stored as strings
- Auction and bid operations may have specific business rules enforced by the backend
- Purchase history is returned as a list of Purchase objects
- All monetary amounts should be positive numbers
- Quantity values should be positive integers

## Related Documentation

- [User API Examples](user_api_examples.md) - For cart management before purchase
- [Store API Examples](store_api_examples.md) - For product and store information
- [Auth API Examples](auth_api_examples.md) - For authentication and JWT tokens 