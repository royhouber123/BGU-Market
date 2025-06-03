# Product API Endpoints - Curl Examples

Base URL: `http://localhost:8080/api/products`

## Product Search

### 1. Search Products by Name
Search for products by name across all stores (case-insensitive, partial match).
```bash
curl -X GET "http://localhost:8080/api/products/search?query=laptop" \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Product search completed",
  "data": [
    {
      "listingId": "listing-123",
      "storeId": "store-1",
      "productId": "product-456",
      "productName": "Gaming Laptop",
      "productDescription": "High-performance gaming laptop",
      "category": "Electronics",
      "quantityAvailable": 5,
      "price": 1299.99,
      "purchaseType": "REGULAR",
      "active": true
    }
  ]
}
```

### 2. Search Products by Product ID
Find all listings for a specific product ID.
```bash
curl -X GET "http://localhost:8080/api/products/id/product-456" \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Product search by ID completed",
  "data": [
    {
      "listingId": "listing-123",
      "storeId": "store-1",
      "productId": "product-456",
      "productName": "Gaming Laptop",
      "productDescription": "High-performance gaming laptop",
      "category": "Electronics",
      "quantityAvailable": 5,
      "price": 1299.99,
      "purchaseType": "REGULAR",
      "active": true
    }
  ]
}
```

## Store-Specific Product Operations

### 3. Get All Products from a Store
Retrieve all product listings from a specific store.
```bash
curl -X GET "http://localhost:8080/api/products/store/store-1" \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Store listings retrieved",
  "data": [
    {
      "listingId": "listing-123",
      "storeId": "store-1",
      "productId": "product-456",
      "productName": "Gaming Laptop",
      "productDescription": "High-performance gaming laptop",
      "category": "Electronics",
      "quantityAvailable": 5,
      "price": 1299.99,
      "purchaseType": "REGULAR",
      "active": true
    },
    {
      "listingId": "listing-124",
      "storeId": "store-1",
      "productId": "product-789",
      "productName": "Wireless Mouse",
      "productDescription": "Ergonomic wireless mouse",
      "category": "Accessories",
      "quantityAvailable": 20,
      "price": 29.99,
      "purchaseType": "REGULAR",
      "active": true
    }
  ]
}
```

### 4. Search Products within a Store
Search for products by name within a specific store.
```bash
curl -X GET "http://localhost:8080/api/products/store/store-1/search?query=mouse" \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Store product search completed",
  "data": [
    {
      "listingId": "listing-124",
      "storeId": "store-1",
      "productId": "product-789",
      "productName": "Wireless Mouse",
      "productDescription": "Ergonomic wireless mouse",
      "category": "Accessories",
      "quantityAvailable": 20,
      "price": 29.99,
      "purchaseType": "REGULAR",
      "active": true
    }
  ]
}
```

## Product Information and Sorting

### 5. Get Products Sorted by Price
Get all products sorted by price in ascending order.
```bash
curl -X GET "http://localhost:8080/api/products/sorted/price" \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Products sorted by price",
  "data": [
    {
      "listingId": "listing-124",
      "storeId": "store-1",
      "productId": "product-789",
      "productName": "Wireless Mouse",
      "productDescription": "Ergonomic wireless mouse",
      "category": "Accessories",
      "quantityAvailable": 20,
      "price": 29.99,
      "purchaseType": "REGULAR",
      "active": true
    },
    {
      "listingId": "listing-123",
      "storeId": "store-1",
      "productId": "product-456",
      "productName": "Gaming Laptop",
      "productDescription": "High-performance gaming laptop",
      "category": "Electronics",
      "quantityAvailable": 5,
      "price": 1299.99,
      "purchaseType": "REGULAR",
      "active": true
    }
  ]
}
```

### 6. Get Specific Product Listing
Retrieve a specific product listing by its listing ID.
```bash
curl -X GET "http://localhost:8080/api/products/listing/listing-123" \
  -H "Content-Type: application/json"
```

**Response Example:**
```json
{
  "success": true,
  "message": "Listing retrieved successfully",
  "data": {
    "listingId": "listing-123",
    "storeId": "store-1",
    "productId": "product-456",
    "productName": "Gaming Laptop",
    "productDescription": "High-performance gaming laptop",
    "category": "Electronics",
    "quantityAvailable": 5,
    "price": 1299.99,
    "purchaseType": "REGULAR",
    "active": true
  }
}
```

### 7. Get Product Information
Get detailed product information by product ID.
```bash
curl -X POST "http://localhost:8080/api/products/info" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-456"
  }'
```

**Response Example:**
```json
{
  "success": true,
  "message": "Product information retrieved successfully",
  "data": {
    "productId": "product-456",
    "productName": "Gaming Laptop",
    "productCategory": "Electronics",
    "productDescription": "High-performance gaming laptop",
    "quantity": 5,
    "price": 1299.99,
    "success": true,
    "message": "Product information retrieved successfully"
  }
}
```

## Testing Commands Summary

### Quick Test Sequence
```bash
# 1. Search products by name
curl -X GET "http://localhost:8080/api/products/search?query=laptop"

# 2. Get products from store
curl -X GET "http://localhost:8080/api/products/store/store-1"

# 3. Get products sorted by price
curl -X GET "http://localhost:8080/api/products/sorted/price"

# 4. Get specific product info
curl -X POST "http://localhost:8080/api/products/info" \
  -H "Content-Type: application/json" \
  -d '{"productId": "product-456"}'
```

## Error Responses

### Product Not Found
```json
{
  "success": false,
  "message": "Product not found",
  "data": null
}
```

### Empty Search Results
```json
{
  "success": true,
  "message": "Product search completed",
  "data": []
}
```

### Invalid Store ID
```json
{
  "success": false,
  "message": "Failed to get store listings: Store not found",
  "data": null
}
```

---

**Note**: All endpoints are public and do not require authentication. The Product API is designed for browsing and discovering products across the marketplace.

**Related Documentation**:
- [Store API Examples](store_api_examples.md) - For managing store products
- [User API Examples](user_api_examples.md) - For adding products to cart
- [Purchase API Examples](purchase_api_examples.md) - For buying products 