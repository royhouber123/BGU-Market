package market.dto;

public class StoreDTO {
    
    public record CreateStoreRequest(
        String storeName,
        String founderId
    ) {}
    
    public record AddManagerRequest(
        String appointerID,
        String newManagerName,
        String storeID
    ) {}
    
    public record AddOwnerRequest(
        String appointerID,
        String newOwnerID,
        String storeID
    ) {}
    
    public record AddPermissionRequest(
        String managerID,
        String appointerID,
        int permissionID,
        String storeID
    ) {}
    
    public record CreateStoreResponse(
        String storeId,
        String storeName,
        boolean success,
        String message
    ) {}
    
    public record AddManagerResponse(
        boolean success,
        String message
    ) {}
    
    public record AddOwnerResponse(
        boolean success,
        String message
    ) {}
    
    public record AddPermissionResponse(
        boolean success,
        String message
    ) {}
    
    public record StoreInfoRequest(
        String storeId
    ) {}
    
    public record StoreInfoResponse(
        String storeId,
        String storeName,
        String founderId,
        boolean success,
        String message
    ) {}
} 