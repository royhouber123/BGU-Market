package market.domain.store.Policies.Discounts.Conditions;

import java.util.Map;

import market.domain.store.IStoreProductsManager;
import market.dto.PolicyDTO;

import java.util.List;



public class CompositeCondition implements DiscountCondition {

    private final List<DiscountCondition> conditions;
    private final LogicOperator operator;

    public CompositeCondition(List<DiscountCondition> conditions, LogicOperator operator) {
        this.conditions = conditions;
        this.operator = operator;
    }

    @Override
    public boolean isSatisfied(Map<String, Integer> listings, IStoreProductsManager productManager) {
        return switch (operator) {
            case AND -> conditions.stream().allMatch(c -> c.isSatisfied(listings, productManager));
            case OR -> conditions.stream().anyMatch(c -> c.isSatisfied(listings, productManager));
            case XOR -> conditions.stream().filter(c -> c.isSatisfied(listings, productManager)).count() == 1;
        };
    }

    @Override
    public PolicyDTO.DiscountCondition toDTO() {
        List<PolicyDTO.DiscountCondition> subDtos = conditions.stream()
            .map(DiscountCondition::toDTO)
            .toList();

        return new PolicyDTO.DiscountCondition(
            "COMPOSITE",           // type
            Map.of(),              // no parameters for composite itself
            subDtos,               // subconditions recursively serialized
            operator.name()        // logic operator as string: AND, OR, XOR
        );
    }
}