package market.infrastructure;

import market.domain.purchase.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPurchaseJpaRepository extends JpaRepository<Purchase, String> {

    List<Purchase> findByUserId(String userId);
}
