package market.infrastructure.PersistenceRepositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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


    // @Override
    // public boolean updateOrRestoreStock(Map<String, Map<String, Integer>> stockMap, boolean isRestore) {
    //     if (stockMap == null || stockMap.isEmpty()) {
    //         return false;
    //     }

    //     // Collect and validate all listings
    //     List<Listing> toProcess = new ArrayList<>();
    //     for (String storeId : stockMap.keySet()) {
    //         Map<String, Integer> listingUpdates = stockMap.get(storeId);
    //         if (listingUpdates == null) {
    //             throw new IllegalArgumentException("Missing listings for store " + storeId);
    //         }

    //         for (String listingId : listingUpdates.keySet()) {
    //             Listing l = getListingById(listingId);
    //             if (l == null || !l.getStoreId().equals(storeId)) {
    //                 throw new IllegalArgumentException("Invalid listing: " + listingId + " for store: " + storeId);
    //             }
    //             toProcess.add(l);
    //         }
    //     }

    //     // Sort to have a consistent order (not strictly needed anymore, but harmless)
    //     toProcess.sort(Comparator.comparing(Listing::getListingId));

    //     // Execute business logic
    //     for (Listing listing : toProcess) {
    //         int quantity = stockMap.get(listing.getStoreId()).get(listing.getListingId());

    //         if (!isRestore) {
    //             // Check stock availability first
    //             if (listing.getQuantityAvailable() < quantity) {
    //                 throw new RuntimeException("Not enough stock for listing: " + listing.getListingId());
    //             }
    //             try {
    //                 listing.purchase(quantity);
    //             } catch (Exception e) {
    //                 throw new RuntimeException("Unexpected error during purchase of " + listing.getListingId(), e);
    //             }
    //         } else {
    //             try {
    //                 listing.restore(quantity);
    //             } catch (Exception e) {
    //                 throw new RuntimeException("Unexpected error during restore of " + listing.getListingId(), e);
    //             }
    //         }
    //     }

    //     // Attempt to save all listings — this is where optimistic locking takes effect
    //     try {
    //         for (Listing listing : toProcess) {
    //             listingJpaRepository.save(listing);
    //         }
    //     } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
    //         throw new RuntimeException("One or more products were updated during. Please refresh your cart and try again.", e);

    //     }

    //     return true;
    // }

    // @Override
    // public boolean updateOrRestoreStock(Map<String, Map<String, Integer>> stockMap, boolean isRestore) {
    //     if (stockMap == null || stockMap.isEmpty()) {
    //         return false;
    //     }

    //     int maxRetries = 100;
    //     for (int attempt = 1; attempt <= maxRetries; attempt++) {
    //         try {
    //             performStockUpdate(stockMap, isRestore);
    //             return true;
    //         } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
    //             if (attempt == maxRetries) {
    //                 throw new RuntimeException("Purchase failed after multiple attempts due to concurrent modifications. Please refresh your cart and try again.", e);
    //             }
    //             try {
    //                 Thread.sleep(50L * attempt); // Optional backoff
    //             } catch (InterruptedException ignored) {}
    //         } catch (RuntimeException e) {
    //             // Don't retry for real issues like not enough stock
    //             throw e;
    //         }
    //     }

    //     return false; // Should never reach here
    // }


    // private void performStockUpdate(Map<String, Map<String, Integer>> stockMap, boolean isRestore) {
    //     List<Listing> toProcess = new ArrayList<>();
    //     for (String storeId : stockMap.keySet()) {
    //         Map<String, Integer> listingUpdates = stockMap.get(storeId);
    //         if (listingUpdates == null) {
    //             throw new IllegalArgumentException("Missing listings for store " + storeId);
    //         }

    //         for (String listingId : listingUpdates.keySet()) {
    //             Listing l = getListingById(listingId);
    //             if (l == null || !l.getStoreId().equals(storeId)) {
    //                 throw new IllegalArgumentException("Invalid listing: " + listingId + " for store: " + storeId);
    //             }
    //             toProcess.add(l);
    //         }
    //     }

    //     toProcess.sort(Comparator.comparing(Listing::getListingId));

    //     for (Listing listing : toProcess) {
    //         int quantity = stockMap.get(listing.getStoreId()).get(listing.getListingId());

    //         if (!isRestore) {
    //             if (listing.getQuantityAvailable() < quantity) {
    //                 throw new RuntimeException("Not enough stock for listing: " + listing.getListingId());
    //             }
    //             try {
    //                 listing.purchase(quantity);
    //             } catch (Exception e) {
    //                 throw new RuntimeException("Unexpected error during purchase of " + listing.getListingId(), e);
    //             }
    //         } else {
    //             try {
    //                 listing.restore(quantity);
    //             } catch (Exception e) {
    //                 throw new RuntimeException("Unexpected error during restore of " + listing.getListingId(), e);
    //             }
    //         }
    //     }

    //     for (Listing listing : toProcess) {
    //         listingJpaRepository.save(listing);  // optimistic locking kicks in here
    //     }
    // }

    @Override
    public boolean updateOrRestoreStock(Map<String, Map<String, Integer>> stockMap, boolean isRestore) {
        if (stockMap == null || stockMap.isEmpty()) {
            return false;
        }

        int maxRetries = 5;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++)
        {
            
            try {
                List<Listing> toProcess = new ArrayList<>();

                // Reload listings fresh every attempt
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

                toProcess.sort(Comparator.comparing(Listing::getListingId));

                // Perform the purchase/restore
                for (Listing listing : toProcess) {
                    int quantity = stockMap.get(listing.getStoreId()).get(listing.getListingId());

                    if (!isRestore) {
                        if (listing.getQuantityAvailable() < quantity) {
                            throw new RuntimeException("Not enough stock for listing: " + listing.getListingId());
                        }
                        listing.purchase(quantity);
                    } else {
                        listing.restore(quantity);
                    }
                }

                // Try to persist
                for (Listing listing : toProcess) {
                    listingJpaRepository.save(listing);  // triggers optimistic locking
                }

                return true;

            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                // Retry if version conflict
                if (attempt == maxRetries) {
                    throw new RuntimeException("Purchase failed after multiple attempts due to concurrent modifications. Please refresh your cart and try again.", e);
                }
                try {
                    Thread.sleep(20L * attempt + (long)(Math.random() * 30));  // short exponential backoff
                } catch (InterruptedException ignored) {}
            } catch (Exception e) {
                // Any other error (e.g., not enough stock): fail immediately
                throw new RuntimeException("Purchase failed, please try again later");
            }
        }

        return false; // should never reach here
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



