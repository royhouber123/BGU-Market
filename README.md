# BGU Market 🛒

**A modern, full-stack e-commerce marketplace built with Spring Boot backend and React frontend**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue.svg)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🚀 Quick Start (Get Running in 5 Minutes!)

### 📊 **IMPORTANT: Auto Demo Data Loading!**

**Start with a fully functional marketplace!** The system automatically loads demo data from `demo-data.txt` on startup, giving you:
- **6 demo users** (including admin) ready to login
- **3 different stores** with varied categories 
- **5 realistic products** with proper pricing
- **Store management** with owners and managers
- **Ready-to-test** shopping cart and purchase functionality

### ⚡ **Quick Setup:**

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd BGU-Market

# 2. Setup MySQL Database
mysql -u root -p
CREATE DATABASE bgu_market;
CREATE USER 'bgu'@'localhost' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON bgu_market.* TO 'bgu'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# 3. Start the backend (demo data loads automatically)
cd backend
mvn clean install
mvn spring-boot:run &  # Runs in background
cd ..

# 4. Frontend setup and start
cd frontend
npm install
npm start
```

**🎉 That's it!** Visit `http://localhost:3000` and you have a fully functional marketplace!

---

## 🎯 Demo Login Credentials (Auto-Loaded)

The system automatically creates these users from `demo-data.txt`:

| Username | Password | Role | Store Owned | Category |
|----------|----------|------|-------------|----------|
| `u1` | `password123` | **Admin** | - | System Admin |
| `u2` | `password123` | Store Owner | Tech Paradise | Electronics |
| `u3` | `password123` | Store Owner | Fashion Hub | Clothing |
| `u4` | `password123` | Store Owner | Book Corner | Books |
| `u5` | `password123` | User/Manager | - | Regular User |
| `u6` | `password123` | User/Manager | - | Regular User |

---

## 📋 Table of Contents

- [Prerequisites](#-prerequisites)
- [Detailed Setup Guide](#-detailed-setup-guide)
- [Demo Data System](#-demo-data-system)
- [Project Structure](#-project-structure)
- [Core Features](#-core-features)
- [API Documentation](#-api-documentation)
- [Database Configuration](#-database-configuration)
- [Testing](#-testing)
- [Troubleshooting](#-troubleshooting)

---

## 🔧 Prerequisites

Ensure you have these installed before starting:

| Tool | Version | Download Link |
|------|---------|---------------|
| **Java** | 17+ | [Download Java](https://adoptium.net/) |
| **Maven** | 3.6+ | [Download Maven](https://maven.apache.org/download.cgi) |
| **MySQL** | 8.0+ | [Download MySQL](https://dev.mysql.com/downloads/) |
| **Node.js** | 16+ | [Download Node.js](https://nodejs.org/) |
| **Git** | Latest | [Download Git](https://git-scm.com/) |

**Quick Version Check:**
```bash
java -version    # Should show 17+
mvn -version     # Should show 3.6+
mysql --version  # Should show 8.0+
node -version    # Should show 16+
npm -version     # Should show 8+
```

---

## 🛠️ Detailed Setup Guide

### 🗄️ **Step 1: MySQL Database Setup**

```bash
# Start MySQL service
sudo systemctl start mysql  # Linux
brew services start mysql   # macOS
# Or start via MySQL Workbench/XAMPP

# Connect and create database
mysql -u root -p

# Run these SQL commands:
CREATE DATABASE bgu_market;
CREATE USER 'bgu'@'localhost' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON bgu_market.* TO 'bgu'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### ⚙️ **Step 2: Backend Setup**

```bash
cd backend

# Install dependencies and compile
mvn clean install

# Start the Spring Boot application
mvn spring-boot:run
```

**🔄 Auto Demo Data Loading:**
- The system reads `backend/src/main/resources/demo-data.txt` on startup
- Creates users, stores, products, and relationships automatically  
- No manual data entry needed!

### 🎨 **Step 3: Frontend Setup**

```bash
# Open new terminal
cd frontend

# Install Node.js dependencies
npm install

# Start React development server
npm start
```

**Access Points:**
- **Frontend**: `http://localhost:3000`
- **Backend API**: `http://localhost:8080` 
- **MySQL Database**: `localhost:3306/bgu_market`

---

## 📊 Demo Data System

### 🔄 **Automatic Loading Process**

The demo data system works automatically:

1. **Startup Detection**: Backend detects `bgu.market.populate-demo-data=true`
2. **File Parsing**: Reads `demo-data.txt` using `DemoDataParser.java`
3. **Data Creation**: Creates users, stores, products, managers, and owners
4. **Ready to Use**: Full marketplace functionality available immediately

### 📝 **Demo Data Format**

The `demo-data.txt` file uses simple commands:

```bash
# Users: username,password
USER u2,password123

# Stores: storeName,founderId  
STORE Tech Paradise,u2

# Products: userName,storeName,productId,name,category,description,quantity,price,type
PRODUCT u2,Tech Paradise,electronics_001,iPhone 15,Electronics,Latest smartphone,50,999.99,REGULAR

# Store Managers: storeName,managerUsername,appointerUsername,permissions
MANAGER Tech Paradise,u3,u2,1,2

# Store Owners: storeName,ownerUsername,appointerUsername
OWNER Tech Paradise,u4,u2
```

### ✏️ **Customizing Demo Data**

To modify the demo data:

1. Edit `backend/src/main/resources/demo-data.txt`
2. Restart the backend server
3. New data will be loaded automatically

---

## 📁 Project Structure

```
BGU-Market/
├── 📖 Documentation
│   ├── README.md                   # This file
│   ├── DEMO_DATA_README.md         # Demo data documentation
│   └── Docs/                       # Additional documentation
│
├── 🎨 Frontend (React App)
│   ├── src/
│   │   ├── components/             # React components
│   │   ├── pages/                  # Page components
│   │   ├── services/               # API service calls
│   │   └── styles/                 # CSS and styling
│   ├── public/                     # Static assets
│   ├── package.json                # Dependencies & scripts
│   └── package-lock.json
│
├── ⚙️ Backend (Spring Boot)
│   ├── src/main/java/market/
│   │   ├── controllers/            # REST API endpoints
│   │   ├── services/               # Business logic
│   │   ├── models/                 # Data models
│   │   ├── repositories/           # Data access layer
│   │   ├── application/Init/       # ⭐ Demo data system
│   │   │   ├── DemoDataParser.java # Parses demo-data.txt
│   │   │   └── DemoDataModels/     # Data models for demo
│   │   └── config/                 # Configuration
│   ├── src/main/resources/
│   │   ├── application.properties          # Main configuration  
│   │   ├── application-mysql.properties    # MySQL settings
│   │   └── demo-data.txt                   # ⭐ DEMO DATA FILE
│   ├── pom.xml                     # Maven dependencies
│   └── README_CONFIGURATION.md
│
└── 🧪 Testing & Scripts
    ├── test_api.sh                 # Basic API testing
    ├── test_purchase_endpoints.sh  # Purchase API tests
    └── test_purchase_realistic.sh  # Realistic scenarios
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
# Login and get JWT token
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "u2", "password": "password123"}'
```

**Store Operations:**
```bash
# Get all stores
curl -X GET "http://localhost:8080/api/stores/all"

# Create new store
curl -X POST "http://localhost:8080/api/stores/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"storeName": "MyStore", "founderId": "u2"}'
```

**Product Search:**
```bash
# Search products
curl -X GET "http://localhost:8080/api/products/search?query=iPhone"

# Get products by category
curl -X GET "http://localhost:8080/api/products/category/Electronics"
```

---

## 🗄️ Database Configuration

### 🔧 **MySQL Setup (Production)**

**Database Configuration:**
```properties
# Connection Settings
spring.datasource.url=jdbc:mysql://localhost:3306/bgu_market
spring.datasource.username=bgu
spring.datasource.password=changeme
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

**Required Database Setup:**
```sql
CREATE DATABASE bgu_market;
CREATE USER 'bgu'@'localhost' IDENTIFIED BY 'changeme';
GRANT ALL PRIVILEGES ON bgu_market.* TO 'bgu'@'localhost';
FLUSH PRIVILEGES;
```

### 🧪 **Alternative: H2 for Development**

To use H2 in-memory database for quick testing:

```bash
# Change application.properties
echo "spring.profiles.active=h2" > backend/src/main/resources/application.properties

# Restart backend
mvn spring-boot:run
```

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

### 🎯 **Manual Testing with Demo Data**

After startup, test these scenarios:

1. **User Experience Testing:**
   - Login as `u2` and browse Tech Paradise store
   - Login as `u3` and add products to cart
   - Test guest user functionality

2. **Store Owner Testing:**
   - Login as any demo user
   - Add new products to their store
   - Manage inventory and pricing

3. **Admin Testing:**
   - Login as `u1` (admin)
   - View all users and stores
   - Test suspension functionality

4. **Purchase Flow Testing:**
   - Add products to cart
   - Execute purchases
   - Test auction/bidding features

---

## 🛠️ Troubleshooting

### 🚨 **Common Issues & Solutions**

#### **"MySQL Connection Failed"**
```bash
# Check MySQL is running
sudo systemctl status mysql  # Linux
brew services list | grep mysql  # macOS

# Test connection
mysql -u bgu -p'changeme' -e "SELECT 1;"

# Recreate database if needed
mysql -u root -p
DROP DATABASE IF EXISTS bgu_market;
CREATE DATABASE bgu_market;
GRANT ALL PRIVILEGES ON bgu_market.* TO 'bgu'@'localhost';
```

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

#### **"Demo data not loading"**
```bash
# Check application.properties
grep "populate-demo-data" backend/src/main/resources/application.properties

# Should show: bgu.market.populate-demo-data=true

# Check demo-data.txt exists
ls -la backend/src/main/resources/demo-data.txt
```

#### **"Frontend can't connect to backend"**
```bash
# Verify backend is running
curl http://localhost:8080/api/admin/verify/u1

# Check proxy in package.json
grep -A 2 "proxy" frontend/package.json
```

### 💡 **Development Tips**

- **Hot Reload**: Both frontend and backend support hot reload
- **Database Reset**: Restart backend to reset and reload demo data
- **Logs**: Check console output for detailed error messages
- **CORS**: Backend configured for `localhost:3000`
- **JWT Tokens**: Check browser dev tools for authentication issues

---

## 🎯 **Quick Commands Cheat Sheet**

```bash
# 🚀 COMPLETE SETUP
git clone <repo> && cd BGU-Market

# Setup MySQL database
mysql -u root -p
# Run: CREATE DATABASE bgu_market; CREATE USER 'bgu'@'localhost' IDENTIFIED BY 'changeme'; GRANT ALL PRIVILEGES ON bgu_market.* TO 'bgu'@'localhost'; FLUSH PRIVILEGES;

# Start backend (demo data loads automatically)
cd backend && mvn spring-boot:run &

# Start frontend
cd ../frontend && npm install && npm start

# 🔄 RESTART EVERYTHING
# Terminal 1: Backend
cd backend && mvn spring-boot:run

# Terminal 2: Frontend  
cd frontend && npm start

# 🧪 QUICK TESTING
./test_api.sh                          # Basic tests
./test_purchase_endpoints.sh           # Purchase API tests
./test_purchase_realistic.sh           # Realistic scenarios

# 🛠️ DATABASE MANAGEMENT
mysql -u bgu -p'changeme' bgu_market   # Connect to database
# Reset: DROP DATABASE bgu_market; CREATE DATABASE bgu_market;

# 🌐 ACCESS POINTS
# Frontend:  http://localhost:3000
# Backend:   http://localhost:8080
# Database:  mysql://localhost:3306/bgu_market
```

---

## 📝 Additional Resources

- **[📖 Backend Configuration Guide](backend/README_CONFIGURATION.md)**
- **[📊 Demo Data Details](DEMO_DATA_README.md)**
- **[🔧 Complete API Documentation](backend/src/main/java/market/controllers/docs/README.md)**
- **[📚 Spring Boot Documentation](https://spring.io/projects/spring-boot)**
- **[⚛️ React Documentation](https://reactjs.org/docs)**
- **[🗄️ MySQL Documentation](https://dev.mysql.com/doc/)**

---

## 🚀 **Ready to Start?**

1. **📥 Clone the repository**
2. **🗄️ Setup MySQL database**
3. **⚡ Run backend (demo data loads automatically)**
4. **🎨 Start frontend**
5. **🎮 Login with demo credentials and explore!**

**🎉 Happy coding and welcome to BGU Market!**

---

*Last updated: $(date +%Y-%m-%d)*
