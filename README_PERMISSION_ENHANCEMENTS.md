# Enhanced User Management and Permission System

This document outlines the improvements made to the BGU Market user management and permission system.

## Key Enhancements

### 1. User Existence Validation

**Problem:** Previously, users could be added to stores without checking if they exist in the system.

**Solution:**
- Added backend endpoint `/api/users/validate/{userId}` to check user existence
- Frontend validates user existence before attempting to add them to stores
- Backend services now validate user existence in `addNewManager` and `addAdditionalStoreOwner` methods

**Implementation:**
```javascript
// Frontend validation
const userExists = await storeService.validateUserExists(userId);
if (!userExists) {
    // Show error message
}
```

```java
// Backend validation in StoreService
try {
    userRepository.findById(newManagerName);
} catch (Exception e) {
    return ApiResponse.fail("User '" + newManagerName + "' does not exist in the system");
}
```

### 2. Permission-Based UI Controls

**Problem:** UI elements were shown to all users regardless of their permissions.

**Solution:**
- Integrated `useStorePermissions` hook throughout UserManagement component
- UI elements are now conditionally rendered based on user permissions
- Added permission checks for:
  - Adding users (owners/managers)
  - Removing users
  - Editing manager permissions
  - Viewing user management interface

**Permission Hierarchy:**
1. **Founder**: Can do everything, cannot be removed
2. **Owner**: Can manage all store aspects including user management
3. **Manager**: Limited to assigned permissions, cannot manage users

### 3. Enhanced Permission System

**Current Permissions (matching backend Permission enum):**
```java
public enum Permission {
    VIEW_ONLY(0),      // Basic viewing permissions
    EDIT_PRODUCTS(1),  // Add, edit, remove products
    EDIT_POLICIES(2),  // Manage store policies
    BID_APPROVAL(3);   // Approve/reject customer bids
}
```

**Permission Descriptions:**
- **View Only (0)**: Can view store information and products
- **Edit Products (1)**: Can add, edit, and remove products
- **Edit Policies (2)**: Can create and modify store policies
- **Bid Approval (3)**: Can approve or reject customer bids

### 4. Backend Validation Improvements

**Enhanced Security:**
- All user addition operations now validate:
  1. User exists in the system
  2. Requester has appropriate permissions
  3. User is not already in the store
  4. Store exists and is accessible

**Error Handling:**
- Comprehensive error messages for different failure scenarios
- Proper HTTP status codes
- Detailed logging for debugging

## API Endpoints

### New Endpoint
```
GET /api/users/validate/{userId}
Response: { "success": true, "data": { "exists": boolean } }
```

### Enhanced Endpoints
- `POST /stores/owners/add` - Now validates user existence
- `POST /stores/managers/add` - Now validates user existence
- All permission management endpoints include proper authorization checks

## Frontend Components

### UserManagement Component Enhancements

**Permission-Based Rendering:**
```jsx
// Only show add user button if user has permissions
{(canManageUsers || isOwnerOrFounder) && (
    <Button onClick={() => setAddUserDialog(true)}>
        Add User
    </Button>
)}

// Only show edit/remove buttons for authorized users
{user.role === "MANAGER" && (canManageUsers || isOwnerOrFounder) && (
    <Button onClick={() => handleEditPermissions(user)}>
        Edit Permissions
    </Button>
)}
```

**User Validation Flow:**
1. User enters username in dialog
2. Frontend validates user exists via API call
3. Checks if user already exists in store
4. Shows appropriate error messages
5. Proceeds with backend API call if validation passes

## Security Considerations

### Frontend Security
- All permission checks are enforced
- UI elements hidden/disabled based on permissions
- Validation feedback prevents invalid operations

### Backend Security
- User existence validation prevents adding non-existent users
- Permission hierarchy strictly enforced
- Only authorized users can perform sensitive operations
- Comprehensive input validation and error handling

## Usage Examples

### Adding a Manager (Frontend)
```javascript
// Check permissions first
if (!canManageUsers && !isOwnerOrFounder) {
    showError("You don't have permission to add users");
    return;
}

// Validate user exists
const userExists = await validateUserExists(userId);
if (!userExists) {
    showError(`User '${userId}' does not exist`);
    return;
}

// Check if already in store
const existingUser = users.find(user => user.id === userId);
if (existingUser) {
    showError(`User is already a ${existingUser.role.toLowerCase()}`);
    return;
}

// Proceed with adding
await storeService.addNewManager(currentUser.userName, userId, storeId);
```

### Backend Permission Check
```java
public ApiResponse<Void> addNewManager(String appointerID, String newManagerName, String storeID) {
    // Validate store exists
    Store s = storeRepository.getStoreByID(storeID);
    if (s == null) {
        return ApiResponse.fail("store doesn't exist");
    }

    // Validate user exists
    try {
        userRepository.findById(newManagerName);
    } catch (Exception e) {
        return ApiResponse.fail("User '" + newManagerName + "' does not exist in the system");
    }

    // Store domain object handles permission validation
    if (s.addNewManager(appointerID, newManagerName)) {
        return ApiResponse.ok(null);
    }
    // Error handling...
}
```

## Benefits

1. **Enhanced Security**: Prevents adding non-existent users and unauthorized operations
2. **Better UX**: Clear error messages and permission-based UI
3. **Maintainability**: Centralized permission logic using hooks
4. **Reliability**: Comprehensive validation at both frontend and backend
5. **Compliance**: Proper authorization hierarchy enforcement

## Future Enhancements

1. **Additional Permissions**: Can easily add new permission types to the enum
2. **Role-Based Access**: Could implement more granular role-based permissions
3. **Audit Trail**: Add logging for all user management operations
4. **Notification System**: Notify users when their permissions change
5. **Batch Operations**: Support for bulk user management operations

## Testing

The enhanced system includes:
- Frontend validation testing
- Backend integration tests for user management
- Permission enforcement testing
- Error handling verification
- UI component testing with different permission levels

This enhanced permission system provides a robust, secure, and user-friendly approach to managing store users and their permissions in the BGU Market application. 