# BGU Market 🛒

**A modern, full-stack e-commerce marketplace built with Spring Boot backend and React frontend**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue.svg)](https://reactjs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🚀 Quick Start (Get Running in 5 Minutes!)

### 📊 **IMPORTANT: Get Startup Data First!**

**Don't start with an empty marketplace!** Run our demo data script to get a fully functional marketplace with users, stores, and products:

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd BGU-Market

# 2. Start the backend
cd backend
mvn clean install
mvn spring-boot:run &  # Runs in background
cd ..

# Wait for backend to start (watch for "Started BguMarketApplication")

# 3. Populate with demo data
chmod +x populate_demo_data.sh
./populate_demo_data.sh

# 4. Frontend setup and start
cd frontend
npm install
npm start
```

**🎉 That's it!** Visit `http://localhost:3000` and you have a fully functional marketplace with:
- **5 demo users** you can login with
- **5 different stores** (Electronics, Books, Clothing, Home, Sports)
- **20 realistic products** with proper pricing
- **Ready-to-test** shopping cart and purchase functionality

---

## 🎯 Demo Login Credentials (After Running Startup Data)

| Username | Password | Store | Category |
|----------|----------|-------|----------|
| `alice` | `password123` | TechHub | Electronics |
| `bob` | `password123` | BookWorld | Books & Literature |
| `charlie` | `password123` | FashionForward | Clothing & Fashion |
| `diana` | `password123` | HomeEssentials | Home & Garden |
| `eve` | `password123` | SportsZone | Sports & Fitness |

**Admin Access:**
- Username: `admin` | Password: `admin`

---

## 📋 Table of Contents

- [Why Use Startup Data?](#-why-use-startup-data)
- [Prerequisites](#-prerequisites)
- [Detailed Setup Guide](#-detailed-setup-guide)
- [Project Structure](#-project-structure)
- [Core Features](#-core-features)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Configuration](#-configuration)
- [Troubleshooting](#-troubleshooting)

---

## 🌟 Why Use Startup Data?

**Skip the tedious setup!** Instead of manually creating users, stores, and products one by one, our startup data script gives you:

### ✅ **What You Get Instantly:**
- **5 Ready-to-Use Accounts**: Test different user roles and store ownership
- **Realistic Product Catalog**: 20 products across 5 categories with proper pricing
- **Functional Marketplace**: Immediately test shopping, purchasing, and store management
- **Demo Scenarios**: Perfect for presentations, testing, and development

### 🎮 **Immediate Testing Capabilities:**
- Login and browse different stores
- Add products to cart and make purchases
- Test auction and bidding features
- Explore admin management functions
- Test store owner/manager workflows

### ⚡ **Perfect For:**
- **Developers**: Quickly test features without manual data entry
- **Demos**: Showcase the full marketplace functionality
- **Testing**: Comprehensive testing scenarios out of the box
- **Learning**: Understand the system with realistic data

---

## 🔧 Prerequisites

Ensure you have these installed before starting:

| Tool | Version | Download Link |
|------|---------|---------------|
| **Java** | 17+ | [Download Java](https://adoptium.net/) |
| **Maven** | 3.6+ | [Download Maven](https://maven.apache.org/download.cgi) |
| **Node.js** | 16+ | [Download Node.js](https://nodejs.org/) |
| **Git** | Latest | [Download Git](https://git-scm.com/) |

**Quick Version Check:**
```bash
java -version    # Should show 17+
mvn -version     # Should show 3.6+
node -version    # Should show 16+
npm -version     # Should show 8+
```

---

## 🛠️ Detailed Setup Guide

### 🔥 Method 1: Quick Start with Demo Data (Recommended)

This gets you a fully functional marketplace in minutes:

```bash
# 1. Clone and enter directory
git clone <repository-url>
cd BGU-Market

# 2. Backend setup and start
cd backend
mvn clean install
mvn spring-boot:run &  # Runs in background
cd ..

# Wait for backend to start (watch for "Started BguMarketApplication")

# 3. Populate with demo data
chmod +x populate_demo_data.sh
./populate_demo_data.sh

# 4. Frontend setup and start
cd frontend
npm install
npm start
```

### 🔧 Method 2: Manual Setup (Empty Database)

If you prefer to start with a clean slate:

#### Backend Setup:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

#### Frontend Setup (New Terminal):
```bash
cd frontend
npm install
npm start
```

**Access Points:**
- **Frontend**: `http://localhost:3000`
- **Backend API**: `http://localhost:8080`
- **Database Console**: `http://localhost:8080/h2-console`

---

## 📁 Project Structure

```
BGU-Market/
├── 🚀 Quick Start Files
│   ├── populate_demo_data.sh      # ⭐ STARTUP DATA SCRIPT
│   ├── DEMO_DATA_README.md        # Demo data documentation
│   └── README.md                  # This file
│
├── 🎨 Frontend (React App)
│   ├── src/
│   │   ├── components/            # React components
│   │   ├── pages/                 # Page components
│   │   ├── services/              # API service calls
│   │   └── styles/                # CSS and styling
│   ├── public/                    # Static assets
│   ├── package.json               # Dependencies & scripts
│   └── package-lock.json
│
├── ⚙️ Backend (Spring Boot)
│   ├── src/main/java/market/
│   │   ├── controllers/           # REST API endpoints
│   │   ├── services/              # Business logic
│   │   ├── models/                # Data models
│   │   ├── repositories/          # Data access layer
│   │   └── config/                # Configuration
│   ├── src/main/resources/
│   │   ├── application.properties # Main configuration
│   │   └── config.properties.example
│   ├── pom.xml                    # Maven dependencies
│   └── README_CONFIGURATION.md
│
└── 🧪 Testing & Scripts
    ├── test_api.sh                # Basic API testing
    ├── test_purchase_endpoints.sh # Purchase API tests
    └── test_purchase_realistic.sh # Realistic scenarios
```

---

## 🌟 Core Features

### 🛒 **E-commerce Functionality**
- **Multi-User System**: Guests, registered users, store owners, admins
- **Shopping Cart**: Add/remove items, quantity management, persistent cart
- **Advanced Purchase System**: Direct purchases, auction system, bidding
- **Order Management**: Purchase history, order tracking, status updates
- **Payment Processing**: Integrated payment workflow

### 🏪 **Store Management**
- **Multi-Store Support**: Users can own multiple stores
- **Product Management**: Add/edit/remove products with categories
- **Inventory Control**: Real-time stock management
- **Store Policies**: Custom discount and purchase rules
- **Permission System**: Store owners, managers, and staff roles

### 🔧 **Administrative Features**
- **User Management**: Account oversight, user statistics
- **Store Oversight**: Monitor all stores, performance metrics
- **Suspension System**: Temporary or permanent user/store suspension
- **System Analytics**: Usage tracking, sales reporting
- **Content Moderation**: Product and store approval workflows

### 💬 **Communication System**
- **Real-time Notifications**: Purchase confirmations, bid updates
- **Event-driven Messaging**: Automated notifications for key events
- **Notification Management**: Mark as read, notification history
- **User Communication**: Messages between buyers and sellers

### 🔍 **Search & Discovery**
- **Advanced Product Search**: Filter by category, price, store
- **Product Recommendations**: Based on user behavior
- **Store Discovery**: Browse stores by category and rating
- **Sorting Options**: Price, popularity, rating, newest

---

## 📚 API Documentation

Our API is organized into 8 comprehensive controllers:

| 🎯 **Controller** | **Endpoints** | **Description** |
|-------------------|---------------|-----------------|
| 🔐 **Authentication** | `/api/auth/*` | Login, logout, JWT token management |
| 👤 **User Management** | `/api/users/*` | Registration, profiles, shopping cart |
| 🏪 **Store Management** | `/api/stores/*` | Store CRUD, products, permissions |
| 🏷️ **Store Policies** | `/api/stores/{id}/policies/*` | Discount & purchase policies |
| 🛒 **Purchase System** | `/api/purchases/*` | Purchases, auctions, bidding |
| 📦 **Product Catalog** | `/api/products/*` | Product search, listings |
| 🔧 **Admin Management** | `/api/admin/*` | User suspension, store management |
| 🔔 **Notifications** | `/api/notifications/*` | User notifications, read status |

### 🚀 **Quick API Examples**

**Authentication:**
```bash
# Register new user
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "password": "password123"}'

# Login and get JWT token
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "password123"}'
```

**Store Operations:**
```bash
# Get all stores
curl -X GET "http://localhost:8080/api/stores/all"

# Create new store
curl -X POST "http://localhost:8080/api/stores/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"storeName": "MyStore", "founderId": "alice"}'
```

**Product Search:**
```bash
# Search products
curl -X GET "http://localhost:8080/api/products/search?query=iPhone"

# Get products by category
curl -X GET "http://localhost:8080/api/products/category/Electronics"
```

### 📖 **Complete API Documentation**

For detailed API documentation with all endpoints, examples, and testing:
- **[📁 Complete API Docs](backend/src/main/java/market/controllers/docs/README.md)**
- **[🔧 Automated API Tests](backend/src/main/java/market/controllers/docs/test_all_apis.sh)**

---

## 🧪 Testing

### 🚀 **Quick Testing Scripts**

**Basic API Testing:**
```bash
# Test core functionality
chmod +x test_api.sh && ./test_api.sh
```

**Comprehensive Purchase API Testing:**
```bash
# Test all purchase endpoints (13 endpoints)
chmod +x test_purchase_endpoints.sh && ./test_purchase_endpoints.sh

# Test realistic purchase scenarios
chmod +x test_purchase_realistic.sh && ./test_purchase_realistic.sh
```

### 🧪 **Testing Coverage**

Our test scripts validate:
- ✅ User registration and authentication
- ✅ Store creation and management
- ✅ Product listing and search
- ✅ Shopping cart functionality
- ✅ Purchase execution (direct and auction)
- ✅ Bidding system operations
- ✅ Admin management features
- ✅ Notification system
- ✅ Error handling and validation

### 🎯 **Manual Testing with Demo Data**

After running the startup data script, test these scenarios:

1. **User Experience Testing:**
   - Login as `alice` and browse her TechHub store
   - Login as `bob` and add books to cart
   - Test guest user functionality

2. **Store Owner Testing:**
   - Login as any demo user
   - Add new products to their store
   - Manage inventory and pricing

3. **Admin Testing:**
   - Login as `admin`
   - View all users and stores
   - Test suspension functionality

4. **Purchase Flow Testing:**
   - Add products to cart
   - Execute purchases
   - Test auction/bidding features

### 🗄️ **Database Access**

**H2 Database Console:**
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:bgumarket`
- **Username**: `sa`
- **Password**: (leave empty)

---

## ⚙️ Configuration

### 🔧 **Backend Configuration**

**Key Settings in `backend/src/main/resources/application.properties`:**

```properties
# Server Configuration
server.port=8080

# Database (H2 In-Memory)
spring.datasource.url=jdbc:h2:mem:bgumarket
spring.datasource.username=sa
spring.datasource.password=

# JWT Security
jwt.secret=bguMarketSecretKey
jwt.expiration=86400000

# Admin User (Auto-created)
admin.username=admin
admin.password=admin

# CORS Configuration
cors.allowed-origins=http://localhost:3000
```

### 🎨 **Frontend Configuration**

**API Proxy (in `frontend/package.json`):**
```json
{
  "proxy": "http://localhost:8080",
  "dependencies": {
    "react": "^18.x",
    "@mui/material": "^5.x",
    "axios": "^1.x"
  }
}
```

### 🔧 **Custom Configuration**

For advanced configuration:
```bash
# Copy example configuration
cp backend/src/main/resources/config.properties.example \
   backend/src/main/resources/config.properties

# Edit as needed
nano backend/src/main/resources/config.properties
```

---

## 🛠️ Troubleshooting

### 🚨 **Common Issues & Solutions**

#### **"Port 8080 already in use"**
```bash
# Kill process using port 8080
lsof -ti:8080 | xargs kill -9

# Or change port in application.properties
echo "server.port=8081" >> backend/src/main/resources/application.properties
```

#### **"Maven build fails"**
```bash
# Check Java version
java -version  # Must be 17+

# Clean and rebuild
cd backend
mvn clean
mvn clean install
```

#### **"npm install fails"**
```bash
# Clear npm cache
npm cache clean --force

# Remove and reinstall
rm -rf node_modules package-lock.json
npm install
```

#### **"Frontend can't connect to backend"**
```bash
# Verify backend is running
curl http://localhost:8080/api/admin/verify/admin

# Check proxy in package.json
grep -A 2 "proxy" frontend/package.json
```

#### **"Demo data script fails"**
```bash
# Ensure backend is fully started
# Look for "Started BguMarketApplication" in logs

# Check backend connection
curl -f http://localhost:8080/h2-console || echo "Backend not ready"

# Restart backend and try again
```

### 💡 **Development Tips**

- **Hot Reload**: Both frontend and backend support hot reload
- **Database Reset**: Restart backend to reset H2 database
- **Logs**: Check console output for detailed error messages
- **CORS**: Backend configured for `localhost:3000`
- **JWT Tokens**: Check browser dev tools for authentication issues

---

## 🎯 **Quick Commands Cheat Sheet**

```bash
# 🚀 COMPLETE SETUP (with demo data)
git clone <repo> && cd BGU-Market
cd backend && mvn spring-boot:run &
cd .. && chmod +x populate_demo_data.sh && ./populate_demo_data.sh
cd frontend && npm install && npm start

# 🔄 RESTART EVERYTHING
# Terminal 1: Backend
cd backend && mvn spring-boot:run

# Terminal 2: Frontend  
cd frontend && npm start

# Terminal 3: Repopulate demo data
./populate_demo_data.sh

# 🧪 QUICK TESTING
./test_api.sh                          # Basic tests
./test_purchase_endpoints.sh           # Purchase API tests
./test_purchase_realistic.sh           # Realistic scenarios

# 🌐 ACCESS POINTS
# Frontend:  http://localhost:3000
# Backend:   http://localhost:8080
# Database:  http://localhost:8080/h2-console
```

---

## 📝 Additional Resources

- **[📖 Detailed Backend Configuration](backend/README_CONFIGURATION.md)**
- **[📊 Demo Data Details](DEMO_DATA_README.md)**
- **[🔧 Complete API Documentation](backend/src/main/java/market/controllers/docs/README.md)**
- **[📚 Spring Boot Documentation](https://spring.io/projects/spring-boot)**
- **[⚛️ React Documentation](https://reactjs.org/docs)**

---

## 🚀 **Ready to Start?**

1. **📥 Clone the repository**
2. **⚡ Run the quick start commands above**
3. **🎮 Use demo login credentials to explore**
4. **🛒 Start building your marketplace features!**

**🎉 Happy coding and welcome to BGU Market!**

---

*Last updated: $(date +%Y-%m-%d)*
