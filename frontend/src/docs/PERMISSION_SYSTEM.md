# Store Permission System Documentation

## Overview

The BGU Market application now includes a comprehensive permission-based system for store management. This system ensures that users can only perform actions they have permission for, improving security and providing better user experience.

## Permission Types

The system defines four main permission levels:

| Permission Code | Name | Description |
|---|---|---|
| 0 | VIEW_ONLY | Basic permission to view store information |
| 1 | EDIT_PRODUCTS | Permission to add, edit, and remove products |
| 2 | EDIT_POLICIES | Permission to manage store policies (discounts, purchase rules) |
| 3 | BID_APPROVAL | Permission to view, approve, reject, and counter customer bids |

## User Roles

### Founder
- **Description**: The original creator of the store
- **Permissions**: All permissions (0, 1, 2, 3)
- **Capabilities**: Can manage all aspects of the store, including user management

### Owner
- **Description**: Store owners appointed by the founder or other owners
- **Permissions**: All permissions (0, 1, 2, 3)
- **Capabilities**: Can manage all aspects of the store, including user management

### Manager
- **Description**: Store managers appointed by owners
- **Permissions**: Assigned by the owner who appointed them
- **Capabilities**: Limited to their assigned permissions

### None
- **Description**: Users with no role in the store
- **Permissions**: No permissions
- **Capabilities**: Can only view public store information

## How It Works

### Backend Implementation

1. **Permission Check Endpoint**: `GET /api/stores/{storeID}/user/{userID}/permissions`
   - Returns user's role and specific capabilities
   - Used by frontend to determine what UI elements to show

2. **Permission Enforcement**: All store modification endpoints check permissions before allowing operations

### Frontend Implementation

1. **Permission Hook**: `useStorePermissions(storeId)`
   - Fetches and caches user permissions for a store
   - Provides helper functions for permission checks

2. **Conditional UI Rendering**: UI elements are conditionally rendered based on permissions
   - Add Product buttons only shown if user has EDIT_PRODUCTS permission
   - Bid Management only shown if user has BID_APPROVAL permission
   - Policy Management only shown if user has EDIT_POLICIES permission

## Usage Examples

### Using the Permission Hook

```javascript
import { useStorePermissions, PERMISSIONS } from '../hooks/useStorePermissions';

function MyComponent({ storeId }) {
  const {
    canEditProducts,
    canEditPolicies,
    canApproveBids,
    role,
    hasPermission
  } = useStorePermissions(storeId);

  return (
    <div>
      {canEditProducts && <AddProductButton />}
      {canApproveBids && <ManageBidsButton />}
      {hasPermission(PERMISSIONS.EDIT_POLICIES) && <PolicyManagement />}
    </div>
  );
}
```

### Permission States

The hook provides these helpful properties:

- `canEditProducts` - Boolean: Can add/edit/remove products
- `canEditPolicies` - Boolean: Can manage store policies
- `canApproveBids` - Boolean: Can manage customer bids
- `canManageUsers` - Boolean: Can manage store owners/managers
- `isOwnerOrFounder` - Boolean: Has owner-level access
- `isManager` - Boolean: Is a store manager
- `hasAnyRole` - Boolean: Has any role in the store
- `role` - String: User's role ('FOUNDER', 'OWNER', 'MANAGER', 'NONE')

## Error Handling

When users try to access features they don't have permission for:

1. **UI Prevention**: Buttons and controls are hidden/disabled
2. **API Errors**: Backend returns 403 Forbidden with descriptive messages
3. **User Guidance**: Clear messages explain what permissions are needed
4. **Graceful Degradation**: Read-only modes when edit permissions are missing

## Permission Management

### For Store Owners

1. Go to Store Management page
2. Find the manager in the user list
3. Grant specific permissions as needed
4. Manager can immediately use new permissions

### For Managers

1. Ask store owner for needed permissions
2. Owner grants permissions through Store Management
3. Refresh the page to see updated permissions
4. Access previously restricted features

## Benefits

1. **Security**: Users can only perform authorized actions
2. **Clarity**: Clear indication of what each user can do
3. **Flexibility**: Granular permission control for different roles
4. **User Experience**: No confusing error messages for unauthorized actions
5. **Scalability**: Easy to add new permissions and roles

## API Endpoints

### Get User Permissions
```
GET /api/stores/{storeID}/user/{userID}/permissions
```

**Response:**
```json
{
  "success": true,
  "data": {
    "role": "MANAGER",
    "canEditProducts": true,
    "canEditPolicies": false,
    "canApproveBids": true,
    "canManageUsers": false,
    "permissions": [0, 1, 3]
  }
}
```

### Grant Permission to Manager
```
POST /api/stores/managers/permissions/add
{
  "managerID": "manager1",
  "appointerID": "owner1", 
  "permissionID": 3,
  "storeID": "1"
}
```

This system ensures that the BGU Market store management is secure, user-friendly, and scalable for different organizational structures. 