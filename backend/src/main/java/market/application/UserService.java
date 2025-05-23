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
    public ApiResponse<Void> register(String userName) {
        logger.info("[UserService] Registering guest: " + userName);
        try {
            repo.register(userName);
            logger.info("[UserService] Guest registered: " + userName);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to register guest: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to register guest: " + userName + ". Exception: " + e.getMessage());
        }
    }

    /** Register a brand-new user. */
    public ApiResponse<Void> register(String userName, String password) {
        logger.info("[UserService] Registering user: " + userName);
        try {
            repo.register(userName, password);
            logger.info("[UserService] User registered: " + userName);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to register user: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to register user: " + userName + ". Exception: " + e.getMessage());
        }
    }


    /** Delete an existing user entirely. */
    public ApiResponse<Void> deleteUser(String userName) {
        logger.info("[UserService] Deleting user: " + userName);
        try {
            repo.delete(userName);
            logger.info("[UserService] User deleted: " + userName);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to delete user: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to delete user: " + userName + ". Exception: " + e.getMessage());
        }
    }

    /** Fetch a user object (if any). */
    public ApiResponse<User> getUser() {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Fetching user: " + userName);
        try {
            User user = repo.findById(userName);
            logger.info("[UserService] User fetched: " + userName);
            return ApiResponse.ok(user);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to fetch user: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to fetch user: " + userName + ". Exception: " + e.getMessage());
        }
    }

    /** Change the user’s log-in name (throws if conflict). */
    public ApiResponse<Boolean> changeUserName(String newName) {
        String oldName = extractUserNameFromToken();
        logger.info("[UserService] Changing username from '" + oldName + "' to '" + newName + "'.");
        try {
            boolean result = repo.changeUserName(oldName, newName);
            logger.info("[UserService] Username changed from '" + oldName + "' to '" + newName + "'.");
            return ApiResponse.ok(result);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to change username from '" + oldName + "' to '" + newName + "'. Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to change username from '" + oldName + "' to '" + newName + "'. Exception: " + e.getMessage());
        }
    }

    /** Update the stored password (mock plaintext for now). */
    public ApiResponse<Boolean> changePassword(String newPassword) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Changing password for user: " + userName);
        try {
            boolean result = repo.changePassword(userName, newPassword);
            logger.info("[UserService] Password changed for user: " + userName);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to change password for user: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to change password for user: " + userName + ". Exception: " + e.getMessage());
        }
    }

    public ApiResponse<Void> addProductToCart(String storeId,
                                 String productName,
                                 int quantity) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Adding product to cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        try {
            User user = repo.findById(userName);
            user.addProductToCart(storeId, productName, quantity);
            logger.info("[UserService] Product added to cart for user: " + userName);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to add product to cart for user: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to add product to cart for user: " + userName + ". Exception: " + e.getMessage());
        }
    }

    public ApiResponse<Void> removeProductFromCart(String storeId,
                                      String productName,
                                      int quantity) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Removing product from cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        try {
            User user = repo.findById(userName);
            user.removeProductFromCart(storeId, productName, quantity);
            logger.info("[UserService] Product removed from cart for user: " + userName);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to remove product from cart for user: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to remove product from cart for user: " + userName + ". Exception: " + e.getMessage());
        }
    }

    public ApiResponse<Void> clearCart() {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Clearing cart for user: " + userName);
        try {
            ShoppingCart cart = repo.getCart(userName);
            cart.clear();
            logger.info("[UserService] Cart cleared for user: " + userName);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to clear cart for user: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to clear cart for user: " + userName + ". Exception: " + e.getMessage());
        }
    }

    public ApiResponse<ShoppingCart> getCart() {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Getting cart for user: " + userName);
        try {
            ShoppingCart cart = repo.getCart(userName);
            logger.info("[UserService] Cart retrieved for user: " + userName);
            return ApiResponse.ok(cart);
        } catch (Exception e) {
            logger.debug("[UserService] Failed to get cart for user: " + userName + ". Exception: " + e.getMessage());
            return ApiResponse.fail("[UserService] Failed to get cart for user: " + userName + ". Exception: " + e.getMessage());
        }
    }

    private String extractUserNameFromToken() {
        String token = TokenUtils.getToken();
        if (token == null) {
            logger.debug("[UserService] Token is missing from request context");
            throw new IllegalStateException("Token is missing from request context");
        }
        Claims claims = authService.parseToken(token).getData();
        return claims.getSubject();
    }

    public ApiResponse<IUserRepository> getUserRepository()
    {
        return ApiResponse.ok(this.repo);
    }
}
