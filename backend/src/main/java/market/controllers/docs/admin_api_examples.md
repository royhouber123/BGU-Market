# 🔧 Admin API Examples

This document provides comprehensive examples for the Admin API endpoints. All admin operations require proper admin authentication.

## 🔗 Base URL
```
http://localhost:8080/api/admin
```

## 📚 Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/stores/close` | Close a store by admin request |
| GET | `/verify/{userId}` | Verify if a user is an admin |
| GET | `/stores` | Get list of all stores for admin management |
| GET | `/users` | Get list of all users for admin management |
| POST | `/users/suspend` | Suspend a user for specified duration |
| POST | `/users/unsuspend` | Unsuspend a user |
| GET | `/users/suspended` | Get list of all suspended users |

---

## 🏪 Store Management

### Close Store
Close a store by admin request.

**Endpoint**: `POST /api/admin/stores/close`

**Request Body**:
```json
{
  "adminId": "admin",
  "storeId": "store123"
}
```

**Example**:
```bash
curl -X POST "http://localhost:8080/api/admin/stores/close" \
  -H "Content-Type: application/json" \
  -d '{
    "adminId": "admin",
    "storeId": "store123"
  }'
```

**Success Response**:
```json
{
  "success": true,
  "data": true,
  "message": "Operation completed successfully"
}
```

### Get All Stores
Retrieve a list of all stores for admin management purposes.

**Endpoint**: `GET /api/admin/stores`

**Example**:
```bash
curl -X GET "http://localhost:8080/api/admin/stores"
```

**Success Response**:
```json
{
  "success": true,
  "data": {
    "stores": [
      {
        "storeId": "store123",
        "storeName": "TechHub",
        "ownerId": "alice",
        "status": "active",
        "products": 10
      }
    ]
  },
  "message": "Operation completed successfully"
}
```

---

## 👤 User Management

### Verify Admin Status
Check if a user has admin privileges.

**Endpoint**: `GET /api/admin/verify/{userId}`

**Example**:
```bash
curl -X GET "http://localhost:8080/api/admin/verify/admin"
```

**Success Response**:
```json
{
  "success": true,
  "data": true,
  "message": "Operation completed successfully"
}
```

### Get All Users
Retrieve a list of all users for admin management.

**Endpoint**: `GET /api/admin/users`

**Example**:
```bash
curl -X GET "http://localhost:8080/api/admin/users"
```

**Success Response**:
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "userId": "alice",
        "username": "alice",
        "status": "active",
        "registrationDate": "2024-01-01"
      }
    ]
  },
  "message": "Operation completed successfully"
}
```

### Suspend User
Suspend a user for a specified duration.

**Endpoint**: `POST /api/admin/users/suspend`

**Request Body**:
```json
{
  "adminId": "admin",
  "userId": "alice",
  "durationHours": 24
}
```

**Parameters**:
- `adminId`: Admin user ID performing the action
- `userId`: User ID to suspend
- `durationHours`: Duration in hours (0 = permanent suspension)

**Example - Temporary Suspension (24 hours)**:
```bash
curl -X POST "http://localhost:8080/api/admin/users/suspend" \
  -H "Content-Type: application/json" \
  -d '{
    "adminId": "admin",
    "userId": "alice",
    "durationHours": 24
  }'
```

**Example - Permanent Suspension**:
```bash
curl -X POST "http://localhost:8080/api/admin/users/suspend" \
  -H "Content-Type: application/json" \
  -d '{
    "adminId": "admin",
    "userId": "alice",
    "durationHours": 0
  }'
```

**Success Response**:
```json
{
  "success": true,
  "data": true,
  "message": "Operation completed successfully"
}
```

### Unsuspend User
Remove suspension from a user account.

**Endpoint**: `POST /api/admin/users/unsuspend`

**Request Body**:
```json
{
  "adminId": "admin",
  "userId": "alice"
}
```

**Example**:
```bash
curl -X POST "http://localhost:8080/api/admin/users/unsuspend" \
  -H "Content-Type: application/json" \
  -d '{
    "adminId": "admin",
    "userId": "alice"
  }'
```

**Success Response**:
```json
{
  "success": true,
  "data": true,
  "message": "Operation completed successfully"
}
```

### Get Suspended Users
Retrieve a list of all currently suspended users.

**Endpoint**: `GET /api/admin/users/suspended?adminId={adminId}`

**Example**:
```bash
curl -X GET "http://localhost:8080/api/admin/users/suspended?adminId=admin"
```

**Success Response**:
```json
{
  "success": true,
  "data": [
    "alice",
    "bob",
    "charlie"
  ],
  "message": "Operation completed successfully"
}
```

---

## 🔧 Testing Scenarios

### Complete Admin Workflow Test
```bash
#!/bin/bash

echo "🔧 Testing Admin API..."

# 1. Verify admin status
echo "1. Verifying admin status..."
curl -X GET "http://localhost:8080/api/admin/verify/admin"

# 2. Get all users
echo -e "\n2. Getting all users..."
curl -X GET "http://localhost:8080/api/admin/users"

# 3. Get all stores
echo -e "\n3. Getting all stores..."
curl -X GET "http://localhost:8080/api/admin/stores"

# 4. Suspend a user (if exists)
echo -e "\n4. Suspending user 'alice' for 1 hour..."
curl -X POST "http://localhost:8080/api/admin/users/suspend" \
  -H "Content-Type: application/json" \
  -d '{
    "adminId": "admin",
    "userId": "alice",
    "durationHours": 1
  }'

# 5. Get suspended users
echo -e "\n5. Getting suspended users..."
curl -X GET "http://localhost:8080/api/admin/users/suspended?adminId=admin"

# 6. Unsuspend the user
echo -e "\n6. Unsuspending user 'alice'..."
curl -X POST "http://localhost:8080/api/admin/users/unsuspend" \
  -H "Content-Type: application/json" \
  -d '{
    "adminId": "admin",
    "userId": "alice"
  }'

echo -e "\n✅ Admin API test complete!"
```

---

## ⚠️ Error Responses

**Admin Authorization Failed**:
```json
{
  "success": false,
  "error": "Unauthorized: Admin privileges required",
  "message": "User does not have admin privileges"
}
```

**User Not Found**:
```json
{
  "success": false,
  "error": "User not found",
  "message": "User with ID 'nonexistent' not found"
}
```

**Store Not Found**:
```json
{
  "success": false,
  "error": "Store not found",
  "message": "Store with ID 'nonexistent' not found"
}
```

**Invalid Duration**:
```json
{
  "success": false,
  "error": "Invalid duration",
  "message": "Duration must be a non-negative number"
}
```

---

## 📝 Notes

- All admin operations require valid admin credentials
- Suspension durations are specified in hours (0 = permanent)
- Admin actions are logged for audit purposes
- Store closure may affect active purchases and auctions
- User suspension prevents login but preserves data

---

*For more API examples, see the [main documentation](README.md)* 