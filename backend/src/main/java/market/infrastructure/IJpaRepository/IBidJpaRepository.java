package market.infrastructure.IJpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import market.domain.purchase.BidEntity;


@Repository
public interface IBidJpaRepository extends JpaRepository<BidEntity, Long> {
    Optional<BidEntity> findByStoreIdAndProductId(String storeId, String productId);
    void deleteByStoreIdAndProductId(String storeId, String productId);
}
