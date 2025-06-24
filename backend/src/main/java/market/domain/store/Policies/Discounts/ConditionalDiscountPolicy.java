package market.domain.store.Policies.Discounts;

import java.util.Map;
import java.util.Objects;

import market.domain.store.IStoreProductsManager;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.DiscountPolicyEntity;
import market.domain.store.Policies.Discounts.Conditions.DiscountCondition;
import market.dto.PolicyDTO;

@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "discount_conditional")
public class ConditionalDiscountPolicy extends DiscountPolicyEntity {

    /**
     * The condition that must be satisfied for the discount to apply. We persist it as a
     * JSON string because DiscountCondition is an interface with multiple implementations.
     */
    @jakarta.persistence.Transient
    private DiscountCondition condition;

    @jakarta.persistence.Lob
    @jakarta.persistence.Column(name = "condition_json", columnDefinition = "TEXT")
    private String conditionJson;

    /**
     * The wrapped discount that is applied when the condition is satisfied.
     */
    @jakarta.persistence.OneToOne(cascade = jakarta.persistence.CascadeType.ALL, targetEntity = DiscountPolicyEntity.class)
    @jakarta.persistence.JoinColumn(name = "inner_discount_id")
    private DiscountPolicyEntity discount;

    protected ConditionalDiscountPolicy() { /* JPA */ }

    public ConditionalDiscountPolicy(DiscountCondition condition, DiscountPolicy discount) {
        if (!(discount instanceof DiscountPolicyEntity entity)) {
            throw new IllegalArgumentException("Inner discount must be persistable (extend DiscountPolicyEntity)");
        }
        this.condition = condition;
        this.discount = (DiscountPolicyEntity) discount;
        serializeCondition();
    }

    @Override
    public double calculateDiscount(Map<String, Integer> listings, IStoreProductsManager productManager) {
        if (condition != null && condition.isSatisfied(listings, productManager)) {
            return discount.calculateDiscount(listings, productManager);
        }
        return 0.0;
    }

    public PolicyDTO.AddDiscountRequest toDTO() {
        PolicyDTO.AddDiscountRequest innerDto = discount.toDTO();

        return new PolicyDTO.AddDiscountRequest(
            "CONDITIONAL",         // type
            innerDto.scope(),      // scope from inner discount
            innerDto.scopeId(),    // scopeId from inner discount
            innerDto.value(),      // discount value
            innerDto.couponCode(), // coupon code (if any)
            condition.toDTO(),     // the condition DTO for this conditional discount
            null,                  // subDiscounts — not applicable for conditional
            null                   // combinationType — not applicable for conditional
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Same object reference
        if (obj == null || getClass() != obj.getClass()) return false; // Different class or null object
        ConditionalDiscountPolicy that = (ConditionalDiscountPolicy) obj;
        return condition.equals(that.condition) && discount.equals(that.discount); // Compare fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, discount); // Hash based on fields
    }

    /* ----------- JSON (de)serialization hooks ----------- */

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    private void serializeCondition() {
        if (condition != null) {
            try {
                this.conditionJson = new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(condition.toDTO());
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize DiscountCondition", e);
            }
        }
    }

    @jakarta.persistence.PostLoad
    private void deserializeCondition() {
        if (conditionJson != null && (condition == null)) {
            try {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                market.dto.PolicyDTO.DiscountCondition dto = mapper.readValue(conditionJson, market.dto.PolicyDTO.DiscountCondition.class);
                this.condition = market.domain.store.Policies.Discounts.Conditions.ConditionFactory.fromDTO(dto);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize DiscountCondition", e);
            }
        }
    }
}