package support;

import market.application.UserService;
import market.application.StoreService;
import market.application.AuthenticationService;
import market.application.PaymentService;
import market.application.ShipmentService;
import market.model.Product;
import market.model.ShoppingCart;

import java.util.List;

public class RealBridge implements Bridge {
    private final UserService userService;
    private final StoreService storeService;
    private final AuthenticationService authService;
    private final PaymentService paymentService;
    private final ShipmentService shipmentService;

    public RealBridge(UserService userService, StoreService storeService,
                      AuthenticationService authService,
                      PaymentService paymentService,
                      ShipmentService shipmentService) {
        this.userService = userService;
        this.storeService = storeService;
        this.authService = authService;
        this.paymentService = paymentService;
        this.shipmentService = shipmentService;
    }

    @Override
    public String register(String username, String password, String email, String address) {
        return userService.register(username, password, email, address);
    }

    @Override
    public String login(String username, String password) {
        return userService.login(username, password);
    }

    @Override
    public void enterAsGuest() {
        userService.initGuestSession();
    }

    @Override
    public void exitAsGuest() {
        userService.endGuestSession();
    }

    @Override
    public String getStoreAndProductInfo() {
        return storeService.getStoreAndProductInfo();
    }

    @Override
    public List<Product> searchProductsGlobally(String keyword) {
        return storeService.searchAllStores(keyword);
    }

    @Override
    public List<Product> searchProductsInStore(String storeName, String keyword) {
        return storeService.searchInStore(storeName, keyword);
    }

    @Override
    public void addProductToCart(String storeName, String productName, int quantity) {
        userService.addToCart(storeName, productName, quantity);
    }

    @Override
    public void updateCartItem(String storeName, String productName, int newQuantity) {
        userService.updateCartItem(storeName, productName, newQuantity);
    }

    @Override
    public String purchaseCart(String deliveryAddress, String creditCard, String expDate, String cvv) {
        if (!userService.hasStock()) return "Product unavailable";

        boolean paid = paymentService.process(creditCard, expDate, cvv);
        if (!paid) return "Payment failed";

        boolean delivered = shipmentService.deliver(deliveryAddress);
        if (!delivered) return "Delivery failed";

        userService.clearCart();
        return "Purchase completed";
    }





    //subscriber added methods:
    @Override
    public String viewPurchaseHistory() {
        return userService.getPurchaseHistory();
    }

    @Override
    public String rateProduct(String productName, int rating) {
        return userService.rateProduct(productName, rating);
    }

    @Override
    public String sendMessageToStore(String storeName, String message) {
        return storeService.sendMessage(storeName, message);
}





    //added for store owner
    @Override
    public String openStore(String storeName, String storeType) {
        return storeService.openStore(storeName, storeType);
    }

    @Override
    public String addProductToStore(String storeName, String productName, String category, double price, int quantity) {
        return storeService.addProduct(storeName, productName, category, price, quantity);
    }

    @Override
    public String editProduct(String storeName, String productName, String fieldName, String newValue) {
        return storeService.editProduct(storeName, productName, fieldName, newValue);
    }

    @Override
    public String removeProduct(String storeName, String productName) {
        return storeService.removeProduct(storeName, productName);
    }

    @Override
    public String viewStorePurchaseHistory(String storeName) {
        return storeService.getPurchaseHistory(storeName);
    }

    @Override
    public String editDiscountPolicy(String storeName, String productName, String discountDetails, String appliedTo) {
        return storeService.editDiscountPolicy(storeName, productName, discountDetails, appliedTo);
    }

    @Override
    public String editPurchasePolicy(String storeName, String productName, String policyDetails) {
        return storeService.editPurchasePolicy(storeName, productName, policyDetails);
    }

    @Override
    public String appointOwner(String storeName, String newOwnerUsername) {
        return storeService.appointOwner(storeName, newOwnerUsername);
    }

    @Override
    public String removeOwner(String storeName, String targetOwnerUsername) {
        return storeService.removeOwner(storeName, targetOwnerUsername);
    }

    @Override
    public String respondToMessage(String storeName, int messageId, String responseText) {
        return storeService.respondToMessage(storeName, messageId, responseText);
    }




    //add for admin:
    @Override
    public String forceCloseStore(String storeName) {
        return adminService.forceCloseStore(storeName);
    }

    @Override
    public String viewSystemLogs() {
        return adminService.getSystemLogs();
    }

    @Override
    public String reviewReport(int reportId) {
        return adminService.reviewUserReport(reportId);
    }

    @Override
    public String generateSystemReport() {
        return adminService.generateSystemSummary();
    }

    @Override
    public String lockUserAccount(String username) {
        return adminService.lockUser(username);
    }

    @Override
    public String unlockUserAccount(String username) {
        return adminService.unlockUser(username);
    }

    @Override
    public String deleteUserAccount(String username) {
        return adminService.deleteUser(username);
    }


    //add for ParallelTests:
    @Override
    public int getProductStock(String storeName, String productName) {
        return storeService.getProductStock(storeName, productName);
    }

    @Override
    public int countOwners(String storeName) {
        return storeService.countOwners(storeName);
    }



    //add for user
    @Override
    public String updateUserEmail(String oldEmail, String newEmail) {
        return userService.updateEmail(oldEmail, newEmail);
    }

    @Override
    public String changePassword(String oldPassword, String newPassword) {
        return userService.changePassword(oldPassword, newPassword);
    }

    @Override
    public String viewLoginHistory() {
        return userService.getLoginHistory();
    }

    @Override
    public String getSavedPaymentAndShippingInfo() {
        return userService.getSavedPaymentAndShippingInfo();
    }





    //add for founder:
        @Override
    public String appointManager(String storeName, String username) {
        return storeService.appointManager(storeName, username);
    }

    @Override
    public String viewAppointmentChain(String storeName) {
        return storeService.viewAppointmentChain(storeName);
    }

    @Override
    public String transferFounderRole(String storeName, String newUsername) {
        return storeService.transferFounderRole(storeName, newUsername);
    }
}
