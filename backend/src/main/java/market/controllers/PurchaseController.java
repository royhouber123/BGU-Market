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
import utils.ApiResponseBuilder;

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
        
        Claims claimsResponse = authService.parseToken(token);
        
        String username = claimsResponse.getSubject();
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
        String token = TokenUtils.getToken();
        if (token == null) {
            return ResponseEntity.ok(ApiResponse.fail("No authentication token provided"));
        }
        return ApiResponseBuilder.build(() ->
            purchaseService.executePurchaseByUsername(
                token,
                request.paymentDetails(),
                request.shippingAddress()
            )
        );
    }

    /**
     * Submit an auction offer
     * POST /api/purchases/auction/offer
     */
    @PostMapping("/auction/offer")
    public ResponseEntity<ApiResponse<Void>> submitAuctionOffer(@RequestBody AuctionOfferRequest request) {
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            purchaseService.submitOffer(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username,
                request.offerAmount(),
                "Default shipping address",
                "Default contact info"
            );
            return null;
        });
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
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            return purchaseService.getAuctionStatus(
                username,
                String.valueOf(storeId),
                String.valueOf(productId)
            );
        });
    }

    /**
     * Submit a bid
     * POST /api/purchases/bid/submit
     */
    @PostMapping("/bid/submit")
    public ResponseEntity<ApiResponse<Void>> submitBid(@RequestBody BidSubmissionRequest request) {
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            purchaseService.submitBid(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username,
                request.bidAmount(),
                "Default shipping address",
                "Default contact info"
            );
            return null;
        });
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
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            return purchaseService.getBidStatus(
                String.valueOf(storeId),
                String.valueOf(productId),
                username
            );
        });
    }

    /**
     * Accept a counter offer
     * POST /api/purchases/bid/counter/accept
     */
    @PostMapping("/bid/counter/accept")
    public ResponseEntity<ApiResponse<Void>> acceptCounterOffer(@RequestBody CounterBidRequest request) {
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            purchaseService.acceptCounterOffer(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username
            );
            return null;
        });
    }

    /**
     * Decline a counter offer
     * POST /api/purchases/bid/counter/decline
     */
    @PostMapping("/bid/counter/decline")
    public ResponseEntity<ApiResponse<Void>> declineCounterOffer(@RequestBody CounterBidRequest request) {
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            purchaseService.declineCounterOffer(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username
            );
            return null;
        });
    }

    /**
     * Get user's purchase history
     * GET /api/purchases/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Purchase>>> getUserPurchaseHistory(@PathVariable int userId) {
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            return purchaseService.getPurchasesByUser(username);
        });
    }

    /**
     * Get store's purchase history
     * GET /api/purchases/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<Purchase>>> getStorePurchaseHistory(@PathVariable int storeId) {
        return ApiResponseBuilder.build(() ->
            purchaseService.getPurchasesByStore(String.valueOf(storeId))
        );
    }
} 