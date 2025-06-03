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
echo "2.5️⃣ SETTING UP ADDITIONAL STORE ROLES FOR ALICE"
echo "-----------------------------------------------"

# Find Alice's token
alice_token=""
for token_data in "${JWT_TOKENS[@]}"; do
    IFS=':' read -r token_user token <<< "$token_data"
    if [ "$token_user" = "alice" ]; then
        alice_token="$token"
        break
    fi
done

if [ -n "$alice_token" ]; then
    # Find BookWorld store (bob's store) to make Alice an additional owner
    for store_info in "${STORE_IDS[@]}"; do
        IFS=':' read -r store_name store_id founder founder_token <<< "$store_info"
        if [ "$store_name" = "BookWorld" ]; then
            echo "👑 Making Alice an additional owner of BookWorld"
            ADD_OWNER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/stores/owners/add" \
              -H "Content-Type: application/json" \
              -H "Authorization: Bearer $founder_token" \
              -d "{\"appointerID\":\"$founder\",\"newOwnerID\":\"alice\",\"storeID\":\"$store_id\"}")
            check_success_continue "$ADD_OWNER_RESPONSE"
            break
        fi
    done
    
    # Find FashionForward store (charlie's store) to make Alice a manager
    for store_info in "${STORE_IDS[@]}"; do
        IFS=':' read -r store_name store_id founder founder_token <<< "$store_info"
        if [ "$store_name" = "FashionForward" ]; then
            echo "👔 Making Alice a manager of FashionForward"
            ADD_MANAGER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/stores/managers/add" \
              -H "Content-Type: application/json" \
              -H "Authorization: Bearer $founder_token" \
              -d "{\"appointerID\":\"$founder\",\"newManagerName\":\"alice\",\"storeID\":\"$store_id\"}")
            check_success_continue "$ADD_MANAGER_RESPONSE"
            break
        fi
    done
else
    echo "⚠️ No token found for Alice - cannot set up additional roles"
fi

echo ""
echo "3️⃣ CREATING DEMO PRODUCTS"
echo "-------------------------"

# Product data: productName:category:description:price:quantity:storeIndex:purchaseType
declare -a PRODUCTS=(
    # TechHub products (index 0)
    "iPhone 15:Electronics:Latest Apple smartphone with advanced features:999.99:50:0:REGULAR"
    "MacBook Pro:Electronics:Powerful laptop for professionals:2499.99:25:0:REGULAR"
    "AirPods Pro:Electronics:Wireless earbuds with noise cancellation:249.99:100:0:REGULAR"
    "Vintage iPad:Electronics:Rare vintage tablet perfect for auction:599.99:1:0:AUCTION"
    
    # BookWorld products (index 1)
    "The Great Gatsby:Books:Classic American literature novel:12.99:200:1:REGULAR"
    "Programming Pearls:Books:Essential book for software developers:29.99:50:1:REGULAR"
    "Custom Artwork Book:Books:One-of-a-kind artwork book open for bids:150.00:1:1:BID"
    "Mystery Book Bundle:Books:Surprise book collection for raffle:25.99:10:1:RAFFLE"
    
    # FashionForward products (index 2)
    "Designer Jeans:Clothing:Premium denim jeans with perfect fit:89.99:100:2:REGULAR"
    "Silk Blouse:Clothing:Elegant silk blouse for formal occasions:129.99:60:2:REGULAR"
    "Vintage Leather Jacket:Clothing:Rare vintage leather jacket for auction:299.99:1:2:AUCTION"
    "Summer Dress:Clothing:Light and comfortable dress for summer:79.99:80:2:REGULAR"
    
    # HomeEssentials products (index 3)
    "Coffee Maker:Home:Automatic coffee maker with timer:149.99:40:3:REGULAR"
    "Vacuum Cleaner:Home:Powerful cordless vacuum cleaner:299.99:25:3:REGULAR"
    "Custom Furniture:Home:Handcrafted custom furniture piece open for bids:499.99:1:3:BID"
    "Mystery Home Box:Home:Surprise home essentials bundle for raffle:75.00:5:3:RAFFLE"
    
    # SportsZone products (index 4)
    "Running Shoes:Sports:Professional running shoes for athletes:159.99:120:4:REGULAR"
    "Yoga Mat:Sports:Non-slip yoga mat for home workouts:39.99:200:4:REGULAR"
    "Rare Basketball:Sports:Signed basketball perfect for auction:199.99:1:4:AUCTION"
    "Sports Mystery Box:Sports:Random sports gear collection for raffle:99.99:8:4:RAFFLE"
)

for product_data in "${PRODUCTS[@]}"; do
    IFS=':' read -r product_name category description price quantity store_index purchase_type <<< "$product_data"
    
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
            \"price\":$price,
            \"purchaseType\":\"$purchase_type\"
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
echo "👑 Alice's Store Roles:"
echo "  • TechHub (Founder)"
echo "  • BookWorld (Additional Owner)"
echo "  • FashionForward (Manager)"
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