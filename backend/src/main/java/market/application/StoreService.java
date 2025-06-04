package market.application;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;

import market.domain.store.IListingRepository;
import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.domain.store.StoreDTO;
import market.domain.user.ISuspensionRepository;
import market.domain.user.IUserRepository;
import utils.ApiResponse;
import utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreService {
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IListingRepository listingRepository;
    private String storeIDs ="1";
    private Logger logger = Logger.getInstance();
    private ISuspensionRepository suspentionRepository; 

    public StoreService(IStoreRepository storeRepository, IUserRepository userRepository, IListingRepository listingRepository,ISuspensionRepository suspentionRepository) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        storeIDs = storeRepository.getNextStoreID();
        this.listingRepository = listingRepository;
        this.suspentionRepository= suspentionRepository;
    }


    /*
    * return storeId
    */
    public market.dto.StoreDTO.CreateStoreResponse createStore(String storeName, String founderId) {
        try {
            suspentionRepository.checkNotSuspended(founderId);// check if user is suspended
            // ? - do we need store type
            if(storeRepository.containsStore(storeName)) {
                logger.debug("Attempted to create store with existing name: " + storeName);
                throw new IllegalArgumentException("The storeName '" + storeName + "' already exists");
            }
            //String id = storeIDs;
            storeIDs = String.valueOf(Integer.valueOf(storeIDs) + 1);
            Store store = new Store(String.valueOf(storeIDs),storeName, founderId,listingRepository);
            //Who is responsable to manage the store id's????????
            storeRepository.addStore(store);
            logger.info("Store created: " + storeName + ", founder: " + founderId + ", id: " + storeIDs);            
            //((Subscriber)userRepository.findById(founderId)).setStoreRole(store.getStoreID(), "Founder");
            
            return new market.dto.StoreDTO.CreateStoreResponse(store.getStoreID());
            //LOG - store added
        } catch (Exception e) {
            logger.error("Failed to create store: " + storeName + ", founder: " + founderId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to create store: " + e.getMessage());
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
    public String closeStore(String storeID, String userName) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.debug("Attempted to close non-existent store: " + storeID);
                throw new IllegalArgumentException("store doesn't exist");
            }

            if (!s.closeStore(userName)) {
                logger.debug("Failed to close store: " + storeID);
                throw new RuntimeException("store can't be closed");
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
            return storeID;

        } catch (Exception e) {
            logger.error("Error closing store: " + storeID + ", by user: " + userName + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error closing store: " + e.getMessage());
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
    public String openStore(String storeID, String userName) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.debug("Attempted to open non-existent store: " + storeID);
                throw new IllegalArgumentException("store doesn't exist");
            }

            s.openStore(userName);
            logger.info("Store opened: " + storeID + ", by user: " + userName);

            //TODO:need to notify all the owners and managers

            return storeID;

        } catch (Exception e) {
            logger.error("Error opening store: " + storeID + ", by user: " + userName + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error opening store: " + e.getMessage());
        }
    }


    

    /**
     * Retrieves a store by its name and returns a StoreDTO.
     *
     * @param storeName the name of the store
     * @return ApiResponse with StoreDTO if found, or error message if not
     */
    public StoreDTO getStore(String storeName) {
        try {
            Store store = storeRepository.getStoreByName(storeName);
            if (store == null) {
                logger.debug("Attempted to get non-existent store: " + storeName);
                throw new IllegalArgumentException("Store '" + storeName + "' does not exist");
            }
            logger.info("Store retrieved: " + storeName);
            return new StoreDTO(store);
        } catch (Exception e) {
            logger.error("Error retrieving store: " + storeName + ". Reason: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve store: " + e.getMessage());
        }
    }


    /*
    appoints 'newOwner' to be an owner of 'storeID' BY 'appointerID'
    assumes aggreement by 'apointerID''s appointer
    */
    public Void addAdditionalStoreOwner(String appointerID, String newOwnerID, String storeID) {
        try {
            suspentionRepository.checkNotSuspended(appointerID);// check if appointer is suspended
            suspentionRepository.checkNotSuspended(newOwnerID);// check if newOwner is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.debug("Attempted to add owner to non-existent store: " + storeID);
                throw new IllegalArgumentException("store doesn't exist");
            }

            // Validate that the user being added exists in the system
            try {
                userRepository.findById(newOwnerID);
            } catch (Exception e) {
                logger.debug("Attempted to add non-existent user as owner: " + newOwnerID + " to store: " + storeID);
                throw new IllegalArgumentException("User '" + newOwnerID + "' does not exist in the system");
            }

            s.addNewOwner(appointerID, newOwnerID);
            logger.info("Added new owner: " + newOwnerID + " to store: " + storeID + ", by: " + appointerID);

            //((Subscriber)userRepository.findById(newOwnerID)).setStoreRole(storeID , "Owner");
            //PAY ATTENTION! לעשות בכל מקום

            return null;

        } catch (Exception e) {
            logger.error("Error adding owner: " + newOwnerID + " to store: " + storeID + ". Reason: " + e.getMessage());
            //TODO:we need to decide how to handle things here
            throw new RuntimeException("Failed to add new owner: " + e.getMessage());
        }
    }

    /*
    requests to add a new owner to a store.
    if the founder requests, it does that.
    if an owner requests, so its send a notification to his appointer, to allow the appointment
    */
    public Void OwnerAppointmentRequest(String appointerID, String newOwnerId, String storeID) {
        try {
            suspentionRepository.checkNotSuspended(appointerID);// check if user is suspended
            suspentionRepository.checkNotSuspended(newOwnerId);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("store doesn't exist");

            if (s.getFounderID().equals(appointerID)) {
                s.addNewOwner(appointerID, newOwnerId);
                logger.info("Founder " + appointerID + " added new owner: " + newOwnerId + " to store: " + storeID);
            } else {
                if (s.isOwner(newOwnerId)) {
                    logger.debug(newOwnerId + " is already an Owner of store:" + storeID);
                    throw new IllegalArgumentException(newOwnerId + " is already an Owner of store:" + storeID);
                }

                if (!s.isOwner(appointerID)) {
                    logger.debug(appointerID + " is NOT an Owner of store:" + storeID);
                    throw new IllegalArgumentException(appointerID + " is NOT an Owner of store:" + storeID);
                }

                String requestTO = s.OwnerAssignedBy(appointerID);
                logger.info("Owner appointment request: " + appointerID + " requests to appoint " + newOwnerId + " in store: " + storeID + ", request sent to: " + requestTO);

                //TODO:notify 'requestTO' that his assignee want to assign new owner
            }

            return null;

        } catch (Exception e) {
            logger.error("Error in owner appointment request for store: " + storeID + ". Reason: " + e.getMessage());
            //TODO:we need to decide how to handle things here
            throw new RuntimeException("Failed to process appointment request: " + e.getMessage());
        }
    }


    /*
    removes 'toRemove' and all the people he assigned
    */
    public List<List<String>> removeOwner(String id, String toRemove, String storeID) {
        List<List<String>> ret = new ArrayList<>();
        ret.add(new ArrayList<>());
        try {
            suspentionRepository.checkNotSuspended(id);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
            throw new IllegalArgumentException("store doesn't exist");

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

            return removedWorkers;
        } catch (Exception e) {
            logger.error("Error removing owner: " + toRemove + " from store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error removing owner: " + e.getMessage());
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
    public Void addNewManager(String appointerID, String newManagerName, String storeID){
        try{
            suspentionRepository.checkNotSuspended(appointerID);// check if user is suspended
            suspentionRepository.checkNotSuspended(newManagerName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null){
                logger.debug("Attempted to add manager to non-existent store: " + storeID);
                throw new IllegalArgumentException("store doesn't exist");
            }

            // Validate that the user being added exists in the system
            try {
                userRepository.findById(newManagerName);
            } catch (Exception e) {
                logger.debug("Attempted to add non-existent user as manager: " + newManagerName + " to store: " + storeID);
                throw new IllegalArgumentException("User '" + newManagerName + "' does not exist in the system");
            }

            if (s.addNewManager(appointerID,newManagerName)){
                logger.info("Added new manager: " + newManagerName + " to store: " + storeID + ", by: " + appointerID);
                //((Subscriber)userRepository.findById(newManagerName)).setStoreRole(storeID,"Manager");
                return null;
            }
            else{
                logger.error("Failed to add manager: " + newManagerName + " to store: " + storeID + ", by: " + appointerID);
                throw new RuntimeException("Failed to add manager: " + newManagerName + " to store: " + storeID + ", by: " + appointerID);
            }

        } catch (Exception e) {
            logger.error("Error adding manager: " + newManagerName + " to store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error adding manager: " + newManagerName + " to store: " + storeID + ". Reason: " + e.getMessage());
        }
    }

    
    /**
     * Removes a manager from the store by delegating to the business logic layer.
     * Only the owner who originally appointed the manager can remove them.
     *
     * @param appointerID ID of the owner who appointed the manager.
     * @param managerID   ID of the manager to be removed.
     * @param storeID     ID of the store where the manager is being removed from.
     * @return "success" if the manager was removed successfully, or an error message if the operation failed.
     * @throws RuntimeException if the store does not exist, the appointer is not authorized,
     *                          or the manager was not assigned by this appointer.
     */
    public Void removeManager(String appointerID, String managerID, String storeID) {
        try {
            suspentionRepository.checkNotSuspended(appointerID);// check if user is suspended
            suspentionRepository.checkNotSuspended(managerID);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.debug("Attempted to remove manager from non-existent store: " + storeID);
                throw new IllegalArgumentException("store doesn't exist");
            }

            if (s.removeManager(appointerID, managerID)) {
                logger.info("Removed manager: " + managerID + " from store: " + storeID + ", by: " + appointerID);
                return null;
            } else {
                logger.error("Failed to remove manager: " + managerID + " from store: " + storeID + ", by: " + appointerID);
                throw new RuntimeException("Failed to remove manager: " + managerID + " from store: " + storeID + ", by: " + appointerID);
            }

        } catch (Exception e) {
            logger.error("Error removing manager: " + managerID + " from store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error removing manager: " + managerID + " from store: " + storeID + ". Reason: " + e.getMessage());
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
    public Void addPermissionToManager(String managerID, String appointerID, int permissionID, String storeID) {
        try {
            suspentionRepository.checkNotSuspended(managerID);// check if user is suspended
            suspentionRepository.checkNotSuspended(appointerID);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.debug("Attempted to add permission to non-existent store: " + storeID);
                throw new IllegalArgumentException("store doesn't exist");
            }
            if (s.addPermissionToManager(managerID, appointerID, permissionID)) {
                logger.info("Added permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
                return null;
            } else {
                logger.error("Failed to add permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
                throw new RuntimeException("Failed to add permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
            }
        } catch (Exception e) {
            logger.error("Error adding permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error adding permission: " + permissionID + " to manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
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
    public Set<Integer> getManagersPermissions(String managerID, String whoIsAsking, String storeID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.debug("Attempted to get permissions for non-existent store: " + storeID);
                throw new IllegalArgumentException("store doesn't exist");
            }
            logger.info("Retrieved permissions for manager: " + managerID + " in store: " + storeID + ", by: " + whoIsAsking);
            return s.getManagersPermmisions(managerID, whoIsAsking);
        } catch (Exception e) {
            logger.error("Error retrieving permissions for manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error retrieving permissions for manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
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
    public Void removePermissionFromManager(String managerID, int permissionID, String appointerID, String storeID) {
        try {
            suspentionRepository.checkNotSuspended(managerID);// check if user is suspended
            suspentionRepository.checkNotSuspended(appointerID);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                logger.debug("Attempted to remove permission from non-existent store: " + storeID);
                throw new IllegalArgumentException("store doesn't exist");
            }
            if (s.removePermissionFromManager(managerID, permissionID, appointerID)) {
                logger.info("Removed permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
                return null;
            } else {
                logger.error("Failed to remove permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
                throw new RuntimeException("Failed to remove permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ", by: " + appointerID);
            }
        } catch (Exception e) {
            logger.error("Error removing permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error removing permission: " + permissionID + " from manager: " + managerID + " in store: " + storeID + ". Reason: " + e.getMessage());
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
     * @param purchaseType Purchase type (REGULAR, BID, AUCTION, RAFFLE).
     * @return "succeed" or error message.
     */
    public String addNewListing(String userName, String storeID, String productId, String productName, String productCategory, String productDescription, int quantity, double price, String purchaseType) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("Store doesn't exist");
            logger.info("Added new listing: " + productName + " to store: " + storeID + ", by: " + userName + " with purchase type: " + purchaseType);
            return s.addNewListing(userName, productId, productName, productCategory, productDescription, quantity, price, purchaseType);
        } catch (Exception e) {
            logger.error("Error adding listing: " + productName + " to store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error adding listing: " + productName + " to store: " + storeID + ". Reason: " + e.getMessage());
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
    public Void removeListing(String userName, String storeID, String listingId) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("Store doesn't exist");
            logger.info("Removed listing: " + listingId + " from store: " + storeID + ", by: " + userName);
            if (s.removeListing(userName, listingId))
                return null;
            else {
                logger.error("Error removing listing: " + listingId + " from store: " + storeID );
                throw new RuntimeException("Error removing listing: " + listingId + " from store: " + storeID);}
        } catch (Exception e) {
            logger.error("Error removing listing: " + listingId + " from store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error removing listing: " + listingId + " from store: " + storeID + ". Reason: " + e.getMessage());
        }
    }

    public boolean editListingPrice(String userName, String storeID, String listingId, double newPrice) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("Store doesn't exist");
            logger.info("User " + userName + " editing price for listing " + listingId + " in store " + storeID);
            return s.editPriceForListing(userName, listingId, newPrice);
        } catch (Exception e) {
            logger.error("Error editing price for listing: " + listingId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error editing price: " + e.getMessage());
        }
    }


    public boolean editListingProductName(String userName, String storeID, String listingId, String newName) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("Store doesn't exist");
            logger.info("User " + userName + " editing name for listing " + listingId + " in store " + storeID);
            return s.editProductName(userName, listingId, newName);
        } catch (Exception e) {
            logger.error("Error editing product name for listing: " + listingId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error editing product name: " + e.getMessage());
        }
    }

    public boolean editListingDescription(String userName, String storeID, String listingId, String newDescription) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("Store doesn't exist");
            logger.info("User " + userName + " editing description for listing " + listingId + " in store " + storeID);
            return s.editProductDescription(userName, listingId, newDescription);
        } catch (Exception e) {
            logger.error("Error editing description for listing: " + listingId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error editing description: " + e.getMessage());
        }
    }

    public boolean editListingQuantity(String userName, String storeID, String listingId, int newQuantity) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("Store doesn't exist");
            logger.info("User " + userName + " editing quantity for listing " + listingId + " in store " + storeID);
            return s.editProductQuantity(userName, listingId, newQuantity);
        } catch (Exception e) {
            logger.error("Error editing quantity for listing: " + listingId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error editing quantity: " + e.getMessage());
        }
    }

    public boolean editListingCategory(String userName, String storeID, String listingId, String newCategory) {
        try {
            suspentionRepository.checkNotSuspended(userName);// check if user is suspended
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("Store doesn't exist");
            logger.info("User " + userName + " editing category for listing " + listingId + " in store " + storeID);
            return s.editProductCategory(userName, listingId, newCategory);
        } catch (Exception e) {
            logger.error("Error editing category for listing: " + listingId + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error editing category: " + e.getMessage());
        }
    }


    public IListingRepository getListingRepository(){
        return listingRepository;
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
    public Void purchaseFromListing( String storeID, String listingId, int quantity) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new IllegalArgumentException("Store doesn't exist");
            logger.info("Purchased " + quantity + " units from listing: " + listingId + " in store: " + storeID);
            s.purchaseFromListing(listingId, quantity);
            return null;
        } catch (Exception e) {
            logger.error("Error purchasing from listing: " + listingId + " in store: " + storeID + ". Reason: " + e.getMessage());
            throw new RuntimeException("Error purchasing from listing: " + listingId + " in store: " + storeID + ". Reason: " + e.getMessage());
        }
    }

    public double getProductPrice(String storeID, String productID) {
        Store s = storeRepository.getStoreByID(storeID);
        Map<String,Integer> prod = new HashMap<>();
        prod.put(productID,1);
        logger.info("Retrieved price for product: " + productID + " in store: " + storeID);
        return s.calculateStoreBagWithDiscount(prod);
    }

    public ApiResponse<Double> getProductDiscountedPrice(String storeID, String listingID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                return ApiResponse.fail("Store doesn't exist");
            }
            
            // Use the existing method that calculates discounted price
            double discountedPrice = s.ProductPriceWithDiscount(listingID);
            
            logger.info("Retrieved discounted price for listing: " + listingID + " in store: " + storeID + " - Final price: " + discountedPrice);
            return ApiResponse.ok(discountedPrice);
        } catch (Exception e) {
            logger.error("Error getting discounted price for listing: " + listingID + " in store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error getting discounted price: " + e.getMessage());
        }
    }

    public boolean isOwner(String storeID, String userID){
        Store s = storeRepository.getStoreByID(storeID);
        return s.isOwner(userID);
    }

     public boolean isManager(String storeID, String userID){
        Store s = storeRepository.getStoreByID(storeID);
        return s.isManager(userID);
    }

    public ApiResponse<Boolean> isFounder(String storeID, String userID){
        Store s = storeRepository.getStoreByID(storeID);
        if (s == null) {
            return ApiResponse.fail("Store doesn't exist");
        }
        return ApiResponse.ok(s.getFounderID().equals(userID));
    }

    /**
     * Get current user's permissions and role in a specific store.
     *
     * @param storeID The store ID to check permissions for
     * @param userID The user ID to check
     * @return Map containing role, permissions, and other relevant info
     */
    public ApiResponse<Map<String, Object>> getCurrentUserPermissions(String storeID, String userID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                return ApiResponse.fail("Store doesn't exist");
            }
            
            Map<String, Object> result = new HashMap<>();
            
            // Check roles in order of hierarchy
            if (s.getFounderID().equals(userID)) {
                result.put("role", "FOUNDER");
                result.put("canEditProducts", true);
                result.put("canEditPolicies", true);
                result.put("canApproveBids", true);
                result.put("canManageUsers", true);
                result.put("permissions", List.of(0, 1, 2, 3)); // All permissions
            } else if (s.isOwner(userID)) {
                result.put("role", "OWNER");
                result.put("canEditProducts", true);
                result.put("canEditPolicies", true);
                result.put("canApproveBids", true);
                result.put("canManageUsers", true);
                result.put("permissions", List.of(0, 1, 2, 3)); // All permissions
            } else if (s.isManager(userID)) {
                result.put("role", "MANAGER");
                Set<Integer> managerPermissions = s.getManagersPermmisions(userID, userID);
                result.put("permissions", new ArrayList<>(managerPermissions));
                
                // Set specific capabilities based on permissions
                result.put("canEditProducts", managerPermissions.contains(1));
                result.put("canEditPolicies", managerPermissions.contains(2));
                result.put("canApproveBids", managerPermissions.contains(3));
                result.put("canManageUsers", false); // Managers can't manage other users
            } else {
                result.put("role", "NONE");
                result.put("canEditProducts", false);
                result.put("canEditPolicies", false);
                result.put("canApproveBids", false);
                result.put("canManageUsers", false);
                result.put("permissions", List.of());
            }
            
            logger.info("Retrieved permissions for user: " + userID + " in store: " + storeID + " - Role: " + result.get("role"));
            return ApiResponse.ok(result);
            
        } catch (Exception e) {
            logger.error("Error getting user permissions for user: " + userID + " in store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error getting user permissions: " + e.getMessage());
        }
    }

    /**
     * Get all store users (owners and managers) with their roles and permissions.
     *
     * @param storeID The store ID to get users for
     * @param requesterId The ID of the user making the request (must be owner)
     * @return Map containing store users data
     */
    public ApiResponse<Map<String, Object>> getStoreUsers(String storeID, String requesterId) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                return ApiResponse.fail("Store doesn't exist");
            }
            
            // Check if requester has permission (must be owner)
            if (!s.isOwner(requesterId)) {
                return ApiResponse.fail("You don't have permission to view store users");
            }
            
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> users = new ArrayList<>();
            
            // Add founder
            Map<String, Object> founder = new HashMap<>();
            founder.put("id", s.getFounderID());
            founder.put("role", "FOUNDER");
            founder.put("permissions", List.of(0, 1, 2, 3));
            founder.put("appointerID", null);
            founder.put("canRemove", false); // Founder cannot be removed
            users.add(founder);
            
            // Add owners (excluding founder to avoid duplicates)
            for (String ownerID : s.getAllOwners()) {
                // Skip founder since we already added them above
                if (ownerID.equals(s.getFounderID())) {
                    continue;
                }
                
                Map<String, Object> owner = new HashMap<>();
                owner.put("id", ownerID);
                owner.put("role", "OWNER");
                owner.put("permissions", List.of(0, 1, 2, 3));
                owner.put("appointerID", s.OwnerAssignedBy(ownerID));
                // Owner can be removed by their appointer or by the requester if they appointed them
                boolean canRemove = requesterId.equals(s.OwnerAssignedBy(ownerID)) || 
                                  (s.getOwnerAssigments(requesterId) != null && s.getOwnerAssigments(requesterId).contains(ownerID));
                owner.put("canRemove", canRemove);
                users.add(owner);
            }
            
            // Add managers
            for (String managerID : s.getAllManagersStrs()) {
                Map<String, Object> manager = new HashMap<>();
                manager.put("id", managerID);
                manager.put("role", "MANAGER");
                
                try {
                    Set<Integer> permissions = s.getManagersPermmisions(managerID, requesterId);
                    manager.put("permissions", new ArrayList<>(permissions));
                } catch (Exception e) {
                    manager.put("permissions", List.of());
                }
                
                manager.put("appointerID", requesterId); // Simplified for now
                manager.put("canRemove", true); // Any owner can remove managers for now
                users.add(manager);
            }
            
            result.put("users", users);
            result.put("totalUsers", users.size());
            
            logger.info("Retrieved store users for store: " + storeID + " by requester: " + requesterId);
            return ApiResponse.ok(result);
            
        } catch (Exception e) {
            logger.error("Error getting store users for store: " + storeID + ". Reason: " + e.getMessage());
            return ApiResponse.fail("Error getting store users: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getInformationAboutStoresAndProducts(){
        List<Map<String, Object>> res = new ArrayList<>();
        for (Store s: storeRepository.getAllActiveStores()){
            Map<String, Object> storeInfo = new HashMap<>();
            StoreDTO storeDTO = new StoreDTO(s);
            
            // Create a proper store object structure
            Map<String, Object> store = new HashMap<>();
            store.put("storeID", storeDTO.getStoreID());
            store.put("storeName", storeDTO.getName());
            store.put("isActive", storeDTO.isActive());
            store.put("founderId", s.getFounderID());
            store.put("description", "Store description"); // Add default description
            store.put("rating", 0); // Add default rating
            
            storeInfo.put("store", store);
            storeInfo.put("listings", s.getAllListings());
            res.add(storeInfo);
        } 
        return res;
    }
}
