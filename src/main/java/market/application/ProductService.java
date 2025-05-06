package market.application;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import market.domain.store.IListingRepository;
import market.domain.store.Listing;

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
        if (query == null || query.isBlank()) return Collections.emptyList();
        return listingRepository.getAllListings().stream()
                .filter(l -> l.getProductName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Searches for listings by exact product ID.
     */
    public List<Listing> searchByProductId(String productId) {
        return listingRepository.getListingsByProductId(productId);
    }

    /**
     * Returns all listings from a specific store.
     */
    public List<Listing> getStoreListings(String storeId) {
        return listingRepository.getListingsByStoreId(storeId);
    }

    /**
     * Searches for listings in a given store by product name (case-insensitive, partial match).
     */
    public List<Listing> searchInStoreByName(String storeId, String query) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        return listingRepository.getListingsByProductNameAndStore(query, storeId).stream()
                .filter(l -> l.getProductName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all listings sorted by price ascending.
     */
    public List<Listing> getAllSortedByPrice() {
        return listingRepository.getAllListings().stream()
                .sorted(Comparator.comparingDouble(Listing::getPrice))
                .collect(Collectors.toList());
    }

    /**
     * Returns a specific listing by its ID.
     */
    public Listing getListing(String listingId) {
        return listingRepository.getListingById(listingId);
    }
}
