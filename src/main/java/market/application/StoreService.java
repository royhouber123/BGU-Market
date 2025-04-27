package market.application;
import market.domain.store.*;

import java.util.List;
import java.util.Set;

public class StoreService {
    private IStoreRepository storeRepository;
    private int storeIDs =1;

    public StoreService(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
        storeIDs = storeRepository.getNextStoreID();
    }

    public void createStore(String storeName, int founderId) throws Exception {
        // ? - do we need store type
        if(storeRepository.containsStore(storeName)) {
            // LOG - error
            throw new Exception("The storeName '" + storeName + "' already exists");
        }
        Store store = new Store(String.valueOf(storeIDs),storeName, founderId);
        storeIDs++;//Who is responsable to manage the store id's????????
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
    public String addAdditionalStoreOwner(int appointerID, int newOwnerID, int storeID){
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
    public String OwnerAppointmentRequest(int appointerID, int newOwnerId,int storeID){
        try{
            Store s = storeRepository.getStoreByID(storeID);
            if (s==null)
                throw new Exception("store doesn't exist");
            if (s.getFounderID()==appointerID){
                s.addNewOwner(appointerID,newOwnerId);
            }
            else{
                if (s.isOwner(newOwnerId)){
                    throw new Exception(newOwnerId + " is already an Owner of store:"+storeID);
                }
                if (!s.isOwner(appointerID)){
                    throw new Exception(appointerID + " is NOT an Owner of store:"+storeID);
                }
                int requestTO=s.OwnerAssignedBy(appointerID);
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
    public String removeOwner(int id, int toRemove, int storeID){
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new Exception("store doesn't exist");
            List<Integer> removedWorkers =  s.removeOwner(id,toRemove);
            for (int i:removedWorkers ){
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
    public String addNewManager(int appointerID, int newManagerID, int storeID){
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
    public String addPermissionToManager(int managerID, int appointerID, int permissionID, int storeID) {
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
    public Set<Integer> getManagersPermissions(int managerID, int whoIsAsking, int storeID) {
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
    public String removePermissionFromManager(int managerID, int permissionID, int appointerID, int storeID) {
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




//    /**
//     * Adds a new product to the specified store.
//     * Requires that the store exists and the user has permission to edit products.
//     *
//     * @param userID      ID of the user attempting to add the product.
//     * @param storeID     ID of the target store.
//     * @param productName Name of the product to be added.
//     * @param category    Category of the product.
//     * @param quantity    Quantity of the product to add.
//     * @param price       Price per unit of the product.
//     * @return "succeed" if the product was added successfully; otherwise, returns an error message.
//     */
//    public String addNewProduct(int userID, int storeID, String productName, String category, int quantity, int price) {
//        try {
//            Store s = storeRepository.getStoreByID(storeID);
//            if (s == null)
//                throw new Exception("store doesn't exist");
//            s.addNewListing(userID,productName,category,quantity,price);
//        }
//        catch (Exception e) {
//            return e.getMessage();
//        }
//        return "succeed";
//    }


    /**
     * Removes a product from the specified store.
     * Requires that the store exists and the user has permission to edit products.
     *
     * @param userID      ID of the user attempting to remove the product.
     * @param storeID     ID of the target store.
     * @param productName Name of the product to remove.
     * @return "succeed" if the product was removed successfully; otherwise, returns an error message.
     */
    public String removeProduct(int userID, int storeID, String productName) {
        try {
            Store s = storeRepository.getStoreByID(storeID);
            if (s == null)
                throw new Exception("store doesn't exist");
            s.removeListing(userID,productName);
        }
        catch (Exception e) {
            return e.getMessage();
        }
        return "succeed";
    }


//    /**
//     * Reduces the quantity of a specific product in the specified store.
//     * Requires that the store exists and the user has permission to edit products.
//     *
//     * @param userID      ID of the user attempting to reduce the product quantity.
//     * @param storeID     ID of the store containing the product.
//     * @param productName Name of the product whose quantity is to be reduced.
//     * @param howMuch     Amount to reduce from the current quantity.
//     * @return "succeed" if the quantity was reduced successfully; otherwise, an error message.
//     */
//    public String reduceProductQuantity(int userID, int storeID, String productName, int howMuch){
//        try {
//            Store s = storeRepository.getStoreByID(storeID);
//            if (s == null)
//                throw new Exception("store doesn't exist");
//            s.reduceProductQuantity(userID,productName,howMuch);
//        }
//        catch (Exception e) {
//            return e.getMessage();
//        }
//        return "succeed";
//    }


//    /**
//     * Updates the quantity of a specific product in the specified store.
//     * Requires that the store exists and the user has permission to edit products.
//     *
//     * @param userID      ID of the user performing the update.
//     * @param storeID     ID of the store where the product is located.
//     * @param productName Name of the product to update.
//     * @param howMuch     The new quantity or amount to update.
//     * @return "succeed" if the quantity was updated successfully; otherwise, an error message.
//     */
//    public String updateProductQuantity(int userID, int storeID, String productName, int howMuch) {
//        try {
//            Store s = storeRepository.getStoreByID(storeID);
//            if (s == null)
//                throw new Exception("store doesn't exist");
//            s.updateProductQuantity(userID, productName, howMuch);
//        } catch (Exception e) {
//            return e.getMessage();
//        }
//        return "succeed";
//    }


//    /**
//     * Adds a new product category to the specified store.
//     * Requires that the store exists and the user has permission to edit products.
//     *
//     * @param userID  ID of the user performing the operation.
//     * @param storeID ID of the store to which the category will be added.
//     * @param catName Name of the category to add.
//     * @return "succeed" if the category was added successfully; otherwise, an error message.
//     */
//    public String addCategory(int userID, int storeID, String catName) {
//        try {
//            Store s = storeRepository.getStoreByID(storeID);
//            if (s == null)
//                throw new Exception("store doesn't exist");
//            s.addCategory(userID, catName);
//        } catch (Exception e) {
//            return e.getMessage();
//        }
//        return "succeed";
//    }


//    /**
//     * Moves a product to a different category within the specified store.
//     * Requires that the store exists and the user has permission to edit products.
//     *
//     * @param userID      ID of the user performing the operation.
//     * @param storeID     ID of the store where the product resides.
//     * @param productName Name of the product to move.
//     * @param catName     Target category name.
//     * @return "succeed" if the product was moved successfully; otherwise, an error message.
//     */
//    public String moveProductToCategory(int userID, int storeID, String productName, String catName) {
//        try {
//            Store s = storeRepository.getStoreByID(storeID);
//            if (s == null)
//                throw new Exception("store doesn't exist");
//            s.moveProductToCategory(userID, productName, catName);
//        } catch (Exception e) {
//            return e.getMessage();
//        }
//        return "succeed";
//    }


//    /**
//     * Updates the price of a product in the specified store.
//     * Requires that the store exists and the user has permission to edit products.
//     *
//     * @param userID      ID of the user performing the update.
//     * @param storeID     ID of the store containing the product.
//     * @param productName Name of the product whose price is to be updated.
//     * @param newPrice    New price to set for the product.
//     * @return "succeed" if the price was updated successfully; otherwise, an error message.
//     */
//    public String updateProductPrice(int userID, int storeID, String productName, int newPrice) {
//        try {
//            Store s = storeRepository.getStoreByID(storeID);
//            if (s == null)
//                throw new Exception("store doesn't exist");
//            s.updateProductPrice(userID, productName, newPrice);
//        } catch (Exception e) {
//            return e.getMessage();
//        }
//        return "succeed";
//    }

    public double getProductPrice(String storeID, String productID) {
        //TODO: implement this method to get product price
        return 0.0;
    }


    public Boolean checkAndUpdateStock(String storeID, String productID, int quantity) {
        //TODO: implement this method to check and update stock
        return true;
    }
}
