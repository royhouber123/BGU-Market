# 📚 BGU Market API Documentation

Welcome to the BGU Market API documentation! This file provides quick access to all available API examples and testing tools.

## 🚀 Quick Links

### 📖 API Documentation
- **[📁 Complete Documentation](docs/README.md)** - Main documentation hub
- **[🔐 Auth API Examples](docs/auth_api_examples.md)** - Login, logout, token validation
- **[👤 User API Examples](docs/user_api_examples.md)** - Registration, profile, shopping cart
- **[🏪 Store API Examples](docs/store_api_examples.md)** - Store management, products, permissions

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

3. **Run the complete test suite**:
   ```bash
   cd backend/src/main/java/market/controllers/docs
   ./test_all_apis.sh
   ```

## 📁 File Structure

```
backend/src/main/java/market/controllers/
├── API_DOCUMENTATION.md         # This file - quick navigation
├── AuthController.java          # Authentication endpoints
├── UserController.java          # User management endpoints  
├── StoreController.java         # Store management endpoints
└── docs/                        # Documentation directory
    ├── README.md                # Main documentation hub
    ├── auth_api_examples.md     # Auth API curl examples
    ├── user_api_examples.md     # User API curl examples
    ├── store_api_examples.md    # Store API curl examples
    └── test_all_apis.sh         # Automated test script
```

## 🔗 Related Documentation

- [Backend Configuration](../../../../../../README_CONFIGURATION.md)
- [Main Project README](../../../../../../README.md)

---

*For detailed API documentation, examples, and testing instructions, visit the [docs directory](docs/README.md).* 