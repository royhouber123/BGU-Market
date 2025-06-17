package market.application.Init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.Logger;
import market.application.StartupConfig;
import market.application.AdminConfig; // ✅ ADD THIS

import market.application.Init.DemoDataModels.*;
import market.application.Init.DemoDataParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component that populates the application with demo data on startup.
 * This performs the following actions:
 * 1. Register users: u2, u3, u4, u5, u6 (u1 admin is already in the system)
 * 2. Login as u2
 * 3. Create store s1 using u2
 * 4. Add product "Bamba" with 20 units and price 30 to store s1
 * 5. Appoint u3 as store manager with edit permissions
 * 6. Appoint u4 and u5 as store owners
 */
@Component
public class DemoDataPopulator {

    private static final Logger logger = Logger.getInstance();
    private final String BASE_URL = "http://localhost:8080";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Environment env;
    
    @Autowired
    private AdminConfig adminConfig;
    
    @Autowired
    private StartupConfig startupConfig;

    /**
     * Execute after the application has fully started to ensure all controllers are initialized.
     * Will only run if the bgu.market.populate-demo-data property is set to true.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void populateDemoData() {
        String populateData = env.getProperty("bgu.market.populate-demo-data");
        String dataFile = env.getProperty("bgu.market.demo-data-file", "demo-data.txt");
        
        if (populateData == null || !populateData.equalsIgnoreCase("true")) {
            logger.info("[DemoDataPopulator] Demo data population disabled (bgu.market.populate-demo-data=" + populateData + ")");
            return;
        }
        
        logger.info("[DemoDataPopulator] Starting demo data population from file: " + dataFile);
        
        try {
            // Parse demo data from file
            DemoDataParser.DemoData demoData = DemoDataParser.parseFromFile(dataFile);
            logger.info("[DemoDataPopulator] Loaded demo data: " + demoData);
            
            // ✅ STEP 1: Initialize admin FIRST
            initializeAdmin(demoData);
            
            // Execute population steps
            Map<String, String> userTokens = createUsers(demoData.getUsers());
            Map<String, String> storeIds = createStores(demoData.getStores(), userTokens);
            addProducts(demoData.getProducts(), userTokens, storeIds);
            appointManagers(demoData.getManagers(), userTokens, storeIds);
            appointOwners(demoData.getOwners(), userTokens, storeIds);
            
            logger.info("[DemoDataPopulator] Demo data population completed successfully!");
        } catch (Exception e) {
            logger.error("[DemoDataPopulator] Error while populating demo data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ✅ INITIALIZE ADMIN: Use AdminConfig instead of direct Environment access
     */
    private void initializeAdmin(DemoDataParser.DemoData demoData) {
        logger.info("[DemoDataPopulator] Initializing admin user...");
        
        String adminUsername;
        String adminPassword;
        
        if (demoData.getAdmins().size() == 1) {
            // Use the single admin from demo data
            DemoAdmin demoAdmin = demoData.getAdmins().get(0);
            adminUsername = demoAdmin.getUsername();
            adminPassword = demoAdmin.getPassword();
            logger.info("[DemoDataPopulator] Using ADMIN from demo data: " + adminUsername);
            
        } else if (demoData.getAdmins().size() == 0) {
            // No admin in demo data - use AdminConfig
            adminUsername = adminConfig.getAdminUsername(); // ✅ USE ADMINCONFIG
            adminPassword = adminConfig.getAdminPassword(); // ✅ USE ADMINCONFIG
            
            if (adminUsername == null || adminPassword == null) {
                throw new RuntimeException("[DemoDataPopulator] No ADMIN in demo data and no admin configuration available - cannot proceed!");
            }
            
            logger.info("[DemoDataPopulator] No ADMIN in demo data, using AdminConfig: " + adminUsername);
            
        } else {
            // Multiple admins in demo data - use AdminConfig as fallback
            adminUsername = adminConfig.getAdminUsername(); // ✅ USE ADMINCONFIG
            adminPassword = adminConfig.getAdminPassword(); // ✅ USE ADMINCONFIG
            
            if (adminUsername == null || adminPassword == null) {
                throw new RuntimeException("[DemoDataPopulator] Multiple ADMINs in demo data (" + demoData.getAdmins().size() + ") and no admin configuration available - cannot proceed!");
            }
            
            logger.warn("[DemoDataPopulator] Multiple ADMINs found in demo data (" + demoData.getAdmins().size() + "), using AdminConfig: " + adminUsername);
            
        }
        
        // ✅ DELEGATE TO STARTUPCONFIG (NO CHANGES TO EXISTING LOGIC)
        startupConfig.initializeAdminWithCredentials(adminUsername, adminPassword);
        logger.info("[DemoDataPopulator] Admin initialization completed: " + adminUsername);
    }
    
    private Map<String, String> createUsers(List<DemoUser> users) {
        logger.info("[DemoDataPopulator] Creating " + users.size() + " users");
        Map<String, String> userTokens = new HashMap<>();
        
        for (DemoUser user : users) {
            try {
                // Register user
                Map<String, String> registerRequest = new HashMap<>();
                registerRequest.put("username", user.getUsername());
                registerRequest.put("password", user.getPassword());
                
                HttpEntity<Map<String, String>> request = new HttpEntity<>(registerRequest, createJsonHeaders());
                String registerUrl = BASE_URL + "/api/users/register";
                restTemplate.postForEntity(registerUrl, request, String.class);
                
                // Login to get JWT token
                Map<String, String> loginRequest = new HashMap<>();
                loginRequest.put("username", user.getUsername());
                loginRequest.put("password", user.getPassword());
                
                HttpEntity<Map<String, String>> loginReq = new HttpEntity<>(loginRequest, createJsonHeaders());
                String loginUrl = BASE_URL + "/api/auth/login";
                ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, loginReq, String.class);
                
                String token = extractToken(response.getBody());
                userTokens.put(user.getUsername(), token);
                
                logger.info("[DemoDataPopulator] Created user: " + user.getUsername());
            } catch (RestClientException e) {
                logger.warn("[DemoDataPopulator] Error creating user " + user.getUsername() + ": " + e.getMessage());
            }
        }
        
        return userTokens;
    }
    
    private Map<String, String> createStores(List<DemoStore> stores, Map<String, String> userTokens) {
        logger.info("[DemoDataPopulator] Creating " + stores.size() + " stores");
        Map<String, String> storeNameToIdMap = new HashMap<>(); // Changed variable name for clarity
        
        for (DemoStore store : stores) {
            try {
                String founderToken = userTokens.get(store.getFounderId());
                if (founderToken == null) {
                    logger.warn("[DemoDataPopulator] No token found for founder: " + store.getFounderId());
                    continue;
                }
                
                Map<String, String> storeRequest = new HashMap<>();
                storeRequest.put("storeName", store.getStoreName());
                storeRequest.put("founderId", store.getFounderId());
                
                HttpHeaders headers = createJsonHeaders();
                headers.set("Authorization", "Bearer " + founderToken);
                HttpEntity<Map<String, String>> request = new HttpEntity<>(storeRequest, headers);
                
                String createStoreUrl = BASE_URL + "/api/stores/create";
                ResponseEntity<String> response = restTemplate.postForEntity(createStoreUrl, request, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    String responseBody = response.getBody();
                    JsonNode root = objectMapper.readTree(responseBody);
                    String actualStoreId = root.path("data").path("storeId").asText();
                    
                    // Map storeName to actual storeId
                    storeNameToIdMap.put(store.getStoreName(), actualStoreId);
                    logger.info("[DemoDataPopulator] Created store: " + store.getStoreName() + " (ID: " + actualStoreId + ")");
                }
            } catch (Exception e) {
                logger.error("[DemoDataPopulator] Error creating store " + store.getStoreName() + ": " + e.getMessage());
            }
        }
        
        return storeNameToIdMap;
    }
    
    private void addProducts(List<DemoProduct> products, Map<String, String> userTokens, Map<String, String> storeNameToIdMap) {
        logger.info("[DemoDataPopulator] Adding " + products.size() + " products");
        
        // Use the correct API endpoint
        String addProductUrl = BASE_URL + "/api/stores/listings/add";
        
        for (DemoProduct product : products) {
            try {
                // Get the actual store ID from storeName mapping
                String actualStoreId = storeNameToIdMap.get(product.getStoreName());
                if (actualStoreId == null) {
                    logger.warn("[DemoDataPopulator] Store not found for product: " + product.getProductName() + " in store: " + product.getStoreName());
                    continue;
                }
                
                // Get the user's token
                String userToken = userTokens.get(product.getUserName());
                if (userToken == null) {
                    logger.warn("[DemoDataPopulator] User token not found: " + product.getUserName());
                    continue;
                }
                
                // Create headers with authorization
                HttpHeaders headers = createJsonHeaders();
                headers.set("Authorization", "Bearer " + userToken);
                
                // Build the request matching your working format
                Map<String, Object> productRequest = new HashMap<>();
                productRequest.put("userName", product.getUserName());
                productRequest.put("storeID", actualStoreId);  // Note: "storeID" not "storeId"
                productRequest.put("productId", product.getProductId());
                productRequest.put("productName", product.getProductName());
                productRequest.put("productCategory", product.getProductCategory());
                productRequest.put("productDescription", product.getProductDescription());
                productRequest.put("quantity", product.getQuantity());
                productRequest.put("price", product.getPrice());
                productRequest.put("purchaseType", product.getPurchaseType());
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(productRequest, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(addProductUrl, request, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("[DemoDataPopulator] Added product: " + product.getProductName() + " to store " + product.getStoreName() + " (" + actualStoreId + ")");
                }
            } catch (Exception e) {
                logger.error("[DemoDataPopulator] Error adding product " + product.getProductName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void appointManagers(List<DemoManager> managers, Map<String, String> userTokens, Map<String, String> storeIds) {
        if (managers.isEmpty()) {
            logger.info("[DemoDataPopulator] No managers to appoint");
            return;
        }
        
        logger.info("[DemoDataPopulator] Appointing " + managers.size() + " managers");
        
        for (DemoManager manager : managers) {
            try {
                String appointerToken = userTokens.get(manager.getAppointerId());
                String storeId = storeIds.get(manager.getStoreName());
                
                if (appointerToken == null) {
                    logger.error("[DemoDataPopulator] Token not found for appointer: " + manager.getAppointerId());
                    continue;
                }
                
                if (storeId == null) {
                    logger.error("[DemoDataPopulator] Store ID not found for store: " + manager.getStoreName());
                    continue;
                }
                
                // ✅ STEP 1: Appoint manager (existing logic)
                appointManager(manager, appointerToken, storeId);
                
                // ✅ STEP 2: Add permissions to manager (new logic)
                addPermissionsToManager(manager, appointerToken, storeId);
                
            } catch (Exception e) {
                logger.error("[DemoDataPopulator] Failed to appoint manager " + manager.getManagerId() + ": " + e.getMessage());
            }
        }
    }
    
    private void appointManager(DemoManager manager, String appointerToken, String storeId) {
        try {
            String url = BASE_URL + "/api/stores/managers/add";
            
            // Your existing appointment logic here
            Map<String, Object> request = new HashMap<>();
            request.put("newManagerName", manager.getManagerId());
            request.put("appointerID", manager.getAppointerId());
            request.put("storeID", storeId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(appointerToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            System.out.println("[DemoDataPopulator] Appointing manager: " + manager.getManagerId() + " to store: " + manager.getStoreName());

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Success!");

            logger.info("[DemoDataPopulator] Manager " + manager.getManagerId() + " appointed to store " + manager.getStoreName() + " - Response: " + response.getStatusCode());
            
        } catch (Exception e) {
            logger.error("[DemoDataPopulator] Failed to appoint manager " + manager.getManagerId() + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * ✅ NEW METHOD: Add permissions to manager
     */
    private void addPermissionsToManager(DemoManager manager, String appointerToken, String storeId) {
        if (manager.getPermissions().isEmpty()) {
            logger.info("[DemoDataPopulator] No permissions to add for manager: " + manager.getManagerId());
            return;
        }
        
        logger.info("[DemoDataPopulator] Adding " + manager.getPermissions().size() + " permissions to manager " + manager.getManagerId());
        
        for (Integer permission : manager.getPermissions()) {
            try {
                String url = BASE_URL + "/api/stores/managers/permissions/add";
                
                // ✅ API REQUEST: Based on your AddPermissionRequest DTO
                Map<String, Object> request = new HashMap<>();
                request.put("managerID", manager.getManagerId());
                request.put("appointerID", manager.getAppointerId());
                request.put("permissionID", permission);
                request.put("storeID", storeId);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(appointerToken);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
                
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                
                String permissionName = getPermissionName(permission);
                logger.info("[DemoDataPopulator] Permission '" + permissionName + "' (" + permission + ") added to manager " + manager.getManagerId() + " - Response: " + response.getStatusCode());

            } catch (Exception e) {
                logger.error("[DemoDataPopulator] Failed to add permission " + permission + " to manager " + manager.getManagerId() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * ✅ HELPER METHOD: Convert permission ID to human-readable name
     */
    private String getPermissionName(int permission) {
        switch (permission) {
            case 0: return "View Only";
            case 1: return "Edit Products";
            case 2: return "Edit Policies";
            case 3: return "Bid Approval";
            default: return "Unknown (" + permission + ")";
        }
    }
    
    private void appointOwners(List<DemoOwner> owners, Map<String, String> userTokens, Map<String, String> storeNameToIdMap) {
        logger.info("[DemoDataPopulator] Appointing " + owners.size() + " store owners");
        
        for (DemoOwner owner : owners) {
            try {
                String storeId = storeNameToIdMap.get(owner.getStoreId());
                String appointerToken = userTokens.get(owner.getAppointerUsername());

                if (storeId == null || appointerToken == null) {
                    logger.warn("[DemoDataPopulator] Skipping owner " + owner.getOwnerUsername() + ": invalid storeId or appointerToken");
                    continue;
                }
                
                // Simple owner appointment request
                Map<String, String> ownerRequest = new HashMap<>();
                ownerRequest.put("appointerID", owner.getAppointerUsername());
                ownerRequest.put("newOwnerID", owner.getOwnerUsername());
                ownerRequest.put("storeID", storeId);
                
                HttpHeaders headers = createJsonHeaders();
                headers.set("Authorization", "Bearer " + appointerToken);
                
                HttpEntity<Map<String, String>> request = new HttpEntity<>(ownerRequest, headers);
                String appointUrl = BASE_URL + "/api/stores/owners/add";
                
                ResponseEntity<String> response = restTemplate.postForEntity(appointUrl, request, String.class);
                logger.info("[DemoDataPopulator] Owner " + owner.getOwnerUsername() + " appointment response: " + response.getStatusCode());
            } catch (RestClientException e) {
                logger.error("[DemoDataPopulator] Error appointing owner " + owner.getOwnerUsername() + ": " + e.getMessage());
            }
        }
    }
    
    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    
    private String extractToken(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("token").asText();
        } catch (Exception e) {
            logger.warn("[DemoDataPopulator] Error extracting token: " + e.getMessage());
            return null;
        }
    }
}
