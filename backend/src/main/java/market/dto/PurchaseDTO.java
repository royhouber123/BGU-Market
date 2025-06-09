package market.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for Purchase-related API operations
 */
public class PurchaseDTO {
    
    // Regular Purchase
    public record ExecutePurchaseRequest(
        String paymentDetails,
        String shippingAddress,
        String currency,
        String cardNumber,
        String month,
        String year,
        String holder,
        String ccv
    ) {}
    
    // Auction Operations
    public record AuctionOfferRequest(
        int storeId,
        String productId,
        double offerAmount,
        String shippingAddress,
        String paymentDetails,
        String currency,
        String cardNumber,
        String month,
        String year,
        String holder,
        String ccv
    ) {}
    
    public record OpenAuctionRequest(
        int storeId,
        String productId,
        String productName,
        String productCategory,
        String productDescription,
        double startingPrice,
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
        String shippingAddress,
        String paymentDetails,
        String currency,
        String cardNumber,
        String month,
        String year,
        String holder,
        String ccv
    ) {}
    
    public record BidApprovalRequest(
        int storeId,
        String productId,
        String bidderUsername
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