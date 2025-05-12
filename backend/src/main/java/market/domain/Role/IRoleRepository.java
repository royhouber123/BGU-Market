package market.domain.Role;
import java.util.Map;
import java.util.Set;
import java.util.List;


public interface IRoleRepository {

    Map<String, List<Role>> getRolesOfUser(String userName);
    Map<String, List<Role>> getRolesOfStore(String storeId);
    boolean userHasPermissionInStore(String userName, String storeId, Permission permission);

    /**
     * puts a founder relation
     *
     * @param userID ID of the user creating the store.
     * @param storeId the store has been created.
     */
    void createStore(String userName, String storeId); //make founder

    /**
     * Adds a new owner to the store, assigned by an existing owner.
     * The new owner will be tracked in the ownership hierarchy.
     *
     * @param appointerID  ID of the current owner assigning the new owner.
     * @param newOwnerID   ID of the user to be added as a new owner.
     * @throws Exception if the appointer is not an owner, or if the new owner is already registered as an owner.
     */
    void addNewOwner(String appointerID, String newOwnerID, String storeId);

    /**
     * Adds a new manager to the store, assigned by an existing owner.
     * The manager will initially have no permissions until assigned explicitly.
     *
     * @param appointerID    ID of the current owner assigning the new manager.
     * @param newManagerID   ID of the user to be added as a new manager.
     * @return {@code true} if the manager was successfully added.
     * @throws Exception if the appointer is not an owner, or if the new manager is already registered as a manager.
     */
    void addNewManager(String appointerID, String newManagerID, String storeId);

    /**
     * Grants a specific permission to a manager.
     * The permission can only be assigned by the owner who originally appointed the manager.
     *
     * @param managerID     ID of the manager receiving the new permission.
     * @param appointerID   ID of the owner who assigned the manager and is now granting permission.
     * @param storeId       the relevant store 
     * @param p  Integer code representing the permission to assign (must be valid in {@link Permission} enum).
     * @throws Exception if the appointer is not an owner, the manager ID is not valid, the permission code is invalid,
     *                   or if the appointer is not the one who originally assigned the manager.
     */
    void addPermissionToManager(String managerID, String appointerID, String storeId, Permission p);

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
    List<Permission> getManagersPermmisions(String userName, String storeId);
    void removePermissionFromManager(String userName, String storeId, Permission p);
    boolean isOwner(String userName, String storeId);
    boolean isManager(String userName, String storeId);
    List<String> getAllManagers(String storeId);
    boolean hasPermission(String userName, String storeId, Permission p);
    String OwnerAssignedBy(String userName, String storeId);
    String getFounderID(String storeId);
    /**
     * Retrieves a list of owner IDs that were directly assigned by the specified owner.
     *
     * @param id ID of the owner whose assigned owners are requested.
     * @param storeId store to be searched.
     * @return A list of user IDs representing owners assigned by this user, or {@code null} if the user is not in the map.
     */
    public List<String> getOwnerAssigments(String userId,String storeId);
    /**
     * Removes an owner and all owners and managers they assigned recursively.
     * This method performs a cascading removal: if an owner is removed, all of their assigned owners and managers are removed too.
     *
     * @param userId        ID of the owner performing the removal.
     * @param userIdToRemove  ID of the owner to be removed.
     * @param storeId store to be searched.
     * @return A list of lists all user IDs (owners(0) and managers(1)) that were removed.
     * @throws Exception If:
     *                   - {@code id} is not an owner,
     *                   - {@code toRemove} is not an owner,
     *                   - {@code toRemove} is the founder,
     *                   - {@code id} is not the one who assigned {@code toRemove}.
     */
    public List<List<String>> removeOwner(String userId, String userIdToRemove, String store) throws Exception;



}
