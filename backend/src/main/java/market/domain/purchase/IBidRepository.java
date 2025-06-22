package market.domain.purchase;

import java.util.*;

public interface IBidRepository {
    BidEntity save(BidEntity entity);
    Optional<BidEntity> findByStoreIdAndProductId(String storeId, String productId);
    void deleteByStoreIdAndProductId(String storeId, String productId);
    void deleteById(Long id);
    List<BidEntity> findAll();
}
