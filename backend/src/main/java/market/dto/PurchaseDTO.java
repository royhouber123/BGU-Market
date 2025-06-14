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
        String productId,
        double offerAmount,
        String shippingAddress,
        String paymentDetails
    ) {}
    
    public record OpenAuctionRequest(
        int storeId,
        String productId,
        String productName,
        String productCategory,
        String productDescription,
        int startingPrice,
        long endTimeMillis
    ) {}
    
    public record AuctionStatusResponse(
        boolean isActive,
        double currentHighestBid,
        String status,
        LocalDateTime endTime
    ) {}
    
    // Bid Operations
    public record BidSubmissionRequest(
        int storeId,
        String productId,
        double bidAmount,
        int quantity,
        String shippingAddress,
        String paymentDetails
    ) {}
    
    public record BidApprovalRequest(
        int storeId,
        String productId,
        String bidderUsername,
        boolean approved
    ) {}
    
    public record CounterBidRequest(
        int storeId,
        String productId,
        String bidderUsername,
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
        String productId,
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