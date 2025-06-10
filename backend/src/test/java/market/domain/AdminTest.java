package market.domain;

import market.application.AdminService;
import market.application.NotificationService;
import market.domain.Role.IRoleRepository;
import market.domain.store.IStoreRepository;
import market.domain.store.Store;
import market.domain.user.Admin;
import market.domain.user.ISuspensionRepository;
import market.domain.user.IUserRepository;
import market.infrastructure.RoleRepository;
import market.infrastructure.StoreRepository;
import market.infrastructure.SuspensionRepository;
import market.infrastructure.UserRepository;
import market.domain.notification.INotificationRepository;
import market.notification.INotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AdminTest {

    private AdminService adminService;
    private IStoreRepository storeRepository;
    private IUserRepository userRepository;
    private IRoleRepository roleRepository;
    private ISuspensionRepository suspensionRepository;
    private NotificationService notificationService;

    @Mock
    private INotificationRepository notificationRepository;
    
    @Mock
    private INotifier notifier;

    @BeforeEach
    public void setup() throws Exception {
        userRepository = new UserRepository();
        storeRepository = new StoreRepository();
        roleRepository = new RoleRepository();
        suspensionRepository = new SuspensionRepository(userRepository);
        
        // Create NotificationService with mocked dependencies
        notificationService = new NotificationService(notificationRepository, notifier);

        Admin admin = new Admin("adminUser");
        ((UserRepository) userRepository).saveAdmin(admin, "adminPw");

        Store store = new Store("1", "StoreOne", "adminUser", null);
        storeRepository.addStore(store);

        adminService = new AdminService(userRepository, storeRepository, roleRepository, suspensionRepository, notificationService);
    }

    @Test
    public void testCloseStoreByAdmin_successful() throws Exception {
        adminService.closeStoreByAdmin("adminUser", "1");

        Store closedStore = storeRepository.getStoreByID("1");
        assertFalse(closedStore.isActive(), "Store should be marked as inactive after closure by admin.");
    }
}
