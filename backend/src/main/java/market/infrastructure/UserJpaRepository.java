package market.infrastructure;

import market.domain.user.User;
import market.domain.user.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUserName(String userName);
    
    @Query("SELECT u FROM User u WHERE TYPE(u) = Admin")
    List<Admin> findAllAdmins();
    
    boolean existsByUserName(String userName);
} 