package market.domain.user;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SuspensionRepository implements ISuspensionRepository {

    // userName -> Suspension
    private final Map<String, Suspension> suspendedUsers = new ConcurrentHashMap<>();
    private final IUserRepository userRepository;

    public SuspensionRepository(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean suspendUser(String userName, long durationMillis) {
        if (userRepository.findById(userName) == null) return false;
        suspendedUsers.put(userName, new Suspension(userName, durationMillis));
        return true;
    }

    @Override
    public boolean unsuspendUser(String userName) {
        return suspendedUsers.remove(userName) != null;
    }

    @Override
    public List<User> getSuspendedUsers() {
        cleanExpiredSuspensions();
        return suspendedUsers.keySet().stream()
                .map(userRepository::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
        Instant now = Instant.now();
        suspendedUsers.entrySet().removeIf(entry -> !entry.getValue().isSuspended());
    }
}