package market.infrastructure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import market.domain.store.IListingRepository;
import market.domain.store.Listing;

/**
 * In-memory implementation of the {@link IListingRepository} interface.
 * Stores listings in maps grouped by listing ID, store ID, product ID, and product name.
 */
public class ListingRepository implements IListingRepository {

   private final Map<String, Listing> listingsById = new ConcurrentHashMap<>();
;

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

    @Override
    
    public boolean updateStockForPurchasedItems(Map<String, Map<String, Integer>> listForUpdateStock) {
        if (listForUpdateStock == null || listForUpdateStock.isEmpty()) {
            return false;
        }

        // Collect listings and sort by ID to avoid deadlocks
        List<Listing> toLock = new ArrayList<>();
        for (String storeId : listForUpdateStock.keySet()) {
            Map<String, Integer> listingUpdates = listForUpdateStock.get(storeId);
            if (listingUpdates == null) throw new IllegalArgumentException("Missing listings for store " + storeId);

            for (String listingId : listingUpdates.keySet()) {
                Listing l = getListingById(listingId);
                if (l == null || !l.getStoreId().equals(storeId))
                    throw new IllegalArgumentException("Invalid listing: " + listingId + " for store: " + storeId);
                toLock.add(l);
            }
        }

        toLock.sort(Comparator.comparing(Listing::getListingId));

        // Lock all listings in sorted order
        List<Object> locks = toLock.stream().map(l -> (Object) l).distinct().toList();
        synchronizedLocks(locks, () -> {
            // Check stock availability
            for (Listing listing : toLock) {
                int requested = listForUpdateStock.get(listing.getStoreId()).get(listing.getListingId());
                if (listing.getQuantityAvailable() < requested)
                    throw new RuntimeException("Not enough stock for listing: " + listing.getListingId());
            }

            // Perform all purchases
            for (Listing listing : toLock) {
                int quantity = listForUpdateStock.get(listing.getStoreId()).get(listing.getListingId());
                try {
                    listing.purchase(quantity);
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected error during purchase of " + listing.getListingId(), e);
                }
            }
        });

        return true;
    }

    // Utility method to synchronize on multiple objects
    private void synchronizedLocks(List<Object> locks, Runnable criticalSection) {
        synchronizedRecursive(locks, 0, criticalSection);
    }

    private void synchronizedRecursive(List<Object> locks, int index, Runnable criticalSection) {
        if (index == locks.size()) {
            criticalSection.run();
            return;
        }
        synchronized (locks.get(index)) {
            synchronizedRecursive(locks, index + 1, criticalSection);
        }
    }


    @Override
    public List<Listing> getListingsByCategory(String category) {
        return listingsById.values().stream()
                .filter(l -> l.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<Listing> getListingsByCategoryAndStore(String category, String storeId) {
        return listingsById.values().stream()
                .filter(l -> l.getCategory().equalsIgnoreCase(category) && l.getStoreId().equals(storeId))
                .collect(Collectors.toList());
    }


    @Override
    public void disableListingsByStoreId(String storeId) {
        listingsById.values().stream()
            .filter(l -> l.getStoreId().equals(storeId))
            .forEach(l -> {
                synchronized (l) {
                    l.disable();
                }
            });
    }
    
    @Override
    public void enableListingsByStoreId(String storeId) {
        listingsById.values().stream()
            .filter(l -> l.getStoreId().equals(storeId))
            .forEach(l -> {
                synchronized (l) {
                    l.enable();
                }
            });
    }

    

    @Override
    public double calculateStoreBagWithoutDiscount(Map<String, Integer> prodsToQuantity) throws Exception {
        double result = 0.0;
        for (Map.Entry<String, Integer> entry : prodsToQuantity.entrySet()) {
            String listingId = entry.getKey();
            int quantity = entry.getValue();

            Listing l = getListingById(listingId);
            if (l == null || !Boolean.TRUE.equals(l.isActive())) {
                throw new Exception("Listing " + listingId + " not found or inactive.");
            }
            result += l.getPrice() * quantity;
        }
        return result;
    }

    @Override
    public double ProductPrice(String listingId) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive())) {
            throw new Exception("Listing " + listingId + " not found or inactive.");
        }
        return l.getPrice();
    }

    
    @Override
    public boolean editPriceForListing(String listingId, Double newPrice) throws Exception {
        if (newPrice < 0)
            throw new IllegalArgumentException("Illegal price for listing: " + listingId);

        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive())) 
            throw new Exception("Listing " + listingId + " not found or inactive.");

        synchronized (l) {
            l.setPrice(newPrice);
        }
        return true;
    }

     @Override
    public void editProductName(String listingId, String newName) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive()))
            throw new Exception("Listing " + listingId + " not found or inactive.");
        synchronized (l) {
            l.setProductName(newName);
        }
    }

    @Override
    public void editProductDescription(String listingId, String newDescription) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive()))
            throw new Exception("Listing " + listingId + " not found or inactive.");
        synchronized (l) {
            l.setProductDescription(newDescription);
        }
    }

    @Override
    public void editProductQuantity(String listingId, int newQuantity) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive()))
            throw new Exception("Listing " + listingId + " not found or inactive.");
        synchronized (l) {
            l.setQuantityAvailable(newQuantity);
        }
    }

    @Override
    public void editProductCategory(String listingId, String newCategory) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive()))
            throw new Exception("Listing " + listingId + " not found or inactive.");
        synchronized (l) {
            l.setCategory(newCategory);
        }
    }


        

}

