package market.infrastructure.IJpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import market.domain.purchase.BidEntity;


@Repository
public interface IBidJpaRepository extends JpaRepository<BidEntity, Long> {
    @Query("SELECT b FROM BidEntity b LEFT JOIN FETCH b.bids WHERE b.storeId = :storeId AND b.productId = :productId")
    Optional<BidEntity> findByStoreIdAndProductId(String storeId, String productId);
    
    void deleteByStoreIdAndProductId(String storeId, String productId);
}
