# 📚 BGU Market Controllers API Documentation

Welcome to the BGU Market API documentation! This directory contains all the REST API controllers and their comprehensive documentation.

## 🚀 Quick Links

### 📖 API Documentation
- **[📁 Complete Documentation](docs/README.md)** - Main documentation hub
- **[🔐 Auth API Examples](docs/auth_api_examples.md)** - Login, logout, token validation
- **[👤 User API Examples](docs/user_api_examples.md)** - Registration, profile, shopping cart
- **[🏪 Store API Examples](docs/store_api_examples.md)** - Store management, products, permissions
- **[🏷️ Store Policies API Examples](docs/store_policies_api_examples.md)** - Store discount and purchase policies
- **[🛒 Purchase API Examples](docs/purchase_api_examples.md)** - Purchases, auctions, bids, history
- **[📦 Product API Examples](docs/product_api_examples.md)** - Product search, listings, information

### 🧪 Testing Tools
- **[🔧 Test Script](docs/test_all_apis.sh)** - Automated API testing script

## ⚡ Quick Start

1. **Start the server**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Test a simple endpoint**:
   ```bash
   curl -X GET "http://localhost:8080/api/stores/info"
   ```

3. **Complete authentication flow**:
   ```bash
   # Register
   curl -X POST "http://localhost:8080/api/users/register" \
     -H "Content-Type: application/json" \
     -d '{"username": "testuser", "password": "password123"}'
   
   # Login and get token
   curl -X POST "http://localhost:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"username": "testuser", "password": "password123"}'
   
   # Use token for authenticated requests
   curl -X GET "http://localhost:8080/api/users/me" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
   ```

4. **Run the complete test suite**:
   ```bash
   cd backend/src/main/java/market/controllers/docs
   ./test_all_apis.sh
   ```

## 📁 Controllers Overview

### 🔐 AuthController.java
**Endpoints**: `/api/auth/*`
- User authentication and JWT token management
- Login/logout operations
- Token validation

**[📖 View Documentation](docs/auth_api_examples.md)**

### 👤 UserController.java  
**Endpoints**: `/api/users/*`
- User registration and profile management
- Shopping cart operations
- User preferences and settings

**[📖 View Documentation](docs/user_api_examples.md)**

### 🏪 StoreController.java
**Endpoints**: `/api/stores/*`
- Store creation and management
- Product listings and inventory
- Store permissions and policies
- Owner and manager operations

**[📖 View Documentation](docs/store_api_examples.md)**

### 🏷️ StorePoliciesController.java
**Endpoints**: `/api/stores/{storeId}/policies/*`
- Store discount policy management
- Store purchase policy management
- Policy retrieval and validation

**[📖 View Documentation](docs/store_policies_api_examples.md)**

### 🛒 PurchaseController.java
**Endpoints**: `/api/purchases/*`
- Purchase execution and processing
- Auction operations and bidding
- Purchase history and tracking
- Counter offers and bid management

**[📖 View Documentation](docs/purchase_api_examples.md)**

### 📦 ProductController.java
**Endpoints**: `/api/products/*`
- Product search across all stores
- Store-specific product listings
- Product information retrieval
- Price-sorted product views

**[📖 View Documentation](docs/product_api_examples.md)**

## 📁 File Structure

```
backend/src/main/java/market/controllers/
├── API_DOCUMENTATION.md         # This file - Controllers overview & quick navigation
├── AuthController.java          # Authentication endpoints
├── UserController.java          # User management endpoints  
├── StoreController.java         # Store management endpoints
├── StorePoliciesController.java # Store policy management endpoints
├── PurchaseController.java      # Purchase and transaction endpoints
├── ProductController.java       # Product search and information endpoints
└── docs/                        # Detailed API documentation
    ├── README.md                # Main documentation hub
    ├── auth_api_examples.md     # Auth API curl examples
    ├── user_api_examples.md     # User API curl examples
    ├── store_api_examples.md    # Store API curl examples
    ├── store_policies_api_examples.md  # Store policies API curl examples
    ├── purchase_api_examples.md # Purchase API curl examples
    ├── product_api_examples.md  # Product API curl examples
    └── test_all_apis.sh         # Automated test script
```

## 🔗 Related Documentation

- [Backend Configuration](../../../../../../README_CONFIGURATION.md)
- [Main Project README](../../../../../../README.md)

---

*For detailed API documentation, examples, and testing instructions, visit the [docs directory](docs/README.md).* 