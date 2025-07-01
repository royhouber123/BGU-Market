# BGU Market - Complete Acceptance Tests Documentatio

## AdminConfigTest

### testAdminUserCreationOnStartup
- **Setup:** Create test instances (UserRepository, AuthService, AdminConfig, StartupConfig) with reflection-based field injection
- **Parameters:** 
  - adminUsername = "admin"
  - adminPassword = "admin"
- **Scenario:** Run startup configuration using startupConfig.run()
- **Expected Result:** Admin user should be created and exist in repository with username "admin"

### testAdminUserLogin
- **Setup:** Run startup to create admin user first
- **Parameters:**
  - username = "admin"
  - password = "admin"
- **Scenario:** Attempt login with admin credentials using authService.login()
- **Expected Result:** Login should return a valid AuthToken with non-null token field

### testAdminUserPasswordUpdate
- **Setup:** Run startup to create admin user, verify initial login works
- **Parameters:**
  - initialPassword = "admin"
  - newPassword = "newPassword"
- **Scenario:** Update admin password via reflection, run startup again, test both passwords
- **Expected Result:** New password should work for login, old password should fail with "Invalid username or password"

### testConfigurationValues
- **Setup:** None required
- **Parameters:** None
- **Scenario:** Read default admin config values using getters
- **Expected Result:** Default username should be "admin", default password should be "admin"

---

## DatabaseConnectionTest

### testDatabaseConnection
- **Setup:** Spring context with MySQL available, AcceptanceTestSpringBase
- **Parameters:**
  - Database URL = "jdbc:mysql://localhost:3306/?useSSL=false"
  - Username = "bgu"
  - Password = "changeme"
- **Scenario:** Test database connection and repository injection
- **Expected Result:** All repositories (userRepository, storeRepository, listingRepository, purchaseRepository) should be injected successfully

### testUserServiceIntegration
- **Setup:** Database connection established
- **Parameters:**
  - testUsername = "testuser"
  - testPassword = "password123"
- **Scenario:** Register user and attempt login
- **Expected Result:** Registration should not throw exception, login should return valid token

---

## FounderTests

### founder_appoints_owner_successfully
- **Setup:** Register users (founderID="1", ownerA="2", ownerB="3", ownerC="4"), create store
- **Parameters:**
  - founderID = "1"
  - ownerA = "2"
  - storeId = generated store ID
  - storeName = "TestStore"
- **Scenario:** Founder appoints ownerA as owner using addAdditionalStoreOwner()
- **Expected Result:** isOwner(storeId, ownerA) should return true

### founder_appoints_manager_successfully
- **Setup:** Same as above
- **Parameters:**
  - founderID = "1"
  - ownerB = "3"
  - storeId = generated store ID
- **Scenario:** Founder appoints ownerB as manager using addNewManager()
- **Expected Result:** isManager(storeId, ownerB) should return true

### founder_cannot_be_removed_from_store
- **Setup:** Create founder and store
- **Parameters:**
  - founderID = "1"
  - storeId = generated store ID
- **Scenario:** Try to remove founder using removeOwner(founderID, founderID, storeId)
- **Expected Result:** Should throw exception with message containing "founder"

### founder_views_appointment_chain
- **Setup:** Build hierarchy: founder → ownerA → ownerB → ownerC
- **Parameters:**
  - founderID = "1", ownerA = "2", ownerB = "3", ownerC = "4"
  - storeId = generated store ID
- **Scenario:** Remove ownerA and check cascade removal
- **Expected Result:** Should return list containing ownerB and ownerC as removed

### founder_attempts_to_transfer_ownership_blocked
- **Setup:** Create founder and store
- **Parameters:**
  - founderID = "1"
  - storeId = generated store ID
- **Scenario:** Try addAdditionalStoreOwner(founderID, founderID, storeId)
- **Expected Result:** Should throw exception mentioning "founder" or "already an owner"

### founder_closes_store_success
- **Setup:** Create active store
- **Parameters:**
  - storeId = generated store ID
  - founderID = "1"
  - storeName = "TestStore"
- **Scenario:** Founder closes store using closeStore()
- **Expected Result:** Store should become inactive, method should return storeId

### founder_closes_store_fail_user_is_not_founder
- **Setup:** Create store with founder
- **Parameters:**
  - storeId = generated store ID
  - ownerA = "2" (non-founder)
- **Scenario:** Non-founder tries to close store
- **Expected Result:** Should throw exception mentioning "founder", store remains active

### founder_closes_store_fail_store_is_already_inactive
- **Setup:** Create store and close it first
- **Parameters:**
  - storeId = generated store ID
  - founderID = "1"
- **Scenario:** Try to close already closed store
- **Expected Result:** Should throw exception mentioning "already closed"

---

## GuestTests

### guest_enters_system_initializes_cart
- **Setup:** Register guest user
- **Parameters:**
  - GUEST = "guest"
- **Scenario:** Get shopping cart for guest using getCart()
- **Expected Result:** Cart should exist and getAllStoreBags() should return empty list

### guest_exits_system_cart_deleted
- **Setup:** Register guest, verify cart exists
- **Parameters:**
  - GUEST = "guest"
- **Scenario:** Delete guest user using deleteUser()
- **Expected Result:** Subsequent getCart() should throw RuntimeException

### guest_registers_with_valid_details
- **Setup:** None
- **Parameters:**
  - username = "new_subscriber"
  - password = "1234"
- **Scenario:** Register new user with valid credentials
- **Expected Result:** User should exist in repository after registration

### guest_login_with_valid_credentials
- **Setup:** Register user first
- **Parameters:**
  - MANAGER1 = "manager1"
  - MANAGER_PASSWORD = "1234"
- **Scenario:** Login with correct credentials
- **Expected Result:** Should return valid AuthToken with non-null token

### guest_login_with_wrong_password
- **Setup:** Register user first
- **Parameters:**
  - MANAGER1 = "manager1"
  - wrongPassword = "wrongPassword"
- **Scenario:** Login with incorrect password
- **Expected Result:** Should fail/throw exception

### guest_gets_stores_and_product_info_when_available
- **Setup:** Create multiple stores with products
- **Parameters:**
  - storeId1 = "MyStore", storeId2 = "AnotherStore"
  - Product1: id="p1", name="Blue Notebook", category="Stationery", price=15.0, quantity=10
  - Product2: id="p2", name="Red Pencil", category="Stationery", price=2.0, quantity=20
- **Scenario:** Get store and product information
- **Expected Result:** Should return both stores with their respective products

### guest_gets_store_info_when_no_stores_active
- **Setup:** Create store and close it
- **Parameters:**
  - storeId = generated ID
  - MANAGER1 = "manager1"
- **Scenario:** Get store information after closing store
- **Expected Result:** All returned stores should have isActive = false

### guest_gets_store_info_when_store_has_no_products
- **Setup:** Create empty store
- **Parameters:**
  - storeId = generated ID with no products
- **Scenario:** Get store information
- **Expected Result:** Store should exist with empty listings list

### guest_can_search_products_across_all_stores_by_keyword
- **Setup:** Create 3 stores with different products
- **Parameters:**
  - Store1: "Notebook Classic", Store2: "Notebook Deluxe", Store3: "Yellow Pencil"
  - keyword1 = "note", keyword2 = "pencil"
- **Scenario:** Search products by keyword using searchByProductName()
- **Expected Result:** "note" should return 2 results, "pencil" should return 1 result

### guest_search_returns_empty_when_no_matches
- **Setup:** Create stores with unrelated products
- **Parameters:**
  - Products: "Notebook", "Pencil Case"
  - keyword = "unicorn-rainbow-sandwich"
- **Scenario:** Search with non-matching keyword
- **Expected Result:** Should return empty list

### guest_searches_in_specific_store_exists
- **Setup:** Add products to specific store
- **Parameters:**
  - storeId = generated ID
  - Products: "Notebook Classic", "Notebook Pro", "Marker Red"
  - keyword = "note"
- **Scenario:** Search within specific store using searchInStoreByName()
- **Expected Result:** Should return 2 notebook products from that store only

### guest_searches_in_specific_store_doesnt_exist
- **Setup:** None
- **Parameters:**
  - nonExistingStoreId = "9999"
  - keyword = "notebook"
- **Scenario:** Search in non-existent store
- **Expected Result:** Should return empty list without throwing exception

### guest_searches_in_specific_store_no_matching_products
- **Setup:** Add unrelated products to store
- **Parameters:**
  - storeId = generated ID
  - Products: "Stapler", "Paper Clips"
  - keyword = "notebook"
- **Scenario:** Search for non-existent product in store
- **Expected Result:** Should return empty list

### guest_adds_product_to_cart_valid
- **Setup:** Create product and guest token
- **Parameters:**
  - GUEST = "guest"
  - Product: id="123", name="Gvina", category="food", price=5.0, quantity=10
  - addQuantity = 2
- **Scenario:** Add product to cart, then remove same quantity
- **Expected Result:** Product added correctly, store bag removed when empty

### guest_purchases_cart_successfully
- **Setup:** Create product, mock payment/shipment success
- **Parameters:**
  - Product: name="Notebook", category="writing", price=25.0, initialQuantity=5
  - purchaseQuantity = 1
  - SHIPPING_ADDRESS = "123 Guest Street"
  - CONTACT_INFO = "guest@example.com"
- **Scenario:** Add product to cart and execute purchase
- **Expected Result:** Purchase succeeds, cart emptied, stock reduced to 4

### guest_purchasing_cart_fails_due_to_stock
- **Setup:** Create product with limited stock
- **Parameters:**
  - Product: initialQuantity = 5
  - cartQuantity = 6 (more than stock)
- **Scenario:** Try to purchase more than available
- **Expected Result:** Should fail with message containing "stock"

### guest_purchasing_cart_fails_due_to_payment_restore_stock
- **Setup:** Create product, mock payment failure
- **Parameters:**
  - Product: initialQuantity = 5
  - cartQuantity = 1
  - paymentService.processPayment() returns fail("Simulated payment failure")
- **Scenario:** Try to purchase with payment failure
- **Expected Result:** Should fail with "payment" error, stock restored to 5, cart intact

### guest_cart_applies_percentage_discount_correctly
- **Setup:** Create 10% percentage discount on "food" category
- **Parameters:**
  - discountType = "PERCENTAGE", scope = "CATEGORY", scopeId = "food", value = 10.0
  - Product: name="Gvina", category="food", price=5.0, quantity=2
  - expectedOriginalPrice = 10.0, expectedDiscount = 1.0, expectedFinalPrice = 9.0
- **Scenario:** Purchase with discount applied
- **Expected Result:** Final price should be 9.0 (10% discount applied)

### guest_cart_applies_coupon_discount_correctly
- **Setup:** Create coupon discount with 5 unit discount
- **Parameters:**
  - discountType = "COUPON", value = 5.0, couponCode = "SAVE5"
  - Product: name="Gvina", price=5.0, quantity=2
  - expectedOriginalPrice = 10.0, expectedFinalPrice = 5.0
- **Scenario:** Purchase with coupon applied
- **Expected Result:** Final price should reflect 5 unit discount

### guest_cart_applies_fixed_product_discount_correctly
- **Setup:** Create fixed product discount
- **Parameters:**
  - discountType = "FIXED", scope = "PRODUCT", productId = "fixed-test-product", value = 4.0
  - Product: price = 12.0, quantity = 3
  - expectedOriginalPrice = 36.0, expectedDiscount = 12.0, expectedFinalPrice = 24.0
- **Scenario:** Calculate discounted price using getStoreBagDiscountPrice()
- **Expected Result:** Should apply $4 discount per item ($12 total discount)

### guest_cart_applies_fixed_store_discount_correctly
- **Setup:** Create store-wide fixed discount
- **Parameters:**
  - discountType = "FIXED", scope = "STORE", value = 8.0
  - Product1: price = 15.0, quantity = 1; Product2: price = 10.0, quantity = 2
  - expectedOriginalPrice = 35.0, expectedDiscount = 8.0, expectedFinalPrice = 27.0
- **Scenario:** Calculate discounted price for multiple items
- **Expected Result:** Should apply $8 discount once to total

### guest_cart_fixed_discount_prevents_negative_price
- **Setup:** Create large discount on cheap product
- **Parameters:**
  - discountType = "FIXED", scope = "PRODUCT", value = 10.0
  - Product: price = 3.0, quantity = 1
- **Scenario:** Apply discount that exceeds product price
- **Expected Result:** Final price should be $0, not negative

### guest_cart_multiple_fixed_discounts_composite
- **Setup:** Create multiple fixed discounts with SUM combination
- **Parameters:**
  - productDiscount: type="FIXED", scope="PRODUCT", value=5.0
  - categoryDiscount: type="FIXED", scope="CATEGORY", scopeId="electronics", value=3.0
  - combinationType = "SUM"
  - Product: category="electronics", price=20.0, quantity=2
- **Scenario:** Apply composite discount
- **Expected Result:** Should apply both discounts: ($5 + $3) × 2 = $16 total discount

---

## StoreManagerTests

### manager_addNewProductToStore_positive
- **Setup:** Appoint manager with EDIT_PRODUCTS permission
- **Parameters:**
  - FOUNDER = "100", MANAGER = "300"
  - Permission = Store.Permission.EDIT_PRODUCTS.getCode()
  - Product: id="1", name="Monitor", category="Electronics", description="HD Monitor", quantity=10, price=699.0, type="REGULAR"
- **Scenario:** Manager adds new product using addNewListing()
- **Expected Result:** Should return non-null listing ID

### manager_addNewProductToStore_negative_noPermission
- **Setup:** Appoint manager without permissions
- **Parameters:**
  - MANAGER = "300" (no EDIT_PRODUCTS permission)
  - Product: id="2", name="Webcam", category="Electronics", price=199.0
- **Scenario:** Manager tries to add product without permission
- **Expected Result:** Should fail with message containing "permission"

### manager_addNewProductToStore_negative_InvalidPrice
- **Setup:** Appoint manager with permission
- **Parameters:**
  - MANAGER = "300" with EDIT_PRODUCTS permission
  - Product: id="3", name="Speaker", price=-99.0 (invalid)
- **Scenario:** Manager tries to add product with negative price
- **Expected Result:** Should fail with "the price of a products needs to be possitive"

### manager_removeProductFromStore_positive
- **Setup:** Appoint manager with permission, add product first
- **Parameters:**
  - MANAGER = "300" with EDIT_PRODUCTS permission
  - Product: id="rm-1", name="Headphones", price=299.0, quantity=7
- **Scenario:** Manager removes existing product using removeListing()
- **Expected Result:** Product should be removed, subsequent getListing() should fail with "not found"

### manager_removeProductFromStore_negative_noPermission
- **Setup:** Appoint manager without permission
- **Parameters:**
  - MANAGER = "300" (no EDIT_PRODUCTS permission)
  - Product: id="rm-2", name="Microphone", price=179.0
- **Scenario:** Manager tries to remove product without permission
- **Expected Result:** Should fail with message containing "permission"

### manager_removeProductFromStore_alternate_ProductNotFound
- **Setup:** Appoint manager with permission
- **Parameters:**
  - MANAGER = "300" with EDIT_PRODUCTS permission
  - listingId = "non-existent-id-xyz"
- **Scenario:** Manager tries to remove non-existent product
- **Expected Result:** Should fail with "error removing listing"

### manager_editProductFromStore_positive
- **Setup:** Appoint manager with permission, add product first
- **Parameters:**
  - MANAGER = "300" with EDIT_PRODUCTS permission
  - Product: id="edit‑1", name="Camera", originalPrice=1500.0, newPrice=1350.0
- **Scenario:** Manager edits product price using editListingPrice()
- **Expected Result:** Should return true, product price should be updated to 1350.0

### manager_editProductFromStore_negative_NoPermission
- **Setup:** Appoint manager without permission
- **Parameters:**
  - MANAGER = "300" (no EDIT_PRODUCTS permission)
  - Product: id="edit‑2", name="Tripod", newPrice=200.0
- **Scenario:** Manager tries to edit price without permission
- **Expected Result:** Should fail with message containing "permission"

### manager_editProductFromStore_alternate_ProductNotFound
- **Setup:** Appoint manager with permission
- **Parameters:**
  - MANAGER = "300" with EDIT_PRODUCTS permission
  - fakeListingId = "nonexistent-999", newPrice = 100.0
- **Scenario:** Manager tries to edit non-existent product
- **Expected Result:** Should fail with message containing "not found"

### manager_editStorePurchasePolicy_positive
- **Setup:** Appoint manager with EDIT_POLICIES permission
- **Parameters:**
  - MANAGER = "300" with Store.Permission.EDIT_POLICIES.getCode()
  - policy = new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", 2)
- **Scenario:** Manager adds purchase policy using addPurchasePolicy()
- **Expected Result:** Should return true

### manager_editStorePurchasePolicy_negative_NoPermission
- **Setup:** Appoint manager without permission
- **Parameters:**
  - MANAGER = "300" (no EDIT_POLICIES permission)
  - policy = new PolicyDTO.AddPurchasePolicyRequest("MINPRICE", 300)
- **Scenario:** Manager tries to add policy without permission
- **Expected Result:** Should fail with message containing "permission"

### manager_editStorePurchasePolicy_alternate_InActiveStore
- **Setup:** Appoint manager with permission, close store
- **Parameters:**
  - MANAGER = "300" with EDIT_POLICIES permission
  - Store closed using closeStore()
  - policy = new PolicyDTO.AddPurchasePolicyRequest("MAXITEMS", 15)
- **Scenario:** Manager tries to add policy to closed store
- **Expected Result:** Should fail with message containing "closed"

### manager_editStoreDiscountPolicy_positive
- **Setup:** Appoint manager with permission, add product first
- **Parameters:**
  - MANAGER = "300" with EDIT_POLICIES permission
  - Product: id="p1", name="Speaker", category="Audio", price=300
  - discount = new PolicyDTO.AddDiscountRequest("PERCENTAGE", "PRODUCT", "p1", 0.25, null, null, List.of(), "SUM")
- **Scenario:** Manager adds discount policy using addDiscount()
- **Expected Result:** Should return true

### manager_editStoreDiscountPolicy_negative_NoPermission
- **Setup:** Appoint manager without permission
- **Parameters:**
  - MANAGER = "300" (no EDIT_POLICIES permission)
  - discount = new PolicyDTO.AddDiscountRequest("PERCENTAGE", "PRODUCT", "p1", 0.1, ...)
- **Scenario:** Manager tries to add discount without permission
- **Expected Result:** Should fail with message containing "permission"

### manager_editStoreDiscountPolicy_alternate_InValidObjectToCreatePolicyTo
- **Setup:** Appoint manager with permission
- **Parameters:**
  - MANAGER = "300" with EDIT_POLICIES permission
  - invalidDiscount = new PolicyDTO.AddDiscountRequest(null, "PRODUCT", "p1", -0.4, ...)
- **Scenario:** Manager tries to add invalid discount (null type, negative value)
- **Expected Result:** Should fail with message containing "null"

---

## NotificationAcceptanceTests

### test_AddNewOwner_SendsNotificationToNewOwner
- **Setup:** Register users, create store
- **Parameters:**
  - storeFounder = "storeFounder123"
  - newOwner = "newOwner456"
  - storeName = "NotificationTestStore"
- **Scenario:** Founder adds new owner using addAdditionalStoreOwner()
- **Expected Result:** New owner receives notification containing "appointed as an owner" and store name

### test_AddNewOwner_VerifyNotificationContent
- **Setup:** Same as above
- **Parameters:** Same as above
- **Scenario:** Add new owner and check notification content
- **Expected Result:** Notification message should be "You have been appointed as an owner of store NotificationTestStore."

### test_AddMultipleOwners_EachReceivesNotification
- **Setup:** Register multiple users, create store
- **Parameters:**
  - storeFounder = "storeFounder123"
  - newOwner = "newOwner456"
  - anotherOwner = "anotherOwner999"
- **Scenario:** Add two new owners sequentially
- **Expected Result:** Each owner receives their own appointment notification

### test_AddNewManager_SendsNotificationToNewManager
- **Setup:** Register users, create store
- **Parameters:**
  - storeFounder = "storeFounder123"
  - newManager = "newManager789"
  - storeName = "NotificationTestStore"
- **Scenario:** Founder adds new manager using addNewManager()
- **Expected Result:** New manager receives notification containing "appointed as a manager"

### test_AddNewManager_VerifyNotificationContent
- **Setup:** Same as above
- **Parameters:** Same as above
- **Scenario:** Add new manager and check notification content
- **Expected Result:** Notification message should be "You have been appointed as a manager of store NotificationTestStore."

### test_OwnerAddsManager_ManagerReceivesNotification
- **Setup:** Founder, owner, manager hierarchy
- **Parameters:**
  - storeFounder = "storeFounder123"
  - newOwner = "newOwner456" (becomes owner first)
  - newManager = "newManager789"
- **Scenario:** Owner (not founder) adds manager
- **Expected Result:** Manager receives appointment notification

### test_RemoveOwner_SendsNotificationToRemovedOwner
- **Setup:** Add owner first, then remove
- **Parameters:**
  - storeFounder = "storeFounder123"
  - newOwner = "newOwner456"
  - storeId = generated store ID
- **Scenario:** Remove existing owner using removeOwner()
- **Expected Result:** Removed owner receives notification containing "removed as an owner"

### test_RemoveOwner_VerifyNotificationContent
- **Setup:** Same as above
- **Parameters:** Same as above
- **Scenario:** Remove owner and check notification content
- **Expected Result:** Notification message should be "You have been removed as an owner from store NotificationTestStore."

### test_RemoveOwnerWithAssignees_AllReceiveNotifications
- **Setup:** Create chain Founder->Owner->Manager, then remove owner
- **Parameters:**
  - Chain: storeFounder -> newOwner -> newManager
- **Scenario:** Remove owner who has assigned managers
- **Expected Result:** Both owner and manager receive removal notifications

### test_RemoveManager_SendsNotificationToRemovedManager
- **Setup:** Add manager first, then remove
- **Parameters:**
  - storeFounder = "storeFounder123"
  - newManager = "newManager789"
- **Scenario:** Remove existing manager using removeManager()
- **Expected Result:** Removed manager receives notification containing "removed as a manager"

### test_RemoveManager_VerifyNotificationContent
- **Setup:** Same as above
- **Parameters:** Same as above
- **Scenario:** Remove manager and check notification content
- **Expected Result:** Notification message should be "You have been removed as a manager from store NotificationTestStore."

### test_BidApproval_SendsNotificationToBidder
- **Setup:** Create bid product, submit bid
- **Parameters:**
  - bidProductId = "bid-product-123"
  - bidAmount = 180.0
  - productPrice = 200.0
  - shipping = "123 Main St", email = "bidder@email.com"
- **Scenario:** Store founder approves bid using approveBid()
- **Expected Result:** Bidder receives notification containing "bid approved" or "bid has been approved"

### test_BidRejection_SendsNotificationToBidder
- **Setup:** Create bid product, submit bid
- **Parameters:**
  - bidProductId = "bid-product-456"
  - bidAmount = 100.0
  - productPrice = 150.0
- **Scenario:** Store founder rejects bid using rejectBid()
- **Expected Result:** Bidder receives notification containing "bid has been rejected"

### test_CounterOfferProposed_SendsNotificationToBidder
- **Setup:** Create bid product, submit bid
- **Parameters:**
  - bidProductId = "bid-product-789"
  - originalBid = 120.0
  - counterOffer = 150.0
  - productPrice = 180.0
- **Scenario:** Store founder proposes counter offer using proposeCounterBid()
- **Expected Result:** Bidder receives notification containing "Counter offer" (if implemented)

---

## SubscriberTests

### register_a_user_successes_and_cart_was_initial
- **Setup:** None
- **Parameters:**
  - username = "user3"
  - password = "password3"
- **Scenario:** Register new user and check cart initialization
- **Expected Result:** User registration succeeds, shopping cart exists and is empty

### register_a_user_failed_already_exists
- **Setup:** Register user first
- **Parameters:**
  - username = "user3"
  - firstPassword = "password3"
  - secondPassword = "password4"
- **Scenario:** Try to register same user twice
- **Expected Result:** Second registration fails with message containing "already exists"

### get_information_about_stores_and_products_successes
- **Setup:** Create store and add product
- **Parameters:**
  - user = "user1"
  - storeName = "store1"
  - Product: id="p1", name="ipad", category="electronics", description="apple", quantity=10, price=1000.0, type="REGULAR"
- **Scenario:** Get store information using getStore()
- **Expected Result:** Store info should be non-null, name should match, product should be searchable

### get_information_about_stores_and_products_success_no_products
- **Setup:** Create empty store
- **Parameters:**
  - user = "user1"
  - storeName = "store1"
- **Scenario:** Get store information for empty store
- **Expected Result:** Store should exist with matching name, product search should return 0 results

### search_for_product_across_all_stores_successes
- **Setup:** Create two stores with different products
- **Parameters:**
  - Store1: user="user1", name="store1", Product: id="p1", name="ipad", price=1000.0
  - Store2: user="user2", name="store2", Product: id="p2", name="iphone", price=1000
- **Scenario:** Search for products using searchByProductName()
- **Expected Result:** Both "ipad" and "iphone" searches should return non-null results

### search_for_product_across_all_stores_no_product
- **Setup:** Create empty stores
- **Parameters:**
  - Store1: user="user1", name="store1" (empty)
  - Store2: user="user2", name="store2" (empty)
  - searchKeyword = "ipad"
- **Scenario:** Search for non-existent product
- **Expected Result:** Search should return empty list

### search_for_product_in_store_successes
- **Setup:** Create store with product
- **Parameters:**
  - Store: user="user1", name="store1"
  - Product: id="p1", name="ipad", category="electronics", price=1000.0
  - searchKeyword = "ipad"
- **Scenario:** Search within specific store using searchInStoreByName()
- **Expected Result:** Should return 1 product

### search_for_product_in_store_no_product
- **Setup:** Create empty store
- **Parameters:**
  - Store: user="user1", name="store1" (empty)
  - searchKeyword = "ipad"
- **Scenario:** Search in empty store
- **Expected Result:** Should return empty list

### add_proudct_to_storebag_successes
- **Setup:** Create store with product, generate user token
- **Parameters:**
  - Store: user="user1", name="store1"
  - Product: id="p1", name="ipad", price=1000.0, quantity=10
  - addQuantity = 2
- **Scenario:** Add product to cart using addProductToCart()
- **Expected Result:** Cart should contain 2 units of the product

### view_and_edit_shopping_cart_successes
- **Setup:** Same as add_proudct_to_storebag_successes
- **Parameters:** Same as above
- **Scenario:** Add product to cart and verify quantity
- **Expected Result:** Cart should show correct quantity (2 units)

### purches_cart_successes
- **Setup:** Create product, mock payment/shipment success
- **Parameters:**
  - Product: id="p1", name="ipad", price=1000.0, initialQuantity=10, purchaseQuantity=2
  - SHIPPING_ADDRESS = "Subscriber Street"
  - CONTACT_INFO = "Suscriber@example.com"
  - Payment/shipment mocked to return success
- **Scenario:** Add to cart and execute purchase
- **Expected Result:** Purchase succeeds, cart emptied, stock reduced to 8

### purches_cart_no_product
- **Setup:** Create empty store
- **Parameters:**
  - Store: user="user1", name="store1" (empty)
  - shippingAddress = "123 Guest Street"
  - contactInfo = "guest@example.com"
- **Scenario:** Try to purchase with empty cart
- **Expected Result:** Should fail with message containing "fail"

### purches_cart_no_enough_money_no_real_credit_card
- **Setup:** Create product, mock payment failure
- **Parameters:**
  - Product: price=1000.0, quantity=2 in cart
  - paymentService mocked to return fail("Insufficient funds")
- **Scenario:** Try to purchase with payment failure
- **Expected Result:** Should fail with message containing "failed", cart quantity remains 2

### exit_from_the_system
- **Setup:** Register user
- **Parameters:**
  - username = "user3"
  - password = "password3"
- **Scenario:** Generate token for user using generateToken()
- **Expected Result:** Token should be non-null

### open_a_store
- **Setup:** Use registered user
- **Parameters:**
  - user = "user1"
  - storeName = "store1"
- **Scenario:** Create store using createStore()
- **Expected Result:** Store ID should be non-null, store should be active

### open_a_store_fail
- **Setup:** Create store first
- **Parameters:**
  - user = "user1"
  - storeName = "store1" (already exists)
- **Scenario:** Try to create store with same name
- **Expected Result:** Should fail with message containing "already exists"

### submit_bid_for_product_successes
- **Setup:** Create bid product, configure mocks
- **Parameters:**
  - Product: id="p1", name="premium-phone", category="electronics", price=1500.0, quantity=1
  - bidAmount = 1200.0
  - SHIPPING_ADDRESS = "Subscriber Street"
  - CONTACT_INFO = "Suscriber@example.com"
- **Scenario:** Submit bid using submitBid()
- **Expected Result:** Bid status should be "Pending Approval"

### submit_bid_for_product_fail
- **Setup:** Create bid product
- **Parameters:**
  - Product: same as above
  - invalidBidAmount = -500.0 (negative)
- **Scenario:** Submit invalid bid with negative amount
- **Expected Result:** Should fail with message containing "positive value"

### purche_proudct_after_auction_succsesses
- **Setup:** Create auction, register bidder
- **Parameters:**
  - Auction: productId="collectible-item-" + timestamp, startingPrice=1500, endTime=currentTime+60000
  - Bidder: user="user2", offerAmount=1800.0
- **Scenario:** Open auction, submit offer, check status
- **Expected Result:** Auction status should show currentMaxOffer=1800.0, startingPrice=1500.0, timeLeftMillis>0

---

## StoreOwnerTests

### owner_addNewProductToStore_positive
- **Setup:** Use founder to add product
- **Parameters:**
  - FOUNDER = "100"
  - Product: id="1", name="Tablet", category="Electronic", description="Android tablet", quantity=5, price=899, type="REGULAR"
- **Scenario:** Add product using addNewListing()
- **Expected Result:** Should return non-null listing ID

### owner_addNewProductToStore_negative_InvalidPrice
- **Setup:** Use founder to add product with invalid price
- **Parameters:**
  - FOUNDER = "100"
  - Product: id="1", name="Tablet", price=-78 (negative)
- **Scenario:** Try to add product with negative price
- **Expected Result:** Should fail with "the price of a products needs to be possitive"

### owner_removeProductFromStore_positive
- **Setup:** Add product first, then remove
- **Parameters:**
  - FOUNDER = "100"
  - Product: id="p‑2", name="Mouse", category="Electronic", description="Wireless", quantity=4, price=129.9
- **Scenario:** Remove product using removeListing()
- **Expected Result:** Product should be removed, subsequent getListing() should fail with "not found"

### owner_removeProductFromStore_negative_ProductNotFound
- **Setup:** Try to remove non-existent product
- **Parameters:**
  - FOUNDER = "100"
  - listingId = "omer" (non-existent)
- **Scenario:** Try to remove non-existent product
- **Expected Result:** Should fail with "error removing listing"

### owner_removeProductFromStore_alternate_InactiveStore
- **Setup:** Add product, close store, then try to remove
- **Parameters:**
  - FOUNDER = "100"
  - Product: id="p‑2", name="Mouse", price=129.9
  - Store closed using closeStore()
- **Scenario:** Try to remove product from closed store
- **Expected Result:** Should fail with message containing "error", product should still exist

### owner_appointAdditionalStoreOwner_positive
- **Setup:** Use founder to appoint owner
- **Parameters:**
  - FOUNDER = "100"
  - OWNER_A = "200"
  - storeId = generated store ID
- **Scenario:** Appoint additional owner using addAdditionalStoreOwner()
- **Expected Result:** isOwner(storeId, OWNER_A) should return true

### owner_appointAdditionalStoreOwner_negative_AlreadyAnOwner
- **Setup:** Appoint owner twice
- **Parameters:**
  - FOUNDER = "100"
  - OWNER_A = "200"
- **Scenario:** Try to appoint same user as owner twice
- **Expected Result:** Should fail with message containing "already an owner"

### owner_removeStoreOwner_positive
- **Setup:** Appoint owner first, then remove
- **Parameters:**
  - FOUNDER = "100"
  - OWNER_A = "200"
- **Scenario:** Remove owner using removeOwner()
- **Expected Result:** isOwner(storeId, OWNER_A) should return false

### owner_removeStoreOwner_negative_TryingToRemoveOwnerNotApointedByHim
- **Setup:** Create hierarchy where MANAGER tries to remove OWNER_A not appointed by them
- **Parameters:**
  - FOUNDER = "100" appoints both OWNER_A = "200" and MANAGER = "300"
  - MANAGER tries to remove OWNER_A
- **Scenario:** Try to remove owner not appointed by the requester
- **Expected Result:** Should fail with message containing "didn't assign"

### owner_removeStoreOwner_alternate_InactiveStore
- **Setup:** Appoint owner, close store, then try to remove
- **Parameters:**
  - FOUNDER = "100", OWNER_A = "200"
  - Store closed using closeStore()
- **Scenario:** Try to remove owner from closed store
- **Expected Result:** Should fail with message containing "is closed for now"

### owner_appointStoreManager_positive
- **Setup:** Appoint manager
- **Parameters:**
  - FOUNDER = "100"
  - MANAGER = "300"
- **Scenario:** Appoint manager using addNewManager()
- **Expected Result:** isManager(storeId, MANAGER) should return true

### owner_appointStoreManager_negative_alreadyManager
- **Setup:** Appoint manager twice
- **Parameters:**
  - FOUNDER = "100"
  - MANAGER = "300"
- **Scenario:** Try to appoint same user as manager twice
- **Expected Result:** Should fail with message containing "already a manager"

### owner_editStoreManagerPermissions_positive
- **Setup:** Appoint manager and add permission
- **Parameters:**
  - FOUNDER = "100", MANAGER = "300"
  - permission = Store.Permission.EDIT_PRODUCTS.getCode()
- **Scenario:** Add permission using addPermissionToManager()
- **Expected Result:** getManagersPermissions() should contain EDIT_PRODUCTS permission

### owner_editStoreManagerPermissions_negative_NotManager
- **Setup:** Try to add permission to non-manager
- **Parameters:**
  - MANAGER = "300" (not appointed as manager)
  - permission = Store.Permission.EDIT_PRODUCTS.getCode()
- **Scenario:** Try to add permission to non-manager
- **Expected Result:** Should fail with message containing "not a manager"

### owner_editStoreManagerPermissions_alternate_NotTheAppointerOfManager
- **Setup:** FOUNDER appoints MANAGER, OWNER_A tries to edit permissions
- **Parameters:**
  - FOUNDER = "100" appoints MANAGER = "300"
  - OWNER_A = "200" tries to add permission
- **Scenario:** Non-appointer tries to edit manager permissions
- **Expected Result:** Should fail with message containing "cant add permission"

### owner_editProductFromStore_positive
- **Setup:** Add product first, then edit
- **Parameters:**
  - FOUNDER = "100"
  - Product: id="p‑2", name="Mouse", originalPrice=129.9, newPrice=99.9
- **Scenario:** Edit product price using editListingPrice()
- **Expected Result:** Should return true, product price should be updated to 99.9

### owner_editProductFromStore_negative_InValidPrice
- **Setup:** Try to edit with invalid price
- **Parameters:**
  - Product: id="p‑3", name="Keyboard", originalPrice=229.0, newPrice=-10.0
- **Scenario:** Try to edit with negative price
- **Expected Result:** Should fail with message containing "illegal price"

### owner_editProductFromStore_alternate_ProductNotFound
- **Setup:** Try to edit non-existent product
- **Parameters:**
  - fakeListingId = "non-existent-id"
  - newPrice = 150.0
- **Scenario:** Try to edit non-existent product
- **Expected Result:** Should fail with message containing "not found"

### owner_addStoreDiscountPolicy_positive
- **Setup:** Create store with product, add discount
- **Parameters:**
  - storeName = "DiscountStore"
  - Product: id="p1", name="TV", category="Electronics", price=2000.0
  - discount = new PolicyDTO.AddDiscountRequest("PERCENTAGE", "PRODUCT", "p1", 0.2, null, null, List.of(), "SUM")
- **Scenario:** Add discount policy using addDiscount()
- **Expected Result:** Should return true

### owner_addStoreDiscountPolicy_alternate_inactiveStore
- **Setup:** Create store, add product, close store, then try to add discount
- **Parameters:**
  - Product: id="p1", name="Fridge", category="Appliances", price=1200.0
  - Store closed using closeStore()
- **Scenario:** Try to add discount to closed store
- **Expected Result:** Should fail with message containing "closed"

### owner_editStoreDiscountPolicy_positive
- **Setup:** Add discount, remove it, add new one
- **Parameters:**
  - Product: id="p1", name="Tablet", price=1000.0
  - oldDiscount = PolicyDTO.AddDiscountRequest("PERCENTAGE", "PRODUCT", "p1", 0.1, ...)
  - newDiscount = PolicyDTO.AddDiscountRequest("PERCENTAGE", "PRODUCT", "p1", 0.2, ...)
- **Scenario:** Remove old discount and add new one
- **Expected Result:** Should return true for new discount addition

### owner_editStorePurchasePolicy_positive
- **Setup:** Add purchase policy, remove it, add new one
- **Parameters:**
  - oldPolicy = new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", 2)
  - newPolicy = new PolicyDTO.AddPurchasePolicyRequest("MINITEMS", 5)
- **Scenario:** Edit purchase policy by removing old and adding new
- **Expected Result:** Should return true for new policy addition

### owner_editStorePurchasePolicy_negative_InValidObjectToCreatePolicyTo
- **Setup:** Try to add invalid policy
- **Parameters:**
  - invalidPolicy = new PolicyDTO.AddPurchasePolicyRequest(null, -3)
- **Scenario:** Try to add policy with null type and negative value
- **Expected Result:** Should fail with message containing "type"

### owner_editStorePurchasePolicy_alternate_InActiveStore
- **Setup:** Close store, then try to add policy
- **Parameters:**
  - Store closed using closeStore()
  - policy = new PolicyDTO.AddPurchasePolicyRequest("MAXITEMS", 10)
- **Scenario:** Try to add policy to closed store
- **Expected Result:** Should fail with message containing "closed"

### acceptance_concurrentAddSameOwnerByMultipleAppointers
- **Setup:** Create multiple owners who try to appoint same user concurrently
- **Parameters:**
  - FOUNDER = "100", OWNER_A = "200", OWNER_B = "y"
  - toAssign = "X"
  - threads = 10
- **Scenario:** Multiple threads try to assign same user as owner
- **Expected Result:** Only one thread should succeed, successCount should be 1

### acceptance_concurrentRemoveSameOwner
- **Setup:** Multiple threads try to remove same owner
- **Parameters:**
  - FOUNDER = "100", OWNER_A = "200"
  - threads = 5
- **Scenario:** Multiple threads try to remove same owner
- **Expected Result:** Only one thread should succeed, owner should no longer exist

### acceptance_transitiveAssignWhileRemovingRoot
- **Setup:** Test concurrent assign and remove operations
- **Parameters:**
  - FOUNDER = "100", OWNER_A = "200"
  - toAssign = "newOwner"
  - Two threads: one assigns, one removes
- **Scenario:** One thread assigns new owner while another removes root owner
- **Expected Result:** If root owner removed, transitive assignment should not persist

### acceptance_concurrentAddSameManagerByMultipleOwners
- **Setup:** Multiple owners try to appoint same manager concurrently
- **Parameters:**
  - OWNER_A = "200", OWNER_B = "ownerB"
  - MANAGER_ID = "newManager"
  - threads = 6
- **Scenario:** Multiple threads try to assign same user as manager
- **Expected Result:** Only one thread should succeed, successCount should be 1

### acceptance_concurrentRemoveSameManager
- **Setup:** Multiple threads try to remove same manager
- **Parameters:**
  - OWNER_A = "200", MANAGER_ID = "managerY"
  - threads = 5
- **Scenario:** Multiple threads try to remove same manager
- **Expected Result:** Only one thread should succeed, manager should no longer exist

### manager_concurrentAddSamePermission_onlyOneEffective
- **Setup:** Multiple threads add same permission concurrently
- **Parameters:**
  - OWNER_A = "200", MANAGER = "300"
  - permission = Store.Permission.EDIT_PRODUCTS.getCode()
  - threads = 10
- **Scenario:** Multiple threads try to add same permission
- **Expected Result:** Permissions set should have size 2, contain EDIT_PRODUCTS

### manager_concurrentRemoveSamePermission_onlyOneSuccess
- **Setup:** Add permission, then multiple threads try to remove it
- **Parameters:**
  - MANAGER = "300", OWNER_A = "200"
  - permission = Store.Permission.EDIT_PRODUCTS.getCode()
  - threads = 10
- **Scenario:** Multiple threads try to remove same permission
- **Expected Result:** Only one thread should succeed, permission should be removed

### manager_concurrentAddAndRemovePermission_finalStateConsistent
- **Setup:** One thread adds permission while another removes it
- **Parameters:**
  - MANAGER = "300", OWNER_A = "200"
  - permission = Store.Permission.EDIT_PRODUCTS.getCode()
  - Two threads: one adds, one removes
- **Scenario:** Concurrent add and remove permission operations
- **Expected Result:** Final permissions set should have size <= 2

---

## UserServiceTests

### testAddProductToCart_ShouldPersistCart
- **Setup:** Create user, login, create multiple stores with listings
- **Parameters:**
  - username = "service_tester"
  - password = "password123"
  - Electronics Store:
    - storeName = "electronics_store"
    - Product1: id="laptop", name="Laptop", category="electronics", description="A powerful laptop", quantity=10, price=999.99, type="REGULAR"
    - Product2: id="mouse", name="Mouse", category="electronics", description="A wireless mouse", quantity=20, price=29.99, type="REGULAR"
  - Book Store:
    - storeName = "book_store"
    - Product: id="spring_in_action", name="Spring in Action", category="books", description="A book about Spring", quantity=15, price=49.99, type="REGULAR"
  - Office Store:
    - storeName = "office_supplies"
    - Product1: id="stapler", name="Stapler", category="office", description="A red stapler", quantity=50, price=9.99, type="REGULAR"
    - Product2: id="paper_clips", name="Paper Clips", category="office", description="A box of paper clips", quantity=1000, price=2.99, type="REGULAR"
  - Cart Contents:
    - Electronics: 1 laptop, 2 mice
    - Books: 1 Spring in Action book
    - Office: 1 stapler, 100 paper clips
- **Scenario:** Add products from multiple stores to cart using addProductToCart()
- **Expected Result:** Cart should contain 3 store bags with correct quantities for each product

### test_login_new_user
- **Setup:** None
- **Parameters:**
  - username = "testuser"
  - password = "password"
- **Scenario:** Register user and attempt login
- **Expected Result:** Both registration and login should complete without throwing exceptions

### testPurchaseFromCart_ShouldCompleteAndClearCart
- **Setup:** Create user, login, logout, login again, create store with products
- **Parameters:**
  - username = "buyer_user"
  - password = "securePw123"
  - storeName = "tech_store"
  - Products:
    - Laptop: productId="laptop_prod", name="Laptop", category="electronics", description="High-end laptop", quantity=10, price=1200.0, type="REGULAR"
    - Mouse: productId="mouse_prod", name="Mouse", category="electronics", description="Wireless mouse", quantity=20, price=25.0, type="REGULAR"
  - Cart Contents:
    - 1 laptop (quantity: 10→9 after purchase)
    - 2 mice (quantity: 20→18 after purchase)
  - Purchase Details:
    - shippingAddress = "123 Market St"
    - paymentDetails = "VISA **** 4242"
- **Scenario:** Add products to cart, execute purchase using executePurchase()
- **Expected Result:** Purchase record created, cart emptied, stock quantities updated correctly

---

## SuspensionServiceTests

### test_suspend_user_persists
- **Setup:** Register user
- **Parameters:**
  - username = "suspend_test_user"
  - password = "password123"
  - suspensionHours = 12
- **Scenario:** Suspend user using suspendUser(), check status and suspended users list
- **Expected Result:** isSuspended() should return true, getSuspendedUsers() should contain the user

### test_unsuspend_user_removes_from_db
- **Setup:** Register user, suspend them first
- **Parameters:**
  - username = "unsuspend_test_user"
  - password = "password123"
  - suspensionHours = 24
- **Scenario:** Suspend user, then unsuspend using unsuspendUser()
- **Expected Result:** isSuspended() should return false, getSuspendedUsers() should not contain the user

### test_suspend_multiple_users
- **Setup:** Register multiple users
- **Parameters:**
  - user1 = "multi_suspend_1", suspensionHours = 24
  - user2 = "multi_suspend_2", suspensionHours = 48
  - password = "password123" (for both)
- **Scenario:** Suspend both users with different suspension periods
- **Expected Result:** getSuspendedUsers() should contain both users

### test_unsuspend_one_of_multiple_users
- **Setup:** Register and suspend multiple users
- **Parameters:**
  - user1 = "unsuspend_one_1", suspensionHours = 24
  - user2 = "unsuspend_one_2", suspensionHours = 24
  - password = "password123" (for both)
- **Scenario:** Suspend both users, then unsuspend only user1
- **Expected Result:** getSuspendedUsers() should not contain user1 but should still contain user2

### test_suspend_permanent
- **Setup:** Register user
- **Parameters:**
  - username = "permanent_suspend_tester"
  - password = "password123"
  - suspensionHours = 0 (permanent suspension)
- **Scenario:** Suspend user permanently, then unsuspend
- **Expected Result:** isSuspended() should return true after permanent suspension, false after unsuspend

---

## PurchaseServiceTests

### testBidApprovedPurchaseIsSavedToDatabase
- **Setup:** Create store with BID-type product, submit and approve bid
- **Parameters:**
  - Store: name="BookStore", owner="approver"
  - Product: id="1", name="Book", category="Books", description="Interesting book", quantity=5, price=50.0, type="BID"
  - Bid: userId="user", bidAmount=40.0, shippingAddress="TLV", contactInfo="user@a.com"
  - approver = "approver"
- **Scenario:** Submit bid using submitBid(), approve using approveBid()
- **Expected Result:** Purchase record created for user, BidEntity persisted with approved=true, rejected=false

### testRejectedBidSavedToDatabase
- **Setup:** Create store with BID-type product, submit and reject bid
- **Parameters:**
  - Store: name="RejectStore", owner="approver"
  - Product: id="2", name="Item", category="Misc", description="Just an item", quantity=3, price=30.0, type="BID"
  - Bid: userId="user", bidAmount=25.0, shippingAddress="TLV", contactInfo="u@a.com"
  - approver = "approver"
- **Scenario:** Submit bid using submitBid(), reject using rejectBid()
- **Expected Result:** BidEntity persisted with rejected=true, approved=false

### testCounterOfferAcceptedSavedToDatabase
- **Setup:** Create store with BID-type product, submit bid, propose and accept counter offer
- **Parameters:**
  - Store: name="CounterStore", owner="approver"
  - Product: id="3", name="Painting", category="Art", description="Oil painting", quantity=2, price=200.0, type="BID"
  - Bid: userId="user", originalBid=150.0, counterOffer=180.0, shippingAddress="TLV", contactInfo="u@a.com"
  - approver = "approver"
- **Scenario:** Submit bid, propose counter offer using proposeCounterBid(), accept using acceptCounterOffer()
- **Expected Result:** Purchase record created, BidEntity updated with approved=true, rejected=false, price=180.0, isCounterOffered=false

