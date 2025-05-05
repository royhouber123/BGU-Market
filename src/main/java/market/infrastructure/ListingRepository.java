package market.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import market.domain.store.IListingRepository;
import market.domain.store.Listing;

/**
 * In-memory implementation of the {@link IListingRepository} interface.
 * Stores listings in maps grouped by listing ID, store ID, product ID, and product name.
 */
public class ListingRepository implements IListingRepository {

   private final Map<String, Listing> listingsById = new HashMap<>();

    @Override
    public String addListing(Listing listing) {
        listingsById.put(listing.getListingId(), listing);
        return listing.getListingId();
    }

    @Override
    public boolean removeListing(String listingId) {
        return listingsById.remove(listingId) != null;
    }

    @Override
    public Listing getListingById(String listingId) {
        return listingsById.get(listingId);
    }

    @Override
    public List<Listing> getListingsByProductId(String productId) {
        return listingsById.values().stream()
                .filter(l -> l.getProductId().equals(productId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> getListingsByProductName(String productName) {
        return listingsById.values().stream()
                .filter(l -> l.getProductName().equals(productName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> getAllListings() {
        return new ArrayList<>(listingsById.values());
    }

    @Override
    public List<Listing> getListingsByProductIdAndStore(String productId, String storeId) {
        return listingsById.values().stream()
                .filter(l -> l.getProductId().equals(productId) && l.getStoreId().equals(storeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> getListingsByProductNameAndStore(String productName, String storeId) {
        return listingsById.values().stream()
                .filter(l -> l.getProductName().equals(productName) && l.getStoreId().equals(storeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> getListingsByStoreId(String storeId) {
        return listingsById.values().stream()
                .filter(l -> l.getStoreId().equals(storeId))
                .collect(Collectors.toList());
    }
}

