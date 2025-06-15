package market.infrastructure;

import market.domain.user.User;
import org.junit.jupiter.api.Test;

import support.AcceptanceTestSpringBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserRepositoryTests extends AcceptanceTestSpringBase {
    
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