package market.application;
import market.domain.user.IUserRepository;
import market.domain.user.User;
import market.domain.user.ShoppingCart;
import market.middleware.TokenUtils;
import io.jsonwebtoken.Claims;
import utils.ApiResponse;
import utils.Logger;

public class UserService {

    private static final Logger logger = Logger.getInstance();
    private final IUserRepository repo;
    private final AuthService authService;

    public UserService(IUserRepository repo, AuthService authService) {
        this.repo = repo;
        this.authService = authService;
    }

    /** Register a brand-new guest. */
    public Void register(String userName) {
        logger.info("[UserService] Registering guest: " + userName);
        repo.register(userName);
        logger.info("[UserService] Guest registered: " + userName);
        return null;
    }

    /** Register a brand-new user. */
    public Void register(String userName, String password) {
        logger.info("[UserService] Registering user: " + userName);
        repo.register(userName, password);
        logger.info("[UserService] User registered: " + userName);
        return null;
    }


    /** Delete an existing user entirely. */
    public Void deleteUser(String userName) {
        logger.info("[UserService] Deleting user: " + userName);
        repo.delete(userName);
        logger.info("[UserService] User deleted: " + userName);
        return null;
    }

    /** Fetch a user object (if any). */
    public User getUser() {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Fetching user: " + userName);
        User user = repo.findById(userName);
        logger.info("[UserService] User fetched: " + userName);
        return user;
    }

    /** Change the user's log-in name (throws if conflict). Returns new JWT token. */
    public String changeUserName(String newName) {
        String oldName = extractUserNameFromToken();
        logger.info("[UserService] Changing username from '" + oldName + "' to '" + newName + "'.");
        boolean result = repo.changeUserName(oldName, newName);
        if (result) {
            // Generate new token with updated username
            User updatedUser = repo.findById(newName);
            String newToken = authService.generateToken(updatedUser);
            logger.info("[UserService] Username changed from '" + oldName + "' to '" + newName + "' and new token generated.");
            return newToken;
        } else {
            throw new RuntimeException("[UserService] Failed to change username from '" + oldName + "' to '" + newName + "'.");
        }
    }

    /** Update the stored password (mock plaintext for now). */
    public boolean changePassword(String newPassword) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Changing password for user: " + userName);
        boolean result = repo.changePassword(userName, newPassword);
        if(result) {
            logger.info("[UserService] Password changed for user: " + userName);
            return result;
        }
        else {
            logger.debug("[UserService] Failed to change password for user: " + userName + ".");
            throw new RuntimeException("[UserService] Failed to change password for user: " + userName + ".");
        }
    }

    public Void addProductToCart(String storeId,
                                 String productName,
                                 int quantity) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Adding product to cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        User user = repo.findById(userName);
        user.addProductToCart(storeId, productName, quantity);
        logger.info("[UserService] Product added to cart for user: " + userName);
        return null;
    }

    public Void removeProductFromCart(String storeId,
                                      String productName,
                                      int quantity) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Removing product from cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        User user = repo.findById(userName);
        user.removeProductFromCart(storeId, productName, quantity);
        logger.info("[UserService] Product removed from cart for user: " + userName);
        return null;
    }

    public Void clearCart() {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Clearing cart for user: " + userName);
        ShoppingCart cart = repo.getCart(userName);
        if (cart != null) {
            cart.clear();
            logger.info("[UserService] Cart cleared for user: " + userName);
        }
        return null;
    }

    public ShoppingCart getCart() {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Getting cart for user: " + userName);
        ShoppingCart cart = repo.getCart(userName);
        if (cart != null) {
            logger.info("[UserService] Cart retrieved for user: " + userName);
        }
        return cart;
    }

    private String extractUserNameFromToken() {
        String token = TokenUtils.getToken();
        if (token == null) {
            logger.debug("[UserService] Token is missing from request context");
            throw new IllegalStateException("Token is missing from request context");
        }
        Claims claims = authService.parseToken(token);
        return claims.getSubject();
    }

    public IUserRepository getUserRepository()
    {
        return this.repo;
    }
}
