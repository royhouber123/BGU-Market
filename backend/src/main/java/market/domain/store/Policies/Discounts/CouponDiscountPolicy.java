package market.domain.store.Policies.Discounts;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.DiscountPolicyEntity;
import market.dto.PolicyDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Map;
import java.util.Objects;

@Entity
@Table(name="discount_coupon")
public class CouponDiscountPolicy extends DiscountPolicyEntity {

    private final String validCouponCode;
    private final double discountAmount; // in $ (fixed discount)
    private boolean couponUsed; // track if user provided a coupon (simulate)

    protected CouponDiscountPolicy() {
        // Default constructor for JPA
        this.validCouponCode = null;
        this.discountAmount = 0.0;
        this.couponUsed = false;
    }

    public CouponDiscountPolicy(String validCouponCode, double discountAmount) {
        if (discountAmount <= 0) {
            throw new IllegalArgumentException("Discount amount must be positive.");
        }
        this.validCouponCode = validCouponCode;
        this.discountAmount = discountAmount;
        this.couponUsed = false;
    }

    // Call this method to simulate user submitting a coupon
    public void submitCoupon(String couponCode) {
        if (couponCode != null && couponCode.equals(validCouponCode)) {
            couponUsed = true;
        }
    }

    @Override
    public double calculateDiscount(Map<String, Integer> listings,IStoreProductsManager productManager) {
        if (couponUsed) {
            return discountAmount;
        }
        return 0.0;
    }

    @Override
    public PolicyDTO.AddDiscountRequest toDTO() {
        return new PolicyDTO.AddDiscountRequest(
            "COUPON",           // type
            null,               // scope (not applicable)
            null,               // scopeId
            discountAmount,     // value (fixed amount)
            validCouponCode,    // couponCode
            null,               // condition (not applicable)
            null,               // subDiscounts (not applicable)
            null                // combinationType (not applicable)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Same object reference
        if (obj == null || getClass() != obj.getClass()) return false; // Different class or null object
        CouponDiscountPolicy that = (CouponDiscountPolicy) obj;
        return Double.compare(that.discountAmount, discountAmount) == 0 && // Compare discountAmount values
               couponUsed == that.couponUsed && // Compare couponUsed (boolean, so uses ==)
               validCouponCode.equals(that.validCouponCode); // Compare validCouponCode (String, uses equals)
    }

    @Override
    public int hashCode() {
        return Objects.hash(validCouponCode, discountAmount, couponUsed); // Use Objects.hash() for simplicity
    }
}