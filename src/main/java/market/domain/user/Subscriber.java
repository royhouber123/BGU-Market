package market.domain.user;

import market.domain.user.roles.Owner;
import market.domain.user.roles.Manager;
import market.domain.user.roles.Founder;
import market.domain.user.roles.Role;

import java.util.HashMap;
import java.util.Map;


public class Subscriber extends User {

    private String shippingAddress;
    public static record StoreRoleKey(String storeId, String roleName) {}
    private final Map<StoreRoleKey, Role> roles;

    public Subscriber(String userName) {
        super(userName);
        this.shippingAddress = "";
        this.roles = new HashMap<Subscriber.StoreRoleKey,Role>();
    }

    public Map<StoreRoleKey, Role> getRoles() {
        return this.roles;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    /**
     * Assigns a role for a given store to this subscriber - Create a ROLE for a new store as well.
     * @param storeId the store identifier
     * @param roleName the name of the role (Owner, Manager, Founder)
     * @throws IllegalArgumentException if the roleName is invalid
     */
    public void setStoreRole(String storeId, String roleName) throws Exception {
        Role role;
        switch (roleName) {
            case "Owner" -> role = new Owner(storeId);
            case "Manager" -> role = new Manager(storeId);
            case "Founder" -> role = new Founder(storeId);
            default -> throw new Exception("Role '" + roleName + "' does not exist");
        }
        roles.put(new StoreRoleKey(storeId, roleName), role);
    }

    public Role getStoreRole(String storeId, String roleName) throws Exception {
        StoreRoleKey key = new StoreRoleKey(storeId, roleName);
        Role role = roles.get(key);
        if (role == null) {
            throw new Exception(
                "User '" + this.getUserName() + "' is not '" + roleName + "' at store '" + storeId + "'");
        }
        return role;
    }

    public boolean isFounder(String storeId)
    {
        StoreRoleKey key = new StoreRoleKey(storeId, "Founder");
        return this.roles.containsKey(key);
    }

    public boolean isManager(String storeId){
        StoreRoleKey key = new StoreRoleKey(storeId, "Manager");
        return this.roles.containsKey(key);
    }

    public boolean isOwner(String storeId){
        StoreRoleKey key = new StoreRoleKey(storeId, "Owner");
        return this.roles.containsKey(key);
    }

    public boolean removeStoreRole(String storeId, String roleName) {
        return roles.remove(new StoreRoleKey(storeId, roleName)) != null;
    }

    /**
     * Removes all roles related to the given storeId.
     * @param storeId the store identifier
     * @return true if any roles were removed, false otherwise
     */
    public boolean removeStore(String storeId) {
        boolean removed = roles.keySet().removeIf(key -> key.storeId.equals(storeId));
        return removed;
    }
}
