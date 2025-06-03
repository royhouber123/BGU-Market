package market.dto;

public class CartDTO {
    
    public record AddProductToCartRequest(
        String storeId,
        String productName,
        int quantity
    ) {}
    
    public record RemoveProductFromCartRequest(
        String storeId,
        String productName,
        int quantity
    ) {}
    
    public record AddProductToCartResponse(
        boolean success,
        String message
    ) {}
    
    public record RemoveProductFromCartResponse(
        boolean success,
        String message
    ) {}
    
    public record GetCartRequest(
        String userId
    ) {}
    
    public record CartItem(
        String storeId,
        String productName,
        int quantity,
        double price
    ) {}
    
    public record GetCartResponse(
        java.util.List<CartItem> items,
        double totalPrice,
        boolean success,
        String message
    ) {}
    
    public record ClearCartRequest(
        String userId
    ) {}
    
    public record ClearCartResponse(
        boolean success,
        String message
    ) {}
} 