package market.infrastructure.IJpaRepository;

import market.domain.user.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISuspensionJpaRepository extends JpaRepository<Suspension, String> { // Changed Long to String

    Suspension findByUserName(String userName);

    List<Suspension> findAll();
}