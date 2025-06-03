package market.application;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import market.domain.store.IListingRepository;
import market.domain.store.Listing;
import utils.ApiResponse;

/**
 * Provides product-related search services across all stores.
 * Designed for use by end users to discover products.
 */
public class ProductService {

    private final IListingRepository listingRepository;

    public ProductService(IListingRepository listingRepository) {
        this.listingRepository = Objects.requireNonNull(listingRepository);
    }

    /**
     * Searches all listings by product name (case-insensitive, partial match).
     */
    public List<Listing> searchByProductName(String query) {
        if (query == null || query.isBlank())
            return Collections.emptyList();

        List<Listing> result = listingRepository.getAllListings().stream()
                .filter(l -> l.getProductName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Searches for listings by exact product ID.
     */
    public List<Listing> searchByProductId(String productId) {
        List<Listing> result = listingRepository.getListingsByProductId(productId);
        return result;
    }

    /**
     * Returns all listings from a specific store.
     */
    public List<Listing> getStoreListings(String storeId) {
        List<Listing> result = listingRepository.getListingsByStoreId(storeId);
        return result;
    }

    /**
     * Searches for listings in a given store by product name (case-insensitive, partial match).
     */
    public List<Listing> searchInStoreByName(String storeId, String query) {
        if (query == null || query.isBlank())
            return Collections.emptyList();

        List<Listing> result = listingRepository.getListingsByStoreId(storeId).stream()
                .filter(l -> l.getProductName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Returns all listings sorted by price ascending.
     */
    public List<Listing> getAllSortedByPrice() {
        List<Listing> result = listingRepository.getAllListings().stream()
                .sorted(Comparator.comparingDouble(Listing::getPrice))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Returns a specific listing by its ID.
     */
    public Listing getListing(String listingId) {
        Listing listing = listingRepository.getListingById(listingId);
        if (listing == null) {
            throw new IllegalArgumentException("Listing not found for ID: " + listingId);
        }
        return listing;
    }
}
