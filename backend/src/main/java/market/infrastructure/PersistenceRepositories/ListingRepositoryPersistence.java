package market.infrastructure.PersistenceRepositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import market.domain.store.IListingRepository;
import market.domain.store.Listing;
import market.infrastructure.IJpaRepository.IListingJpaRepository;


@Repository
@Transactional
public class ListingRepositoryPersistence implements IListingRepository {

    @Autowired
    private IListingJpaRepository listingJpaRepository;
    
   

    @Override
    public String addListing(Listing listing) {
        Listing savedListing = listingJpaRepository.save(listing);
        return savedListing.getListingId();
    }

    @Override
    public boolean removeListing(String listingId) {
        Optional<Listing> listing = listingJpaRepository.findById(listingId);
        if (listing.isPresent()) {
            listingJpaRepository.delete(listing.get());
            return true;
        }
        return false;
    }

    @Override
    public Listing getListingById(String listingId) {
        return listingJpaRepository.findById(listingId).orElse(null);
    }


   
   


    @Override
    public List<Listing> getListingsByProductId(String productId) {
        return listingJpaRepository.findByProductId(productId);
    }

    @Override
    public List<Listing> getListingsByProductName(String productName) {
        return listingJpaRepository.findByProductName(productName);
    }   

    @Override
    public List<Listing> getAllListings() {
        List<Listing> listings = listingJpaRepository.findAll();
        return listings.stream()
                .sorted(Comparator.comparing(Listing::getProductName))
                .collect(Collectors.toList());
    }   

    @Override
    public List<Listing> getListingsByProductIdAndStore(String productId, String storeId) {
        return listingJpaRepository.findByStoreIdAndProductId(storeId, productId);
    }

    @Override
    public List<Listing> getListingsByProductNameAndStore(String productName, String storeId) {
        return listingJpaRepository.findByStoreIdAndProductName(storeId, productName);
    }

    @Override
    public List<Listing> getListingsByStoreId(String storeId) {
        return listingJpaRepository.findByStoreId(storeId);
    }

    @Override
    public List<Listing> getListingsByCategory(String category) {
        return listingJpaRepository.findByCategory(category);
    }

    @Override
    public List<Listing> getListingsByCategoryAndStore(String category, String storeId) {
        return listingJpaRepository.findByStoreIdAndCategory(storeId, category);
    }



    @Override
    public boolean updateOrRestoreStock(Map<String, Map<String, Integer>> stockMap, boolean isRestore) {
        if (stockMap == null || stockMap.isEmpty()) {
            return false;
        }
    
        // Collect listings and sort by ID to avoid deadlocks
        List<Listing> toLock = new ArrayList<>();
        for (String storeId : stockMap.keySet()) {
            Map<String, Integer> listingUpdates = stockMap.get(storeId);
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
            if(!isRestore) {
                // Check stock availability for purchase
                for (Listing listing : toLock) {
                    int requestedQuantity = stockMap.get(listing.getStoreId()).get(listing.getListingId());
    
                    if (listing.getQuantityAvailable() < requestedQuantity)
                        throw new RuntimeException("Not enough stock for listing: " + listing.getListingId());
                }
            }
            // Check stock availability and perform the appropriate action (update or restore)
            for (Listing listing : toLock) {
                int quantity = stockMap.get(listing.getStoreId()).get(listing.getListingId());
                if (!isRestore) {
                    // Perform purchase
                    try {
                        listing.purchase(quantity);
                    } catch (Exception e) {
                        throw new RuntimeException("Unexpected error during purchase of " + listing.getListingId(), e);
                    }
                } else {
                    // For restoring stock, just restore the stock
                    try {
                        listing.restore(quantity);
                    } catch (Exception e) {
                        throw new RuntimeException("Unexpected error during restore of " + listing.getListingId(), e);
                    }
                }
            }
        });
        // Save all listings after the operation
        for (Listing listing : toLock) {
            listingJpaRepository.save(listing);
        }
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


       @Override//
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
    public void disableListingsByStoreId(String storeId) {
        listingJpaRepository.findAll().stream()
            .filter(l -> l.getStoreId().equals(storeId))
            .forEach(l -> {
                synchronized (l) {
                    l.disable();
                    listingJpaRepository.save(l);
                }
            });
    }

    @Override
    public void enableListingsByStoreId(String storeId) {
        listingJpaRepository.findAll().stream()
            .filter(l -> l.getStoreId().equals(storeId))
            .forEach(l -> {
                synchronized (l) {
                    l.enable();
                    listingJpaRepository.save(l);
                }
            });
    }

    @Override//
    public double ProductPrice(String listingId) throws Exception {
        Listing listing = getListingById(listingId);
        if (listing == null) {
            throw new Exception("Listing not found: " + listingId);
        }
        return listing.getPrice();
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
            listingJpaRepository.save(l);
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
            listingJpaRepository.save(l);
        }
    }

    @Override
    public void editProductDescription(String listingId, String newDescription) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive()))
            throw new Exception("Listing " + listingId + " not found or inactive.");
        synchronized (l) {
            l.setProductDescription(newDescription);
            listingJpaRepository.save(l);
        }
    }

    @Override
    public void editProductQuantity(String listingId, int newQuantity) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive()))
            throw new Exception("Listing " + listingId + " not found or inactive.");
        synchronized (l) {
            l.setQuantityAvailable(newQuantity);
            listingJpaRepository.save(l);
        }
    }

    @Override
    public void editProductCategory(String listingId, String newCategory) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive()))
            throw new Exception("Listing " + listingId + " not found or inactive.");
        synchronized (l) {
            l.setCategory(newCategory);
            listingJpaRepository.save(l);
        }
    }


}
