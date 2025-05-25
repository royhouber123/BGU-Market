#!/bin/bash

# BGU Market Demo Data Population Script
# This script populates the application with demo data for testing and development

# Configuration
BASE_URL="http://localhost:8080"

echo "🚀 Starting BGU Market Demo Data Population"
echo "Base URL: $BASE_URL"
echo "=============================================="

# Helper function to extract data from JSON response
extract_json_value() {
    echo "$1" | grep -o "\"$2\":\"[^\"]*" | cut -d'"' -f4
}

# Helper function to check if response indicates success
check_success() {
    if echo "$1" | grep -q '"success":true'; then
        echo "✅ SUCCESS"
        return 0
    else
        echo "❌ FAILED"
        echo "Response: $1"
        return 1
    fi
}

# Helper function to check success but continue on failure
check_success_continue() {
    if echo "$1" | grep -q '"success":true'; then
        echo "✅ SUCCESS"
        return 0
    else
        echo "⚠️ FAILED (continuing...)"
        echo "Response: $1"
        return 1
    fi
}

echo ""
echo "1️⃣ CREATING DEMO USERS"
echo "----------------------"

# Create demo users
declare -a USERS=(
    "alice:password123"
    "bob:password123"
    "charlie:password123"
    "diana:password123"
    "eve:password123"
)

declare -a JWT_TOKENS=()

for user_data in "${USERS[@]}"; do
    IFS=':' read -r username password <<< "$user_data"
    
    echo "👤 Creating user: $username"
    USER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
      -H "Content-Type: application/json" \
      -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    
    if check_success_continue "$USER_RESPONSE"; then
        # Login to get JWT token
        echo "🔐 Logging in user: $username"
        LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
          -H "Content-Type: application/json" \
          -d "{\"username\":\"$username\",\"password\":\"$password\"}")
        
        if check_success_continue "$LOGIN_RESPONSE"; then
            JWT_TOKEN=$(extract_json_value "$LOGIN_RESPONSE" "token")
            JWT_TOKENS+=("$username:$JWT_TOKEN")
            echo "🎫 Token obtained for $username"
        fi
    fi
done

echo ""
echo "2️⃣ CREATING DEMO STORES"
echo "-----------------------"

# Store data: storeName:founderUsername
declare -a STORES=(
    "TechHub:alice"
    "BookWorld:bob"
    "FashionForward:charlie"
    "HomeEssentials:diana"
    "SportsZone:eve"
)

declare -a STORE_IDS=()

for store_data in "${STORES[@]}"; do
    IFS=':' read -r store_name founder <<< "$store_data"
    
    # Find JWT token for founder
    founder_token=""
    for token_data in "${JWT_TOKENS[@]}"; do
        IFS=':' read -r token_user token <<< "$token_data"
        if [ "$token_user" = "$founder" ]; then
            founder_token="$token"
            break
        fi
    done
    
    if [ -n "$founder_token" ]; then
        echo "🏪 Creating store: $store_name (founder: $founder)"
        CREATE_STORE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/stores/create" \
          -H "Content-Type: application/json" \
          -H "Authorization: Bearer $founder_token" \
          -d "{\"storeName\":\"$store_name\",\"founderId\":\"$founder\"}")
        
        if check_success_continue "$CREATE_STORE_RESPONSE"; then
            STORE_ID=$(extract_json_value "$CREATE_STORE_RESPONSE" "storeId")
            STORE_IDS+=("$store_name:$STORE_ID:$founder:$founder_token")
            echo "🏪 Store created with ID: $STORE_ID"
        fi
    else
        echo "⚠️ No token found for founder: $founder"
    fi
done

echo ""
echo "3️⃣ CREATING DEMO PRODUCTS"
echo "-------------------------"

# Product data: productName:category:description:price:quantity:storeIndex
declare -a PRODUCTS=(
    # TechHub products (index 0)
    "iPhone 15:Electronics:Latest Apple smartphone with advanced features:999.99:50:0"
    "MacBook Pro:Electronics:Powerful laptop for professionals:2499.99:25:0"
    "AirPods Pro:Electronics:Wireless earbuds with noise cancellation:249.99:100:0"
    "iPad Air:Electronics:Versatile tablet for work and entertainment:599.99:75:0"
    
    # BookWorld products (index 1)
    "The Great Gatsby:Books:Classic American literature novel:12.99:200:1"
    "Programming Pearls:Books:Essential book for software developers:29.99:50:1"
    "Dune:Books:Epic science fiction masterpiece:15.99:150:1"
    "Clean Code:Books:A handbook of agile software craftsmanship:39.99:75:1"
    
    # FashionForward products (index 2)
    "Designer Jeans:Clothing:Premium denim jeans with perfect fit:89.99:100:2"
    "Silk Blouse:Clothing:Elegant silk blouse for formal occasions:129.99:60:2"
    "Leather Jacket:Clothing:Genuine leather jacket with modern style:299.99:30:2"
    "Summer Dress:Clothing:Light and comfortable dress for summer:79.99:80:2"
    
    # HomeEssentials products (index 3)
    "Coffee Maker:Home:Automatic coffee maker with timer:149.99:40:3"
    "Vacuum Cleaner:Home:Powerful cordless vacuum cleaner:299.99:25:3"
    "Bed Sheets Set:Home:Luxury cotton bed sheets set:89.99:60:3"
    "Kitchen Knife Set:Home:Professional chef knife set:199.99:35:3"
    
    # SportsZone products (index 4)
    "Running Shoes:Sports:Professional running shoes for athletes:159.99:120:4"
    "Yoga Mat:Sports:Non-slip yoga mat for home workouts:39.99:200:4"
    "Basketball:Sports:Official size basketball for outdoor play:29.99:150:4"
    "Fitness Tracker:Sports:Smart fitness tracker with heart rate monitor:199.99:80:4"
)

for product_data in "${PRODUCTS[@]}"; do
    IFS=':' read -r product_name category description price quantity store_index <<< "$product_data"
    
    # Get store information by index
    if [ "$store_index" -lt "${#STORE_IDS[@]}" ]; then
        store_info="${STORE_IDS[$store_index]}"
        IFS=':' read -r store_name store_id founder founder_token <<< "$store_info"
        
        # Generate unique product ID
        PRODUCT_ID="PROD_$(date +%s)_$(shuf -i 1000-9999 -n 1)"
        
        echo "📦 Adding product: $product_name to store: $store_name"
        ADD_LISTING_RESPONSE=$(curl -s -X POST "$BASE_URL/api/stores/listings/add" \
          -H "Content-Type: application/json" \
          -H "Authorization: Bearer $founder_token" \
          -d "{
            \"userName\":\"$founder\",
            \"storeID\":\"$store_id\",
            \"productId\":\"$PRODUCT_ID\",
            \"productName\":\"$product_name\",
            \"productCategory\":\"$category\",
            \"productDescription\":\"$description\",
            \"quantity\":$quantity,
            \"price\":$price
          }")
        
        check_success_continue "$ADD_LISTING_RESPONSE"
    else
        echo "⚠️ Invalid store index: $store_index"
    fi
done

echo ""
echo "4️⃣ CREATING GUEST USERS"
echo "-----------------------"

# Create some guest users for testing
declare -a GUESTS=(
    "guest_shopper1"
    "guest_shopper2"
    "guest_browser"
)

for guest in "${GUESTS[@]}"; do
    echo "👻 Creating guest user: $guest"
    GUEST_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register/guest" \
      -H "Content-Type: application/json" \
      -d "{\"username\":\"$guest\"}")
    check_success_continue "$GUEST_RESPONSE"
done

echo ""
echo "5️⃣ VERIFICATION - CHECKING CREATED DATA"
echo "---------------------------------------"

# Get all stores and products information
echo "📋 Getting all stores and products information..."
ALL_INFO_RESPONSE=$(curl -s -X GET "$BASE_URL/api/stores/info")
check_success_continue "$ALL_INFO_RESPONSE"

# Test product search
echo "🔍 Testing product search functionality..."
SEARCH_RESPONSE=$(curl -s -X GET "$BASE_URL/api/products/search?query=iPhone" \
  -H "Content-Type: application/json")
check_success_continue "$SEARCH_RESPONSE"

# Test products sorted by price
echo "💰 Testing products sorted by price..."
SORTED_RESPONSE=$(curl -s -X GET "$BASE_URL/api/products/sorted/price" \
  -H "Content-Type: application/json")
check_success_continue "$SORTED_RESPONSE"

echo ""
echo "🎉 DEMO DATA POPULATION COMPLETE!"
echo "================================="
echo ""
echo "📊 Summary of Created Data:"
echo "• Users: ${#USERS[@]} regular users + ${#GUESTS[@]} guest users"
echo "• Stores: ${#STORES[@]} stores with different categories"
echo "• Products: ${#PRODUCTS[@]} products across all stores"
echo ""
echo "👥 Demo Users (username:password):"
for user_data in "${USERS[@]}"; do
    IFS=':' read -r username password <<< "$user_data"
    echo "  • $username:$password"
done
echo ""
echo "🏪 Demo Stores:"
for store_data in "${STORES[@]}"; do
    IFS=':' read -r store_name founder <<< "$store_data"
    echo "  • $store_name (owned by $founder)"
done
echo ""
echo "📦 Product Categories:"
echo "  • Electronics (TechHub)"
echo "  • Books (BookWorld)"
echo "  • Clothing (FashionForward)"
echo "  • Home (HomeEssentials)"
echo "  • Sports (SportsZone)"
echo ""
echo "🚀 Your application is now ready for frontend testing!"
echo "Users can now:"
echo "  ✅ Register and login"
echo "  ✅ Browse stores and products"
echo "  ✅ Search for products"
echo "  ✅ Add products to cart"
echo "  ✅ Execute purchases"
echo "  ✅ Manage stores (if they're owners)"
echo ""
echo "💡 To test the frontend, try logging in with any of the demo users above." 