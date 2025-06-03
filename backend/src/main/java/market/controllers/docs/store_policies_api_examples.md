# Store Policies API Endpoints - Curl Examples

Base URL: `http://localhost:8080/api/stores/{storeId}/policies`

## Discount Policies

### 1. Add Discount Policy
Add a new discount policy to a store.

#### Simple Percentage Discount
```bash
curl -X POST "http://localhost:8080/api/stores/1/policies/discounts?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PERCENTAGE",
    "scope": "PRODUCT",
    "scopeId": "PROD001",
    "value": 0.15,
    "couponCode": null,
    "condition": null,
    "subDiscounts": [],
    "combinationType": "SUM"
  }'
```

#### Fixed Amount Discount
```bash
curl -X POST "http://localhost:8080/api/stores/1/policies/discounts?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "FIXED",
    "scope": "CATEGORY",
    "scopeId": "Electronics",
    "value": 50.0,
    "couponCode": null,
    "condition": null,
    "subDiscounts": [],
    "combinationType": "SUM"
  }'
```

#### Conditional Discount (with minimum basket total)
```bash
curl -X POST "http://localhost:8080/api/stores/1/policies/discounts?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PERCENTAGE",
    "scope": "STORE",
    "scopeId": "1",
    "value": 0.10,
    "couponCode": null,
    "condition": {
      "type": "BASKET_TOTAL_AT_LEAST",
      "params": {
        "threshold": 100.0
      },
      "subConditions": null,
      "logic": null
    },
    "subDiscounts": [],
    "combinationType": "SUM"
  }'
```

#### Coupon-Based Discount
```bash
curl -X POST "http://localhost:8080/api/stores/1/policies/discounts?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PERCENTAGE",
    "scope": "STORE",
    "scopeId": "1",
    "value": 0.20,
    "couponCode": "SAVE20",
    "condition": null,
    "subDiscounts": [],
    "combinationType": "SUM"
  }'
```

#### Composite Discount (multiple conditions)
```bash
curl -X POST "http://localhost:8080/api/stores/1/policies/discounts?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "COMPOSITE",
    "scope": "STORE",
    "scopeId": "1",
    "value": 0.0,
    "couponCode": null,
    "condition": {
      "type": "COMPOSITE",
      "params": {},
      "subConditions": [
        {
          "type": "BASKET_TOTAL_AT_LEAST",
          "params": {
            "threshold": 200.0
          },
          "subConditions": null,
          "logic": null
        },
        {
          "type": "PRODUCT_QUANTITY_AT_LEAST",
          "params": {
            "productId": "PROD001",
            "quantity": 2
          },
          "subConditions": null,
          "logic": null
        }
      ],
      "logic": "AND"
    },
    "subDiscounts": [
      {
        "type": "PERCENTAGE",
        "scope": "PRODUCT",
        "scopeId": "PROD001",
        "value": 0.15,
        "couponCode": null,
        "condition": null,
        "subDiscounts": [],
        "combinationType": "SUM"
      }
    ],
    "combinationType": "MAXIMUM"
  }'
```

### 2. Remove Discount Policy
Remove an existing discount policy from a store.

```bash
curl -X DELETE "http://localhost:8080/api/stores/1/policies/discounts?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PERCENTAGE",
    "scope": "PRODUCT",
    "scopeId": "PROD001",
    "value": 0.15,
    "couponCode": null,
    "condition": null,
    "subDiscounts": [],
    "combinationType": "SUM"
  }'
```

### 3. Get All Discount Policies
Retrieve all discount policies for a store.

```bash
curl -X GET "http://localhost:8080/api/stores/1/policies/discounts?userId=owner1"
```

## Purchase Policies

### 4. Add Purchase Policy

#### Minimum Items Policy
```bash
curl -X POST "http://localhost:8080/api/stores/1/policies/purchase?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MINITEMS",
    "value": 2
  }'
```

#### Maximum Items Policy
```bash
curl -X POST "http://localhost:8080/api/stores/1/policies/purchase?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MAXITEMS",
    "value": 10
  }'
```

#### Minimum Price Policy
```bash
curl -X POST "http://localhost:8080/api/stores/1/policies/purchase?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MINPRICE",
    "value": 50
  }'
```

### 5. Remove Purchase Policy
Remove an existing purchase policy from a store.

```bash
curl -X DELETE "http://localhost:8080/api/stores/1/policies/purchase?userId=owner1" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "MINITEMS",
    "value": 2
  }'
```

### 6. Get All Purchase Policies
Retrieve all purchase policies for a store.

```bash
curl -X GET "http://localhost:8080/api/stores/1/policies/purchase?userId=owner1"
```

## Example Responses

### Successful Response (Add/Remove Policy)
```json
{
  "success": true,
  "data": true,
  "error": null
}
```

### Error Response (No Permission)
```json
{
  "success": false,
  "data": null,
  "error": "User owner1 doesn't have permission to ADD discount!"
}
```

### Get Discounts Response
```json
{
  "success": true,
  "data": [
    {
      "type": "PERCENTAGE",
      "scope": "PRODUCT",
      "scopeId": "PROD001",
      "value": 0.15,
      "couponCode": null,
      "condition": null,
      "subDiscounts": [],
      "combinationType": "SUM"
    },
    {
      "type": "FIXED",
      "scope": "CATEGORY",
      "scopeId": "Electronics",
      "value": 50.0,
      "couponCode": null,
      "condition": null,
      "subDiscounts": [],
      "combinationType": "SUM"
    }
  ],
  "error": null
}
```

### Get Purchase Policies Response
```json
{
  "success": true,
  "data": [
    {
      "type": "MINITEMS",
      "value": 2
    },
    {
      "type": "MAXITEMS",
      "value": 10
    }
  ],
  "error": null
}
```

## Policy Types and Parameters

### Discount Types
- **PERCENTAGE**: Percentage-based discount (value: 0.0-1.0)
- **FIXED**: Fixed amount discount (value: positive number)
- **COMPOSITE**: Complex discount with multiple sub-discounts

### Discount Scopes
- **PRODUCT**: Apply to specific product (scopeId: productId)
- **CATEGORY**: Apply to product category (scopeId: categoryName)
- **STORE**: Apply to entire store (scopeId: storeId)

### Purchase Policy Types
- **MINITEMS**: Minimum number of items required (value: positive integer)
- **MAXITEMS**: Maximum number of items allowed (value: positive integer)
- **MINPRICE**: Minimum total price required (value: positive integer)

### Condition Types
- **BASKET_TOTAL_AT_LEAST**: Require minimum basket total
- **PRODUCT_QUANTITY_AT_LEAST**: Require minimum quantity of specific product
- **CATEGORY_QUANTITY_AT_LEAST**: Require minimum quantity from category
- **COMPOSITE**: Combine multiple conditions with AND/OR/XOR logic

### Combination Types
- **SUM**: Add all applicable discounts
- **MAXIMUM**: Apply only the highest discount

## Notes

- Replace `localhost:8080` with your actual server address and port
- Replace `{storeId}` with actual store ID (e.g., "1")
- Replace `userId` parameter with actual user ID
- Only store owners and managers with EDIT_POLICIES permission can add/remove policies
- All users with proper access can view policies
- The store must be active (open) to modify policies
- Discount values for percentage discounts should be between 0.0 and 1.0 (e.g., 0.15 = 15%)
- Fixed discount values should be positive numbers representing the discount amount
- Purchase policy values should be positive integers

## Quick Test Sequence

1. First create a store and add products:
   ```bash
   # Create store
   curl -X POST "http://localhost:8080/api/stores/create" \
     -H "Content-Type: application/json" \
     -d '{"storeName":"TestStore", "founderId":"owner1"}'
   
   # Add a product
   curl -X POST "http://localhost:8080/api/stores/listings/add" \
     -H "Content-Type: application/json" \
     -d '{
       "userName":"owner1",
       "storeID":"1",
       "productId":"PROD001",
       "productName":"Test Product",
       "productCategory":"Electronics",
       "productDescription":"A test product",
       "quantity":10,
       "price":100.0
     }'
   ```

2. Add policies and test them with the endpoints above.

3. Verify policies are working by checking discount calculations during purchase operations. 