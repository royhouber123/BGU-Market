package market.application;

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

    /**
     * Execute after the application has fully started to ensure all controllers are initialized.
     * Will only run if the bgu.market.populate-demo-data property is set to true.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void populateDemoData() {
        String populateData = env.getProperty("bgu.market.populate-demo-data");
        if (populateData == null || !populateData.equalsIgnoreCase("true")) {
            logger.info("[DemoDataPopulator] Demo data population disabled");
            return;
        }
        
        logger.info("[DemoDataPopulator] Starting demo data population...");
        
        try {
            // Step 1: Register users u2-u6
            logger.info("[DemoDataPopulator] Step 1: Creating users");
            Map<String, String> users = createUsers();
            
            // Get u2's token - we'll use this for all operations
            String u2Token = users.get("u2");
            if (u2Token == null) {
                logger.error("[DemoDataPopulator] Failed to get token for u2, cannot continue");
                return;
            }
            logger.info("[DemoDataPopulator] Successfully authenticated as u2");
            
            // Step 2: Create store s1 as u2
            logger.info("[DemoDataPopulator] Step 2: Creating store s1");
            String storeId = createStore(u2Token);
            if (storeId == null) {
                logger.error("[DemoDataPopulator] Store creation failed, aborting demo data population");
                return;
            }
            logger.info("[DemoDataPopulator] Using storeId: " + storeId);
            
            System.out.println("Store created successfully" + storeId);
            // Step 3: Add products to store
            logger.info("[DemoDataPopulator] Step 3: Adding products to store " + storeId);
            addProduct(u2Token, storeId);
            
            // Step 4: Appoint u3 as store manager
            logger.info("[DemoDataPopulator] Step 4: Appointing u3 as store manager");
            appointStoreManager(u2Token, storeId, "u3");
            
            // Step 5: Appoint u4 and u5 as store owners
            logger.info("[DemoDataPopulator] Step 5: Appointing u4 and u5 as store owners");
            appointStoreOwners(u2Token, storeId);
            
            logger.info("[DemoDataPopulator] Demo data population completed successfully!");
        } catch (Exception e) {
            logger.error("[DemoDataPopulator] Error while populating demo data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Map<String, String> createUsers() {
        Map<String, String> userTokens = new HashMap<>();
        
        // Define user credentials
        String[][] users = {
            {"u2", "password123"},
            {"u3", "password123"},
            {"u4", "password123"},
            {"u5", "password123"},
            {"u6", "password123"}
        };
        
        for (String[] user : users) {
            String username = user[0];
            String password = user[1];
            
            try {
                // Register user
                Map<String, String> registerRequest = new HashMap<>();
                registerRequest.put("username", username);
                registerRequest.put("password", password);
                
                HttpEntity<Map<String, String>> request = new HttpEntity<>(registerRequest, createJsonHeaders());
                String registerUrl = BASE_URL + "/api/users/register";
                restTemplate.postForEntity(registerUrl, request, String.class);
                logger.info("[DemoDataPopulator] User registered: " + username);
                
                // Login to get JWT token
                Map<String, String> loginRequest = new HashMap<>();
                loginRequest.put("username", username);
                loginRequest.put("password", password);
                
                HttpEntity<Map<String, String>> loginReq = new HttpEntity<>(loginRequest, createJsonHeaders());
                String loginUrl = BASE_URL + "/api/auth/login";
                ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, loginReq, String.class);
                
                String token = extractToken(response.getBody());
                userTokens.put(username, token);
                
                logger.info("[DemoDataPopulator] User logged in: " + username);
            } catch (RestClientException e) {
                logger.warn("[DemoDataPopulator] Error creating user " + username + ": " + e.getMessage());
            }
        }
        
        return userTokens;
    }
    
    /**
     * Creates store and verifies it exists before returning
     * 
     * @param u2Token JWT token for user u2
     * @return storeId if store creation was successful, null otherwise
     */
    private String createStore(String u2Token) {
        try {
            Map<String, String> storeRequest = new HashMap<>();
            storeRequest.put("storeName", "s1");
            storeRequest.put("founderId", "u2");
            HttpHeaders headers = createJsonHeaders();
            headers.set("Authorization", "Bearer " + u2Token);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(storeRequest, headers);
            String createStoreUrl = BASE_URL + "/api/stores/create";
            ResponseEntity<String> response = restTemplate.postForEntity(createStoreUrl, request, String.class);
            logger.info("[DemoDataPopulator] Store creation response: " + response.getStatusCode());
            if (!response.getStatusCode().is2xxSuccessful()) {
                return null;
            }
            String responseBody = response.getBody();
            JsonNode root = objectMapper.readTree(responseBody);
            String storeId = root.path("data").path("storeId").asText();
            return storeId.isEmpty() ? null : storeId;
        } catch (Exception e) {
            logger.error("[DemoDataPopulator] Error creating store s1: " + e.getMessage());
            return null;
        }
    }
    
    private void addProduct(String u2Token, String storeId) {
        // Define products similar to populate_demo_data.sh with established categories
        // Format: productId, productName, category, description, quantity, price, purchaseType
        Object[][] products = {
            {"bamba_001", "Bamba", "Snacks", "Peanut snack", 20, 30, "REGULAR"},
            {"electronics_001", "iPhone 15", "Electronics", "Latest Apple smartphone with advanced features", 50, 999.99, "REGULAR"},
            {"electronics_002", "MacBook Pro", "Electronics", "Powerful laptop for professionals", 25, 2499.99, "REGULAR"},
            {"books_001", "The Great Gatsby", "Books", "Classic American literature novel", 200, 12.99, "REGULAR"},
            {"books_002", "Programming Pearls", "Books", "Essential book for software developers", 50, 29.99, "REGULAR"},
            {"clothing_001", "Designer Jeans", "Clothing", "Premium denim jeans with perfect fit", 100, 89.99, "REGULAR"},
            {"clothing_002", "Silk Blouse", "Clothing", "Elegant silk blouse for formal occasions", 60, 129.99, "REGULAR"},
            {"home_001", "Coffee Maker", "Home", "Automatic coffee maker with timer", 40, 149.99, "REGULAR"},
            {"sports_001", "Running Shoes", "Sports", "Professional running shoes for athletes", 120, 159.99, "REGULAR"}
        };
        
        HttpHeaders headers = createJsonHeaders();
        headers.set("Authorization", "Bearer " + u2Token);
        String addProductUrl = BASE_URL + "/api/stores/listings/add";
        
        for (Object[] product : products) {
            try {
                // Create product request
                Map<String, Object> productRequest = new HashMap<>();
                productRequest.put("userName", "u2");
                productRequest.put("storeID", storeId);
                productRequest.put("productId", product[0]);
                productRequest.put("productName", product[1]);
                productRequest.put("productCategory", product[2]);
                productRequest.put("productDescription", product[3]);
                productRequest.put("quantity", product[4]);
                productRequest.put("price", product[5]);
                productRequest.put("purchaseType", product[6]);
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(productRequest, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(addProductUrl, request, String.class);
                System.out.println("Response product: " + response.getBody());
                logger.info("[DemoDataPopulator] Added product: " + product[1] + " (" + product[2] + ") - Response: " + response.getStatusCode());
            } catch (RestClientException e) {
                logger.error("[DemoDataPopulator] Error adding product " + product[1] + ": " + e.getMessage());
            }
        }
    }
    
    private void appointStoreManager(String u2Token, String storeId, String managerUsername) {
        try {
            // Step 1: Appoint manager
            Map<String, String> managerRequest = new HashMap<>();
            managerRequest.put("appointerID", "u2");
            managerRequest.put("newManagerName", managerUsername);
            managerRequest.put("storeID", storeId);
            
            HttpHeaders headers = createJsonHeaders();
            headers.set("Authorization", "Bearer " + u2Token);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(managerRequest, headers);
            String appointUrl = BASE_URL + "/api/stores/managers/add";
            
            ResponseEntity<String> managerResponse = restTemplate.postForEntity(appointUrl, request, String.class);
            logger.info("[DemoDataPopulator] Manager appointment response: " + managerResponse.getStatusCode());
            
            // Step 2: Grant permissions
            Map<String, Object> permissionsRequest = new HashMap<>();
            permissionsRequest.put("managerID", managerUsername);
            permissionsRequest.put("appointerID", "u2");
            permissionsRequest.put("permissionID", 1); // EDIT_PRODUCT permission
            permissionsRequest.put("storeID", storeId);
            
            HttpEntity<Map<String, Object>> permRequest = new HttpEntity<>(permissionsRequest, headers);
            String permissionsUrl = BASE_URL + "/api/stores/managers/permissions/add";
            
            ResponseEntity<String> permResponse = restTemplate.postForEntity(permissionsUrl, permRequest, String.class);
            logger.info("[DemoDataPopulator] Permission grant response: " + permResponse.getStatusCode());
        } catch (RestClientException e) {
            logger.error("[DemoDataPopulator] Error with manager " + managerUsername + ": " + e.getMessage());
        }
    }
    
    private void appointStoreOwners(String u2Token, String storeId) {
        String[] owners = {"u4", "u5"};
        
        for (String owner : owners) {
            try {
                // Simple owner appointment request
                Map<String, String> ownerRequest = new HashMap<>();
                ownerRequest.put("appointerID", "u2");
                ownerRequest.put("newOwnerID", owner);
                ownerRequest.put("storeID", storeId);
                
                HttpHeaders headers = createJsonHeaders();
                headers.set("Authorization", "Bearer " + u2Token);
                
                HttpEntity<Map<String, String>> request = new HttpEntity<>(ownerRequest, headers);
                String appointUrl = BASE_URL + "/api/stores/owners/add";
                
                ResponseEntity<String> response = restTemplate.postForEntity(appointUrl, request, String.class);
                logger.info("[DemoDataPopulator] Owner " + owner + " appointment response: " + response.getStatusCode());
            } catch (RestClientException e) {
                logger.error("[DemoDataPopulator] Error appointing owner " + owner + ": " + e.getMessage());
            }
        }
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
    
    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
