package market.controllers;

import market.application.StorePoliciesService;
import market.dto.PolicyDTO;
import utils.ApiResponse;
import utils.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for store policy operations (discounts and purchase policies).
 * This controller delegates all business logic to the StorePoliciesService.
 */
@RestController
@RequestMapping("/api/stores/{storeId}/policies")
@CrossOrigin(origins = "*")
public class StorePoliciesController {

    @Autowired
    private StorePoliciesService storePoliciesService;

    /**
     * Add a new discount policy to a store
     * POST /api/stores/{storeId}/policies/discounts
     */
    @PostMapping("/discounts")
    public ResponseEntity<ApiResponse<Boolean>> addDiscount(
            @PathVariable String storeId,
            @RequestParam String userId,
            @RequestBody PolicyDTO.AddDiscountRequest request) {
        return ApiResponseBuilder.build(() -> 
            storePoliciesService.addDiscount(storeId, userId, request)
        );
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
        return ApiResponseBuilder.build(() -> 
            storePoliciesService.removeDiscount(storeId, userId, request)
        );
    }

    /**
     * Get all discount policies for a store
     * GET /api/stores/{storeId}/policies/discounts
     */
    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<List<PolicyDTO.AddDiscountRequest>>> getDiscounts(
            @PathVariable String storeId,
            @RequestParam String userId) {
        return ApiResponseBuilder.build(() -> 
            storePoliciesService.getDiscounts(storeId, userId)
        );
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
        return ApiResponseBuilder.build(() -> 
            storePoliciesService.addPurchasePolicy(storeId, userId, request)
        );
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
        return ApiResponseBuilder.build(() -> 
            storePoliciesService.removePurchasePolicy(storeId, userId, request)
        );
    }

    /**
     * Get all purchase policies for a store
     * GET /api/stores/{storeId}/policies/purchase
     */
    @GetMapping("/purchase")
    public ResponseEntity<ApiResponse<List<PolicyDTO.AddPurchasePolicyRequest>>> getPurchasePolicies(
            @PathVariable String storeId,
            @RequestParam String userId) {
        return ApiResponseBuilder.build(() -> 
            storePoliciesService.getPurchasePolicies(storeId, userId)
        );
    }
} 