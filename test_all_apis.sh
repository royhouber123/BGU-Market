# Extract user ID from the JWT token or use a default
USER_ID="1"

# Test purchase execution
echo "💳 Executing purchase..."
EXECUTE_PURCHASE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/purchases/execute" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"userId\":$USER_ID,
    \"paymentDetails\":\"Credit Card: **** **** **** 1234\",
    \"shippingAddress\":\"123 Test St, Test City, TC 12345\"
  }")
check_success_continue "$EXECUTE_PURCHASE_RESPONSE"

# Test bid submission
echo "🏷️ Submitting a bid..."
BID_SUBMISSION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/purchases/bid/submit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"userId\":$USER_ID,
    \"storeId\":1,
    \"productId\":1,
    \"bidAmount\":75.00,
    \"quantity\":1
  }")
check_success_continue "$BID_SUBMISSION_RESPONSE"

# Test bid status check
echo "📊 Checking bid status..."
BID_STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/purchases/bid/status/1/1/$USER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")
check_success_continue "$BID_STATUS_RESPONSE"

# Test auction offer submission
echo "🎯 Submitting auction offer..."
AUCTION_OFFER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/purchases/auction/offer" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "{
    \"userId\":$USER_ID,
    \"storeId\":1,
    \"productId\":1,
    \"offerAmount\":120.00
  }")
check_success_continue "$AUCTION_OFFER_RESPONSE"

# Test auction status check
echo "📈 Checking auction status..."
AUCTION_STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/purchases/auction/status/$USER_ID/1/1" \
  -H "Authorization: Bearer $JWT_TOKEN")
check_success_continue "$AUCTION_STATUS_RESPONSE"

# Test user purchase history
echo "📜 Getting user purchase history..."
USER_PURCHASE_HISTORY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/purchases/user/$USER_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")
check_success_continue "$USER_PURCHASE_HISTORY_RESPONSE"

# Test store purchase history
echo "🏪 Getting store purchase history..."
STORE_PURCHASE_HISTORY_RESPONSE=$(curl -s -X GET "$BASE_URL/api/purchases/store/1" \
  -H "Authorization: Bearer $JWT_TOKEN")
check_success_continue "$STORE_PURCHASE_HISTORY_RESPONSE" 