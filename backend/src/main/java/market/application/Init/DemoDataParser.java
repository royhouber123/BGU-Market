package market.application.Init;

import market.application.Init.DemoDataModels.*;
import utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class DemoDataParser {
    
    private static final Logger logger = Logger.getInstance();
    
    public static class DemoData {
        private final List<DemoUser> users = new ArrayList<>();
        private final List<DemoStore> stores = new ArrayList<>();
        private final List<DemoProduct> products = new ArrayList<>();
        private final List<DemoManager> managers = new ArrayList<>();
        private final List<DemoOwner> owners = new ArrayList<>();
        
        // Getters
        public List<DemoUser> getUsers() { return users; }
        public List<DemoStore> getStores() { return stores; }
        public List<DemoProduct> getProducts() { return products; }
        public List<DemoManager> getManagers() { return managers; }
        public List<DemoOwner> getOwners() { return owners; }
        
        @Override
        public String toString() {
            return String.format("DemoData{users=%d, stores=%d, products=%d, managers=%d, owners=%d}",
                    users.size(), stores.size(), products.size(), managers.size(), owners.size());
        }
    }
    
    public static DemoData parseFromFile(String filename) throws IOException {
        logger.info("[DemoDataParser] Parsing demo data from file: " + filename);
        
        DemoData data = new DemoData();
        
        try (InputStream inputStream = DemoDataParser.class.getClassLoader().getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            
            if (inputStream == null) {
                throw new IOException("Demo data file not found: " + filename);
            }
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                parseLine(line.trim(), lineNumber, data);
            }
        }
        
        logger.info("[DemoDataParser] Parsing completed: " + data);
        return data;
    }
    
    private static void parseLine(String line, int lineNumber, DemoData data) {
        // Skip empty lines and comments
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }
        
        try {
            String[] parts = line.split("\\s+", 2); // Split into command and arguments
            if (parts.length < 2) {
                logger.warn("[DemoDataParser] Invalid line " + lineNumber + ": " + line);
                return;
            }
            
            String command = parts[0].toUpperCase();
            String arguments = parts[1];
            
            switch (command) {
                case "USER":
                    parseUser(arguments, data);
                    break;
                case "STORE":
                    parseStore(arguments, data);
                    break;
                case "PRODUCT":
                    parseProduct(arguments, data);
                    break;
                case "MANAGER":
                    parseManager(arguments, data);
                    break;
                case "OWNER":
                    parseOwner(arguments, data);
                    break;
                default:
                    logger.warn("[DemoDataParser] Unknown command at line " + lineNumber + ": " + command);
            }
        } catch (Exception e) {
            logger.error("[DemoDataParser] Error parsing line " + lineNumber + ": " + line + " - " + e.getMessage());
        }
    }
    
    private static void parseUser(String arguments, DemoData data) {
        String[] parts = arguments.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("USER requires format: username,password");
        }
        
        String username = parts[0].trim();
        String password = parts[1].trim();
        
        data.getUsers().add(new DemoUser(username, password));
        logger.debug("[DemoDataParser] Parsed user: " + username);
    }
    
    private static void parseStore(String arguments, DemoData data) {
        String[] parts = arguments.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("STORE requires format: storeId,storeName,founderId");
        }
        
        String storeName = parts[0].trim();
        String founderId = parts[1].trim();
        
        data.getStores().add(new DemoStore(storeName, founderId));
        logger.debug("[DemoDataParser] Parsed store: " + storeName);
    }
    
    private static void parseProduct(String arguments, DemoData data) {
        String[] parts = arguments.split(",");
        if (parts.length != 9) {
            throw new IllegalArgumentException("PRODUCT requires format: userName,storeName,productId,productName,productCategory,productDescription,quantity,price,purchaseType");
        }
        
        String userName = parts[0].trim();
        String storeName = parts[1].trim();
        String productId = parts[2].trim();
        String productName = parts[3].trim();
        String productCategory = parts[4].trim();
        String productDescription = parts[5].trim();
        int quantity = Integer.parseInt(parts[6].trim());
        double price = Double.parseDouble(parts[7].trim());
        String purchaseType = parts[8].trim();
        
        data.getProducts().add(new DemoProduct(userName, storeName, productId, productName, productCategory, productDescription, quantity, price, purchaseType));
        logger.debug("[DemoDataParser] Parsed product: " + productId + " for store: " + storeName + " by user: " + userName);
    }
    
    private static void parseManager(String arguments, DemoData data) {
        String[] parts = arguments.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("MANAGER requires format: storeId,managerUsername,appointerUsername");
        }
        
        String storeId = parts[0].trim();
        String managerUsername = parts[1].trim();
        String appointerUsername = parts[2].trim();
        
        data.getManagers().add(new DemoManager(storeId, managerUsername, appointerUsername));
        logger.debug("[DemoDataParser] Parsed manager: " + managerUsername + " for store " + storeId);
    }
    
    private static void parseOwner(String arguments, DemoData data) {
        String[] parts = arguments.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("OWNER requires format: storeId,ownerUsername,appointerUsername");
        }
        
        String storeId = parts[0].trim();
        String ownerUsername = parts[1].trim();
        String appointerUsername = parts[2].trim();
        
        data.getOwners().add(new DemoOwner(storeId, ownerUsername, appointerUsername));
        logger.debug("[DemoDataParser] Parsed owner: " + ownerUsername + " for store " + storeId);
    }
}