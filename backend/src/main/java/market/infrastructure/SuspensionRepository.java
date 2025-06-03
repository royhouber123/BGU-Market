package market.infrastructure;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        suspendedUsers.put(userName, new Suspension(userName, durationHours));
        return true;
    }

    @Override
    public boolean unsuspendUser(String userName) {
        return suspendedUsers.remove(userName) != null;
    }

    @Override
    public List<String> getSuspendedUsers() {
        cleanExpiredSuspensions();
        return new ArrayList<>(suspendedUsers.keySet());
    }

    @Override
    public boolean isSuspended(String userName) {
        cleanExpiredSuspensions();
        Suspension s = suspendedUsers.get(userName);
        return s != null && s.isSuspended();
    }

    @Override
    public void checkNotSuspended(String userName) throws Exception {
        if (isSuspended(userName)) {
            throw new Exception("The user "+userName+ " is suspended!");
        }
    }

    private void cleanExpiredSuspensions() {
        suspendedUsers.entrySet().removeIf(entry -> !entry.getValue().isSuspended());
    }
}