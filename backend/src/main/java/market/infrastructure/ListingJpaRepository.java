
package market.infrastructure;

import market.domain.store.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingJpaRepository extends JpaRepository<Listing, String> {

    List<Listing> findByStoreId(String storeId);
    List<Listing> findByProductId(String productId);
    List<Listing> findByProductName(String productName);
    List<Listing> findByStoreIdAndProductId(String storeId, String productId);
    List<Listing> findByStoreIdAndProductName(String storeId, String productName);
    List<Listing> findByCategory(String category);
    List<Listing> findByStoreIdAndCategory(String storeId, String category);
    List<Listing> findByStoreIdAndActiveTrue(String storeId);
    List<Listing> getListingsByCategoryAndStore(String category, String storeId);


    
}
