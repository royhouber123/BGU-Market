package market.controllers;

import market.application.PurchaseService;
import market.dto.PurchaseDTO.*;
import market.domain.purchase.Purchase;
import market.middleware.TokenUtils;
import market.application.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utils.ApiResponse;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Purchase operations in BGU Market
 * Handles regular purchases, auction operations, bid operations, and purchase history
 */
@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "*")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;
    
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
        
        ApiResponse<Claims> claimsResponse = authService.parseToken(token);
        if (!claimsResponse.isSuccess()) {
            throw new IllegalStateException("Invalid token: " + claimsResponse.getError());
        }
        
        String username = claimsResponse.getData().getSubject();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Invalid token: no username found");
        }
        
        return username;
    }

    /**
     * Execute a regular purchase from user's shopping cart
     * POST /api/purchases/execute
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<String>> executePurchase(@RequestBody ExecutePurchaseRequest request) {
        try {
            String token = TokenUtils.getToken();
            if (token == null) {
                return ResponseEntity.ok(ApiResponse.fail("No authentication token provided"));
            }
            
            ApiResponse<String> result = purchaseService.executePurchaseByUsername(
                token,
                request.paymentDetails(),
                request.shippingAddress()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Purchase failed: " + e.getMessage()));
        }
    }

    /**
     * Submit an auction offer
     * POST /api/purchases/auction/offer
     */
    @PostMapping("/auction/offer")
    public ResponseEntity<ApiResponse<Void>> submitAuctionOffer(@RequestBody AuctionOfferRequest request) {
        try {
            String username = extractUsernameFromToken();
            
            ApiResponse<Void> result = purchaseService.submitOffer(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username, // Use username instead of integer userId
                request.offerAmount(),
                "Default shipping address", // You may want to add this to the DTO
                "Default contact info" // You may want to add this to the DTO
            );
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Auction offer failed: " + e.getMessage()));
        }
    }

    /**
     * Get auction status
     * GET /api/purchases/auction/status/{userId}/{storeId}/{productId}
     */
    @GetMapping("/auction/status/{userId}/{storeId}/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuctionStatus(
            @PathVariable int userId,
            @PathVariable int storeId,
            @PathVariable int productId) {
        try {
            String username = extractUsernameFromToken();
            
            ApiResponse<Map<String, Object>> result = purchaseService.getAuctionStatus(
                username, // Use username instead of integer userId
                String.valueOf(storeId), 
                String.valueOf(productId)
            );
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Failed to get auction status: " + e.getMessage()));
        }
    }

    /**
     * Submit a bid
     * POST /api/purchases/bid/submit
     */
    @PostMapping("/bid/submit")
    public ResponseEntity<ApiResponse<Void>> submitBid(@RequestBody BidSubmissionRequest request) {
        try {
            String username = extractUsernameFromToken();
            
            ApiResponse<Void> result = purchaseService.submitBid(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username, // Use username instead of integer userId
                request.bidAmount(),
                "Default shipping address", // You may want to add this to the DTO
                "Default contact info" // You may want to add this to the DTO
            );
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Bid submission failed: " + e.getMessage()));
        }
    }

    /**
     * Get bid status
     * GET /api/purchases/bid/status/{storeId}/{productId}/{userId}
     */
    @GetMapping("/bid/status/{storeId}/{productId}/{userId}")
    public ResponseEntity<ApiResponse<String>> getBidStatus(
            @PathVariable int storeId,
            @PathVariable int productId,
            @PathVariable int userId) {
        try {
            String username = extractUsernameFromToken();
            
            ApiResponse<String> result = purchaseService.getBidStatus(
                String.valueOf(storeId), 
                String.valueOf(productId), 
                username // Use username instead of integer userId
            );
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Failed to get bid status: " + e.getMessage()));
        }
    }

    /**
     * Accept a counter offer
     * POST /api/purchases/bid/counter/accept
     */
    @PostMapping("/bid/counter/accept")
    public ResponseEntity<ApiResponse<Void>> acceptCounterOffer(@RequestBody CounterBidRequest request) {
        try {
            String username = extractUsernameFromToken();
            
            ApiResponse<Void> result = purchaseService.acceptCounterOffer(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username // Use username instead of integer userId
            );
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Accept counter offer failed: " + e.getMessage()));
        }
    }

    /**
     * Decline a counter offer
     * POST /api/purchases/bid/counter/decline
     */
    @PostMapping("/bid/counter/decline")
    public ResponseEntity<ApiResponse<Void>> declineCounterOffer(@RequestBody CounterBidRequest request) {
        try {
            String username = extractUsernameFromToken();
            
            ApiResponse<Void> result = purchaseService.declineCounterOffer(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username // Use username instead of integer userId
            );
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Decline counter offer failed: " + e.getMessage()));
        }
    }

    /**
     * Get user's purchase history
     * GET /api/purchases/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Purchase>>> getUserPurchaseHistory(@PathVariable int userId) {
        try {
            String username = extractUsernameFromToken();
            
            ApiResponse<List<Purchase>> result = purchaseService.getPurchasesByUser(username);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Failed to get purchase history: " + e.getMessage()));
        }
    }

    /**
     * Get store's purchase history
     * GET /api/purchases/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<Purchase>>> getStorePurchaseHistory(@PathVariable int storeId) {
        try {
            ApiResponse<List<Purchase>> result = purchaseService.getPurchasesByStore(String.valueOf(storeId));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("Failed to get store purchase history: " + e.getMessage()));
        }
    }
} 