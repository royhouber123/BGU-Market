#!/bin/bash

# Store API Testing Script
BASE_URL="http://localhost:8080/api/stores"
USER_BASE_URL="http://localhost:8080/api/users"
AUTH_BASE_URL="http://localhost:8080/api/auth"

echo "===== BGU Market Store API Testing ====="
echo

# First, let's register a user and get a token (if needed)
echo "1. Register a test user..."
curl -X POST "$USER_BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser", "password":"password123"}' \
  --silent | jq '.'
echo

echo "2. Login to get token..."
TOKEN_RESPONSE=$(curl -X POST "$AUTH_BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser", "password":"password123"}' \
  --silent)
echo $TOKEN_RESPONSE | jq '.'
TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.data.token // empty')
echo "Token: $TOKEN"
echo

# Test Store Creation
echo "3. Create a new store..."
curl -X POST "$BASE_URL/create" \
  -H "Content-Type: application/json" \
  -d '{"storeName":"TestStore", "founderId":"testuser"}' \
  --silent | jq '.'
echo

# Test Get Store
echo "4. Get store information..."
curl -X GET "$BASE_URL/TestStore" \
  --silent | jq '.'
echo

# Test Store Management Operations
echo "5. Register another user for ownership tests..."
curl -X POST "$USER_BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"newowner", "password":"password123"}' \
  --silent | jq '.'
echo

echo "6. Add additional store owner..."
curl -X POST "$BASE_URL/owners/add" \
  -H "Content-Type: application/json" \
  -d '{"appointerID":"testuser", "newOwnerID":"newowner", "storeID":"2"}' \
  --silent | jq '.'
echo

echo "7. Register a manager user..."
curl -X POST "$USER_BASE_URL/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"manager1", "password":"password123"}' \
  --silent | jq '.'
echo

echo "8. Add new manager..."
curl -X POST "$BASE_URL/managers/add" \
  -H "Content-Type: application/json" \
  -d '{"appointerID":"testuser", "newManagerName":"manager1", "storeID":"2"}' \
  --silent | jq '.'
echo

echo "9. Add permission to manager..."
curl -X POST "$BASE_URL/managers/permissions/add" \
  -H "Content-Type: application/json" \
  -d '{"managerID":"manager1", "appointerID":"testuser", "permissionID":1, "storeID":"2"}' \
  --silent | jq '.'
echo

echo "10. Get manager permissions..."
curl -X GET "$BASE_URL/2/managers/manager1/permissions?whoIsAsking=testuser" \
  --silent | jq '.'
echo

# Test Listing Operations
echo "11. Add new listing to store..."
curl -X POST "$BASE_URL/listings/add" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"testuser",
    "storeID":"2",
    "productId":"PROD001",
    "productName":"Test Product",
    "productCategory":"Electronics",
    "productDescription":"A test product for demonstration",
    "quantity":10,
    "price":29.99
  }' \
  --silent | jq '.'
echo

echo "12. Edit listing price..."
curl -X PUT "$BASE_URL/listings/price" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"testuser",
    "storeID":"2",
    "listingId":"PROD001",
    "newPrice":24.99
  }' \
  --silent | jq '.'
echo

echo "13. Edit listing product name..."
curl -X PUT "$BASE_URL/listings/name" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"testuser",
    "storeID":"2",
    "listingId":"PROD001",
    "newValue":"Updated Test Product"
  }' \
  --silent | jq '.'
echo

echo "14. Edit listing description..."
curl -X PUT "$BASE_URL/listings/description" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"testuser",
    "storeID":"2",
    "listingId":"PROD001",
    "newValue":"An updated description for the test product"
  }' \
  --silent | jq '.'
echo

echo "15. Edit listing quantity..."
curl -X PUT "$BASE_URL/listings/quantity" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"testuser",
    "storeID":"2",
    "listingId":"PROD001",
    "newQuantity":15
  }' \
  --silent | jq '.'
echo

echo "16. Edit listing category..."
curl -X PUT "$BASE_URL/listings/category" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"testuser",
    "storeID":"2",
    "listingId":"PROD001",
    "newValue":"Technology"
  }' \
  --silent | jq '.'
echo

# Test Query Operations
echo "17. Get product price..."
curl -X GET "$BASE_URL/2/products/PROD001/price" \
  --silent | jq '.'
echo

echo "18. Check if user is owner..."
curl -X GET "$BASE_URL/2/owners/testuser/check" \
  --silent | jq '.'
echo

echo "19. Check if user is manager..."
curl -X GET "$BASE_URL/2/managers/manager1/check" \
  --silent | jq '.'
echo

echo "20. Get all stores and products information..."
curl -X GET "$BASE_URL/info" \
  --silent | jq '.'
echo

# Test Store State Operations
echo "21. Close store..."
curl -X POST "$BASE_URL/2/close?userName=testuser" \
  --silent | jq '.'
echo

echo "22. Open store..."
curl -X POST "$BASE_URL/2/open?userName=testuser" \
  --silent | jq '.'
echo

# Test Remove Operations
echo "23. Remove permission from manager..."
curl -X DELETE "$BASE_URL/2/managers/manager1/permissions/1?appointerID=testuser" \
  --silent | jq '.'
echo

echo "24. Remove listing..."
curl -X DELETE "$BASE_URL/2/listings/PROD001?userName=testuser" \
  --silent | jq '.'
echo

echo "25. Remove owner..."
curl -X DELETE "$BASE_URL/2/owners/newowner?requesterId=testuser" \
  --silent | jq '.'
echo

echo "===== Store API Testing Complete =====" 