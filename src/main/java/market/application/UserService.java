package market.application;

import java.util.Optional;

import market.domain.user.IUserRepository;
import market.domain.user.User;
import market.domain.user.ShoppingCart;

public class UserService {

    private final IUserRepository repo;

    public UserService(IUserRepository repo) {
        this.repo = repo;
    }

    /** Register a brand-new user. */
    public void register(String userName, String password) {
        repo.register(userName, password);
    }

    /** Delete an existing user entirely. */
    public void deleteUser(String userName) {
        repo.delete(userName);
    }

    /** Fetch a user object (if any). */
    public Optional<User> getUser(String userName) {
        return Optional.ofNullable(repo.findById(userName));
    }

    /** Change the user’s log-in name (returns false if conflict). */
    public boolean changeUserName(String oldName, String newName) {
        return repo.changeUserName(oldName, newName);
    }

    /** Update the stored password (mock plaintext for now). */
    public boolean changePassword(String userName, String newPassword) {
        return repo.changePassword(userName, newPassword);
    }

    public void addProductToCart(String userName,
                                 int storeId,
                                 String productName,
                                 int quantity) {
        Optional.ofNullable(repo.findById(userName))
                .ifPresent(u -> u.addProductToCart(storeId, productName, quantity));
    }

    public void removeProductFromCart(String userName,
                                      int storeId,
                                      String productName,
                                      int quantity) {
        Optional.ofNullable(repo.findById(userName))
                .ifPresent(u -> u.removeProductFromCart(storeId, productName, quantity));
    }

    public void clearCart(String userName) {
        Optional.ofNullable(repo.getCart(userName))
                .ifPresent(cart -> cart.clear());  
    }

    public ShoppingCart getCart(String userName) {
        return repo.getCart(userName);
    }
}
