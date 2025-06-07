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
        // Validate request
        if (request == null) {
            return ResponseEntity.ok(ApiResponse.fail("Request body cannot be null"));
        }

        // Additional validation for request fields
        if (request.productId() == null) {
            return ResponseEntity.ok(ApiResponse.fail("Product ID cannot be null"));
        }

        if (request.storeId() <= 0) {
            return ResponseEntity.ok(ApiResponse.fail("Invalid store ID"));
        }

        if (request.offerAmount() <= 0) {
            return ResponseEntity.ok(ApiResponse.fail("Offer amount must be positive"));
        }

        if (request.shippingAddress() == null || request.shippingAddress().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail("Shipping address is required"));
        }

        if (request.paymentDetails() == null || request.paymentDetails().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail("Payment details are required"));
        }

        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            purchaseService.submitOffer(
                String.valueOf(request.storeId()),
                String.valueOf(request.productId()),
                username,
                request.offerAmount(),
                request.shippingAddress(),
                request.paymentDetails()
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
            @PathVariable String productId) {
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            return purchaseService.getAuctionStatus(
                username,
                String.valueOf(storeId),
                productId
            );
        });
    }

    /**
     * Open an auction
     * POST /api/purchases/auction/open
     */
    @PostMapping("/auction/open")
    public ResponseEntity<ApiResponse<Void>> openAuction(@RequestBody OpenAuctionRequest request) {
        // Validate request
        if (request == null) {
            return ResponseEntity.ok(ApiResponse.fail("Request body cannot be null"));
        }

        if (request.storeId() <= 0) {
            return ResponseEntity.ok(ApiResponse.fail("Invalid store ID"));
        }

        if (request.productId() == null || request.productId().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail("Product ID is required"));
        }

        if (request.productName() == null || request.productName().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail("Product name is required"));
        }

        if (request.productCategory() == null || request.productCategory().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail("Product category is required"));
        }

        if (request.productDescription() == null || request.productDescription().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail("Product description is required"));
        }

        if (request.startingPrice() <= 0) {
            return ResponseEntity.ok(ApiResponse.fail("Starting price must be positive"));
        }

        if (request.endTimeMillis() <= System.currentTimeMillis()) {
            return ResponseEntity.ok(ApiResponse.fail("End time must be in the future"));
        }

        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            purchaseService.openAuction(
                username,
                String.valueOf(request.storeId()),
                request.productId(),
                request.productName(),
                request.productCategory(),
                request.productDescription(),
                request.startingPrice(),
                request.endTimeMillis()
            );
            return null;
        });
    }

    /**
     * Submit a bid
     * POST /api/purchases/bid/submit
     */
    @PostMapping("/bid/submit")
    public ResponseEntity<ApiResponse<Void>> submitBid(@RequestBody BidSubmissionRequest request) {
        // Validate request
        if (request == null) {
            return ResponseEntity.ok(ApiResponse.fail("Request body cannot be null"));
        }

        // Additional validation for request fields
        if (request.productId() == null) {
            return ResponseEntity.ok(ApiResponse.fail("Product ID cannot be null"));
        }

        if (request.storeId() <= 0) {
            return ResponseEntity.ok(ApiResponse.fail("Invalid store ID"));
        }

        if (request.bidAmount() <= 0) {
            return ResponseEntity.ok(ApiResponse.fail("Bid amount must be positive"));
        }

        if (request.shippingAddress() == null || request.shippingAddress().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail("Shipping address is required"));
        }

        if (request.paymentDetails() == null || request.paymentDetails().trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail("Payment details are required"));
        }

        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            purchaseService.submitBid(
                String.valueOf(request.storeId()),
                request.productId(), // Use productId directly as String
                username,
                request.bidAmount(),
                request.shippingAddress(), // Use actual shipping address from request
                request.paymentDetails() // Use actual payment details from request
            );
            return null;
        });
    }

    /**
     * Get bid status for a user
     * GET /api/purchases/bid/status/{storeId}/{productId}/{userId}
     */
    @GetMapping("/bid/status/{storeId}/{productId}/{userId}")
    public ResponseEntity<ApiResponse<String>> getBidStatus(
            @PathVariable int storeId,
            @PathVariable String productId,
            @PathVariable int userId) {
        return ApiResponseBuilder.build(() -> {
            String username = extractUsernameFromToken();
            return purchaseService.getBidStatus(
                String.valueOf(storeId),
                productId,  // Use productId directly as String
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
                request.productId(), // Use productId directly as String
                username // Use username instead of integer userId
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
                request.productId(), // Use productId directly as String
                username // Use username instead of integer userId
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

    /**
     * Get all bids for a specific product
     * GET /api/purchases/bids/{storeId}/{productId}
     */
    @GetMapping("/bids/{storeId}/{productId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getProductBids(
            @PathVariable int storeId,
            @PathVariable String productId) {
            String username = extractUsernameFromToken();
            return ApiResponseBuilder.build(() ->
                purchaseService.getProductBids(
                    String.valueOf(storeId), 
                    productId, // Use productId directly as String
                    username
                )
            );
    }

    /**
     * Get current user's bids for a specific product
     * GET /api/purchases/my-bids/{storeId}/{productId}
     */
    @GetMapping("/my-bids/{storeId}/{productId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMyProductBids(
            @PathVariable int storeId,
            @PathVariable String productId) {
            String username = extractUsernameFromToken();
            return ApiResponseBuilder.build(() ->
                purchaseService.getMyProductBids(
                    String.valueOf(storeId), 
                    productId, // Use productId directly as String
                    username
                )
            );
    }

    /**
     * Approve a bid
     * POST /api/purchases/bid/approve
     */
    @PostMapping("/bid/approve")
    public ResponseEntity<ApiResponse<Void>> approveBid(@RequestBody BidApprovalRequest request) {
        String username = extractUsernameFromToken();
        return ApiResponseBuilder.build(() -> {
            purchaseService.approveBid(
                String.valueOf(request.storeId()),
                request.productId(), // Use productId directly as String
                request.bidderUsername(),
                username // approver username
            );
            return null;
        });
    }

    /**
     * Reject a bid
     * POST /api/purchases/bid/reject
     */
    @PostMapping("/bid/reject")
    public ResponseEntity<ApiResponse<Void>> rejectBid(@RequestBody BidApprovalRequest request) {
        String username = extractUsernameFromToken();
        return ApiResponseBuilder.build(() -> {
            purchaseService.rejectBid(
                String.valueOf(request.storeId()),
                request.productId(), // Use productId directly as String
                request.bidderUsername(),
                username // approver username
            );
            return null;
        });
    }

    /**
     * Propose a counter bid
     * POST /api/purchases/bid/counter
     */
    @PostMapping("/bid/counter")
    public ResponseEntity<ApiResponse<Void>> proposeCounterBid(@RequestBody CounterBidRequest request) {
        String username = extractUsernameFromToken();
        return ApiResponseBuilder.build(() -> {
            purchaseService.proposeCounterBid(
                String.valueOf(request.storeId()),
                request.productId(), // Use productId directly as String
                request.bidderUsername(),
                username, // approver username
                request.counterAmount()
            );
            return null;
        });
    }
} 