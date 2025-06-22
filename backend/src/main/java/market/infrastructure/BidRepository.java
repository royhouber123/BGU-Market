package market.infrastructure;

import market.domain.purchase.BidEntity;
import market.domain.purchase.IBidRepository;
import org.springframework.stereotype.Repository;
import java.util.*;


public class BidRepository implements IBidRepository {

    private final Map<Long, BidEntity> byId = new HashMap<>();
    private final Map<String, BidEntity> byStoreAndProduct = new HashMap<>();
    private long nextId = 1;

    @Override
    public BidEntity save(BidEntity entity) {
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        byId.put(entity.getId(), entity);
        byStoreAndProduct.put(key(entity.getStoreId(), entity.getProductId()), entity);
        return entity;
    }

    @Override
    public Optional<BidEntity> findByStoreIdAndProductId(String storeId, String productId) {
        return Optional.ofNullable(byStoreAndProduct.get(key(storeId, productId)));
    }

    @Override
    public void deleteById(Long id) {
        BidEntity removed = byId.remove(id);
        if (removed != null) {
            byStoreAndProduct.remove(key(removed.getStoreId(), removed.getProductId()));
        }
    }

    @Override
    public void deleteByStoreIdAndProductId(String storeId, String productId) {
        byStoreAndProduct.remove(key(storeId, productId));
    }

    @Override
    public List<BidEntity> findAll() {
        return new ArrayList<>(byId.values());
    }

    private String key(String storeId, String productId) {
        return storeId + "::" + productId;
    }
}
