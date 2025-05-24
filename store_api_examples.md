# Store API Endpoints - Curl Examples

Base URL: `http://localhost:8080/api/stores`

## Store Management

### 1. Create Store
```bash
curl -X POST "http://localhost:8080/api/stores/create" \
  -H "Content-Type: application/json" \
  -d '{"storeName":"MyStore", "founderId":"user123"}'
```

### 2. Get Store Information
```bash
curl -X GET "http://localhost:8080/api/stores/MyStore"
```

### 3. Close Store
```bash
curl -X POST "http://localhost:8080/api/stores/1/close?userName=user123"
```

### 4. Open Store
```bash
curl -X POST "http://localhost:8080/api/stores/1/open?userName=user123"
```

## Owner Management

### 5. Add Additional Store Owner
```bash
curl -X POST "http://localhost:8080/api/stores/owners/add" \
  -H "Content-Type: application/json" \
  -d '{"appointerID":"user123", "newOwnerID":"newowner", "storeID":"1"}'
```

### 6. Owner Appointment Request
```bash
curl -X POST "http://localhost:8080/api/stores/owners/request" \
  -H "Content-Type: application/json" \
  -d '{"appointerID":"owner1", "newOwnerID":"newowner", "storeID":"1"}'
```

### 7. Remove Owner
```bash
curl -X DELETE "http://localhost:8080/api/stores/1/owners/ownerToRemove?requesterId=user123"
```

### 8. Check if User is Owner
```bash
curl -X GET "http://localhost:8080/api/stores/1/owners/user123/check"
```

## Manager Management

### 9. Add New Manager
```bash
curl -X POST "http://localhost:8080/api/stores/managers/add" \
  -H "Content-Type: application/json" \
  -d '{"appointerID":"owner1", "newManagerName":"manager1", "storeID":"1"}'
```

### 10. Add Permission to Manager
```bash
curl -X POST "http://localhost:8080/api/stores/managers/permissions/add" \
  -H "Content-Type: application/json" \
  -d '{"managerID":"manager1", "appointerID":"owner1", "permissionID":1, "storeID":"1"}'
```

### 11. Get Manager Permissions
```bash
curl -X GET "http://localhost:8080/api/stores/1/managers/manager1/permissions?whoIsAsking=owner1"
```

### 12. Remove Permission from Manager
```bash
curl -X DELETE "http://localhost:8080/api/stores/1/managers/manager1/permissions/1?appointerID=owner1"
```

### 13. Check if User is Manager
```bash
curl -X GET "http://localhost:8080/api/stores/1/managers/manager1/check"
```

## Product Listing Management

### 14. Add New Listing
```bash
curl -X POST "http://localhost:8080/api/stores/listings/add" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"owner1",
    "storeID":"1",
    "productId":"PROD001",
    "productName":"Laptop",
    "productCategory":"Electronics",
    "productDescription":"High-performance laptop",
    "quantity":5,
    "price":999.99
  }'
```

### 15. Remove Listing
```bash
curl -X DELETE "http://localhost:8080/api/stores/1/listings/PROD001?userName=owner1"
```

### 16. Edit Listing Price
```bash
curl -X PUT "http://localhost:8080/api/stores/listings/price" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"owner1",
    "storeID":"1",
    "listingId":"PROD001",
    "newPrice":899.99
  }'
```

### 17. Edit Listing Product Name
```bash
curl -X PUT "http://localhost:8080/api/stores/listings/name" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"owner1",
    "storeID":"1",
    "listingId":"PROD001",
    "newValue":"Gaming Laptop"
  }'
```

### 18. Edit Listing Description
```bash
curl -X PUT "http://localhost:8080/api/stores/listings/description" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"owner1",
    "storeID":"1",
    "listingId":"PROD001",
    "newValue":"High-performance gaming laptop with RTX graphics"
  }'
```

### 19. Edit Listing Quantity
```bash
curl -X PUT "http://localhost:8080/api/stores/listings/quantity" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"owner1",
    "storeID":"1",
    "listingId":"PROD001",
    "newQuantity":10
  }'
```

### 20. Edit Listing Category
```bash
curl -X PUT "http://localhost:8080/api/stores/listings/category" \
  -H "Content-Type: application/json" \
  -d '{
    "userName":"owner1",
    "storeID":"1",
    "listingId":"PROD001",
    "newValue":"Gaming"
  }'
```

## Query Operations

### 21. Get Product Price
```bash
curl -X GET "http://localhost:8080/api/stores/1/products/PROD001/price"
```

### 22. Get All Stores and Products Information
```bash
curl -X GET "http://localhost:8080/api/stores/info"
```

### 23. Get Listing Repository
```bash
curl -X GET "http://localhost:8080/api/stores/listings/repository"
```

## Notes

- Replace `localhost:8080` with your actual server address and port
- Replace store IDs, user names, and product IDs with actual values from your system
- Some endpoints require authentication - add authorization headers as needed
- The permission IDs are integers representing different permission types
- Make sure the application is running before executing these commands

## Quick Test Sequence

1. First register users and get authentication tokens:
   ```bash
   curl -X POST "http://localhost:8080/api/users/register" \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser", "password":"password123"}'
   ```

2. Create a store:
   ```bash
   curl -X POST "http://localhost:8080/api/stores/create" \
     -H "Content-Type: application/json" \
     -d '{"storeName":"TestStore", "founderId":"testuser"}'
   ```

3. Add a product listing:
   ```bash
   curl -X POST "http://localhost:8080/api/stores/listings/add" \
     -H "Content-Type: application/json" \
     -d '{
       "userName":"testuser",
       "storeID":"2",
       "productId":"TEST001",
       "productName":"Test Product",
       "productCategory":"Test",
       "productDescription":"A test product",
       "quantity":10,
       "price":19.99
     }'
   ``` 