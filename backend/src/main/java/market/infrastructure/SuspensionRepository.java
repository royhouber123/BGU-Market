package market.infrastructure;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import market.domain.user.ISuspensionRepository;
import market.domain.user.IUserRepository;
import market.domain.user.Suspension;
import market.domain.user.User;

public class SuspensionRepository implements ISuspensionRepository {

    // userName -> Suspension
    private final Map<String, Suspension> suspendedUsers = new ConcurrentHashMap<>();
    private final IUserRepository userRepository;

    public SuspensionRepository(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean suspendUser(String userName, long durationHours) {
        if (userRepository.findById(userName) == null) return false;
        // Store with lowercase key for case-insensitive lookups
        suspendedUsers.put(userName.toLowerCase(), new Suspension(userName, durationHours));
        return true;
    }

    @Override
    public boolean unsuspendUser(String userName) {
        // Use lowercase key for case-insensitive lookups
        return suspendedUsers.remove(userName.toLowerCase()) != null;
    }

    @Override
    public List<String> getSuspendedUsers() {
        cleanExpiredSuspensions();
        // Convert keys back to original case for return
        return suspendedUsers.values().stream()
                .map(Suspension::getUserName)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSuspended(String userName) {
        cleanExpiredSuspensions();
        // Use lowercase key for case-insensitive lookups
        Suspension s = suspendedUsers.get(userName.toLowerCase());
        return s != null && s.isSuspended();
    }

    @Override
    public void checkNotSuspended(String userName) throws RuntimeErrorException {
        if (isSuspended(userName)) {
            throw new RuntimeException("The user "+userName+ " is suspended!");
        }
    }

    private void cleanExpiredSuspensions() {
        suspendedUsers.entrySet().removeIf(entry -> !entry.getValue().isSuspended());
    }
}