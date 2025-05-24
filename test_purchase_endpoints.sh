#!/bin/bash

# Test script for Purchase Controller endpoints
# Make sure the Spring Boot application is running on localhost:8080

BASE_URL="http://localhost:8080/api"
CONTENT_TYPE="Content-Type: application/json"

echo "==============================================="
echo "Testing Purchase Controller Endpoints"
echo "==============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Note: Most endpoints require valid data setup (users, stores, products, etc.)${NC}"
echo ""

# Test 1: Execute Regular Purchase
echo -e "${YELLOW}1. Testing Regular Purchase Execution${NC}"
echo "POST /api/purchases/execute"
curl -X POST "$BASE_URL/purchases/execute" \
  -H "$CONTENT_TYPE" \
  -d '{
    "userId": "testuser1",
    "shippingAddress": "123 Test Street, Test City",
    "contactInfo": "test@email.com"
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 2: Submit Auction Offer
echo -e "${YELLOW}2. Testing Auction Offer Submission${NC}"
echo "POST /api/purchases/auction/offer"
curl -X POST "$BASE_URL/purchases/auction/offer" \
  -H "$CONTENT_TYPE" \
  -d '{
    "storeId": "store1",
    "productId": "product1",
    "userId": "testuser1",
    "offerPrice": 150.00,
    "shippingAddress": "123 Test Street, Test City",
    "contactInfo": "test@email.com"
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 3: Open Auction
echo -e "${YELLOW}3. Testing Auction Opening${NC}"
echo "POST /api/purchases/auction/open"
curl -X POST "$BASE_URL/purchases/auction/open" \
  -H "$CONTENT_TYPE" \
  -d '{
    "userId": "storeowner1",
    "storeId": "store1",
    "productId": "auctionproduct1",
    "productName": "Auction Item",
    "productCategory": "Electronics",
    "productDescription": "Test auction item",
    "startingPrice": 100,
    "endTimeMillis": 1735689600000
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 4: Get Auction Status
echo -e "${YELLOW}4. Testing Auction Status Retrieval${NC}"
echo "GET /api/purchases/auction/status/{userId}/{storeId}/{productId}"
curl -X GET "$BASE_URL/purchases/auction/status/testuser1/store1/product1" \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 5: Submit Bid
echo -e "${YELLOW}5. Testing Bid Submission${NC}"
echo "POST /api/purchases/bid/submit"
curl -X POST "$BASE_URL/purchases/bid/submit" \
  -H "$CONTENT_TYPE" \
  -d '{
    "storeId": "store1",
    "productId": "product1",
    "userId": "testuser1",
    "offerPrice": 120.00,
    "shippingAddress": "123 Test Street, Test City",
    "contactInfo": "test@email.com"
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 6: Approve Bid
echo -e "${YELLOW}6. Testing Bid Approval${NC}"
echo "POST /api/purchases/bid/approve"
curl -X POST "$BASE_URL/purchases/bid/approve" \
  -H "$CONTENT_TYPE" \
  -d '{
    "storeId": "store1",
    "productId": "product1",
    "userId": "testuser1",
    "approverId": "approver1"
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 7: Reject Bid
echo -e "${YELLOW}7. Testing Bid Rejection${NC}"
echo "POST /api/purchases/bid/reject"
curl -X POST "$BASE_URL/purchases/bid/reject" \
  -H "$CONTENT_TYPE" \
  -d '{
    "storeId": "store1",
    "productId": "product1",
    "userId": "testuser1",
    "approverId": "approver1"
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 8: Propose Counter Bid
echo -e "${YELLOW}8. Testing Counter Bid Proposal${NC}"
echo "POST /api/purchases/bid/counter"
curl -X POST "$BASE_URL/purchases/bid/counter" \
  -H "$CONTENT_TYPE" \
  -d '{
    "storeId": "store1",
    "productId": "product1",
    "userId": "testuser1",
    "approverId": "approver1",
    "newAmount": 110.00
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 9: Accept Counter Offer
echo -e "${YELLOW}9. Testing Counter Offer Acceptance${NC}"
echo "POST /api/purchases/bid/counter/accept"
curl -X POST "$BASE_URL/purchases/bid/counter/accept" \
  -H "$CONTENT_TYPE" \
  -d '{
    "storeId": "store1",
    "productId": "product1",
    "userId": "testuser1"
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 10: Decline Counter Offer
echo -e "${YELLOW}10. Testing Counter Offer Decline${NC}"
echo "POST /api/purchases/bid/counter/decline"
curl -X POST "$BASE_URL/purchases/bid/counter/decline" \
  -H "$CONTENT_TYPE" \
  -d '{
    "storeId": "store1",
    "productId": "product1",
    "userId": "testuser1"
  }' \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 11: Get Bid Status
echo -e "${YELLOW}11. Testing Bid Status Retrieval${NC}"
echo "GET /api/purchases/bid/status/{storeId}/{productId}/{userId}"
curl -X GET "$BASE_URL/purchases/bid/status/store1/product1/testuser1" \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 12: Get Purchases by User
echo -e "${YELLOW}12. Testing Purchases by User${NC}"
echo "GET /api/purchases/user/{userId}"
curl -X GET "$BASE_URL/purchases/user/testuser1" \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""

# Test 13: Get Purchases by Store
echo -e "${YELLOW}13. Testing Purchases by Store${NC}"
echo "GET /api/purchases/store/{storeId}"
curl -X GET "$BASE_URL/purchases/store/store1" \
  -w "\nStatus: %{http_code}\n" && echo ""

echo ""
echo -e "${GREEN}==============================================="
echo "Purchase Controller Testing Complete!"
echo -e "===============================================${NC}"
echo ""
echo "Note: Many of these endpoints will return errors because they require:"
echo "- Valid users to be registered"
echo "- Stores to be created"
echo "- Products to be added"
echo "- Shopping carts to have items"
echo "- Proper authentication tokens"
echo ""
echo "The important thing is that the endpoints are accessible and return proper HTTP responses." 