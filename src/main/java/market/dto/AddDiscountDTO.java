package market.dto;

import java.util.List;

public record AddDiscountDTO(
    String type,
    String scope,
    String scopeId,
    double value,
    String couponCode,
    DiscountConditionDTO condition,
    List<AddDiscountDTO> subDiscounts,
    String combinationType
) {}
