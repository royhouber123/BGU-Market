package market.infrastructure;

import market.domain.purchase.*;
import org.springframework.stereotype.Repository;

import java.util.*;

public class AuctionRepository implements IAuctionRepository {

    private final Map<Long, AuctionEntity> byId = new HashMap<>();
    private final Map<String, AuctionEntity> byStoreAndProduct = new HashMap<>();
    private long nextId = 1;

    @Override
    public AuctionEntity save(AuctionEntity entity) {
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        byId.put(entity.getId(), entity);
        byStoreAndProduct.put(key(entity.getStoreId(), entity.getProductId()), entity);
        return entity;
    }

    @Override
    public List<AuctionEntity> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public Optional<AuctionEntity> findByStoreIdAndProductId(String storeId, String productId) {
        return Optional.ofNullable(byStoreAndProduct.get(key(storeId, productId)));
    }

    @Override
    public void deleteById(Long id) {
        AuctionEntity removed = byId.remove(id);
        if (removed != null) {
            byStoreAndProduct.remove(key(removed.getStoreId(), removed.getProductId()));
        }
    }

    private String key(String storeId, String productId) {
        return storeId + "::" + productId;
    }
}
