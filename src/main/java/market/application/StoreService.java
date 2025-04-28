package market.application;
import market.domain.store.*;

import java.util.List;
import java.util.Set;

public class StoreService {
    private IStoreRepository storeRepository;
    private String storeIDs ="1";

    public StoreService(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
        storeIDs = storeRepository.getNextStoreID();
    }

    public void createStore(String storeName, String founderId) throws Exception {
        // ? - do we need store type
        if(storeRepository.containsStore(storeName)) {
            // LOG - error
            throw new Exception("The storeName '" + storeName + "' already exists");
        }
        Store store = new Store(String.valueOf(storeIDs),storeName, founderId);
        //Who is responsable to manage the store id's????????
        storeRepository.addStore(store);
        //LOG - store added
    }

    public StoreDTO getStore(String storeName) throws Exception {

        Store store = storeRepository.getStoreByName(storeName);
        if(store == null) {
            return null;
        }
        return new StoreDTO(store);
    }


/*
appoints 'newOwner' to be an owner of 'storeID' BY 'appointerID'
assumes aggreement by 'apointerID''s appointer
 */
    public String addAdditionalStoreOwner(String appointerID, String newOwnerID, String storeID){
        try
        {
            Store s = storeRepository.getStoreByID(storeID);
            if (s==null){
                throw new Exception("store doesn't exist");
            }
            s.addNewOwner(appointerID,newOwnerID);
        }
        catch (Exception e){
            //TODO:we need to decide how to handle things here
            return e.getMessage();
    }
        return "success";
    }

    /*
    requests to add a new owner to a store.
    if the founder requests, it does that.
    if an owner requests, so its send a notification to his appointer, to allow the appointment
     */
    public String OwnerAppointmentRequest(String appointerID, String newOwnerId,String storeID){
        try{
            Store s = storeRepository.getStoreByID(storeID);
            if (s==null)
                throw new Exception("store doesn't exist");
            if (s.getFounderID().equals(appointerID)){
                s.addNewOwner(appointerID,newOwnerId);
            }
            else{
                if (s.isOwner(newOwnerId)){
                    throw new Exception(newOwnerId + " is already an Owner of store:"+storeID);
                }
                if (!s.isOwner(appointerID)){
                    throw new Exception(appointerID + " is NOT an Owner of store:"+storeID);
                }
                String requestTO=s.OwnerAssignedBy(appointerID);
                //TODO:notify 'requestTO' that his assignee want to assign new owner
            }
        }catch (Exception e){
            //TODO:we need to decide how to handle things here
            return e.getMessage();
        }
        return "success";
    }



    /*
    removes 'toRemove' and all the people he assigned
     */
    public String removeOwner(String id, String toRemove, String storeID){
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new Exception("store doesn't exist");
            List<String> removedWorkers =  s.removeOwner(id,toRemove);
            for (String i:removedWorkers ){
                //TODO: need to change data on those ussers
            }
        }
        catch (Exception e){
            return e.getMessage();
        }
        return "succeed";
    }

    /**
     * Adds a new manager to the store by delegating to the business logic layer.
     * Only an existing owner of the store can appoint a new manager.
     *
     * @param appointerID ID of the owner appointing the new manager.
     * @param newManagerID ID of the user being assigned as a new manager.
     * @param storeID ID of the store where the manager is being added.
     * @return "success" if the manager was added successfully, "failed" if the operation was unsuccessful.
     * @throws RuntimeException if the store does not exist, the appointer is not an owner,
     *                           the new manager is already assigned, or any other business rule is violated.
     */
    public String addNewManager(String appointerID, String newManagerID, String storeID){
        try{
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null){
                throw new Exception("store doesn't exist");
            }
            if (s.addNewManager(appointerID,newManagerID)){
                return "success";
            }
            else{
                return "failed";
            }

        } catch (Exception e) {
            return e.getMessage();
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
    public String addPermissionToManager(String managerID, String appointerID, int permissionID, String storeID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                throw new Exception("store doesn't exist");
            }
            if (s.addPermissionToManager(managerID, appointerID, permissionID)) {
                return "success";
            } else {
                return "failed";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                throw new Exception("store doesn't exist");
            }
            return s.getManagersPermmisions(managerID, whoIsAsking);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public String removePermissionFromManager(String managerID, int permissionID, String appointerID, String storeID) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null) {
                throw new Exception("store doesn't exist");
            }
            if (s.removePermissionFromManager(managerID, permissionID, appointerID)) {
                return "success";
            } else {
                return "failed";
            }
        } catch (Exception e) {


            throw new RuntimeException(e);
        }
    }



    /**
     * Adds a new listing to the specified store.
     *
     * @param userID User trying to add.
     * @param storeID Store ID.
     * @param productId Product ID.
     * @param productName Product name.
     * @param productDescription Description of the product.
     * @param quantity Quantity to add.
     * @param price Price per unit.
     * @return "succeed" or error message.
     */
    public String addNewListing(String userID, String storeID, String productId, String productName, String productDescription, int quantity, double price) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new Exception("Store doesn't exist");
            s.addNewListing(userID, productId, productName, productDescription, quantity, price);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "succeed";
    }




    /**
     * Removes a listing from the specified store.
     *
     * @param userID User ID.
     * @param storeID Store ID.
     * @param listingId ID of the listing to remove.
     * @return "succeed" or error message.
     */
    public String removeListing(String userID, String storeID, String listingId) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new Exception("Store doesn't exist");
            s.removeListing(userID, listingId);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "succeed";
    }



    /**
     * Purchases quantity from a listing.
     *
     *
     * @param storeID Store ID.
     * @param listingId ID of the listing to purchase from.
     * @param quantity How many units to buy.
     * @return "succeed" or error message.
     */
    public String purchaseFromListing( String storeID, String listingId, int quantity) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new Exception("Store doesn't exist");
            s.purchaseFromListing(listingId, quantity);
        } catch (Exception e) {
            return e.getMessage();
        }
        return "succeed";
    }

    public double getProductPrice(String storeID, String productID) {
        //TODO: implement this method to get product price
        return 0.0;
    }

    public String getProductListing(String storeID, String productID) {
        //TODO: implement this method to get product listing
        return null;
    }


    public Boolean checkAndUpdateStock(String storeID, String productID, int quantity) {
        //TODO: implement this method to check and update stock
        return true;
    }
}
