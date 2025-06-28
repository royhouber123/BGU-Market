package market.infrastructure.PersistenceRepositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import market.domain.store.IListingRepository;
import market.domain.store.Listing;
import market.infrastructure.IJpaRepository.IListingJpaRepository;

@Primary
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

        // Collect and validate all listings
        List<Listing> toProcess = new ArrayList<>();
        for (String storeId : stockMap.keySet()) {
            Map<String, Integer> listingUpdates = stockMap.get(storeId);
            if (listingUpdates == null) {
                throw new IllegalArgumentException("Missing listings for store " + storeId);
            }

            for (String listingId : listingUpdates.keySet()) {
                Listing l = getListingById(listingId);
                if (l == null || !l.getStoreId().equals(storeId)) {
                    throw new IllegalArgumentException("Invalid listing: " + listingId + " for store: " + storeId);
                }
                toProcess.add(l);
            }
        }

        // Sort to have a consistent order (not strictly needed anymore, but harmless)
        toProcess.sort(Comparator.comparing(Listing::getListingId));

        // Execute business logic
        for (Listing listing : toProcess) {
            int quantity = stockMap.get(listing.getStoreId()).get(listing.getListingId());

            if (!isRestore) {
                // Check stock availability first
                if (listing.getQuantityAvailable() < quantity) {
                    throw new RuntimeException("Not enough stock for listing: " + listing.getListingId());
                }
                try {
                    listing.purchase(quantity);
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected error during purchase of " + listing.getListingId(), e);
                }
            } else {
                try {
                    listing.restore(quantity);
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected error during restore of " + listing.getListingId(), e);
                }
            }
        }

        // Attempt to save all listings — this is where optimistic locking takes effect
        try {
            for (Listing listing : toProcess) {
                listingJpaRepository.save(listing);
            }
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("One or more products were updated during. Please refresh your cart and try again.", e);

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
        listingJpaRepository.updateNameWithoutVersion(listingId, newName); 
    }

    @Override
    public void editProductDescription(String listingId, String newDescription) throws Exception {
        Listing l = getListingById(listingId);
        if (l == null || !Boolean.TRUE.equals(l.isActive()))
            throw new Exception("Listing " + listingId + " not found or inactive.");
        listingJpaRepository.updateDescriptionWithoutVersion(listingId, newDescription);
        
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
        listingJpaRepository.updateCategoryWithoutVersion(listingId, newCategory);
        }
    }



