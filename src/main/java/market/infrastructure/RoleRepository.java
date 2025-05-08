package market.infrastructure;

import market.domain.Role.IRoleRepository;
import market.domain.Role.Role;
import market.domain.Role.Permission;

import java.util.*;

public class RoleRepository implements IRoleRepository {

    // Only what admin needs — userID → roles per store
    private final Map<String, Map<String, List<Role>>> storeToUserRoles = new HashMap<>();

    @Override
    public void removeAllRolesForUserInStore(String userId, String storeId) {
        Map<String, List<Role>> userRoles = storeToUserRoles.get(storeId);
        if (userRoles != null) {
            userRoles.remove(userId);
        }
    }

    @Override
    public List<String> getAllStoreUserIdsWithRoles(String storeId) {
        Map<String, List<Role>> userRoles = storeToUserRoles.get(storeId);
        if (userRoles == null) return Collections.emptyList();
        return new ArrayList<>(userRoles.keySet());
    }

    // Stub all unused methods
    @Override public Map<String, List<Role>> getRolesOfUser(String userName) { throw new UnsupportedOperationException(); }
    @Override public Map<String, List<Role>> getRolesOfStore(String storeId) { throw new UnsupportedOperationException(); }
    @Override public boolean userHasPermissionInStore(String userName, String storeId, Permission permission) { throw new UnsupportedOperationException(); }
    @Override public void createStore(String userName, String storeId) { throw new UnsupportedOperationException(); }
    @Override public void addNewOwner(String appointerID, String newOwnerID, String storeId) { throw new UnsupportedOperationException(); }
    @Override public void addNewManager(String appointerID, String newManagerID, String storeId) { throw new UnsupportedOperationException(); }
    @Override public void addPermissionToManager(String managerID, String appointerID, String storeId, Permission p) { throw new UnsupportedOperationException(); }
    @Override public List<Permission> getManagersPermmisions(String userName, String storeId) { throw new UnsupportedOperationException(); }
    @Override public void removePermissionFromManager(String userName, String storeId, Permission p) { throw new UnsupportedOperationException(); }
    @Override public boolean isOwner(String userName, String storeId) { throw new UnsupportedOperationException(); }
    @Override public boolean isManager(String userName, String storeId) { throw new UnsupportedOperationException(); }
    @Override public boolean hasPermission(String userName, String storeId, Permission p) { throw new UnsupportedOperationException(); }
    @Override public String OwnerAssignedBy(String userName, String storeId) { throw new UnsupportedOperationException(); }
    @Override public String getFounderID(String storeId) { throw new UnsupportedOperationException(); }
    @Override public List<String> getOwnerAssigments(String userId, String storeId) { throw new UnsupportedOperationException(); }
    @Override public List<List<String>> removeOwner(String userId, String userIdToRemove, String storeId) throws Exception { throw new UnsupportedOperationException(); }

    @Override
    public List<String> getAllManagers(String storeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllManagers'");
    }
}
