package market.infrastructure.IJpaRepository;

import market.domain.purchase.AuctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAuctionJpaRepository extends JpaRepository<AuctionEntity, Long> {
    Optional<AuctionEntity> findByStoreIdAndProductId(String storeId, String productId);
}
