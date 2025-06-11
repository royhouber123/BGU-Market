
package market.infrastructure.IJpaRepository;

import market.domain.store.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IListingJpaRepository extends JpaRepository<Listing, String> {

    List<Listing> findByStoreId(String storeId);
    List<Listing> findByProductId(String productId);
    List<Listing> findByProductName(String productName);
    List<Listing> findByStoreIdAndProductId(String storeId, String productId);
    List<Listing> findByStoreIdAndProductName(String storeId, String productName);
    List<Listing> findByCategory(String category);
    List<Listing> findByStoreIdAndCategory(String storeId, String category);
    List<Listing> findByStoreIdAndActiveTrue(String storeId);
    
    @Query("SELECT l FROM Listing l WHERE l.category = :category AND l.storeId = :storeId")
    List<Listing> getListingsByCategoryAndStore(String category, @Param("storeId") String storeId);


    
}
