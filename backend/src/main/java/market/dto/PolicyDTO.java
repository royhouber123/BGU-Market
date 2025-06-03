package market.dto;

import java.util.List;
import java.util.Map;

public class PolicyDTO {
    
    public record AddDiscountRequest(
        String type,
        String scope,
        String scopeId,
        double value,
        String couponCode,
        DiscountCondition condition,
        List<AddDiscountRequest> subDiscounts,
        String combinationType
    ) {}
    
    public record DiscountCondition(
        String type,                   // e.g., "BASKET_TOTAL_AT_LEAST", "PRODUCT_QUANTITY_AT_LEAST", "COMPOSITE"
        Map<String, Object> params,   // flexible key-value params
        List<DiscountCondition> subConditions, // used for composite
        String logic                  // AND, OR, XOR for composite
    ) {}
    
    public record AddPurchasePolicyRequest(
        String type,   // e.g., "MINITEMS", "MAXITEMS", "MINPRICE"
        int value      // threshold value: number of items or price
    ) {}
    
    public record AddDiscountResponse(
        String discountId,
        boolean success,
        String message
    ) {}
    
    public record AddPurchasePolicyResponse(
        String policyId,
        boolean success,
        String message
    ) {}
    
    public record RemoveDiscountRequest(
        String discountId
    ) {}
    
    public record RemovePurchasePolicyRequest(
        String policyId
    ) {}
    
    public record RemoveDiscountResponse(
        boolean success,
        String message
    ) {}
    
    public record RemovePurchasePolicyResponse(
        boolean success,
        String message
    ) {}
    
    public record GetDiscountsRequest(
        String storeId
    ) {}
    
    public record GetPoliciesRequest(
        String storeId
    ) {}
    
    public record DiscountInfo(
        String discountId,
        String type,
        String scope,
        String scopeId,
        double value,
        String couponCode
    ) {}
    
    public record PolicyInfo(
        String policyId,
        String type,
        int value
    ) {}
    
    public record GetDiscountsResponse(
        List<DiscountInfo> discounts,
        boolean success,
        String message
    ) {}
    
    public record GetPoliciesResponse(
        List<PolicyInfo> policies,
        boolean success,
        String message
    ) {}
} 