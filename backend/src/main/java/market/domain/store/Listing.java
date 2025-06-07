package market.domain.store;

import java.beans.ConstructorProperties;
import java.util.UUID;

import org.springframework.stereotype.Controller;

import jakarta.persistence.*;
import lombok.*;
import market.domain.purchase.PurchaseType;




/**
 * Represents a single listing for a product in the store.
 * A listing defines how the product is sold (regular, auction, bid).
 */
@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "listings")
public class Listing {
    @Id
    @Column(name = "listing_id", nullable = false, unique = true)
    private  String listingId;          // Unique ID for this listing
    
    @Column(name = "store_id", nullable = false)
    private String storeId;             // ID of the store that owns this listing
    
    @Column(name = "product_id", nullable = false)
    private String productId;           // ID of the product type
    
    @Column(name = "product_name", nullable = false)
    private  String productName;         // Product display name
    
    @Column(name = "product_description", nullable = true)
    private  String productDescription;  // Product description
    
    @Column(name = "quantity_available", nullable = false)
    private int quantityAvailable;            // Stock for this listing
    
    @Column(name = "purchase_type", nullable = false)
    @Enumerated(EnumType.STRING) 
    private PurchaseType purchaseType; // How it is purchased
    
    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "category", nullable = true)
    private String category = "";

    @Column(name = "active", nullable = false)
    private Boolean active;


    /**
     * Constructs a new Listing with an auto-generated listing ID.
     *
     * @param storeId              ID of the store offering this listing.
     * @param productId            ID of the product.
     * @param productName          Product name.
     * @param productCategory      Product category.
     * @param productDescription   Product description.
     * @param quantityAvailable    Quantity available.
     * @param purchaseType     Purchase behavior (regular, auction, bid).
     */
    public Listing(String storeId, String productId, String productName, String productCategory, String productDescription, int quantityAvailable, PurchaseType purchaseType, double price) {
        this.listingId = UUID.randomUUID().toString();
        this.storeId = storeId;
        this.productId = productId;
        this.productName = productName;
        this.category = productCategory;
        this.productDescription = productDescription;
        this.quantityAvailable = quantityAvailable;
        this.purchaseType = purchaseType;
        if (price<0 )
            throw new IllegalArgumentException("the price of a products needs to be possitive");
        this.price = price;
        this.active= true;
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

        /**
     * Attempts to restore a quantity to this listing.
     *
     * @param quantityRequested Number of units to restore.
     * @return Result of restore attempt.
     * @throws Exception if the quantity requested is negative.
     */
    public boolean restore(int quantityRequested) throws Exception {
        if (quantityRequested < 0) {
            throw new Exception("Cannot restore a negative quantity for listing " + listingId);
        }
        quantityAvailable += quantityRequested;
        return true;
    }

    // ==================== Getters ====================
    /**
     * Getters for the listing properties.
     */
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


    public void disable(){
        this.active=false;
    }

    public void enable(){
        this.active=true;
    }

    public boolean isActive(){
        return active;
    }

    public void setPrice(double newPrice) {
    if (newPrice < 0) {
        throw new IllegalArgumentException("Price must be non-negative");
    }
    this.price = newPrice;
}

    public void setProductName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        this.productName = newName;
    }

    public void setProductDescription(String newDescription) {
        this.productDescription = (newDescription != null) ? newDescription : "";
    }

    public void setQuantityAvailable(int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        this.quantityAvailable = newQuantity;
    }

    public void setCategory(String newCategory) {
        this.category = (newCategory != null) ? newCategory : "";
    }


    
    

}
