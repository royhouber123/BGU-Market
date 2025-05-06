package market.domain.store;

import java.util.UUID;

import market.domain.purchase.PurchaseType;




/**
 * Represents a single listing for a product in the store.
 * A listing defines how the product is sold (regular, auction, bid).
 */
public class Listing {
    private final String listingId;          // Unique ID for this listing
    private final String storeId;             // ID of the store that owns this listing
    private final String productId;           // ID of the product type
    private final String productName;         // Product display name
    private final String productDescription;  // Product description
    private int quantityAvailable;            // Stock for this listing
    private final PurchaseType purchaseType; // How it is purchased
    private double price;
    private String category = ""; //TODO: still need to add this to the constructor 
    private Boolean isClosed=false;


    /**
     * Constructs a new Listing with an auto-generated listing ID.
     *
     * @param storeId              ID of the store offering this listing.
     * @param productId            ID of the product.
     * @param productName          Product name.
     * @param productDescription   Product description.
     * @param quantityAvailable    Quantity available.
     * @param purchaseType     Purchase behavior (regular, auction, bid).
     */
    public Listing(String storeId, String productId, String productName, String productDescription, int quantityAvailable, PurchaseType purchaseType, double price) {
        this.listingId = UUID.randomUUID().toString();
        this.storeId = storeId;
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.quantityAvailable = quantityAvailable;
        this.purchaseType = purchaseType;
        this.price = price;
    }

    /**
     * Attempts to purchase a quantity from this listing.
     *
     * @param quantityRequested Number of units to buy.
     * @return Result of purchase attempt.
     * @throws Exception if not enough stock or invalid purchase.
     */
    public boolean purchase(int quantityRequested) throws Exception {
        if (quantityRequested > quantityAvailable) {
            throw new Exception("Not enough stock available for listing " + listingId);
        }
        quantityAvailable -= quantityRequested;
        return true;
    }

    // ==================== Getters ====================

    public String getListingId() {
        return listingId;
    }

    public double getPrice() {
        return price;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public PurchaseType getPurchaseType() {
        return purchaseType;
    }

    public String getCategory() {
        return category;
    }


    public void closeStore(){
        this.isClosed=true;
    }

    public void openStore(){
        this.isClosed=false;
    }
    

}
