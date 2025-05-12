package market.domain.store;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import market.domain.purchase.PurchaseType;
import market.domain.store.Policies.DiscountPolicy;
import market.domain.store.Policies.PolicyHandler;
import market.domain.store.Policies.PurchasePolicy;



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

    private HashMap<String,String> ownerToWhoAssignedHim;
    private HashMap<String, List<String>> ownerToAssignedOwners;
    //every mangaer has a key in the dict.
    // if manager 1 assign manager 2, so manager2 in managers[manager1]


    //TODO: implement manager interface (probably state design pattern) with appropriate permissions

    private HashMap<String, List<Manager>> ownerToAssignedManagers;



    private IStoreProductsManager storeProductsManager;

    private PolicyHandler policyHandler;


    public Store(String storeID,String name, String founderID, IListingRepository repository) throws IllegalArgumentException {
        // ? - do we need store type

        if(!isValidName(name)){
            //LOG - error
            throw new IllegalArgumentException("market.domain.store.Store name illegal!");
        }
        this.storeID=storeID;
        this.name = name;
        this.storeProductsManager = new StoreProductManager(storeID,repository);
        this.active = true;

        this.ownerToWhoAssignedHim = new HashMap<>();
        this.ownerToAssignedOwners = new HashMap<>();
        this.ownerToAssignedManagers = new HashMap<>();

        //assigning founder as manager and owner
        this.founderID = founderID;
        this.ownerToAssignedOwners.put(founderID, new ArrayList<>());
        this.ownerToAssignedManagers.put(founderID, new ArrayList<>());

        this.policyHandler = new PolicyHandler();
    }


    /**
     * Closes the store.
     * Only the founder of the store is allowed to perform this action.
     * After closing, the store will become inactive and certain operations will be blocked.
     *
     * @param userID ID of the user attempting to close the store.
     * @return {@code true} if the store was successfully closed.
     * @throws Exception if:
     *      - The user is not the founder of the store.
     *      - The store is already closed.
     */
    public boolean closeStore(String userID) throws Exception {
        if (!userID.equals(founderID))
            throw new Exception("User:"+ userID +" is not the founder of store:"+ storeID);
        if (!active)
            throw new Exception("the store:"+ storeID +" is already closed");
        active = false;
        //need to update the status of all listings on store 
        return true;
    }

    /**
     * Checks whether the store is currently open (active).
     *
     * @return {@code true} if the store is open; {@code false} if it is closed.
     */
    public boolean isOpen() {
        return active;
    }

    /**
     * Throws an exception if the store is currently closed.
     * Useful as a helper method to enforce that certain operations can only happen when the store is open.
     *
     * @throws Exception if the store is closed.
     */
    private void storeClosedExeption() throws Exception {
        if (!active){
            throw new Exception("Store:" + storeID + " is closed for now");
        }
    }

    /**
     * Reopens the store.
     * Only the founder of the store is allowed to perform this action.
     * After reopening, the store becomes active and available for operations.
     *
     * @param userID ID of the user attempting to open the store.
     * @return {@code true} if the store was successfully reopened.
     * @throws Exception if:
     *      - The user is not the founder of the store.
     *      - The store is already open.
     */
    public boolean openStore(String userID) throws Exception {
        if (!userID.equals(founderID)){
            throw new Exception("User:"+ userID +" is not the founder of store:"+ storeID);
        }
        if (active)
            throw new Exception("Store:" + storeID + " is already open");
        active = true;
        //TODO: need to update all the listings status 
        return true;
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
    public boolean addNewOwner(String appointerID, String newOwnerID) throws Exception {
       if (!isOwner(appointerID))
           throw new Exception("the user:"+appointerID+" is not an owner of the store: "+storeID);

        if (isOwner(newOwnerID))
            throw new Exception("the user:"+appointerID+" is already an owner of the store: "+storeID);
        storeClosedExeption();//actions are available only when open
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
    public boolean addNewManager(String appointerID, String newManagerID) throws Exception {
        if (!isOwner(appointerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);

        if (isManager(newManagerID))
            throw new Exception("the user:"+newManagerID+" is already a manager of the store: "+storeID);
        storeClosedExeption();//actions are available only when open
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
    public boolean addPermissionToManager( String managerID, String appointerID, int permissionID) throws Exception {
        if(!isOwner(appointerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);
        if(!isManager(managerID))
            throw new Exception("the user:"+appointerID+" is not a owner of the store: "+storeID);
        storeClosedExeption();//actions are available only when open
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
    public Set<Integer> getManagersPermmisions(String managerID, String whoIsAsking) throws Exception {
        if(!isOwner(whoIsAsking) && !isManager(managerID))
            throw new IllegalArgumentException("the user:"+whoIsAsking+" is not an owner or a manager of the store: "+storeID);
        Manager manager = getManager(managerID);
        if (manager == null)
            throw new Exception("the user:"+managerID+" is not a manager of the store: "+storeID);
        return manager.getPermissions().stream().map((perm)->(Integer)perm.getCode()).collect(Collectors.toSet());
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
    public boolean removePermissionFromManager(String managerID, int permissionID, String appointerID) throws Exception {
        if (!isOwner(appointerID))
            throw new Exception("The user: " + appointerID + " is not an owner of the store: " + storeID);

        if (!isManager(managerID))
            throw new Exception("The user: " + managerID + " is not a manager of the store: " + storeID);

        Manager manager = getManager(managerID);
        if (manager == null)
            throw new Exception("Manager with ID " + managerID + " not found in store: " + storeID);
        storeClosedExeption();//actions are available only when open
        Permission permission = Permission.fromCode(permissionID); // throws if invalid

        return manager.removePermission(permission, appointerID); // throws if appointer isn't authorized
    }


    /**
     * Checks whether a given user ID corresponds to an owner of the store.
     *
     * @param id ID of the user to check.
     * @return {@code true} if the user is an owner of the store; {@code false} otherwise.
     */
    public boolean isOwner(String id){
        return ownerToAssignedOwners.containsKey(id);
    }

    /**
     * Checks whether a given user ID corresponds to a manager of the store.
     *
     * @param id ID of the user to check.
     * @return {@code true} if the user is a manager of the store; {@code false} otherwise.
     */
    public boolean isManager(String id){
        for(List<Manager> l :this.ownerToAssignedManagers.values()){
            for (Manager m : l){
                if(m.getID().equals(id)){
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
    private Manager getManager(String id){
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

    public List<String> getAllManagersStrs(){
        return this.getAllManagers().stream().map(Manager::getID).toList();
    }

    /**
     * Returns the ID of the owner who appointed the specified user as an owner.
     *
     * @param id ID of the user whose appointer is being queried.
     * @return The ID of the owner who assigned this user, or {@code -1} if the user has no recorded appointer.
     */
    public String OwnerAssignedBy(String id){
        if (ownerToWhoAssignedHim.containsKey(id)){
            return ownerToWhoAssignedHim.get(id);
        }
        return null;
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
    public List<String> getOwnerAssigments(String id){
        return ownerToAssignedOwners.get(id);
    }


    /**
     * Removes an owner and all owners and managers they assigned recursively.
     * This method performs a cascading removal: if an owner is removed, all of their assigned owners and managers are removed too.
     *
     * @param id        ID of the owner performing the removal.
     * @param toRemove  ID of the owner to be removed.
     * @return A list of lists all user IDs (owners(0) and managers(1)) that were removed.
     * @throws Exception If:
     *                   - {@code id} is not an owner,
     *                   - {@code toRemove} is not an owner,
     *                   - {@code toRemove} is the founder,
     *                   - {@code id} is not the one who assigned {@code toRemove}.
     */
    public List<List<String>> removeOwner(String id, String toRemove) throws Exception {
        List<List<String>> res = new ArrayList<>();
        res.add(new ArrayList<>());
        res.add(new ArrayList<>());
        if(founderID.equals(toRemove)){
            throw new Exception(toRemove +" is the founder of store:"+ storeID);
        }
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
        storeClosedExeption();//actions are available only when open
        Queue<String> queue = new ArrayDeque<>();
        queue.add(toRemove);
        while(!queue.isEmpty()){
            String next = queue.remove();
            //add to remove list
            res.get(0).add(next);
            List<String> assigments = getOwnerAssigments(next);

            //enter the owners he assigned to the queue
            for (String a:assigments){
                queue.add(a);
            }
            //remove him

            ownerToAssignedOwners.get(OwnerAssignedBy(next)).remove(Integer.valueOf(next));
            ownerToWhoAssignedHim.remove(next);


            if (ownerToAssignedManagers.get(next)!= null){
                //remove the managers he assign
                for (Manager a: ownerToAssignedManagers.get(next)){
                    res.get(1).add(a.getID());
                }
                ownerToAssignedManagers.remove(next);
            }


        }
        for (String o:res.get(0)){
            if (isOwner(o)){
                ownerToAssignedOwners.remove(o);
            }
        }
        return res;
    }

    /**
    if the user called is an owner,
    returns a hashmap.
    key - worker (manager or owner)
    value - set(permissions_values).   if manager
            NULL.                      if owner
     */
    public HashMap<String, Set<Integer>> getPositionsInStore(String requestUser) throws Exception{
        if(!isOwner(requestUser))
            throw new Exception("User " + requestUser + " is not an owner!");

        HashMap<String, Set<Integer>> workerToPermission = new HashMap<>();
        for(String owner : this.ownerToWhoAssignedHim.keySet()){
            workerToPermission.put(owner, null);
        }

        for(Manager m : getAllManagers()){
            Set<Permission> ps = m.getPermissions();
            workerToPermission.put(m.getID(), ps.stream().map((perm) -> (Integer) perm.getCode()).collect(Collectors.toSet()));
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
    public String addNewListing(String userID, String productId, String productName, String productCategory, String productDescription, int quantity, double unitPrice) throws Exception {
        if (!checkProductsPermission(userID))
            throw new Exception("User " + userID + " doesn't have permission to ADD listing!");
        storeClosedExeption();//actions are available only when open
        Listing newListing = new Listing(
                this.storeID,
                productId,
                productName,
                productCategory,
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
    public boolean removeListing(String userID, String listingId) throws Exception {
        if (!checkProductsPermission(userID))
            throw new Exception("User " + userID + " doesn't have permission to REMOVE listing!");

        storeClosedExeption();//actions are available only when open
        return storeProductsManager.removeListing(listingId);
    }

    /**
     * Purchases quantity from a specific listing.
     *
     * @param listingId ID of the listing.
     * @param quantity Quantity to purchase.
     * @return {@link PurchaseType} of the purchase attempt.
     * @throws Exception if permission is missing or invalid.
     */
    public boolean purchaseFromListing(String listingId, int quantity) throws Exception {
        // Note: Maybe here you don't even need permission checking - depends if buying is free to any user
        //TODO: check wtf is going on here
        storeClosedExeption();//actions are available only when open
        return storeProductsManager.purchaseFromListing(listingId, quantity);
    }

    public boolean canEditPolicies(String userID) throws Exception {
        return isOwner(userID) || (isManager(userID) &&
                getManager(userID).getPermissions().contains(Permission.EDIT_POLICIES));
    }

    /**
 * Retrieves all the listings available in the store.
 * <p>
 * This method gathers all listings managed by the {@link IStoreProductsManager}
 * and returns them as a list. Each listing represents a product available for purchase
 * in the store.
 * </p>
 *
 * @return A {@link List} of {@link Listing} objects representing all products listed in the store.
 */
public List<Listing> getAllListings() {
    return storeProductsManager.getAllListings();
}

    /**
     * Adds a new purchase policy to the store.
     * Only users with {@code EDIT_POLICIES} permission can add policies.
     *
     * @param userID ID of the user attempting to add the policy.
     * @param policy The {@link PurchasePolicy} to be added.
     * @return {@code true} if the policy was successfully added.
     * @throws Exception if the user lacks the required permission.
     */
    public boolean addPolicy(String userID, PurchasePolicy policy) throws Exception {
        if(!canEditPolicies(userID)){
            throw new Exception("User " + userID + " doesn't have permission to ADD policy!");
        }
        storeClosedExeption();//actions are available only when open
        policyHandler.addPurchasePolicy(policy);
        return true;
    }

    /**
     * Removes an existing purchase policy from the store.
     * Only users with {@code EDIT_POLICIES} permission can remove policies.
     *
     * @param userID ID of the user attempting to remove the policy.
     * @param policy The {@link PurchasePolicy} to be removed.
     * @return {@code true} if the policy was successfully removed.
     * @throws Exception if the user lacks the required permission.
     */
    public boolean removePolicy(String userID, PurchasePolicy policy) throws Exception {
        if(!canEditPolicies(userID)){
            throw new Exception("User " + userID + " doesn't have permission to ADD policy!");
        }
        storeClosedExeption();//actions are available only when open
        policyHandler.removePurchasePolicy(policy);
        return true;
    }

    /**
     * Retrieves the list of purchase policies defined in the store.
     * Only owners and managers are allowed to view the current policies.
     *
     * @param userID ID of the user requesting the policies.
     * @return A {@link List} of {@link PurchasePolicy} currently active in the store.
     * @throws Exception if the user lacks the required permission.
     */
    public List<PurchasePolicy> getPolicies(String userID) throws Exception {
        if(!isManager(userID) || !isOwner(userID)){
            throw new Exception("User " + userID + " doesn't have permission to Get policy!");
        }
        return policyHandler.getPolicies();
    }

    /**
     * Adds a new discount policy to the store.
     * Only users with {@code EDIT_POLICIES} permission can add discount policies.
     *
     * @param userId ID of the user attempting to add the discount policy.
     * @param discountPolicy The {@link DiscountPolicy} to be added.
     * @return {@code true} if the discount policy was successfully added.
     * @throws Exception if the user lacks the required permission.
     */
    public boolean addDiscount(String userId, DiscountPolicy discountPolicy) throws Exception {
        if(!canEditPolicies(userId)){
            throw new Exception("User " + userId + " doesn't have permission to ADD discount!");
        }
        storeClosedExeption();//actions are available only when open
        policyHandler.addDiscountPolicy(discountPolicy);
        return true;
    }


    /**
     * Removes an existing discount policy from the store.
     * Only users with {@code EDIT_POLICIES} permission can remove discount policies.
     *
     * @param userID ID of the user attempting to remove the discount policy.
     * @param discountPolicy The {@link DiscountPolicy} to be removed.
     * @return {@code true} if the discount policy was successfully removed.
     * @throws Exception if the user lacks the required permission.
     */
    public boolean removeDiscount(String userID, DiscountPolicy discountPolicy) throws Exception {
        if(!canEditPolicies(userID)){
            throw new Exception("User " + userID + " doesn't have permission to REMOVE discount!");
        }
        storeClosedExeption();//actions are available only when open
        policyHandler.removeDiscountPolicy(discountPolicy);
        return true;
    }


    /**
     * Retrieves the list of discount policies defined in the store.
     * Only owners and managers are allowed to view the current discount policies.
     *
     * @param userID ID of the user requesting the discount policies.
     * @return A {@link List} of {@link DiscountPolicy} currently active in the store.
     * @throws Exception if the user lacks the required permission.
     */
    public List<DiscountPolicy> getDiscountPolicies(String userID) throws Exception {
        if(!isManager(userID) || !isOwner(userID)){
            throw new Exception("User " + userID + " doesn't have permission to Get discount policies!");
        }
        return policyHandler.getDiscountPolicies();
    }


    public double calculateStoreBagWithDiscount(Map<String,Integer> prodsToQuantity){
        return policyHandler.calculateDiscount(prodsToQuantity);
    }

    public double calculateStoreBagWithoutDiscount(Map<String,Integer> prodsToQuantity) throws Exception {
        double result = 0.0;
        Listing l;
        for (Map.Entry<String, Integer> entry : prodsToQuantity.entrySet()) {
            l = storeProductsManager.getListingById(entry.getKey());
            if(l==null){
                throw new Exception("Listing " + l + " not found");
            }
            result += l.getPrice() * entry.getValue();
        }
        return result;
    }

    public double ProductPrice(String prodId) throws Exception {
        Listing l = storeProductsManager.getListingById(prodId);
        if(l == null){
            throw new Exception("Listing " + l + " not found");
        }
        return l.getPrice();
    }

    public double ProductPriceWithDiscount(String prodId) throws Exception{
        double price = ProductPrice(prodId);
        Map<String, Integer> map = new HashMap<>();
        map.put(prodId,(Integer)1);
        double discount = policyHandler.calculateDiscount(map);
        return price - discount;
    }

    public boolean isPurchaseAllowed(Map<String, Integer> listings) {
        return policyHandler.isPurchaseAllowed(listings);
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
    public boolean checkProductsPermission(String userID){
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


    /**
 * Checks if a user has permission to approve bids in the store.
 * <p>
 * A user is allowed to approve bids if:
 * <ul>
 *     <li>They are an owner of the store, or</li>
 *     <li>They are a manager and have the {@link Permission#BID_APPROVAL} permission.</li>
 * </ul>
 *
 * @param approverID ID of the user attempting to approve a bid.
 * @return {@code true} if the user is allowed to approve bids, {@code false} otherwise.
 */

    public boolean checkBidPermission(String approverID){
        return isOwner(approverID) || (isManager(approverID) && getManager(approverID).hasPermission(Permission.BID_APPROVAL));
    }


    public Set<String> getAllOwners(){
        return this.ownerToAssignedOwners.keySet();
    }




/**
 * Retrieves the set of user IDs that are allowed to approve bids in the store.
 * <p>
 * A user is considered eligible to approve bids if:
 * <ul>
 *     <li>They are an owner of the store, or</li>
 *     <li>They are a manager with the {@link Permission#BID_APPROVAL} permission.</li>
 * </ul>
 *
 * @return A {@link Set} of user IDs who are authorized to approve bids.
 */
    public Set<String> getApproversForBid(){
        Set<String> res = new HashSet<>();
        for (String s:getAllOwners())
            res.add(s);
        for (Manager m:getAllManagers())
            if (m.hasPermission(Permission.BID_APPROVAL))
                res.add(m.id);    
        return res;

    }


    public enum Permission{
        VIEW_ONLY(0),
        EDIT_PRODUCTS(1),
        EDIT_POLICIES(2),
        BID_APPROVAL(3);

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

        String id;
        String apointedBy;


        private Set<Permission> permissions;

        public Manager(String id, String apointedBy){
            this.id = id;
            this.apointedBy = apointedBy;
            permissions = new HashSet<>();
            permissions.add(Permission.VIEW_ONLY);
        }

        public boolean addPermission(Permission permission, String byWho) throws Exception {
            if(byWho != apointedBy){
                throw new Exception("user " + byWho + " cant add permission to manager " + this.id + " because he is not his appointer");
            }
            return permissions.add(permission);
        }

        public boolean removePermission(Permission permission, String byWho) throws Exception {
            if(byWho != apointedBy){
                throw new Exception("user " + byWho + " cant add permission to manager " + this.id + " because he is not his appointer");
            }
            return permissions.remove(permission);
        }

        public String getApointedBy() {
            return apointedBy;
        }
        public String getID() {
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
