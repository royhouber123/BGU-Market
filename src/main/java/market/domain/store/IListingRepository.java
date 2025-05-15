package market.domain.store;

import java.util.List;
import java.util.Map;

/**
 * Repository interface for managing and querying {@link Listing} entities.
 * This abstraction allows for flexible implementation of listing-related logic,
 * such as search, indexing, or database-backed storage.
 */
public interface IListingRepository {

    /**
     * Retrieves a listing by its unique ID.
     *
     * @param listingId The unique identifier of the listing.
     * @return The {@link Listing} object if found; {@code null} otherwise.
     */
    Listing getListingById(String listingId);

    /**
     * Retrieves all listings for the given store.
     *
     * @param storeId The ID of the store.
     * @return A list of listings belonging to the store.
     */
    List<Listing> getListingsByStoreId(String storeId);

    /**
     * Retrieves all listings for the given product ID.
     *
     * @param productId The ID of the product.
     * @return A list of listings that offer the given product.
     */
    List<Listing> getListingsByProductId(String productId);

    /**
     * Retrieves all listings with a matching product name.
     *
     * @param productName The name of the product.
     * @return A list of listings that contain the product name.
     */
    List<Listing> getListingsByProductName(String productName);

    /**
     * Returns all listings in the repository.
     *
     * @return A list of all listings.
     */
    List<Listing> getAllListings();

    /**
     * Adds a new listing to the repository.
     *
     * @param listing The listing to add.
     * @return The listing ID of the newly added listing.
     * @throws IllegalArgumentException if a listing with the same ID already exists.
     */
    String addListing(Listing listing);

    /**
     * Removes a listing by its ID.
     *
     * @param listingId The ID of the listing to remove.
     * @return {@code true} if the listing was removed successfully; {@code false} otherwise.
     */
    boolean removeListing(String listingId);
    // Added methods for scoped queries:
    List<Listing> getListingsByProductIdAndStore(String productId, String storeId);
    List<Listing> getListingsByProductNameAndStore(String productName, String storeId);
    boolean updateStockForPurchasedItems(Map<String, Map<String, Integer>> listForUpdateStock);

    List<Listing> getListingsByCategory(String category);
    List<Listing> getListingsByCategoryAndStore(String category, String storeId);
    void disableListingsByStoreId(String storeId);
    void enableListingsByStoreId(String storeId);
    double calculateStoreBagWithoutDiscount(Map<String, Integer> prodsToQuantity) throws Exception;
    double ProductPrice(String listingId) throws Exception;
    boolean editPriceForListing(String listingId, Double newPrice)throws Exception;
    void editProductName(String listingId, String newName) throws Exception;
    void editProductDescription(String listingId, String newDescription) throws Exception;
    void editProductQuantity(String listingId, int newQuantity) throws Exception;
    void editProductCategory(String listingId, String newCategory) throws Exception;

    
}
