package market.application;

import org.springframework.test.annotation.Commit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import support.AcceptanceTestSpringBase;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@EnabledIf("market.application.UserServiceTests#isMySQLAvailable")
public class SuspensionServiceTests extends AcceptanceTestSpringBase {

    @BeforeEach
    void setUp() {
        String password = "password123";
        userService.register("suspend_test_user", password);
        userService.register("unsuspend_test_user", password);
        userService.register("multi_suspend_1", password);
        userService.register("multi_suspend_2", password);
        userService.register("unsuspend_one_1", password);
        userService.register("unsuspend_one_2", password);
        userService.register("permanent_suspend_tester", password);
    }

    @AfterEach
    void tearDown() {
        userService.unsuspendUser("suspend_test_user");
        userService.unsuspendUser("unsuspend_test_user");
        userService.unsuspendUser("multi_suspend_1");
        userService.unsuspendUser("multi_suspend_2");
        userService.unsuspendUser("unsuspend_one_1");
        userService.unsuspendUser("unsuspend_one_2");
        userService.unsuspendUser("permanent_suspend_tester");

        userService.deleteUser("suspend_test_user");
        userService.deleteUser("unsuspend_test_user");
        userService.deleteUser("multi_suspend_1");
        userService.deleteUser("multi_suspend_2");
        userService.deleteUser("unsuspend_one_1");
        userService.deleteUser("unsuspend_one_2");
        userService.deleteUser("permanent_suspend_tester");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_suspend_user_persists() {
        String username = "suspend_test_user";
        userService.suspendUser(username, 12);

        assertTrue(userService.isSuspended(username), "User should be suspended.");
        List<String> suspendedUsers = userService.getSuspendedUsers();
        assertTrue(suspendedUsers.contains(username), "Suspended users list should contain the user.");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_unsuspend_user_removes_from_db() {
        String username = "unsuspend_test_user";
        userService.suspendUser(username, 24);
        assertTrue(userService.isSuspended(username), "User should be suspended.");

        userService.unsuspendUser(username);
        assertFalse(userService.isSuspended(username), "User should not be suspended after unsuspend.");
        List<String> suspendedUsers = userService.getSuspendedUsers();
        assertFalse(suspendedUsers.contains(username), "Suspended users list should not contain the user.");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_suspend_multiple_users() {
        String user1 = "multi_suspend_1";
        String user2 = "multi_suspend_2";
        userService.suspendUser(user1, 24);
        userService.suspendUser(user2, 48);

        List<String> suspendedUsers = userService.getSuspendedUsers();
        assertTrue(suspendedUsers.contains(user1), "Suspended users list should contain user1.");
        assertTrue(suspendedUsers.contains(user2), "Suspended users list should contain user2.");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_unsuspend_one_of_multiple_users() {
        String user1 = "unsuspend_one_1";
        String user2 = "unsuspend_one_2";
        userService.suspendUser(user1, 24);
        userService.suspendUser(user2, 24);

        userService.unsuspendUser(user1);

        List<String> suspendedUsers = userService.getSuspendedUsers();
        assertFalse(suspendedUsers.contains(user1), "User1 should not be in suspended users list.");
        assertTrue(suspendedUsers.contains(user2), "User2 should still be in suspended users list.");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_suspend_permanent() {
        String username = "permanent_suspend_tester";
        userService.suspendUser(username, 0);
        assertTrue(userService.isSuspended(username), "User should be permanently suspended.");

        userService.unsuspendUser(username);
        assertFalse(userService.isSuspended(username), "User should not be suspended.");
    }

    static boolean isMySQLAvailable() {
        try {
            DriverManager.getConnection("jdbc:mysql://localhost:3306/?useSSL=false", "bgu", "changeme").close();
            return true;
        } catch (Exception e) {
            System.out.println("⚠️  MySQL not available – skipping UserServiceTests");
            return false;
        }
    }
}