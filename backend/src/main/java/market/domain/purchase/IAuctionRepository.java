package market.domain.purchase;

import java.util.List;
import java.util.Optional;

public interface IAuctionRepository {
    AuctionEntity save(AuctionEntity entity);
    List<AuctionEntity> findAll();
    Optional<AuctionEntity> findByStoreIdAndProductId(String storeId, String productId);
    void deleteById(Long id);
}
