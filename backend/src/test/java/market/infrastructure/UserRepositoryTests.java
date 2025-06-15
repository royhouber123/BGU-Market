package market.infrastructure;

import market.domain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import java.sql.DriverManager;

import support.AcceptanceTestSpringBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIf("market.infrastructure.UserRepositoryTests#isMySQLAvailable")
public class UserRepositoryTests extends AcceptanceTestSpringBase {
    
    static boolean isMySQLAvailable() {
        try {
            DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false", "bgu", "changeme").close();
            return true;
        } catch (Exception e) {
            System.out.println("⚠️  MySQL not available – skipping UserRepositoryTests");
            return false;
        }
    }

    @Test
    void testSaveAndFindUserWithCart() {
        // Arrange
        String username = "cart_tester";
        String password = "password123";
        
        // Act
        userRepository.register(username, password);
        User user = userRepository.findById(username);
        user.addProductToCart("1", "TestProduct", 1);
        userRepository.save(user);

        User foundUser = userRepository.findById(username);

        // Assert
        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUserName());
        assertNotNull(foundUser.getShoppingCart());
        assertEquals(1, foundUser.getShoppingCart().getStoreBag("1").getProductQuantity("TestProduct"));
    }
} 