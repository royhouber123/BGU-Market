# 🔔 Notification API Examples

This document provides comprehensive examples for the Notification API endpoints. The notification system keeps users informed about important market activities.

## 🔗 Base URL
```
http://localhost:8080/api/notifications
```

## 📚 Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{userId}` | Get all notifications for a user |
| POST | `/{userId}/read/{notificationId}` | Mark a notification as read |

---

## 📬 Get User Notifications

Retrieve all notifications for a specific user.

**Endpoint**: `GET /api/notifications/{userId}`

**Path Parameters**:
- `userId`: The ID of the user whose notifications to retrieve

**Example**:
```bash
curl -X GET "http://localhost:8080/api/notifications/alice"
```

**Success Response**:
```json
[
  {
    "id": "notif123",
    "userId": "alice",
    "title": "Purchase Completed",
    "message": "Your purchase of iPhone 15 has been completed successfully",
    "type": "PURCHASE_SUCCESS",
    "timestamp": "2024-01-15T10:30:00Z",
    "isRead": false,
    "data": {
      "orderId": "order456",
      "productId": "prod789",
      "storeId": "store123"
    }
  },
  {
    "id": "notif124",
    "userId": "alice",
    "title": "Bid Accepted",
    "message": "Your bid of $950 for MacBook Pro has been accepted",
    "type": "BID_ACCEPTED",
    "timestamp": "2024-01-14T15:45:00Z",
    "isRead": true,
    "data": {
      "bidId": "bid789",
      "productId": "prod456",
      "storeId": "store123",
      "amount": 950
    }
  },
  {
    "id": "notif125",
    "userId": "alice",
    "title": "Store Policy Updated",
    "message": "TechHub has updated their return policy",
    "type": "STORE_UPDATE",
    "timestamp": "2024-01-13T09:15:00Z",
    "isRead": false,
    "data": {
      "storeId": "store123",
      "storeName": "TechHub",
      "updateType": "POLICY_CHANGE"
    }
  }
]
```

**Empty Response** (no notifications):
```json
[]
```

---

## ✅ Mark Notification as Read

Mark a specific notification as read for a user.

**Endpoint**: `POST /api/notifications/{userId}/read/{notificationId}`

**Path Parameters**:
- `userId`: The ID of the user who owns the notification
- `notificationId`: The ID of the notification to mark as read

**Example**:
```bash
curl -X POST "http://localhost:8080/api/notifications/alice/read/notif123"
```

**Success Response**:
```
HTTP 200 OK
(No response body - successful completion)
```

---

## 📋 Notification Types

The system generates notifications for various events:

### Purchase-Related Notifications
- `PURCHASE_SUCCESS`: Purchase completed successfully
- `PURCHASE_FAILED`: Purchase failed due to various reasons
- `PAYMENT_PROCESSED`: Payment has been processed
- `ORDER_SHIPPED`: Order has been shipped

### Auction & Bidding Notifications
- `BID_ACCEPTED`: Your bid has been accepted
- `BID_REJECTED`: Your bid has been rejected
- `BID_COUNTER_OFFER`: Store made a counter offer to your bid
- `AUCTION_WON`: You won an auction
- `AUCTION_LOST`: You lost an auction
- `AUCTION_ENDING_SOON`: Auction ending in 1 hour

### Store Management Notifications
- `STORE_UPDATE`: Store policies or information updated
- `PRODUCT_ADDED`: New product added to followed store
- `PRICE_CHANGE`: Product price changed
- `INVENTORY_LOW`: Product inventory running low (for store owners)

### User Account Notifications
- `ACCOUNT_SUSPENDED`: Account has been suspended
- `ACCOUNT_REACTIVATED`: Account has been reactivated
- `PERMISSION_GRANTED`: New store permissions granted
- `PERMISSION_REVOKED`: Store permissions revoked

---

## 🔧 Testing Scenarios

### Complete Notification Workflow Test
```bash
#!/bin/bash

echo "🔔 Testing Notification API..."

# 1. Get all notifications for user
echo "1. Getting notifications for user 'alice'..."
RESPONSE=$(curl -s -X GET "http://localhost:8080/api/notifications/alice")
echo "$RESPONSE" | jq '.'

# 2. Extract first notification ID (if any exist)
NOTIF_ID=$(echo "$RESPONSE" | jq -r '.[0].id // empty')

if [ ! -z "$NOTIF_ID" ]; then
    echo -e "\n2. Marking notification $NOTIF_ID as read..."
    curl -X POST "http://localhost:8080/api/notifications/alice/read/$NOTIF_ID"
    
    echo -e "\n3. Getting notifications again to verify read status..."
    curl -s -X GET "http://localhost:8080/api/notifications/alice" | jq '.'
else
    echo -e "\n2. No notifications found for user 'alice'"
fi

echo -e "\n✅ Notification API test complete!"
```

### Test with Multiple Users
```bash
#!/bin/bash

echo "🔔 Testing Notifications for Multiple Users..."

USERS=("alice" "bob" "charlie" "diana" "eve")

for user in "${USERS[@]}"; do
    echo -e "\n📬 Getting notifications for user: $user"
    curl -s -X GET "http://localhost:8080/api/notifications/$user" | jq '. | length' | xargs -I {} echo "  → {} notifications found"
done

echo -e "\n✅ Multi-user notification test complete!"
```

---

## 🎯 Integration Examples

### Frontend Integration (JavaScript)
```javascript
// Get user notifications
async function getUserNotifications(userId) {
    try {
        const response = await fetch(`/api/notifications/${userId}`);
        const notifications = await response.json();
        return notifications;
    } catch (error) {
        console.error('Failed to fetch notifications:', error);
        return [];
    }
}

// Mark notification as read
async function markNotificationAsRead(userId, notificationId) {
    try {
        await fetch(`/api/notifications/${userId}/read/${notificationId}`, {
            method: 'POST'
        });
        console.log('Notification marked as read');
    } catch (error) {
        console.error('Failed to mark notification as read:', error);
    }
}

// Display notifications in UI
function displayNotifications(notifications) {
    const container = document.getElementById('notifications');
    container.innerHTML = '';
    
    notifications.forEach(notification => {
        const div = document.createElement('div');
        div.className = `notification ${notification.isRead ? 'read' : 'unread'}`;
        div.innerHTML = `
            <h4>${notification.title}</h4>
            <p>${notification.message}</p>
            <small>${new Date(notification.timestamp).toLocaleString()}</small>
            ${!notification.isRead ? `<button onclick="markAsRead('${notification.id}')">Mark as Read</button>` : ''}
        `;
        container.appendChild(div);
    });
}
```

### Real-time Updates (WebSocket Integration)
```javascript
// Connect to WebSocket for real-time notifications
const ws = new WebSocket('ws://localhost:8080/notifications');

ws.onmessage = function(event) {
    const notification = JSON.parse(event.data);
    
    // Add to notification list
    addNotificationToUI(notification);
    
    // Show browser notification if supported
    if (Notification.permission === 'granted') {
        new Notification(notification.title, {
            body: notification.message,
            icon: '/notification-icon.png'
        });
    }
};
```

---

## ⚠️ Error Responses

**User Not Found**:
```json
{
  "error": "User not found",
  "message": "User with ID 'nonexistent' does not exist",
  "status": 404
}
```

**Notification Not Found**:
```json
{
  "error": "Notification not found",
  "message": "Notification with ID 'notif999' not found for user 'alice'",
  "status": 404
}
```

**Unauthorized Access**:
```json
{
  "error": "Unauthorized",
  "message": "User 'bob' cannot access notifications for user 'alice'",
  "status": 403
}
```

---

## 📝 Implementation Notes

### Notification Lifecycle
1. **Creation**: Notifications are automatically created by the system based on user actions
2. **Delivery**: Notifications are stored in the database and accessible via API
3. **Reading**: Users can mark notifications as read, changing their status
4. **Retention**: Notifications are kept for audit purposes (configurable retention period)

### Performance Considerations
- Notifications are indexed by userId for fast retrieval
- Consider pagination for users with many notifications
- Read status updates are immediate and don't require page refresh

### Security Notes
- Users can only access their own notifications
- Admin users can access any user's notifications
- Notification content should not contain sensitive information directly

---

*For more API examples, see the [main documentation](README.md)* 