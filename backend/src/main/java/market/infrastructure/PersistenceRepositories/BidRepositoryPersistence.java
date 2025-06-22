package market.infrastructure.PersistenceRepositories;

import market.domain.purchase.*;
import market.infrastructure.IJpaRepository.IBidJpaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.*;


@Repository
public class BidRepositoryPersistence implements IBidRepository {

    private final IBidJpaRepository jpaRepository;

    public BidRepositoryPersistence(IBidJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BidEntity save(BidEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<BidEntity> findByStoreIdAndProductId(String storeId, String productId) {
        return jpaRepository.findByStoreIdAndProductId(storeId, productId);
    }

    @Override
    public void deleteByStoreIdAndProductId(String storeId, String productId) {
        jpaRepository.deleteByStoreIdAndProductId(storeId, productId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<BidEntity> findAll() {
        return jpaRepository.findAll();
    }
}
