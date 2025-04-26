package market.domain.store;

import java.util.List;

/**
 * Interface for managing listings in a store.
 * This interface defines operations for adding, removing, updating, purchasing, and querying listings.
 */
public interface IStoreProductsManager {

    /**
     * Adds a new listing to the store's catalog.
     *
     * @param listing The listing to add.
     * @return {@code true} if the listing was added successfully; {@code false} otherwise.
     */
    boolean addListing(Listing listing);

    /**
     * Removes a listing from the store based on its ID.
     *
     * @param listingId ID of the listing to remove.
     * @return {@code true} if the listing was successfully removed; {@code false} otherwise.
     */
    boolean removeListing(String listingId);

    /**
     * Retrieves a listing by its ID.
     *
     * @param listingId ID of the listing.
     * @return The {@link Listing} if found; {@code null} otherwise.
     */
    Listing getListingById(String listingId);

    /**
     * Retrieves all listings associated with a specific product name.
     *
     * @param productName Name of the product.
     * @return A list of {@link Listing} matching the product name.
     */
    List<Listing> getListingsByProductName(String productName);

    /**
     * Retrieves all listings associated with a specific product ID.
     *
     * @param productId ID of the product.
     * @return A list of {@link Listing} matching the product ID.
     */
    List<Listing> getListingsByProductId(String productId);

    /**
     * Retrieves all listings belonging to this store.
     *
     * @return A list of all {@link Listing} currently managed by the store.
     */
    List<Listing> getAllListings();

    /**
     * Purchases a quantity of a product from a specific listing.
     *
     * @param listingId ID of the listing to purchase from.
     * @param quantity  Number of units to purchase.
     * @return The result of the purchase attempt.
     * @throws Exception If the listing does not exist or the purchase is invalid.
     */
    boolean purchaseFromListing(String listingId, int quantity) throws Exception;
}
