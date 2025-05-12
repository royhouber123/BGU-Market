package market.application;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import market.domain.store.IListingRepository;
import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.domain.store.StoreDTO;
import market.domain.user.IUserRepository;
import utils.ApiResponse;
import utils.Logger;


public class StoreService {
    private IStoreRepository storeRepository;
    private IListingRepository listingRepository;
    private String storeIDs ="1";
    private IUserRepository userRepository;
    private Logger logger = Logger.getInstance();

    public StoreService(IStoreRepository storeRepository, IUserRepository userRepository, IListingRepository listingRepository) {
        this.storeRepository = storeRepository;
        storeIDs = storeRepository.getNextStoreID();
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }


    /*
    * return storeId
    */
    public ApiResponse<String> createStore(String storeName, String founderId) {
        try {
            // ? - do we need store type
            if(storeRepository.containsStore(storeName)) {
                logger.error("Attempted to create store with existing name: " + storeName);
                return ApiResponse.fail("The storeName '" + storeName + "' already exists");
            }
            String id = storeIDs;
            storeIDs = String.valueOf(Integer.valueOf(storeIDs) + 1);
            Store store = new Store(String.valueOf(storeIDs),storeName, founderId,listingRepository);
            //Who is responsable to manage the store id's????????
            storeRepository.addStore(store);
            logger.info("Store created: " + storeName + ", founder: " + founderId + ", id: " + storeIDs);            
            //((Subscriber)userRepository.findById(founderId)).setStoreRole(store.getStoreID(), "Founder");
            
            return ApiResponse.ok(store.getStoreID());
            //LOG - store added
        } catch (Exception e) {
            logger.error("Failed to create store: " + storeName + ", founder: " + founderId + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to create store: " + e.getMessage());
        }
    }


    /**
     * Closes a specific store.
     * Only the founder of the store is authorized to perform this operation.
     * If successful, the store is marked as inactive (closed).
     *
     * @param storeID ID of the store to close.
     * @param userName  ID of the user attempting to close the store.
     * @return A message indicating "success" if the operation succeeded, or an error message if it failed.
     * @throws Exception if the store does not exist or the closure fails internally.
     */
    public ApiResponse<String> closeStore(String storeID, String userName) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.error("Attempted to close non-existent store: " + storeID);
                return ApiResponse.fail("store doesn't exist");
            }

            if (!s.closeStore(userName)) {
                logger.error("Failed to close store: " + storeID);
                return ApiResponse.fail("store can't be closed");
            }

            Set<String> users = s.getPositionsInStore(userName).keySet();
            for (String ownerOrManager : users) {
                if (s.isManager(ownerOrManager))
                    ; // ((Subscriber)userRepository.findById(ownerOrManager)).removeStoreRole(storeID,"Manager");
                if (s.isOwner(ownerOrManager))
                    ; // ((Subscriber)userRepository.findById(ownerOrManager)).removeStoreRole(storeID,"Owner");
                if (s.getFounderID().equals(ownerOrManager)) {
                    ; // ((Subscriber)userRepository.findById(ownerOrManager)).removeStoreRole(storeID,"Founder");
                    ; // ((Subscriber)userRepository.findById(ownerOrManager)).removeStore(storeID);
                }

                //TODO:need to notify all the owners and managers
            }

            logger.info("Store closed: " + storeID + ", by user: " + userName);
            return ApiResponse.ok(storeID);

        } catch (Exception e) {
            logger.error("Error closing store: " + storeID + ", by user: " + userName + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error closing store: " + e.getMessage());
        }
    }

    /**
     * Reopens a specific store.
     * Only the founder of the store is authorized to perform this operation.
     * If successful, the store is marked as active (open).
     *
     * @param storeID ID of the store to open.
     * @param userName  ID of the user attempting to open the store.
     * @return A message indicating "success" if the operation succeeded, or an error message if it failed.
     * @throws Exception if the store does not exist or the reopening fails internally.
     */
    public ApiResponse<String> openStore(String storeID, String userName) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.error("Attempted to open non-existent store: " + storeID);
                return ApiResponse.fail("store doesn't exist");
            }

            s.openStore(userName);
            logger.info("Store opened: " + storeID + ", by user: " + userName);

            //TODO:need to notify all the owners and managers

            return ApiResponse.ok(storeID);

        } catch (Exception e) {
            logger.error("Error opening store: " + storeID + ", by user: " + userName + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error opening store: " + e.getMessage());
        }
    }


    

    /**
     * Retrieves a store by its name and returns a StoreDTO.
     *
     * @param storeName the name of the store
     * @return ApiResponse with StoreDTO if found, or error message if not
     */
    public ApiResponse<StoreDTO> getStore(String storeName) {
        try {
            Store store = storeRepository.getStoreByName(storeName);
            if (store == null) {
                logger.error("Attempted to get non-existent store: " + storeName);
                return ApiResponse.fail("Store '" + storeName + "' does not exist");
            }
            logger.info("Store retrieved: " + storeName);
            return ApiResponse.ok(new StoreDTO(store));
        } catch (Exception e) {
            logger.error("Error retrieving store: " + storeName + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Failed to retrieve store: " + e.getMessage());
        }
    }


    /*
    appoints 'newOwner' to be an owner of 'storeID' BY 'appointerID'
    assumes aggreement by 'apointerID''s appointer
    */
    public ApiResponse<Void> addAdditionalStoreOwner(String appointerID, String newOwnerID, String storeID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.error("Attempted to add owner to non-existent store: " + storeID);
                return ApiResponse.fail("store doesn't exist");
            }

            s.addNewOwner(appointerID, newOwnerID);
            logger.info("Added new owner: " + newOwnerID + " to store: " + storeID + ", by: " + appointerID);

            //((Subscriber)userRepository.findById(newOwnerID)).setStoreRole(storeID , "Owner");
            //PAY ATTENTION! לעשות בכל מקום

            return ApiResponse.ok(null);

        } catch (Exception e) {
            logger.error("Error adding owner: " + newOwnerID + " to store: " + storeID + ". Reason: " + e.getMessage());
            //TODO:we need to decide how to handle things here
            return ApiResponse.fail("Failed to add new owner: " + e.getMessage());
        }
    }

    /*
    requests to add a new owner to a store.
    if the founder requests, it does that.
    if an owner requests, so its send a notification to his appointer, to allow the appointment
    */
    public ApiResponse<Void> OwnerAppointmentRequest(String appointerID, String newOwnerId, String storeID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                return ApiResponse.fail("store doesn't exist");

            if (s.getFounderID().equals(appointerID)) {
                s.addNewOwner(appointerID, newOwnerId);
                logger.info("Founder " + appointerID + " added new owner: " + newOwnerId + " to store: " + storeID);
            } else {
                if (s.isOwner(newOwnerId)) {
                    logger.error(newOwnerId + " is already an Owner of store:" + storeID);
                    return ApiResponse.fail(newOwnerId + " is already an Owner of store:" + storeID);
                }

                if (!s.isOwner(appointerID)) {
                    logger.error(appointerID + " is NOT an Owner of store:" + storeID);
                    return ApiResponse.fail(appointerID + " is NOT an Owner of store:" + storeID);
                }

                String requestTO = s.OwnerAssignedBy(appointerID);
                logger.info("Owner appointment request: " + appointerID + " requests to appoint " + newOwnerId + " in store: " + storeID + ", request sent to: " + requestTO);

                //TODO:notify 'requestTO' that his assignee want to assign new owner
            }

            return ApiResponse.ok(null);

        } catch (Exception e) {
            logger.error("Error in owner appointment request for store: " + storeID + ". Reason: " + e.getMessage());
            //TODO:we need to decide how to handle things here
            return ApiResponse.fail("Failed to process appointment request: " + e.getMessage());
        }
    }


    /*
    removes 'toRemove' and all the people he assigned
    */
    public ApiResponse<List<List<String>>> removeOwner(String id, String toRemove, String storeID) {
        List<List<String>> ret = new ArrayList<>();
        ret.add(new ArrayList<>());
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                return ApiResponse.fail("store doesn't exist");

            List<List<String>> removedWorkers = s.removeOwner(id, toRemove);
            logger.info("Removed owner: " + toRemove + " from store: " + storeID + ", by: " + id);

            // for (String i:removedWorkers.get(0) ){
            //     ((Subscriber)userRepository.findById(i)).removeStoreRole(id,"Owner");
            //     //TODO: need to change data on those users
            // }
            // for (String i:removedWorkers.get(1) ){
            //     ((Subscriber)userRepository.findById(i)).removeStoreRole(id,"Manager");
            //     //TODO: need to change data on those users
            // }

            return ApiResponse.ok(removedWorkers);
        } catch (Exception e) {
            logger.error("Error removing owner: " + toRemove + " from store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error removing owner: " + e.getMessage());
        }
    }


    /**
     * Adds a new manager to the store by delegating to the business logic layer.
     * Only an existing owner of the store can appoint a new manager.
     *
     * @param appointerID ID of the owner appointing the new manager.
     * @param newManagerName ID of the user being assigned as a new manager.
     * @param storeID ID of the store where the manager is being added.
     * @return "success" if the manager was added successfully, "failed" if the operation was unsuccessful.
     * @throws RuntimeException if the store does not exist, the appointer is not an owner,
     *                           the new manager is already assigned, or any other business rule is violated.
     */
    public ApiResponse<Void> addNewManager(String appointerID, String newManagerName, String storeID){
        try{
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null){
                logger.error("Attempted to add manager to non-existent store: " + storeID);
                throw new Exception("store doesn't exist");
            }
            if (s.addNewManager(appointerID,newManagerName)){
                logger.info("Added new manager: " + newManagerName + " to store: " + storeID + ", by: " + appointerID);
                //((Subscriber)userRepository.findById(newManagerName)).setStoreRole(storeID,"Manager");
                return ApiResponse.ok(null);
            }
            else{
                logger.error("Failed to add manager: " + newManagerName + " to store: " + storeID + ", by: " + appointerID);
                return ApiResponse.fail("Failed to add manager: " + newManagerName + " to store: " + storeID + ", by: " + appointerID);
            }

        } catch (Exception e) {
            logger.error("Error adding manager: " + newManagerName + " to store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error adding manager: " + newManagerName + " to store: " + storeID + ". Reason: " + e.getMessage());
        }
    }

    /**
     * Grants a specific permission to a manager in the specified store.
     * Only the owner who originally appointed the manager can grant permissions.
     *
     * @param managerID    ID of the manager receiving the new permission.
     * @param appointerID  ID of the owner who appointed the manager and is now granting the permission.
     * @param permissionID Integer code representing the permission to assign (must be valid in {Permission} enum).
     * @param storeID      ID of the store where the manager belongs.
     * @return "success" if the permission was successfully added, "failed" if the operation did not complete.
     * @throws RuntimeException if the store does not exist, the appointer is not authorized,
     *                           the manager is invalid, the permission code is invalid,
     *                           or any other business rule violation occurs.
     */
    public ApiResponse<Void> addPermissionToManager(String managerID, String appointerID, int permissionID, String storeID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.error("Attempted to add permission to non-existent store: " + storeID);
                throw new Exception("store doesn't exist");
            }
            if (s.addPermissionToManager(managerID, appointerID, permissionID)) {
                logger.info("Added permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
                return ApiResponse.ok(null);
            } else {
                logger.error("Failed to add permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
                return ApiResponse.fail("Failed to add permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
            }
        } catch (Exception e) {
            logger.error("Error adding permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error adding permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
        }
    }

    /**
     * Retrieves the set of permission codes assigned to a specific manager in a store.
     * The requester must be either the store owner or a manager themself.
     *
     * @param managerID   ID of the manager whose permissions are being requested.
     * @param whoIsAsking ID of the user making the request (must be an owner or the manager).
     * @param storeID     ID of the store where the manager belongs.
     * @return A {@link Set} of integer codes representing the manager's current permissions.
     * @throws RuntimeException if the store does not exist, the requester is unauthorized,
     *                           the manager is invalid, or any other business rule violation occurs.
     */
    public ApiResponse<Set<Integer>> getManagersPermissions(String managerID, String whoIsAsking, String storeID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.error("Attempted to get permissions for non-existent store: " + storeID);
                throw new Exception("store doesn't exist");
            }
            logger.info("Retrieved permissions for manager: " + managerID + " in store: " + storeID + ", by: " + whoIsAsking);
            return ApiResponse.ok(s.getManagersPermmisions(managerID, whoIsAsking));
        } catch (Exception e) {
            logger.error("Error retrieving permissions for manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error retrieving permissions for manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
        }
    }

    /**
     * Removes a specific permission from a manager in the specified store.
     * Only the owner who originally appointed the manager can remove permissions.
     *
     * @param managerID    ID of the manager whose permission is being revoked.
     * @param permissionID Integer code representing the permission to remove (must be valid in { Permission} enum).
     * @param appointerID  ID of the owner who appointed the manager and is requesting to remove the permission.
     * @param storeID      ID of the store where the manager belongs.
     * @return "success" if the permission was successfully removed, "failed" if the operation did not complete.
     * @throws RuntimeException if the store does not exist, the appointer is unauthorized,
     *                           the manager ID is invalid, the permission code is invalid,
     *                           or any other business rule violation occurs.
     */
    public ApiResponse<Void> removePermissionFromManager(String managerID, int permissionID, String appointerID, String storeID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.error("Attempted to remove permission from non-existent store: " + storeID);
                throw new Exception("store doesn't exist");
            }
            if (s.removePermissionFromManager(managerID, permissionID, appointerID)) {
                logger.info("Removed permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
                return ApiResponse.ok(null);
            } else {
                logger.error("Failed to remove permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
                return ApiResponse.fail("Failed to remove permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
            }
        } catch (Exception e) {
            logger.error("Error removing permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error removing permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
        }
    }



    /**
     * Adds a new listing to the specified store.
     *
     * @param userName User trying to add.
     * @param storeID Store ID.
     * @param productId Product ID.
     * @param productName Product name.
     * @param productDescription Description of the product.
     * @param quantity Quantity to add.
     * @param price Price per unit.
     * @return "succeed" or error message.
     */
    public ApiResponse<String> addNewListing(String userName, String storeID, String productId, String productName, String productCategory, String productDescription, int quantity, double price) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new Exception("Store doesn't exist");
            logger.info("Added new listing: " + productName + " to store: " + storeID + ", by: " + userName);
            return ApiResponse.ok(s.addNewListing(userName, productId, productName, productCategory, productDescription, quantity, price));
        } catch (Exception e) {
            logger.error("Error adding listing: " + productName + " to store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error adding listing: " + productName + " to store: " + storeID + ". Reason: " + e.getMessage());
        }
        
    }




    /**
     * Removes a listing from the specified store.
     *
     * @param userName User ID.
     * @param storeID Store ID.
     * @param listingId ID of the listing to remove.
     * @return "succeed" or error message.
     */
    public ApiResponse<Void> removeListing(String userName, String storeID, String listingId) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new Exception("Store doesn't exist");
            logger.info("Removed listing: " + listingId + " from store: " + storeID + ", by: " + userName);
            if (s.removeListing(userName, listingId))
                return ApiResponse.ok(null);
            else {
                logger.error("Error removing listing: " + listingId + " from store: " + storeID );
                return ApiResponse.fail("Error removing listing: " + listingId + " from store: " + storeID);}
        } catch (Exception e) {
            logger.error("Error removing listing: " + listingId + " from store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error removing listing: " + listingId + " from store: " + storeID + ". Reason: " + e.getMessage());
        }
    }


    public ApiResponse<IListingRepository> getListingRepository(){
        return ApiResponse.ok(listingRepository);
    }


//I think this function is not relevant any more. all purchases are from purchase service
    /**
     * Purchases quantity from a listing.
     *
     *
     * @param storeID Store ID.
     * @param listingId ID of the listing to purchase from.
     * @param quantity How many units to buy.
     * @return "succeed" or error message.
     */
    public ApiResponse<Void> purchaseFromListing( String storeID, String listingId, int quantity) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                return ApiResponse.fail("Store doesn't exist");
            logger.info("Purchased " + quantity + " units from listing: " + listingId + " in store: " + storeID);
            s.purchaseFromListing(listingId, quantity);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            logger.error("Error purchasing from listing: " + listingId + " in store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error purchasing from listing: " + listingId + " in store: " + storeID + ". Reason: " + e.getMessage());
        }
    }

    public ApiResponse<Double> getProductPrice(String storeID, String productID) {
        Store s = storeRepository.getStoreByID(storeID);
        Map<String,Integer> prod = new HashMap<>();
        prod.put(productID,1);
        logger.info("Retrieved price for product: " + productID + " in store: " + storeID);
        return ApiResponse.ok(s.calculateStoreBagWithDiscount(prod));
    }



    public ApiResponse<Boolean> isOwner(String storeID, String userID){
        Store s = storeRepository.getStoreByID(storeID);
        return ApiResponse.ok(s.isOwner(userID));
    }

     public ApiResponse<Boolean> isManager(String storeID, String userID){
        Store s = storeRepository.getStoreByID(storeID);
        return ApiResponse.ok(s.isManager(userID));
    }
}
