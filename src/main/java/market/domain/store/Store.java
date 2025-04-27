package market.domain.store;

import market.domain.purchase.PurchaseType;

import java.util.*;
import java.util.stream.*;



/**
 * Represents a store with a hierarchical role-based structure of owners and managers.
 * Owners can assign other owners and managers, and manage product-related permissions.
 * Managers can be granted specific permissions (such as editing products or policies).
 * Products are managed through an injected {@link IStoreProductsManager} interface.
 */
public class Store {
    private String storeID;
    private String name;
    boolean active;

    String founderID;

    private HashMap<Integer,Integer> ownerToWhoAssignedHim;
    private HashMap<Integer, List<Integer>> ownerToAssignedOwners;
    //every mangaer has a key in the dict.
    // if manager 1 assign manager 2, so manager2 in managers[manager1]

    //TODO: implement manager interface (probably state design pattern) with appropriate permissions

    private HashMap<Integer, List<Manager>> ownerToAssignedManagers;

    // ? - do we need store type


    private IStoreProductsManager storeProductsManager;


    public Store(String storeID,String name, String founderID) throws IllegalArgumentException {
        // ? - do we need store type

        if(!isValidName(name)){
            //LOG - error
            throw new IllegalArgumentException("market.domain.store.Store name illegal!");
        }
        this.storeID=storeID;
        this.name = name;
        this.storeProductsManager = new StoreProductManager(storeID);
        this.active = true;

        this.ownerToWhoAssignedHim = new HashMap<>();
        this.ownerToAssignedOwners = new HashMap<>();
        this.ownerToAssignedManagers = new HashMap<>();

        //assigning founder as manager and owner
        this.founderID = founderID;
        this.ownerToAssignedOwners.put(founderID, new ArrayList<>());
        this.ownerToAssignedManagers.put(founderID, new ArrayList<>());

    }



    /*
        validates there are no illegal characters
     */
    //TODO:
    private boolean isValidName(String name) {
        return true;
    }

    /**
     * Adds a new owner to the store, assigned by an existing owner.
     * The new owner will be tracked in the ownership hierarchy.
     *
     * @param appointerID  ID of the current owner assigning the new owner.
     * @param newOwnerID   ID of the user to be added as a new owner.
     * @return {@code true} if the new owner was successfully added.
     * @throws Exception if the appointer is not an owner, or if the new owner is already registered as an owner.
     */
    public boolean addNewOwner(int appointerID, int newOwnerID) throws Exception {
       if (!isOwner(appointerID))
           throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);

        if (isOwner(newOwnerID))
            throw new Exception("the user:"+appointerID+" is already a owner of the store: "+storeID);

        ownerToAssignedOwners.get(appointerID).add(newOwnerID);
        ownerToAssignedOwners.put(newOwnerID,new ArrayList<>());
        ownerToWhoAssignedHim.put(newOwnerID,appointerID);
        ownerToAssignedManagers.put(newOwnerID,new ArrayList<>());
        return true;
    }


    /**
     * Adds a new manager to the store, assigned by an existing owner.
     * The manager will initially have no permissions until assigned explicitly.
     *
     * @param appointerID    ID of the current owner assigning the new manager.
     * @param newManagerID   ID of the user to be added as a new manager.
     * @return {@code true} if the manager was successfully added.
     * @throws Exception if the appointer is not an owner, or if the new manager is already registered as a manager.
     */
    public boolean addNewManager(int appointerID, int newManagerID) throws Exception {
        if (!isOwner(appointerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);

        if (isManager(newManagerID))
            throw new Exception("the user:"+appointerID+" is already a owner of the store: "+storeID);

        Manager newManager = new Manager(newManagerID, appointerID);
        return ownerToAssignedManagers.get(appointerID).add(newManager);
    }


    /**
     * Grants a specific permission to a manager.
     * The permission can only be assigned by the owner who originally appointed the manager.
     *
     * @param managerID     ID of the manager receiving the new permission.
     * @param appointerID   ID of the owner who assigned the manager and is now granting permission.
     * @param permissionID  Integer code representing the permission to assign (must be valid in {@link Permission} enum).
     * @return {@code true} if the permission was added successfully.
     * @throws Exception if the appointer is not an owner, the manager ID is not valid, the permission code is invalid,
     *                   or if the appointer is not the one who originally assigned the manager.
     */
    public boolean addPermissionToManager( int managerID, int appointerID, int permissionID) throws Exception {
        if(!isOwner(appointerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);
        if(!isManager(managerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);

        Manager manager = getManager(managerID);
        Permission p = Permission.fromCode(permissionID); //if invalid code, exception is thrown here

        manager.addPermission(p,appointerID); //if appointer is not the real one, throws here
        return true;
    }

    /**
     * Retrieves the set of permission codes granted to a specific manager in the store.
     * The caller must be either an owner of the store or the manager themself.
     *
     * @param managerID   ID of the manager whose permissions are being requested.
     * @param whoIsAsking ID of the user making the request (must be an owner or the manager).
     * @return A {@link Set} of integer codes representing the manager's current permissions.
     *         Each code corresponds to a value in the {@link Permission} enum.
     * @throws IllegalArgumentException if the requesting user is neither an owner nor the manager.
     * @throws Exception if the given manager ID does not correspond to a valid manager in this store.
     */
    public Set<Integer> getManagersPermmisions(int managerID, int whoIsAsking) throws Exception {
        if(!isOwner(whoIsAsking) && !isManager(managerID))
            throw new IllegalArgumentException("the user:"+whoIsAsking+" is not an owner or a manager of the store: "+storeID);
        Manager manager = getManager(managerID);
        if (manager == null)
            throw new Exception("the user:"+managerID+" is not a manager of the store: "+storeID);
        return manager.getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet());
    }


    /**
     * Removes a specific permission from a manager.
     * Only the owner who originally appointed the manager can revoke permissions from them.
     *
     * @param managerID     ID of the manager whose permission is being revoked.
     * @param permissionID  Integer code representing the permission to remove (must be valid in {@link Permission} enum).
     * @param appointerID   ID of the owner who appointed the manager and is requesting to remove the permission.
     * @return {@code true} if the permission was successfully removed.
     * @throws Exception if the appointer is not an owner of the store, the manager ID is invalid,
     *                   the permission code is invalid, or if the appointer is not the one who appointed the manager.
     */
    public boolean removePermissionFromManager(int managerID, int permissionID, int appointerID) throws Exception {
        if (!isOwner(appointerID))
            throw new Exception("The user: " + appointerID + " is not an owner of the store: " + storeID);

        if (!isManager(managerID))
            throw new Exception("The user: " + managerID + " is not a manager of the store: " + storeID);

        Manager manager = getManager(managerID);
        if (manager == null)
            throw new Exception("Manager with ID " + managerID + " not found in store: " + storeID);

        Permission permission = Permission.fromCode(permissionID); // throws if invalid

        return manager.removePermission(permission, appointerID); // throws if appointer isn't authorized
    }


    /**
     * Checks whether a given user ID corresponds to an owner of the store.
     *
     * @param id ID of the user to check.
     * @return {@code true} if the user is an owner of the store; {@code false} otherwise.
     */
    public boolean isOwner(int id){
        return ownerToAssignedOwners.containsKey(id);
    }

    /**
     * Checks whether a given user ID corresponds to a manager of the store.
     *
     * @param id ID of the user to check.
     * @return {@code true} if the user is a manager of the store; {@code false} otherwise.
     */
    public boolean isManager(int id){
        for(List<Manager> l :this.ownerToAssignedManagers.values()){
            for (Manager m : l){
                if(m.getID()==id){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the {@link Manager} object associated with a given user ID.
     *
     * @param id ID of the manager to retrieve.
     * @return The {@link Manager} instance if found; {@code null} otherwise.
     */
    private Manager getManager(int id){
        for (Manager m : getAllManagers()){
            if(m.getID()==id){
                return m;
            }
        }
        return null;
    }

    /**
     * Collects and returns a list of all managers assigned in the store.
     *
     * @return A list of all {@link Manager} instances currently assigned to the store.
     */
    private List<Manager> getAllManagers(){
        List<Manager> managers = new ArrayList<>();
        for(List<Manager> l :this.ownerToAssignedManagers.values()){
            managers.addAll(l);
        }
        return managers;
    }

    /**
     * Returns the ID of the owner who appointed the specified user as an owner.
     *
     * @param id ID of the user whose appointer is being queried.
     * @return The ID of the owner who assigned this user, or {@code -1} if the user has no recorded appointer.
     */
    public int OwnerAssignedBy(int id){
        if (ownerToWhoAssignedHim.containsKey(id)){
            return ownerToWhoAssignedHim.get(id);
        }
        return -1;
    }

    /**
     * Returns the ID of the founder of the store.
     * The founder is the original creator of the store and cannot be removed as an owner.
     *
     * @return The user ID of the store's founder.
     */
    public String getFounderID(){
        return founderID;
    }

    /**
     * Retrieves a list of owner IDs that were directly assigned by the specified owner.
     *
     * @param id ID of the owner whose assigned owners are requested.
     * @return A list of user IDs representing owners assigned by this user, or {@code null} if the user is not in the map.
     */
    public List<Integer> getOwnerAssigments(int id){
        return ownerToAssignedOwners.get(id);
    }


    /**
     * Removes an owner and all owners and managers they assigned recursively.
     * This method performs a cascading removal: if an owner is removed, all of their assigned owners and managers are removed too.
     *
     * @param id        ID of the owner performing the removal.
     * @param toRemove  ID of the owner to be removed.
     * @return A list of all user IDs (owners and managers) that were removed.
     * @throws Exception If:
     *                   - {@code id} is not an owner,
     *                   - {@code toRemove} is not an owner,
     *                   - {@code toRemove} is the founder,
     *                   - {@code id} is not the one who assigned {@code toRemove}.
     */
    public List<Integer> removeOwner(int id, int toRemove) throws Exception {
        List<Integer> res = new ArrayList<>();
        if (!isOwner(id)){
            throw new Exception(id +" is not a owner of store:"+ storeID);
        }
        if (!isOwner(toRemove)){
            throw new Exception(toRemove +" is not a owner of store:"+ storeID);
        }
        if (toRemove == getFounderID()){
            throw new Exception(toRemove +" is the FOUNDER of store:"+ storeID+ " can't remove him");
        }
        if (ownerToWhoAssignedHim.get(toRemove)!= id){
            throw new Exception(id +" didn't assign " + toRemove + " to be owner of store:"+ storeID);
        }
        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(toRemove);
        while(!queue.isEmpty()){
            int next = queue.remove();
            //add to remove list
            res.add(next);
            List<Integer> assigments = getOwnerAssigments(next);

            //enter the owners he assigned to the queue
            for (int a:assigments){
                queue.add(a);
            }
            //remove him

            ownerToAssignedOwners.get(OwnerAssignedBy(next)).remove(Integer.valueOf(next));
            ownerToWhoAssignedHim.remove(next);


            if (ownerToAssignedManagers.get(next)!= null){
                //remove the managers he assign
                for (Manager a: ownerToAssignedManagers.get(next)){
                    res.add(a.getID());
                }
                ownerToAssignedManagers.remove(next);
            }


        }
        for (int o:res){
            if (isOwner(o)){
                ownerToAssignedOwners.remove(o);
            }
        }
        return res;
    }

    /*
    if the user called is an owner,
    returns a hashmap.
    key - worker (manager or owner)
    value - set(permissions_values).   if manager
            NULL.                      if owner
     */
    public HashMap<Integer, Set<Integer>> getPositionsInStore(Integer requestUser) throws Exception{
        if(!isOwner(requestUser))
            throw new Exception("User " + requestUser.toString() + " is not an owner!");

        HashMap<Integer, Set<Integer>> workerToPermission = new HashMap<>();
        for(Integer owner : this.ownerToWhoAssignedHim.keySet()){
            workerToPermission.put(owner, null);
        }

        for(Manager m : getAllManagers()){
            Set<Permission> ps = m.getPermissions();
            workerToPermission.put(m.getID(), ps.stream().map(Permission::getCode).collect(Collectors.toSet()));
        }
        return workerToPermission;
    }





    /**
     * Adds a new listing to the store.
     * The user must have permission to edit products.
     *
     * @param userID User ID trying to add.
     * @param productId Product ID.
     * @param productName Product name.
     * @param productDescription Product description.
     * @param quantity Quantity to sell.
     * @param unitPrice Price per unit.
     * @return {@code true} if listing was added successfully.
     * @throws Exception if user lacks permission.
     */
    public boolean addNewListing(int userID, String productId, String productName, String productDescription, int quantity, int unitPrice) throws Exception {
        if (!checkProductsPermission(userID))
            throw new Exception("User " + userID + " doesn't have permission to ADD listing!");

        Listing newListing = new Listing(
                this.storeID,
                productId,
                productName,
                productDescription,
                quantity,
                PurchaseType.REGULAR,
                unitPrice
        );
        return storeProductsManager.addListing(newListing);
    }


    /**
     * Removes a listing from the store.
     * User must have permission.
     *
     * @param userID User attempting the removal.
     * @param listingId ID of the listing to remove.
     * @return {@code true} if successfully removed.
     * @throws Exception if user lacks permission.
     */
    public boolean removeListing(int userID, String listingId) throws Exception {
        if (!checkProductsPermission(userID))
            throw new Exception("User " + userID + " doesn't have permission to REMOVE listing!");

        return storeProductsManager.removeListing(listingId);
    }

    /**
     * Purchases quantity from a specific listing.
     *
     * @param userID User attempting to buy.
     * @param listingId ID of the listing.
     * @param quantity Quantity to purchase.
     * @return {@link PurchaseType} of the purchase attempt.
     * @throws Exception if permission is missing or invalid.
     */
    public boolean purchaseFromListing(int userID, String listingId, int quantity) throws Exception {
        // Note: Maybe here you don't even need permission checking - depends if buying is free to any user
        return storeProductsManager.purchaseFromListing(listingId, quantity);
    }

    public List<Listing> getListingsByProductName(String productName) {
        return storeProductsManager.getListingsByProductName(productName);
    }

    public List<Listing> getListingsByProductId(String productId) {
        return storeProductsManager.getListingsByProductId(productId);
    }

    public Listing getListing(String listingID) {
        return storeProductsManager.getListingById(listingID);
    }


    /**
     * Checks whether a user has permission to edit store products.
     * A user has permission if they are an owner or a manager with {@code EDIT_PRODUCTS} permission.
     *
     * @param userID ID of the user to check.
     * @return {@code true} if the user has permission to edit products, {@code false} otherwise.
     */
    public boolean checkProductsPermission(int userID){
        return isOwner(userID) || (isManager(userID) && getManager(userID).hasPermission(Permission.EDIT_PRODUCTS));
    }

    public String getStoreID() {
        return storeID;
    }

    public String getName() {
        return name;
    }

    public boolean isActive(){
        return active;
    }


    public enum Permission{
        VIEW_ONLY(0),
        EDIT_PRODUCTS(1),
        EDIT_POLICIES(2);

        private final int code;

        Permission(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        // Reverse lookup
        public static Permission fromCode(int code) throws IllegalArgumentException {
            for (Permission p : Permission.values()) {
                if (p.getCode() == code) {
                    return p;
                }
            }
            throw new IllegalArgumentException("Invalid permission code: " + code);
        }
    }

    class Manager{

        int id;
        int apointedBy;


        private Set<Permission> permissions;

        public Manager(int id, int apointedBy){
            this.id = id;
            this.apointedBy = apointedBy;
            permissions = new HashSet<>();
            permissions.add(Permission.VIEW_ONLY);
        }

        public boolean addPermission(Permission permission, int byWho) throws Exception {
            if(byWho != apointedBy){
                throw new Exception("user " + byWho + " cant add permission to manager " + this.id + " because he is not his appointer");
            }
            return permissions.add(permission);
        }

        public boolean removePermission(Permission permission, int byWho) throws Exception {
            if(byWho != apointedBy){
                throw new Exception("user " + byWho + " cant add permission to manager " + this.id + " because he is not his appointer");
            }
            return permissions.remove(permission);
        }

        public int getApointedBy() {
            return apointedBy;
        }
        public int getID() {
            return id;
        }

        public boolean hasPermission(Permission permission){
            return permissions.contains(permission);
        }

        public Set<Permission> getPermissions() {
            return permissions;
        }

    }

}