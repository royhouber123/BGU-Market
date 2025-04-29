package market.application;

import java.util.Optional;

import market.domain.user.IUserRepository;
import market.domain.user.User;
import market.domain.user.ShoppingCart;
import market.middleware.TokenUtils;
import market.application.AuthService;
import io.jsonwebtoken.Claims;

public class UserService {

    private final IUserRepository repo;
    private final AuthService authService;

    public UserService(IUserRepository repo, AuthService authService) {
        this.repo = repo;
        this.authService = authService;
    }

    /** Register a brand-new user. */
    public void register(String userName, String password) throws Exception {
        repo.register(userName, password);
    }

    /** Delete an existing user entirely. */
    public void deleteUser() throws Exception {
        String userName = extractUserNameFromToken();
        repo.delete(userName);
    }

    /** Fetch a user object (if any). */
    public User getUser() throws Exception {
        String userName = extractUserNameFromToken();
        return repo.findById(userName);
    }

    /** Change the user’s log-in name (throws if conflict). */
    public boolean changeUserName(String newName) throws Exception {
        String oldName = extractUserNameFromToken();
        return repo.changeUserName(oldName, newName);
    }

    /** Update the stored password (mock plaintext for now). */
    public boolean changePassword(String newPassword) throws Exception {
        String userName = extractUserNameFromToken();
        return repo.changePassword(userName, newPassword);
    }

    public void addProductToCart(String userName,
                                 String storeId,
                                 String productName,
                                 int quantity) throws Exception {
        String userName = extractUserNameFromToken();
        User user = repo.findById(userName);
        user.addProductToCart(storeId, productName, quantity);
    }

    public void removeProductFromCart(int storeId,
                                      String productName,
                                      int quantity) throws Exception {
        String userName = extractUserNameFromToken();
        User user = repo.findById(userName);
        user.removeProductFromCart(storeId, productName, quantity);
    }

    public void clearCart() throws Exception {
        String userName = extractUserNameFromToken();
        ShoppingCart cart = repo.getCart(userName);
        cart.clear();
    }

    public ShoppingCart getCart() throws Exception {
        String userName = extractUserNameFromToken();
        return repo.getCart(userName);
    }

    private String extractUserNameFromToken() {
        String token = TokenUtils.getAccessToken();
        if (token == null) {
            throw new IllegalStateException("Access token is missing from request context");
        }
        Claims claims = authService.parseAccessToken(token);
        return claims.getSubject();
    }
}
