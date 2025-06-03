package market.dto;

public class ProductDTO {
    
    public record AddListingRequest(
        String userName,
        String storeID,
        String productId,
        String productName,
        String productCategory,
        String productDescription,
        int quantity,
        double price,
        String purchaseType
    ) {}
    
    public record EditListingRequest(
        String userName,
        String storeID,
        String listingId,
        String newValue
    ) {}
    
    public record EditListingPriceRequest(
        String userName,
        String storeID,
        String listingId,
        double newPrice
    ) {}
    
    public record EditListingQuantityRequest(
        String userName,
        String storeID,
        String listingId,
        int newQuantity
    ) {}
    
    public record AddListingResponse(
        String productId,
        boolean success,
        String message
    ) {}
    
    public record EditListingResponse(
        boolean success,
        String message
    ) {}
    
    public record ProductInfoRequest(
        String productId
    ) {}
    
    public record ProductInfoResponse(
        String productId,
        String productName,
        String productCategory,
        String productDescription,
        int quantity,
        double price,
        boolean success,
        String message
    ) {}
} 