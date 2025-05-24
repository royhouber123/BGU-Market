package market.controllers;

import market.application.StorePoliciesService;
import market.dto.PolicyDTO;
import utils.ApiResponse;
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
        ApiResponse<Boolean> response = storePoliciesService.addDiscount(storeId, userId, request);
        return ResponseEntity.ok(response);
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
        ApiResponse<Boolean> response = storePoliciesService.removeDiscount(storeId, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all discount policies for a store
     * GET /api/stores/{storeId}/policies/discounts
     */
    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<List<PolicyDTO.AddDiscountRequest>>> getDiscounts(
            @PathVariable String storeId,
            @RequestParam String userId) {
        ApiResponse<List<PolicyDTO.AddDiscountRequest>> response = storePoliciesService.getDiscounts(storeId, userId);
        return ResponseEntity.ok(response);
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
        ApiResponse<Boolean> response = storePoliciesService.addPurchasePolicy(storeId, userId, request);
        return ResponseEntity.ok(response);
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
        ApiResponse<Boolean> response = storePoliciesService.removePurchasePolicy(storeId, userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all purchase policies for a store
     * GET /api/stores/{storeId}/policies/purchase
     */
    @GetMapping("/purchase")
    public ResponseEntity<ApiResponse<List<PolicyDTO.AddPurchasePolicyRequest>>> getPurchasePolicies(
            @PathVariable String storeId,
            @RequestParam String userId) {
        ApiResponse<List<PolicyDTO.AddPurchasePolicyRequest>> response = storePoliciesService.getPurchasePolicies(storeId, userId);
        return ResponseEntity.ok(response);
    }
} 