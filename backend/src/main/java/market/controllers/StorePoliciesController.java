package market.controllers;

import market.application.StorePoliciesService;
import market.dto.PolicyDTO;
import market.middleware.TokenUtils;
import market.application.AuthService;
import io.jsonwebtoken.Claims;
import utils.ApiResponse;
import utils.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST Controller for store policy operations (discounts and purchase policies).
 * This controller delegates all business logic to the StorePoliciesService.
 */
@RestController
@RequestMapping("/api/stores/{storeId}/policies")
@CrossOrigin(origins = "*")
public class StorePoliciesController {

    private static final Logger logger = LoggerFactory.getLogger(StorePoliciesController.class);

    @Autowired
    private StorePoliciesService storePoliciesService;
    
    @Autowired
    private AuthService authService;

    /**
     * Helper method to extract username from JWT token
     */
    private String extractUsernameFromToken() {
        String token = TokenUtils.getToken();
        if (token == null) {
            throw new IllegalStateException("No authentication token provided");
        }
        
        Claims claimsResponse = authService.parseToken(token);
        
        String username = claimsResponse.getSubject();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Invalid token: no username found");
        }
        
        return username;
    }

    /**
     * Add a new discount policy to a store
     * POST /api/stores/{storeId}/policies/discounts
     */
    @PostMapping("/discounts")
    public ResponseEntity<ApiResponse<Boolean>> addDiscount(
            @PathVariable String storeId,
            @RequestParam String userId,
            @RequestBody PolicyDTO.AddDiscountRequest request) {
        
        logger.info("Starting addDiscount endpoint - storeId: {}, userId: {}", storeId, userId);
        logger.debug("AddDiscount request details: {}", request);
        
        try {
            // Extract actual username from JWT token
            String authenticatedUsername = extractUsernameFromToken();
            logger.debug("Authenticated username from token: {}", authenticatedUsername);
            
            // Validate that the userId parameter matches the authenticated user
            if (!authenticatedUsername.equals(userId)) {
                logger.warn("Authenticated user {} does not match userId parameter {}", authenticatedUsername, userId);
                throw new IllegalStateException("User ID mismatch: authenticated user does not match provided user ID");
            }
            
            ResponseEntity<ApiResponse<Boolean>> response = ApiResponseBuilder.build(() -> {
                logger.debug("Calling StorePoliciesService.addDiscount with storeId: {}, userId: {}", storeId, authenticatedUsername);
                Boolean result = storePoliciesService.addDiscount(storeId, authenticatedUsername, request);
                logger.debug("StorePoliciesService.addDiscount returned: {}", result);
                return result;
            });
            
            logger.info("Successfully completed addDiscount endpoint - storeId: {}, userId: {}, response status: {}", 
                       storeId, authenticatedUsername, response.getStatusCode());
            return response;
            
        } catch (Exception e) {
            logger.error("Error in addDiscount endpoint - storeId: {}, userId: {}, error: {}", 
                        storeId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Remove a discount policy from a store
     * DELETE /api/stores/{storeId}/policies/discounts
     */
    @DeleteMapping("/discounts")
    public ResponseEntity<ApiResponse<Boolean>> removeDiscount(
            @PathVariable String storeId,
            @RequestParam String userId,
            @RequestBody PolicyDTO.AddDiscountRequest request) {
        
        logger.info("Starting removeDiscount endpoint - storeId: {}, userId: {}", storeId, userId);
        
        try {
            String authenticatedUsername = extractUsernameFromToken();
            logger.debug("Authenticated username from token: {}", authenticatedUsername);
            
            if (!authenticatedUsername.equals(userId)) {
                logger.warn("Authenticated user {} does not match userId parameter {}", authenticatedUsername, userId);
                throw new IllegalStateException("User ID mismatch: authenticated user does not match provided user ID");
            }
            
            return ApiResponseBuilder.build(() -> 
                storePoliciesService.removeDiscount(storeId, authenticatedUsername, request)
            );
        } catch (Exception e) {
            logger.error("Error in removeDiscount endpoint - storeId: {}, userId: {}, error: {}", 
                        storeId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all discount policies for a store
     * GET /api/stores/{storeId}/policies/discounts
     */
    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<List<PolicyDTO.AddDiscountRequest>>> getDiscounts(
            @PathVariable String storeId,
            @RequestParam String userId) {
        
        logger.info("Starting getDiscounts endpoint - storeId: {}, userId: {}", storeId, userId);
        
        try {
            String authenticatedUsername = extractUsernameFromToken();
            logger.debug("Authenticated username from token: {}", authenticatedUsername);
            
            if (!authenticatedUsername.equals(userId)) {
                logger.warn("Authenticated user {} does not match userId parameter {}", authenticatedUsername, userId);
                throw new IllegalStateException("User ID mismatch: authenticated user does not match provided user ID");
            }
            
            return ApiResponseBuilder.build(() -> 
                storePoliciesService.getDiscounts(storeId, authenticatedUsername)
            );
        } catch (Exception e) {
            logger.error("Error in getDiscounts endpoint - storeId: {}, userId: {}, error: {}", 
                        storeId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Add a new purchase policy to a store
     * POST /api/stores/{storeId}/policies/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<Boolean>> addPurchasePolicy(
            @PathVariable String storeId,
            @RequestParam String userId,
            @RequestBody PolicyDTO.AddPurchasePolicyRequest request) {
        
        logger.info("Starting addPurchasePolicy endpoint - storeId: {}, userId: {}", storeId, userId);
        
        try {
            String authenticatedUsername = extractUsernameFromToken();
            logger.debug("Authenticated username from token: {}", authenticatedUsername);
            
            if (!authenticatedUsername.equals(userId)) {
                logger.warn("Authenticated user {} does not match userId parameter {}", authenticatedUsername, userId);
                throw new IllegalStateException("User ID mismatch: authenticated user does not match provided user ID");
            }
            
            return ApiResponseBuilder.build(() -> 
                storePoliciesService.addPurchasePolicy(storeId, authenticatedUsername, request)
            );
        } catch (Exception e) {
            logger.error("Error in addPurchasePolicy endpoint - storeId: {}, userId: {}, error: {}", 
                        storeId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Remove a purchase policy from a store
     * DELETE /api/stores/{storeId}/policies/purchase
     */
    @DeleteMapping("/purchase")
    public ResponseEntity<ApiResponse<Boolean>> removePurchasePolicy(
            @PathVariable String storeId,
            @RequestParam String userId,
            @RequestBody PolicyDTO.AddPurchasePolicyRequest request) {
        
        logger.info("Starting removePurchasePolicy endpoint - storeId: {}, userId: {}", storeId, userId);
        
        try {
            String authenticatedUsername = extractUsernameFromToken();
            logger.debug("Authenticated username from token: {}", authenticatedUsername);
            
            if (!authenticatedUsername.equals(userId)) {
                logger.warn("Authenticated user {} does not match userId parameter {}", authenticatedUsername, userId);
                throw new IllegalStateException("User ID mismatch: authenticated user does not match provided user ID");
            }
            
            return ApiResponseBuilder.build(() -> 
                storePoliciesService.removePurchasePolicy(storeId, authenticatedUsername, request)
            );
        } catch (Exception e) {
            logger.error("Error in removePurchasePolicy endpoint - storeId: {}, userId: {}, error: {}", 
                        storeId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all purchase policies for a store
     * GET /api/stores/{storeId}/policies/purchase
     */
    @GetMapping("/purchase")
    public ResponseEntity<ApiResponse<List<PolicyDTO.AddPurchasePolicyRequest>>> getPurchasePolicies(
            @PathVariable String storeId,
            @RequestParam String userId) {
        
        logger.info("Starting getPurchasePolicies endpoint - storeId: {}, userId: {}", storeId, userId);
        
        try {
            String authenticatedUsername = extractUsernameFromToken();
            logger.debug("Authenticated username from token: {}", authenticatedUsername);
            
            if (!authenticatedUsername.equals(userId)) {
                logger.warn("Authenticated user {} does not match userId parameter {}", authenticatedUsername, userId);
                throw new IllegalStateException("User ID mismatch: authenticated user does not match provided user ID");
            }
            
            return ApiResponseBuilder.build(() -> 
                storePoliciesService.getPurchasePolicies(storeId, authenticatedUsername)
            );
        } catch (Exception e) {
            logger.error("Error in getPurchasePolicies endpoint - storeId: {}, userId: {}, error: {}", 
                        storeId, userId, e.getMessage(), e);
            throw e;
        }
    }
} 