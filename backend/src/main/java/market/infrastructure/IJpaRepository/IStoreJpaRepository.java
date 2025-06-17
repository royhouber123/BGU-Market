package market.infrastructure.IJpaRepository;

import market.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IStoreJpaRepository extends JpaRepository<Store, String> {
     boolean existsByName(String name);
}
