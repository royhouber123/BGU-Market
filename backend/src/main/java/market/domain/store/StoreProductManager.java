package market.domain.store;

import java.util.List;

/**
 * Manages the collection of listings (products for sale) for a specific store.
 * Delegates all storage and search to the injected ListingRepository,
 * while enforcing that operations only affect listings in this manager's store.
 */
public class StoreProductManager implements IStoreProductsManager {

    private final String storeId;
    private final IListingRepository listingRepository;

    public StoreProductManager(String storeId, IListingRepository listingRepository) {
        this.storeId = storeId;
        this.listingRepository = listingRepository;
    }

    public String getStoreId() {
        return storeId;
    }

    @Override
    public String addListing(Listing listing) {
        if (!listing.getStoreId().equals(this.storeId)) {
            throw new IllegalArgumentException("Listing storeId does not match StoreProductManager storeId!");
        }
        if (listingRepository.getListingById(listing.getListingId()) != null) {
            throw new IllegalArgumentException("Listing ID already exists");
        }
        return listingRepository.addListing(listing);
    }

    @Override
    public boolean removeListing(String listingId) {
        Listing listing = listingRepository.getListingById(listingId);
        if (listing == null || !listing.getStoreId().equals(this.storeId)) {
            return false;
        }
        return listingRepository.removeListing(listingId);
    }

    @Override
    public Listing getListingById(String listingId) {
        Listing listing = listingRepository.getListingById(listingId);
        return (listing != null && listing.getStoreId().equals(this.storeId)) ? listing : null;
    }

    @Override
    public List<Listing> getListingsByProductName(String productName) {
        return listingRepository.getListingsByProductNameAndStore(productName, storeId);
    }

    @Override
    public List<Listing> getListingsByProductId(String productId) {
        return listingRepository.getListingsByProductIdAndStore(productId, storeId);
    }

    @Override
    public List<Listing> getAllListings() {
        return listingRepository.getListingsByStoreId(storeId);
    }

    @Override
    public boolean purchaseFromListing(String listingId, int quantity) throws Exception {
        Listing listing = listingRepository.getListingById(listingId);
        if (listing == null || !listing.getStoreId().equals(this.storeId)) {
            throw new Exception("Listing with ID " + listingId + " not found in store " + storeId);
        }
        return listing.purchase(quantity);
    }



    @Override
    public void disableAllListings() {
        listingRepository.disableListingsByStoreId(storeId);
    }
    
    @Override
    public void enableAllListings() {
        listingRepository.enableListingsByStoreId(storeId);
    }

    @Override
    public Boolean editPriceForListing(String listingId,double newPrice) throws Exception{
        return listingRepository.editPriceForListing(listingId, newPrice);
    }
    @Override
    public Boolean editProductName(String listingId, String newName) throws Exception {
        listingRepository.editProductName(listingId, newName);
        return true;
    }
    @Override
    public Boolean editProductDescription(String listingId, String newDescription) throws Exception {
        listingRepository.editProductDescription(listingId, newDescription);
        return true;
    }
    @Override
    public Boolean editProductQuantity(String listingId, int newQuantity) throws Exception {
        listingRepository.editProductQuantity(listingId, newQuantity);
        return true;
    }
    @Override
    public Boolean editProductCategory(String listingId, String newCategory) throws Exception {
        listingRepository.editProductCategory(listingId, newCategory);
        return true;
    }
    
}
