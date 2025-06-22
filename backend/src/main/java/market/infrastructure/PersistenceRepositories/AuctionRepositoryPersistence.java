package market.infrastructure.PersistenceRepositories;

import market.domain.purchase.*;
import market.infrastructure.IJpaRepository.IAuctionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AuctionRepositoryPersistence implements IAuctionRepository {

    private final IAuctionJpaRepository auctionjpaRepository;

    @Autowired
    public AuctionRepositoryPersistence(IAuctionJpaRepository jpaRepository) {
        this.auctionjpaRepository = jpaRepository;
    }

    @Override
    public AuctionEntity save(AuctionEntity entity) {
        return auctionjpaRepository.save(entity);
    }

    @Override
    public List<AuctionEntity> findAll() {
        return auctionjpaRepository.findAll();
    }

    @Override
    public Optional<AuctionEntity> findByStoreIdAndProductId(String storeId, String productId) {
        return auctionjpaRepository.findByStoreIdAndProductId(storeId, productId);
    }

    @Override
    public void deleteById(Long id) {
        auctionjpaRepository.deleteById(id);
    }
}
