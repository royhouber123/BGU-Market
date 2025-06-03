package market.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SuspensionRepositoryTest {

    private IUserRepository userRepository;
    private SuspensionRepository suspensionRepository;

    private final String EXISTING_USER = "user1";
    private final String NON_EXISTENT_USER = "ghost";

    private User user1;

    @BeforeEach
    public void setup() {
        userRepository = mock(IUserRepository.class);
        suspensionRepository = new SuspensionRepository(userRepository);

        user1 = new User(EXISTING_USER);
        
        when(userRepository.findById(EXISTING_USER)).thenReturn(user1);
        when(userRepository.findById(NON_EXISTENT_USER)).thenReturn(null);
    }

    @Test
    public void testSuspendUserExistingUser() {
        boolean result = suspensionRepository.suspendUser(EXISTING_USER, 1000L);
        assertTrue(result, "Suspend should succeed for existing user");
        assertTrue(suspensionRepository.isSuspended(EXISTING_USER), "User should be suspended");
    }

    @Test
    public void testSuspendUserNonExistingUser() {
        boolean result = suspensionRepository.suspendUser(NON_EXISTENT_USER, 1000L);
        assertFalse(result, "Suspend should fail for non-existent user");
    }

    @Test
    public void testUnsuspendUser() {
        suspensionRepository.suspendUser(EXISTING_USER, 10000L);
        assertTrue(suspensionRepository.isSuspended(EXISTING_USER), "User should be suspended");

        boolean unsuspendResult = suspensionRepository.unsuspendUser(EXISTING_USER);
        assertTrue(unsuspendResult, "Unsuspend should succeed");
        assertFalse(suspensionRepository.isSuspended(EXISTING_USER), "User should no longer be suspended");
    }

    @Test
    public void testGetSuspendedUsers() {
        suspensionRepository.suspendUser(EXISTING_USER, 10000L);
        List<User> suspendedUsers = suspensionRepository.getSuspendedUsers();

        assertEquals(1, suspendedUsers.size(), "There should be one suspended user");
        assertEquals(EXISTING_USER, suspendedUsers.get(0).getUserName());
    }

    @Test
    public void testSuspensionExpires() throws InterruptedException {
        suspensionRepository.suspendUser(EXISTING_USER, 200); // 200 ms suspension

        assertTrue(suspensionRepository.isSuspended(EXISTING_USER), "User should initially be suspended");

        Thread.sleep(300);  // wait for suspension to expire

        assertFalse(suspensionRepository.isSuspended(EXISTING_USER), "User suspension should expire");
    }

    @Test
    public void testCheckNotSuspendedThrows() {
        suspensionRepository.suspendUser(EXISTING_USER, 10000L);

        Exception ex = assertThrows(Exception.class, () -> {
            suspensionRepository.checkNotSuspended(EXISTING_USER);
        });

        assertTrue(ex.getMessage().contains("suspended"));
    }

    @Test
    public void testCheckNotSuspendedNoThrow() {
        assertDoesNotThrow(() -> {
            suspensionRepository.checkNotSuspended(EXISTING_USER);
        });
    }
}