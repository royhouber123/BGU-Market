package market.domain.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
need to implement!!!!!!!!!!!!!
 */

/**
 * Manages the collection of listings (products for sale) for a specific store.
 */
public class StoreProductManager implements IStoreProductsManager {

    private final String storeId; // ID of the store this manager belongs to
    private final Map<String, Listing> listingsById;
    private final Map<String, List<Listing>> listingsByProductId;
    private final Map<String, List<Listing>> listingsByProductName;

    public StoreProductManager(String storeId) {
        this.storeId = storeId;
        this.listingsById = new HashMap<>();
        this.listingsByProductId = new HashMap<>();
        this.listingsByProductName = new HashMap<>();
    }


    public String getStoreId() {
            return storeId;
        }
    @Override
    public boolean addListing(Listing listing) {

        if (!listing.getStoreId().equals(this.storeId)) {
            throw new IllegalArgumentException("Listing storeId does not match StoreProductManager storeId!");
        }

        if (listingsById.containsKey(listing.getListingId())) {
            return false; // Already exists
        }

        listingsById.put(listing.getListingId(), listing);

        listingsByProductId.computeIfAbsent(listing.getProductId(), k -> new ArrayList<>()).add(listing);
        listingsByProductName.computeIfAbsent(listing.getProductName(), k -> new ArrayList<>()).add(listing);

        return true;
    }

    @Override
    public boolean removeListing(String listingId) {
        Listing listing = listingsById.remove(listingId);
        if (listing == null) {
            return false; // Nothing to remove
        }

        List<Listing> byProductId = listingsByProductId.get(listing.getProductId());
        if (byProductId != null) {
            byProductId.remove(listing);
            if (byProductId.isEmpty()) {
                listingsByProductId.remove(listing.getProductId());
            }
        }

        List<Listing> byProductName = listingsByProductName.get(listing.getProductName());
        if (byProductName != null) {
            byProductName.remove(listing);
            if (byProductName.isEmpty()) {
                listingsByProductName.remove(listing.getProductName());
            }
        }

        return true;
    }



    @Override
    public Listing getListingById(String listingId) {
        return listingsById.get(listingId);
    }

    @Override
    public List<Listing> getListingsByProductName(String productName) {
        return listingsByProductName.getOrDefault(productName, Collections.emptyList());
    }

    @Override
    public List<Listing> getListingsByProductId(String productId) {
        return listingsByProductId.getOrDefault(productId, Collections.emptyList());
    }

    @Override
    public List<Listing> getAllListings() {
        return new ArrayList<>(listingsById.values());
    }

    @Override
    public boolean purchaseFromListing(String listingId, int quantity) throws Exception {
        Listing listing = listingsById.get(listingId);
        if (listing == null) {
            throw new Exception("Listing with ID " + listingId + " does not exist!");
        }
        return listing.purchase(quantity);
    }
}
