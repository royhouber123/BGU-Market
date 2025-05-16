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
    public ApiResponse<List<Listing>> searchByProductName(String query) {
        try {
            if (query == null || query.isBlank())
                return ApiResponse.ok(Collections.emptyList());

            List<Listing> result = listingRepository.getAllListings().stream()
                    .filter(l -> l.getProductName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());

            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to search by product name: " + e.getMessage());
        }
    }

    /**
     * Searches for listings by exact product ID.
     */
    public ApiResponse<List<Listing>> searchByProductId(String productId) {
        try {
            List<Listing> result = listingRepository.getListingsByProductId(productId);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to search by product ID: " + e.getMessage());
        }
    }

    /**
     * Returns all listings from a specific store.
     */
    public ApiResponse<List<Listing>> getStoreListings(String storeId) {
        try {
            List<Listing> result = listingRepository.getListingsByStoreId(storeId);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to get store listings: " + e.getMessage());
        }
    }

    /**
     * Searches for listings in a given store by product name (case-insensitive, partial match).
     */
    public ApiResponse<List<Listing>> searchInStoreByName(String storeId, String query) {
        try {
            if (query == null || query.isBlank())
                return ApiResponse.ok(Collections.emptyList());

            List<Listing> result = listingRepository.getListingsByStoreId(storeId).stream()
                    .filter(l -> l.getProductName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());

            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to search in store by name: " + e.getMessage());
        }
    }

    /**
     * Returns all listings sorted by price ascending.
     */
    public ApiResponse<List<Listing>> getAllSortedByPrice() {
        try {
            List<Listing> result = listingRepository.getAllListings().stream()
                    .sorted(Comparator.comparingDouble(Listing::getPrice))
                    .collect(Collectors.toList());

            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to sort listings by price: " + e.getMessage());
        }
    }

    /**
     * Returns a specific listing by its ID.
     */
    public ApiResponse<Listing> getListing(String listingId) {
        try {
            Listing listing = listingRepository.getListingById(listingId);
            if (listing == null) {
                return ApiResponse.fail("Listing not found for ID: " + listingId);
            }
            return ApiResponse.ok(listing);
        } catch (Exception e) {
            return ApiResponse.fail("Failed to get listing: " + e.getMessage());
        }
    }
}
