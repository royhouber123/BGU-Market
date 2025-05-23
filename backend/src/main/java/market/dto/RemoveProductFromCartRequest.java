package market.dto;

public record RemoveProductFromCartRequest(
    String storeId,
    String productName,
    int quantity
) {} 