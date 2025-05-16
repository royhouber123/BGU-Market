package market.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.domain.user.Admin;
import market.domain.user.IUserRepository;
import market.domain.Role.IRoleRepository;
import utils.Logger;


public class AdminService {

    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final IRoleRepository roleRepository;
    private static final Logger logger = Logger.getInstance();

    public AdminService(IUserRepository userRepository,
                        IStoreRepository storeRepository,
                        IRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Closes a store by request of a system administrator.
     *
     * @param adminId ID of the acting system administrator
     * @param storeId ID of the store to close
     * @throws Exception if the user is not an admin, or the store is invalid or already inactive
     */
    public void closeStoreByAdmin(String adminId, String storeId) throws Exception {
        logger.info("Admin " + adminId + " is attempting to close store " + storeId);

        // validate Asmin exists
        Admin admin = (Admin) userRepository.findById(adminId);
        if (admin == null) {
            logger.error("User with ID " + adminId + " is not an admin.");
            throw new Exception("User is not an admin or not found.");
        }

        //Validate store exists and is active
        Store store = storeRepository.getStoreByID(storeId);
        if (store == null) {
            logger.error("Store with ID " + storeId + " not found.");
            throw new Exception("Store not found.");
        }

        if (!store.isActive()) {
            logger.info("Store " + storeId + " is already inactive.");
            throw new Exception("Store is already inactive.");
        }

        //Deactivate the store
        store.setActive(false);
        storeRepository.save(store);
        logger.info("Store " + storeId + " has been deactivated.");

        //Remove all owners and managers
        List<String> affectedUserIds = roleRepository.getAllStoreUserIdsWithRoles(storeId);
        for (String userId : affectedUserIds) {
            roleRepository.removeAllRolesForUserInStore(userId, storeId);
        }
        logger.info("Removed all roles from " + affectedUserIds.size() + " users in store " + storeId);

        //Prepare notification system. In the future, notify affected users about store closure and role removal.
    }
}
