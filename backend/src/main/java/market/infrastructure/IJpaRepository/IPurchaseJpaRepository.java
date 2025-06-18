package market.infrastructure.IJpaRepository;

import market.domain.purchase.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPurchaseJpaRepository extends JpaRepository<Purchase, String> {

    List<Purchase> findByUserId(String userId);

    @Query("SELECT DISTINCT p FROM Purchase p JOIN p.products prod WHERE prod.storeId = :storeId")
    List<Purchase> findByStoreId(@Param("storeId") String storeId);
}
