package market.dto;

import java.util.List;
import java.util.Map;

public record DiscountConditionDTO(
    String type,                   // e.g., "BASKET_TOTAL_AT_LEAST", "PRODUCT_QUANTITY_AT_LEAST", "COMPOSITE"
    Map<String, Object> params,   // flexible key-value params
    List<DiscountConditionDTO> subConditions, // used for composite
    String logic                  // AND, OR, XOR for composite
) {}
