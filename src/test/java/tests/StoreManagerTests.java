package tests;

import org.junit.jupiter.api.Test;

public class StoreManagerTests {
    
@Test public void manager_addNewProductToStore_positive() {}
@Test public void manager_addNewProductToStore_negative_noPermission() {}
@Test public void manager_addNewProductToStore_negative_InvalidPrice() {}

@Test public void manager_removeProductFromStore_positive() {}
@Test public void manager_removeProductFromStore_negative_noPermission() {}
@Test public void manager_removeProductFromStore_alternate_ProductNotFound() {}

@Test public void manager_editProductFromStore_positive() {}
@Test public void manager_editProductFromStore_negative_NoPermission() {}
@Test public void manager_editProductFromStore_alternate_ProductNotFound() {}

@Test public void manager_editStorePurchasePolicy_positive() {}
@Test public void manager_editStorePurchasePolicy_negative_NoPermission() {}
@Test public void manager_editStorePurchasePolicy_alternate_InActiveStore() {}

@Test public void manager_editStoreDiscountPolicy_positive() {}
@Test public void manager_editStoreDiscountPolicy_negative_NoPermission() {}
@Test public void manager_editStoreDiscountPolicy_alternate_InValidObjectToCreatePolicyTo() {}

@Test public void manager_requestStoreRoles_positive() {}
@Test public void manager_requestStoreRoles_negative() {}
@Test public void manager_requestStoreRoles_alternate() {}

@Test public void manager_respondToUserMessages_positive() {}
@Test public void manager_respondToUserMessages_negative() {}
@Test public void manager_respondToUserMessages_alternate() {}

@Test public void manager_viewStorePurchaseHistory_positive() {}
@Test public void manager_viewStorePurchaseHistory_negative() {}
@Test public void manager_viewStorePurchaseHistory_alternate() {}





}
