package market.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import market.domain.Role.IRoleRepository;
import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.domain.user.Admin;
import market.domain.user.ISuspensionRepository;
import market.domain.user.IUserRepository;
import market.domain.user.User;
import utils.Logger;

/**
 * Provides administrative operations for the marketplace system,
 * such as managing stores and user suspensions.
 */
public class AdminService {

    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final IRoleRepository roleRepository;
    private final ISuspensionRepository suspensionRepository;

    private static final Logger logger = Logger.getInstance();

    /**
     * Constructs an AdminService with access to user, store, role, and suspension repositories.
     *
     * @param userRepository Repository for user data
     * @param storeRepository Repository for store data
     * @param roleRepository Repository for role management
     * @param suspensionRepository Repository for suspension management
     */
    public AdminService(IUserRepository userRepository,
                        IStoreRepository storeRepository,
                        IRoleRepository roleRepository,
                        ISuspensionRepository suspensionRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.roleRepository = roleRepository;
        this.suspensionRepository = suspensionRepository;
    }

    /**
     * Closes a store by request of a system administrator.
     * This action deactivates the store and removes all roles assigned to users in that store.
     *
     * @param adminId ID of the acting system administrator
     * @param storeId ID of the store to close
     * @throws Exception if the admin or store is invalid, or store is already inactive
     */
    public void closeStoreByAdmin(String adminId, String storeId) throws Exception {
        logger.info("Admin " + adminId + " is attempting to close store " + storeId);

        // Validate admin credentials
        if (!validateAdmin(adminId)) {
            throw new Exception("Permission denied: User is not an admin.");
        }

        Store store = storeRepository.getStoreByID(storeId);
        if (store == null) {
            logger.error("Store with ID " + storeId + " not found.");
            throw new Exception("Store not found.");
        }

        if (!store.isActive()) {
            logger.debug("Store " + storeId + " is already inactive.");
            throw new Exception("Store is already inactive.");
        }

        store.setActive(false);
        storeRepository.save(store);
        logger.info("Store " + storeId + " has been deactivated.");

        List<String> affectedUserIds = roleRepository.getAllStoreUserIdsWithRoles(storeId);
        for (String userId : affectedUserIds) {
            roleRepository.removeAllRolesForUserInStore(userId, storeId);
        }
        logger.info("Removed roles from " + affectedUserIds.size() + " users in store " + storeId);
    }

    /**
     * Suspends a user for a specified duration (in milliseconds).
     * If duration is 0, the suspension is permanent.
     *
     * @param adminId ID of the acting admin
     * @param targetUserId ID of the user to suspend
     * @param durationHours Duration of suspension in hours (0 = permanent)
     * @throws Exception if the admin or user is invalid, or suspension fails
     */
    public void suspendUser(String adminId, String targetUserId, long durationHours) throws Exception {
        logger.info("Admin " + adminId + " is attempting to suspend user " + targetUserId +
                 " for " + (durationHours == 0 ? "an indefinite period" : durationHours + " hours"));

        if (!validateAdmin(adminId)) {
            throw new Exception("Permission denied: User is not an admin.");
        }

        if (userRepository.findById(targetUserId) == null) {
            logger.error("User " + targetUserId + " does not exist.");
            throw new Exception("User not found.");
        }

        boolean success = suspensionRepository.suspendUser(targetUserId, durationHours);
        if (!success) {
            logger.error("Suspension failed for user " + targetUserId);
            throw new Exception("Suspension failed.");
        }

        logger.info("User " + targetUserId + " suspended for " +
                    (durationHours == 0 ? "an indefinite period." : durationHours + " hours."));
    }

    /**
     * Lifts the suspension from a user.
     *
     * @param adminId ID of the acting admin
     * @param targetUserId ID of the user to unsuspend
     * @throws Exception if the admin is invalid or user is not suspended
     */
    public void unsuspendUser(String adminId, String targetUserId) throws Exception {
        logger.info("Admin " + adminId + " is attempting to unsuspend user " + targetUserId);

        if (!validateAdmin(adminId)) {
            throw new Exception("Permission denied: User is not an admin.");
        }

        boolean success = suspensionRepository.unsuspendUser(targetUserId);
        if (!success) {
            logger.debug("User " + targetUserId + " was not suspended.");
            throw new Exception("User was not suspended or does not exist.");
        }

        logger.info("User " + targetUserId + " has been unsuspended.");
    }

    /**
     * Retrieves the usernames of all currently suspended users.
     *
     * @param adminId ID of the acting admin
     * @return List of usernames for suspended users
     * @throws Exception if the admin is invalid
     */
    public List<String> getSuspendedUserIds(String adminId) throws Exception {
        if (!validateAdmin(adminId)) {
            throw new Exception("Permission denied: User is not an admin.");
        }
        logger.info("Admin " + adminId + " is requesting suspended users");
        List<String> suspended = suspensionRepository.getSuspendedUsers();
        System.out.println("Suspended users: " + suspended);
        return suspended;
    }

    /**
     * Validates that a user is an administrator.
     *
     * @param adminId ID of the user to validate
     * @return The Admin object if valid
     * @throws Exception if the user is not an admin
     */
    public Boolean validateAdmin(String adminId) throws Exception {
        try {
            User user = userRepository.findById(adminId);
            if (!(user instanceof Admin)) {
                logger.error("User " + adminId + " is not an admin.");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error validating admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all users in the system for admin management purposes.
     *
     * @param adminId ID of the acting system administrator
     * @return Map containing user data with user IDs as keys
     * @throws Exception if the user is not an admin
     */
    public Map<String, Object> getAllUsers() throws Exception {
        logger.info("Admin is requesting all users");
        
        // Get all users from repository
        List<User> allUsers = userRepository.getAllUsers();
        
        // Convert to a map of user data for the response
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> usersMap = new HashMap<>();
        
        for (User user : allUsers) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUserName());
            userData.put("isAdmin", user instanceof Admin);
            
            // Get user's store roles if any
            Map<String, List<market.domain.Role.Role>> roles = storeRepository.getUsersRoles(user.getUserName());
            userData.put("roles", roles);
            
            usersMap.put(user.getUserName(), userData);
        }
        
        result.put("users", usersMap);
        result.put("count", allUsers.size());
        
        logger.info("Returning " + allUsers.size() + " users");
        return result;
    }

    /**
     * Retrieves all stores in the system for admin management purposes.
     *
     * @return Map containing store data with store IDs as keys
     * @throws Exception if the user is not an admin
     */
    public Map<String, Object> getAllStores() throws Exception {
        logger.info("Admin is requesting all stores");
        
        // Get all active stores
        List<Store> activeStores = storeRepository.getAllActiveStores();
        
        // Convert to a map of store data for the response
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> storesMap = new HashMap<>();
        
        for (Store store : activeStores) {
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("name", store.getName());
            storeData.put("active", store.isActive());
            storeData.put("owners", store.getAllOwnersStrs());
            storeData.put("managers", store.getAllManagersStrs());
            
            storesMap.put(store.getStoreId(), storeData);
        }
        
        result.put("stores", storesMap);
        result.put("count", activeStores.size());
        
        logger.info("Returning " + activeStores.size() + " active stores");
        return result;
    }
}