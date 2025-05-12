package market.application;
import market.domain.user.IUserRepository;
import market.domain.user.User;
import market.domain.user.ShoppingCart;
import market.middleware.TokenUtils;
import io.jsonwebtoken.Claims;
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
    public void register(String userName) throws Exception {
        logger.info("[UserService] Registering guest: " + userName);
        try {
            repo.register(userName);
            logger.info("[UserService] Guest registered: " + userName);
        } catch (Exception e) {
            logger.error("[UserService] Failed to register guest: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }

    /** Register a brand-new user. */
    public void register(String userName, String password) throws Exception {
        logger.info("[UserService] Registering user: " + userName);
        try {
            repo.register(userName, password);
            logger.info("[UserService] User registered: " + userName);
        } catch (Exception e) {
            logger.error("[UserService] Failed to register user: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }


    /** Delete an existing user entirely. */
    public void deleteUser() throws Exception {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Deleting user: " + userName);
        try {
            repo.delete(userName);
            logger.info("[UserService] User deleted: " + userName);
        } catch (Exception e) {
            logger.error("[UserService] Failed to delete user: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }

    /** Fetch a user object (if any). */
    public User getUser() throws Exception {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Fetching user: " + userName);
        try {
            User user = repo.findById(userName);
            logger.info("[UserService] User fetched: " + userName);
            return user;
        } catch (Exception e) {
            logger.error("[UserService] Failed to fetch user: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }

    /** Change the user’s log-in name (throws if conflict). */
    public boolean changeUserName(String newName) throws Exception {
        String oldName = extractUserNameFromToken();
        logger.info("[UserService] Changing username from '" + oldName + "' to '" + newName + "'.");
        try {
            boolean result = repo.changeUserName(oldName, newName);
            logger.info("[UserService] Username changed from '" + oldName + "' to '" + newName + "'.");
            return result;
        } catch (Exception e) {
            logger.error("[UserService] Failed to change username from '" + oldName + "' to '" + newName + "'. Exception: " + e.getMessage());
            throw e;
        }
    }

    /** Update the stored password (mock plaintext for now). */
    public boolean changePassword(String newPassword) throws Exception {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Changing password for user: " + userName);
        try {
            boolean result = repo.changePassword(userName, newPassword);
            logger.info("[UserService] Password changed for user: " + userName);
            return result;
        } catch (Exception e) {
            logger.error("[UserService] Failed to change password for user: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }

    public void addProductToCart(String storeId,
                                 String productName,
                                 int quantity) throws Exception {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Adding product to cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        try {
            User user = repo.findById(userName);
            user.addProductToCart(storeId, productName, quantity);
            logger.info("[UserService] Product added to cart for user: " + userName);
        } catch (Exception e) {
            logger.error("[UserService] Failed to add product to cart for user: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }

    public void removeProductFromCart(String storeId,
                                      String productName,
                                      int quantity) throws Exception {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Removing product from cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        try {
            User user = repo.findById(userName);
            user.removeProductFromCart(storeId, productName, quantity);
            logger.info("[UserService] Product removed from cart for user: " + userName);
        } catch (Exception e) {
            logger.error("[UserService] Failed to remove product from cart for user: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }

    public void clearCart() throws Exception {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Clearing cart for user: " + userName);
        try {
            ShoppingCart cart = repo.getCart(userName);
            cart.clear();
            logger.info("[UserService] Cart cleared for user: " + userName);
        } catch (Exception e) {
            logger.error("[UserService] Failed to clear cart for user: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }

    public ShoppingCart getCart() throws Exception {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Getting cart for user: " + userName);
        try {
            ShoppingCart cart = repo.getCart(userName);
            logger.info("[UserService] Cart retrieved for user: " + userName);
            return cart;
        } catch (Exception e) {
            logger.error("[UserService] Failed to get cart for user: " + userName + ". Exception: " + e.getMessage());
            throw e;
        }
    }

    private String extractUserNameFromToken() {
        String token = TokenUtils.getToken();
        if (token == null) {
            logger.error("[UserService] Token is missing from request context");
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
