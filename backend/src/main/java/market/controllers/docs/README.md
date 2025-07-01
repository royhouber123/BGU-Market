# 📚 BGU Market API Documentation

Welcome to the comprehensive API documentation for the BGU Market system. This documentation provides detailed examples, testing instructions, and complete API reference for all available endpoints.

## 🚀 Quick Navigation

### 📖 API Documentation by Controller

| Controller | Endpoints | Documentation |
|------------|-----------|---------------|
| 🔐 **Authentication** | `/api/auth/*` | **[Auth API Examples](auth_api_examples.md)** |
| 👤 **User Management** | `/api/users/*` | **[User API Examples](user_api_examples.md)** |
| 🏪 **Store Management** | `/api/stores/*` | **[Store API Examples](store_api_examples.md)** |
| 🏷️ **Store Policies** | `/api/stores/{storeId}/policies/*` | **[Store Policies API Examples](store_policies_api_examples.md)** |
| 🛒 **Purchase System** | `/api/purchases/*` | **[Purchase API Examples](purchase_api_examples.md)** |
| 📦 **Product Catalog** | `/api/products/*` | **[Product API Examples](product_api_examples.md)** |
| 🔧 **Admin Management** | `/api/admin/*` | **[Admin API Examples](admin_api_examples.md)** |
| 🔔 **Notifications** | `/api/notifications/*` | **[Notification API Examples](notification_api_examples.md)** |

### 🧪 Testing Resources
- **[🔧 Complete Test Script](test_all_apis.sh)** - Automated testing for all APIs

---

## 🎯 Getting Started

### Prerequisites
1. Backend server running on `http://localhost:8080`
2. `curl` command available for testing
3. Optional: `jq` for JSON formatting

### Quick Test
```bash
# Test server connectivity
curl -X GET "http://localhost:8080/api/stores/info"

# Complete authentication flow
curl -X POST "http://localhost:8080/api/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

---

## 📋 API Overview

### Core Functionality

#### 🔐 Authentication & Authorization
- User registration (regular and guest users)
- JWT-based authentication
- Token validation and refresh
- Session management

#### 👤 User Management
- User profile management
- Shopping cart operations
- User preferences and settings
- Account status and history

#### 🏪 Store Operations
- Store creation and management
- Product catalog management
- Store permissions and roles
- Owner and manager operations

#### 🛒 Commerce Features
- Purchase execution and processing
- Auction system with bidding
- Counter-offer negotiations
- Purchase history and tracking

#### 🔧 Administrative Controls
- System-wide user management
- Store oversight and closure
- User suspension and account control
- Administrative reporting and analytics

#### 🔔 Communication System
- Real-time notifications
- Event-based messaging
- Notification status management
- User preference controls

---

## 🛠️ API Testing

### Automated Testing
Run the comprehensive test suite to validate all endpoints:

```bash
# Make script executable
chmod +x test_all_apis.sh

# Run all tests
./test_all_apis.sh
```

### Manual Testing
Each API documentation file includes:
- ✅ Complete endpoint examples
- ✅ Request/response samples
- ✅ Error handling examples
- ✅ Integration scenarios

### Testing Workflow
1. **Start with Authentication**: Register and login to get JWT tokens
2. **Setup Test Data**: Create users, stores, and products
3. **Test Core Features**: Execute purchases, manage stores, handle notifications
4. **Test Admin Features**: User management, store oversight
5. **Error Testing**: Validate error handling and edge cases

---

## 📊 Response Format

All API responses follow a consistent format:

### Success Response
```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Operation completed successfully"
}
```

### Error Response
```json
{
  "success": false,
  "error": "Error type",
  "message": "Detailed error message"
}
```

---

## 🔗 Integration Examples

### Frontend Integration (JavaScript)
```javascript
// API base URL
const API_BASE = 'http://localhost:8080/api';

// Generic API call function
async function apiCall(endpoint, method = 'GET', data = null) {
  const token = localStorage.getItem('jwt_token');
  const config = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    }
  };
  
  if (data) {
    config.body = JSON.stringify(data);
  }
  
  const response = await fetch(`${API_BASE}${endpoint}`, config);
  return response.json();
}

// Usage examples
const user = await apiCall('/users/me');
const stores = await apiCall('/stores');
const purchase = await apiCall('/purchases/execute', 'POST', purchaseData);
```

### Backend Integration (Java)
```java
// Service integration example
@Service
public class MarketService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private PurchaseService purchaseService;
    
    public void processUserPurchase(String userId, PurchaseRequest request) {
        // Validate user
        userService.validateUser(userId);
        
        // Process purchase
        Purchase purchase = purchaseService.executePurchase(request);
        
        // Send notification
        notificationService.sendPurchaseConfirmation(userId, purchase);
    }
}
```

---

## 🎯 Common Use Cases

### E-commerce Workflow
1. **User Registration**: Create account or continue as guest
2. **Store Discovery**: Browse available stores and products
3. **Shopping Cart**: Add/remove items, manage quantities
4. **Purchase Process**: Execute purchase with payment processing
5. **Order Tracking**: Monitor purchase status and history

### Store Management Workflow
1. **Store Creation**: Register as store owner
2. **Product Management**: Add/update/remove products
3. **Policy Configuration**: Set discount and purchase policies
4. **Order Fulfillment**: Process customer orders
5. **Analytics**: Track sales and performance

### Administrative Workflow
1. **User Oversight**: Monitor user activity and compliance
2. **Store Management**: Oversee store operations and policies
3. **Dispute Resolution**: Handle conflicts and issues
4. **System Maintenance**: Perform administrative tasks

---

## 📝 Development Notes

### Rate Limiting
- API calls are rate-limited to prevent abuse
- Limits vary by endpoint and user type
- See individual endpoint documentation for details

### Caching
- Frequently accessed data is cached for performance
- Cache invalidation occurs automatically on updates
- Some endpoints support conditional requests

### Security
- All sensitive operations require authentication
- Admin operations require elevated privileges
- Input validation and sanitization applied
- HTTPS recommended for production use

---

## 🆘 Troubleshooting

### Common Issues

**Connection Refused**
- Ensure backend server is running on port 8080
- Check firewall settings and network connectivity

**Authentication Failures**
- Verify JWT token format and expiration
- Check user credentials and account status

**Permission Denied**
- Confirm user has required permissions
- Verify admin status for administrative operations

**Data Not Found**
- Check entity IDs and references
- Verify data existence in database

### Getting Help
- Check individual API documentation files
- Review error messages and response codes
- Test with provided example commands
- Validate request format and parameters

---

## 📖 Additional Resources

- **[Main Project README](../../../../../../../README.md)** - Project setup and configuration
- **[Controller Source Code](../)** - Implementation details
- **[Demo Data Setup](../../../../../../../DEMO_DATA_README.md)** - Test data population

---

*Last updated: 2024-01-15*
*For the most current information, see individual API documentation files.* 