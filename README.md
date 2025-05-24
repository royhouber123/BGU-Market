# BGU Market

A full-stack e-commerce application built with Spring Boot backend and React frontend.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Backend Setup](#backend-setup)
- [Frontend Setup](#frontend-setup)
- [Running the Application](#running-the-application)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Default Admin Credentials](#default-admin-credentials)

## 🔧 Prerequisites

Before running the application, make sure you have the following installed:

- **Java 17 or higher** - [Download here](https://adoptium.net/)
- **Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- **Node.js 16+ and npm** - [Download here](https://nodejs.org/)
- **Git** - [Download here](https://git-scm.com/)

## 📁 Project Structure

```
BGU-Market/
├── backend/          # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/         # Java source code
│   │   │   └── resources/    # Configuration files
│   │   └── test/             # Test files
│   ├── pom.xml              # Maven dependencies
│   └── README_CONFIGURATION.md
├── frontend/         # React application
│   ├── src/
│   ├── public/
│   ├── package.json         # npm dependencies
│   └── package-lock.json
├── test_api.sh      # API testing script
└── README.md        # This file
```

## 🚀 Backend Setup

### 1. Navigate to Backend Directory
```bash
cd backend
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Configuration File
The backend uses `application.properties` for configuration. The file is already configured with default settings:

- **Location**: `backend/src/main/resources/application.properties`
- **Default Port**: 8080
- **Database**: H2 in-memory database
- **H2 Console**: Available at `http://localhost:8080/h2-console`

**Important Configuration Values:**
```properties
# Server runs on port 8080
server.port=8080

# H2 Database (in-memory)
spring.datasource.url=jdbc:h2:mem:bgumarket
spring.datasource.username=sa
spring.datasource.password=

# Admin user credentials (created automatically)
admin.username=admin
admin.password=admin

# JWT configuration
jwt.secret=bguMarketSecretKey
jwt.expiration=86400000
```

### 4. Run the Backend Server
```bash
mvn spring-boot:run
```

**Alternative way to run:**
```bash
# Build the jar file first
mvn clean package

# Run the jar file
java -jar target/bgu-market-1.0-SNAPSHOT.jar
```

The backend server will start on `http://localhost:8080`

### ✅ Verify Backend is Running
- Visit `http://localhost:8080/h2-console` - H2 Database Console
- The server logs should show "Started BguMarketApplication"

## 🎨 Frontend Setup

### 1. Navigate to Frontend Directory
```bash
cd frontend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Run the Frontend Application
```bash
npm start
```

The frontend will start on `http://localhost:3000` and automatically open in your browser.

### ✅ Verify Frontend is Running
- Visit `http://localhost:3000` - React application
- The page should load the BGU Market interface

## 🏃‍♂️ Running the Application

### Quick Start (Both Services)

1. **Terminal 1 - Backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Terminal 2 - Frontend:**
   ```bash
   cd frontend
   npm start
   ```

3. **Access the application:**
   - Frontend: `http://localhost:3000`
   - Backend API: `http://localhost:8080`
   - H2 Database Console: `http://localhost:8080/h2-console`

## ⚙️ Configuration

### Backend Configuration

The main configuration file is `backend/src/main/resources/application.properties`. Key settings:

- **Database**: Uses H2 in-memory database (data is lost on restart)
- **Security**: JWT-based authentication
- **CORS**: Configured to accept requests from frontend
- **Admin User**: Automatically created on startup

### Frontend Configuration

The frontend is configured to proxy API requests to the backend:

- **Proxy**: `http://localhost:8080` (defined in `package.json`)
- **Dependencies**: React 18, Material-UI, Axios for API calls
- **Routing**: React Router for navigation

### Custom Configuration (Optional)

You can create a `config.properties` file based on the example:

```bash
cp backend/src/main/resources/config.properties.example backend/src/main/resources/config.properties
```

Then modify the values as needed.

## 📚 API Documentation

The BGU Market backend provides comprehensive REST APIs for all market operations. Below are the main API controllers and their endpoints:

### 🛒 Purchase API (`/api/purchases`)

The Purchase Controller handles all purchase-related operations including regular purchases, auctions, and bid-based negotiations.

#### Regular Purchase Operations

**Execute Purchase**
```bash
POST /api/purchases/execute
Content-Type: application/json

{
  "userId": "string",
  "shippingAddress": "string",
  "contactInfo": "string"
}
```

#### Auction Operations

**Submit Auction Offer**
```bash
POST /api/purchases/auction/offer
Content-Type: application/json

{
  "storeId": "string",
  "productId": "string", 
  "userId": "string",
  "offerPrice": number,
  "shippingAddress": "string",
  "contactInfo": "string"
}
```

**Open New Auction**
```bash
POST /api/purchases/auction/open
Content-Type: application/json

{
  "userId": "string",
  "storeId": "string",
  "productId": "string",
  "productName": "string",
  "productCategory": "string",
  "productDescription": "string",
  "startingPrice": number,
  "endTimeMillis": number
}
```

**Get Auction Status**
```bash
GET /api/purchases/auction/status/{userId}/{storeId}/{productId}
```

#### Bid Operations

**Submit Bid**
```bash
POST /api/purchases/bid/submit
Content-Type: application/json

{
  "storeId": "string",
  "productId": "string",
  "userId": "string", 
  "offerPrice": number,
  "shippingAddress": "string",
  "contactInfo": "string"
}
```

**Approve/Reject Bid**
```bash
POST /api/purchases/bid/approve
POST /api/purchases/bid/reject
Content-Type: application/json

{
  "storeId": "string",
  "productId": "string",
  "userId": "string",
  "approverId": "string"
}
```

**Counter Bid Operations**
```bash
POST /api/purchases/bid/counter
POST /api/purchases/bid/counter/accept
POST /api/purchases/bid/counter/decline
```

**Get Bid Status**
```bash
GET /api/purchases/bid/status/{storeId}/{productId}/{userId}
```

#### Purchase History

**Get User Purchases**
```bash
GET /api/purchases/user/{userId}
```

**Get Store Purchases**
```bash
GET /api/purchases/store/{storeId}
```

### 👤 User API (`/api/users`)

**User Registration**
```bash
POST /api/users/register/guest
POST /api/users/register
Content-Type: application/json

{
  "username": "string",
  "password": "string"  // Optional for guest
}
```

**Shopping Cart Operations**
```bash
POST /api/users/cart/add
POST /api/users/cart/remove
GET /api/users/cart
DELETE /api/users/cart
```

### 🏪 Store API (`/api/stores`)

**Store Management**
```bash
POST /api/stores/create
GET /api/stores/{storeId}
POST /api/stores/{storeId}/products
```

### 🔐 Authentication API (`/api/auth`)

**Login**
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

### 📖 API Documentation
- **[📁 Complete Documentation](docs/README.md)** - Main documentation hub
- **[🔐 Auth API Examples](docs/auth_api_examples.md)** - Login, logout, token validation
- **[👤 User API Examples](docs/user_api_examples.md)** - Registration, profile, shopping cart
- **[🏪 Store API Examples](docs/store_api_examples.md)** - Store management, products, permissions
- **[🛒 Purchase API Examples](docs/purchase_api_examples.md)** - Purchases, auctions, bids, history

### 📋 Complete API Documentation

For detailed API documentation with all endpoints, request/response examples, and error codes, see:

- **Purchase API**: `backend/src/main/java/market/controllers/docs/purchase_api_documentation.md`
- **Controller Source**: `backend/src/main/java/market/controllers/`

## 🧪 Testing

### API Testing

**General API Testing:**
Use the provided script to test basic API endpoints:

```bash
chmod +x test_api.sh
./test_api.sh
```

This script tests:
- Login endpoint
- User registration
- Guest registration

**Purchase API Testing:**
Use the specialized Purchase API test scripts:

```bash
# Comprehensive endpoint testing (all 13 endpoints)
chmod +x test_purchase_endpoints.sh
./test_purchase_endpoints.sh

# Realistic scenario testing with data setup
chmod +x test_purchase_realistic.sh
./test_purchase_realistic.sh
```

The Purchase API tests cover:
- ✅ Regular purchase execution
- ✅ Auction operations (submit offers, open auctions, get status)
- ✅ Bid operations (submit, approve, reject, counter-offers)
- ✅ Purchase history retrieval
- ✅ Error handling and validation

### Manual Testing
- **Frontend**: Use the web interface at `http://localhost:3000`
- **Backend APIs**: Use tools like Postman, curl, or the provided test scripts
- **Database**: Check data via H2 console at `http://localhost:8080/h2-console`

### API Testing Examples

**Test Purchase Execution:**
```bash
curl -X POST "http://localhost:8080/api/purchases/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser1",
    "shippingAddress": "123 Main Street, City, State",
    "contactInfo": "user@example.com"
  }'
```

**Test User Registration:**
```bash
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123"
  }'
```

**Test Purchase History:**
```bash
curl -X GET "http://localhost:8080/api/purchases/user/testuser1"
```

### H2 Database Console Access
- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:bgumarket`
- **Username**: `sa`
- **Password**: (leave empty)

## 🔐 Default Admin Credentials

The system automatically creates an admin user on startup:

- **Username**: `admin`
- **Password**: `admin`

You can use these credentials to:
- Log into the application
- Access admin features
- Test the authentication system

## 🛠️ Troubleshooting

### Common Issues

1. **Port 8080 already in use**
   - Change `server.port` in `application.properties`
   - Kill the process using port 8080: `lsof -ti:8080 | xargs kill -9`

2. **Maven build fails**
   - Ensure Java 17+ is installed: `java -version`
   - Clear Maven cache: `mvn clean`

3. **npm install fails**
   - Clear npm cache: `npm cache clean --force`
   - Delete `node_modules` and `package-lock.json`, then run `npm install`

4. **Frontend can't connect to backend**
   - Ensure backend is running on port 8080
   - Check proxy configuration in `frontend/package.json`

### Development Tips

- **Hot Reload**: Both frontend and backend support hot reload during development
- **Database Reset**: Restart the backend to reset the H2 database
- **Logs**: Check console output for error messages
- **CORS Issues**: Backend is configured to accept requests from `localhost:3000`

## 📝 Additional Resources

- **Backend Documentation**: See `backend/README_CONFIGURATION.md` for detailed configuration options
- **Spring Boot Docs**: [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **React Docs**: [https://reactjs.org/docs](https://reactjs.org/docs)

---

## 🚀 Quick Commands Summary

```bash
# Clone and setup
git clone <repository-url>
cd BGU-Market

# Backend
cd backend
mvn clean install
mvn spring-boot:run

# Frontend (in new terminal)
cd frontend
npm install
npm start

# Testing
chmod +x test_api.sh && ./test_api.sh                    # Basic API tests
chmod +x test_purchase_endpoints.sh && ./test_purchase_endpoints.sh    # Purchase API tests
chmod +x test_purchase_realistic.sh && ./test_purchase_realistic.sh    # Realistic purchase tests

# Access
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
# Database: http://localhost:8080/h2-console
# Purchase API: http://localhost:8080/api/purchases
```

**Happy coding! 🎉**
