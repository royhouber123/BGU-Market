package support;

import market.domain.product.Product;
import java.util.List;

public interface Bridge {
    //add for guest
    String register(String username, String password, String email, String address);
    String login(String username, String password);
    void enterAsGuest();
    void exitAsGuest();
    String getStoreAndProductInfo();

    List<Product> searchProductsGlobally(String keyword);
    List<Product> searchProductsInStore(String storeName, String keyword);

    void addProductToCart(String storeName, String productName, int quantity);
    void updateCartItem(String storeName, String productName, int newQuantity);

    String purchaseCart(String deliveryAddress, String creditCard, String expDate, String cvv);



/// add for subscriber tests
    String viewPurchaseHistory();
    String rateProduct(String productName, int rating);
    String sendMessageToStore(String storeName, String message);






/// add for storemanager:
    String openStore(String storeName, String storeType);
    String addProductToStore(String storeName, String productName, String category, double price, int quantity);
    String editProduct(String storeName, String productName, String fieldName, String newValue);
    String removeProduct(String storeName, String productName);
    String viewStorePurchaseHistory(String storeName);

    String editDiscountPolicy(String storeName, String productName, String discountDetails, String appliedTo);
    String editPurchasePolicy(String storeName, String productName, String policyDetails);

    String appointOwner(String storeName, String newOwnerUsername);
    String removeOwner(String storeName, String targetOwnerUsername);

    String respondToMessage(String storeName, int messageId, String responseText);



/// add for admin:
    String forceCloseStore(String storeName);
    String viewSystemLogs();
    String reviewReport(int reportId);
    String generateSystemReport();

    String lockUserAccount(String username);
    String unlockUserAccount(String username);
    String deleteUserAccount(String username);


//add for ParallelTests

    int getProductStock(String storeName, String productName);
    int countOwners(String storeName);



//add for user:
    String updateUserEmail(String oldEmail, String newEmail);
    String changePassword(String oldPassword, String newPassword);
    String viewLoginHistory();
    String getSavedPaymentAndShippingInfo();

//add for owner
    String appointManager(String storeName, String username);
    String viewAppointmentChain(String storeName);
    String transferFounderRole(String storeName, String newUsername);

    }