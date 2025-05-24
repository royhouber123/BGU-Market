package market.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for Purchase-related API operations
 */
public class PurchaseDTO {
    
    // Regular Purchase
    public record ExecutePurchaseRequest(
        int userId,
        String paymentDetails,
        String shippingAddress
    ) {}
    
    // Auction Operations
    public record AuctionOfferRequest(
        int userId,
        int storeId,
        int productId,
        double offerAmount
    ) {}
    
    public record OpenAuctionRequest(
        int storeId,
        int productId,
        double startingPrice,
        LocalDateTime endTime
    ) {}
    
    public record AuctionStatusResponse(
        boolean isActive,
        double currentHighestBid,
        String status,
        LocalDateTime endTime
    ) {}
    
    // Bid Operations
    public record BidSubmissionRequest(
        int userId,
        int storeId,
        int productId,
        double bidAmount,
        int quantity
    ) {}
    
    public record BidApprovalRequest(
        int storeId,
        int productId,
        int userId,
        boolean approved
    ) {}
    
    public record CounterBidRequest(
        int storeId,
        int productId,
        int userId,
        double counterAmount
    ) {}
    
    public record BidStatusResponse(
        String status,
        double currentBidAmount,
        double counterOffer,
        boolean isApproved
    ) {}
    
    // Purchase History
    public record PurchaseHistoryItem(
        int purchaseId,
        int storeId,
        String storeName,
        List<PurchasedProductItem> products,
        double totalAmount,
        LocalDateTime purchaseDate,
        String status
    ) {}
    
    public record PurchasedProductItem(
        int productId,
        String productName,
        int quantity,
        double pricePerUnit,
        double totalPrice
    ) {}
    
    public record PurchaseHistoryResponse(
        List<PurchaseHistoryItem> purchases,
        int totalPurchases,
        double totalSpent
    ) {}
} 