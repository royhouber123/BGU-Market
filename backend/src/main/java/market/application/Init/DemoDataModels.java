package market.application.Init;

import java.util.List;
import java.util.ArrayList;

public class DemoDataModels {
    
    public static class DemoUser {
        private final String username;
        private final String password;
        
        public DemoUser(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        
        @Override
        public String toString() {
            return "DemoUser{username='" + username + "'}";
        }
    }
    
    public static class DemoStore {
        private final String storeName;
        private final String founderId;
        
        public DemoStore(String storeName, String founderId) {
            this.storeName = storeName;
            this.founderId = founderId;
        }
        
        public String getStoreName() { return storeName; }
        public String getFounderId() { return founderId; }
        
        @Override
        public String toString() {
            return "DemoStore{storeName='" + storeName + "', founderId='" + founderId + "'}";
        }
    }
    
    public static class DemoProduct {
        private final String userName;       // Added userName
        private final String storeName;      // Keep storeName for mapping
        private final String productId;
        private final String productName;    // Renamed from 'name' to match controller
        private final String productCategory; // Renamed from 'category' 
        private final String productDescription; // Renamed from 'description'
        private final int quantity;
        private final double price;
        private final String purchaseType;
        
        public DemoProduct(String userName, String storeName, String productId, String productName, 
                          String productCategory, String productDescription, int quantity, double price, String purchaseType) {
            this.userName = userName;
            this.storeName = storeName;
            this.productId = productId;
            this.productName = productName;
            this.productCategory = productCategory;
            this.productDescription = productDescription;
            this.quantity = quantity;
            this.price = price;
            this.purchaseType = purchaseType;
        }
        
        // Getters
        public String getUserName() { return userName; }
        public String getStoreName() { return storeName; }
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getProductCategory() { return productCategory; }
        public String getProductDescription() { return productDescription; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public String getPurchaseType() { return purchaseType; }
        
        @Override
        public String toString() {
            return "DemoProduct{productId='" + productId + "', productName='" + productName + "', store='" + storeName + "', user='" + userName + "'}";
        }
    }
    
    public static class DemoManager {
        private final String storeName;
        private final String managerId;
        private final String appointerId;
        private final List<Integer> permissions; // ✅ CHANGED: Now a list of integers

        public DemoManager(String storeName, String managerId, String appointerId, List<Integer> permissions) {
            this.storeName = storeName;
            this.managerId = managerId;
            this.appointerId = appointerId;
            this.permissions = permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
        }

        // Getters
        public String getStoreName() { return storeName; }
        public String getManagerId() { return managerId; }
        public String getAppointerId() { return appointerId; }
        public List<Integer> getPermissions() { return new ArrayList<>(permissions); } // ✅ NEW

        @Override
        public String toString() {
            return "DemoManager{storeName='" + storeName + "', managerId='" + managerId + 
                   "', appointerId='" + appointerId + "', permissions=" + permissions + "}";
        }
    }
    
    public static class DemoOwner {
        private final String storeId;
        private final String ownerUsername;
        private final String appointerUsername;
        
        public DemoOwner(String storeId, String ownerUsername, String appointerUsername) {
            this.storeId = storeId;
            this.ownerUsername = ownerUsername;
            this.appointerUsername = appointerUsername;
        }
        
        // Getters
        public String getStoreId() { return storeId; }
        public String getOwnerUsername() { return ownerUsername; }
        public String getAppointerUsername() { return appointerUsername; }
        
        @Override
        public String toString() {
            return "DemoOwner{storeId='" + storeId + "', owner='" + ownerUsername + "'}";
        }
    }
    
    public static class DemoAdmin {
        private final String username;
        private final String password;
        
        public DemoAdmin(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        
        @Override
        public String toString() {
            return "DemoAdmin{username='" + username + "'}";
        }
    }
}