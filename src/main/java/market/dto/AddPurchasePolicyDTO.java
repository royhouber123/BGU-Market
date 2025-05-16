package market.dto;

public record AddPurchasePolicyDTO(
    String type,   // e.g., "MINITEMS", "MAXITEMS", "MINPRICE"
    int value      // threshold value: number of items or price
) {}