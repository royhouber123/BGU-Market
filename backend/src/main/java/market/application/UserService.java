package market.application;
import market.domain.user.IUserRepository;
import market.domain.user.ISuspensionRepository;
import market.domain.user.User;
import market.domain.user.ShoppingCart;
import market.middleware.TokenUtils;
import io.jsonwebtoken.Claims;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.ApiResponse;
import utils.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = Logger.getInstance();
    private final IUserRepository repo;
    private final AuthService authService;
    private final ISuspensionRepository suspensionRepository; 
    
    @Autowired
    public UserService(IUserRepository repo, AuthService authService, ISuspensionRepository suspensionRepository) {
        this.repo = repo;
        this.authService = authService;
        this.suspensionRepository=suspensionRepository; 
    }

    /** Register a brand-new guest. */
    public Void register(String userName) {
        logger.info("[UserService] Registering guest: " + userName);
        repo.register(userName);
        logger.info("[UserService] Guest registered: " + userName);
        return null;
    }

    @Transactional
    /** Register a brand-new user. */
    public Void register(String userName, String password) {
        logger.info("[UserService] Registering user: " + userName);
        repo.register(userName, password);
        logger.info("[UserService] User registered: " + userName);
        return null;
    }


    @Transactional
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

    /** Validate if a user exists in the system. */
    public ApiResponse<Map<String, Boolean>> validateUserExists(String userName) {
        logger.info("[UserService] Validating user existence: " + userName);
        try {
            User user = repo.findById(userName);
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", true);
            logger.info("[UserService] User exists: " + userName);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            logger.debug("[UserService] User does not exist: " + userName + ". Exception: " + e.getMessage());
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", false);
            return ApiResponse.ok(result);
        }
    }

    @Transactional
    /** Change the user's log-in name (throws if conflict). Returns new JWT token. */
    public String changeUserName(String newName) {
        String oldName = extractUserNameFromToken();
        logger.info("[UserService] Changing username from '" + oldName + "' to '" + newName + "'.");
        suspensionRepository.checkNotSuspended(oldName);// check if user is suspended
        boolean result = repo.changeUserName(oldName, newName);
        if (result) {
            // Generate new token with updated username
            User updatedUser = repo.findById(newName);
            String newToken = authService.generateToken(updatedUser);
            logger.info("[UserService] Username changed from '" + oldName + "' to '" + newName + "' and new token generated.");
            repo.save(updatedUser);
            return newToken;
        } else {
            throw new RuntimeException("[UserService] Failed to change username from '" + oldName + "' to '" + newName + "'.");
        }
    }

    @Transactional
    /** Update the stored password (mock plaintext for now). */
    public boolean changePassword(String newPassword) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Changing password for user: " + userName);
        suspensionRepository.checkNotSuspended(userName);// check if user is suspended
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

    @Transactional
    public Void addProductToCart(String storeId,
                                 String productName,
                                 int quantity) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Adding product to cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        suspensionRepository.checkNotSuspended(userName);// check if user is suspended
        User user = repo.findById(userName);
        System.out.println("Adding product to cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        user.addProductToCart(storeId, productName, quantity);
        repo.save(user);
        //debug!
        ShoppingCart cart = repo.getCart(userName);
        System.out.println("Cart product names: " + cart.getAllStoreBags().iterator().next().getProducts().keySet());
        logger.info("[UserService] Product added to cart for user: " + userName);
        return null;
    }

    @Transactional
    public Void removeProductFromCart(String storeId,
                                      String productName,
                                      int quantity) {
        String userName = extractUserNameFromToken();
        logger.info("[UserService] Removing product from cart for user: " + userName + ", storeId: " + storeId + ", product: " + productName + ", quantity: " + quantity);
        User user = repo.findById(userName);
        user.removeProductFromCart(storeId, productName, quantity);
        repo.save(user);
        logger.info("[UserService] Product removed from cart for user: " + userName);
        return null;
    }

    @Transactional
    public Void clearCart() {
        String userName = extractUserNameFromToken();
        User user = repo.findById(userName);
        logger.info("[UserService] Clearing cart for user: " + userName);
        ShoppingCart cart = repo.getCart(userName);
        if (cart != null) {
            cart.clear();
            repo.save(user);
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

    @Transactional
    public boolean suspendUser(String userName, long durationHours) {
        return suspensionRepository.suspendUser(userName, durationHours);
    }

    @Transactional
    public boolean unsuspendUser(String userName) {
        return suspensionRepository.unsuspendUser(userName);
    }

    public List<String> getSuspendedUsers() {
        return suspensionRepository.getSuspendedUsers();
    }

    public boolean isSuspended(String userName) {
        return suspensionRepository.isSuspended(userName);
    }
}