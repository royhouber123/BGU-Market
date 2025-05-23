package market.dto;

public record AddProductToCartRequest(
    String storeId,
    String productName,
    int quantity
) {} 