package market.application;

import java.util.Optional;

import market.domain.user.IUserRepository;
import market.domain.user.User;

public class UserService {
    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // public Map<String, List<String>> extractStoreRoles(String token) {
    //     Claims claims = parseAccessToken(token);
    //     return claims.get("storeRoles", Map.class);
    // }

    // if (storeRoles.containsKey("store123") && storeRoles.get("store123").contains("Owner")) {
    // // allow action
    // }

    /**
     * Registers a new user with given ID.
     */

    public void registerUser(String userName , String password) {
        userRepository.register(userName , password);
    }

    /**
     * Retrieves an existing user by ID.
     */
    public Optional<User> getUser(String userName) {
        return Optional.ofNullable(userRepository.findById(userName));
    }

    /**
     * Adds a product to the specified user's cart.
     */
    public void addProductToCart(String userName, String storeId, String productId, int quantity) {
        User user = userRepository.findById(userName);
        if (user != null) {
            user.addProductToCart(storeId, productId, quantity);
        }
    }

    /**
     * Removes a product from the specified user's cart.
     */
    public void removeProductFromCart(String userName, String storeId, String productId, int quantity) {
        User user = userRepository.findById(userName);
        if (user != null) {
            user.removeProductFromCart(storeId, productId , quantity);
        }
    }

    /**
     * Deletes a user by ID.
     */
    public void deleteUser(String userName) {
        userRepository.delete(userName);
    }

    public void clearCart(String userName){

    }
}
