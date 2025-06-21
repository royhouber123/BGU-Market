package market.infrastructure.IJpaRepository;

import market.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import market.domain.store.IListingRepository;


@Repository
public interface IStoreJpaRepository extends JpaRepository<Store, String> {
     boolean existsByName(String name);
     Optional<Store> findByName(String name);
     List<Store> findByActiveTrue();


     @Query("SELECT s FROM Store s JOIN s.storeRoles r WHERE r.userId = :userId")
     List<Store> findStoresByUserId(@Param("userId") String userId);
}
