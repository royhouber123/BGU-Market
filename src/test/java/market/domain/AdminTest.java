package market.domain;

import market.application.AdminService;
import market.domain.Role.IRoleRepository;
import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.domain.user.Admin;
import market.domain.user.IUserRepository;
import market.infrastructure.RoleRepository;
import market.infrastructure.StoreRepository;
import market.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    private AdminService adminService;
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IRoleRepository roleRepository;

    @BeforeEach
    public void setup() throws Exception {
        userRepository = new UserRepository();
        storeRepository = new StoreRepository();
        roleRepository = new RoleRepository();

        Admin admin = new Admin("adminUser");
        ((UserRepository) userRepository).saveAdmin(admin, "adminPw");

        Store store = new Store("1", "StoreOne", "adminUser", null);
        storeRepository.addStore(store);

        adminService = new AdminService(userRepository, storeRepository, roleRepository);
    }

    @Test
    public void testCloseStoreByAdmin_successful() throws Exception {
        adminService.closeStoreByAdmin("adminUser", "1");

        Store closedStore = storeRepository.getStoreByID("1");
        assertFalse(closedStore.isActive(), "Store should be marked as inactive after closure by admin.");
    }
}
