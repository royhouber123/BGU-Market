package market.infrastructure.PersistenceRepositories;

import market.domain.user.ISuspensionRepository;
import market.domain.user.Suspension;
import market.domain.user.IUserRepository;
import market.infrastructure.IJpaRepository.ISuspensionJpaRepository;
import market.infrastructure.IJpaRepository.IUserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import javax.management.RuntimeErrorException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Primary
@Repository("suspensionRepositoryJpa")
@Transactional
public class SuspensionRepositoryPersistance implements ISuspensionRepository {

    @Autowired
    private ISuspensionJpaRepository suspensionJpaRepository;

    @Autowired
    private IUserJpaRepository userRepository;

    @Override
    public boolean suspendUser(String userName, long durationHours) {
        if (userRepository.findById(userName) == null) return false;

        Suspension existingSuspension = suspensionJpaRepository.findByUserName(userName);
        if (existingSuspension != null) {
            // Update existing suspension
            LocalDateTime israelTime = LocalDateTime.now(ZoneId.of("Asia/Jerusalem"));
            existingSuspension.setSuspensionStart(israelTime);
            existingSuspension.setSuspensionDurationHours(durationHours);
            suspensionJpaRepository.save(existingSuspension);
        } else {
            // Create new suspension
            System.out.println("Suspending user: " + userName);
            Suspension suspension = new Suspension(userName, durationHours);
            System.out.println("Suspension userName: " + suspension.getUserName());
            suspensionJpaRepository.save(suspension);
        }
        return true;
    }

    @Override
    public boolean unsuspendUser(String userName) {
        Suspension suspension = suspensionJpaRepository.findByUserName(userName);
        if (suspension != null) {
            suspensionJpaRepository.delete(suspension);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getSuspendedUsers() {
        cleanExpiredSuspensions();
        List<Suspension> suspensions = suspensionJpaRepository.findAll();
        return suspensions.stream()
                .filter(Suspension::isSuspended)
                .map(Suspension::getUserName)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSuspended(String userName) {
        cleanExpiredSuspensions();
        Suspension suspension = suspensionJpaRepository.findByUserName(userName);
        return suspension != null && suspension.isSuspended();
    }

    @Override
    public void checkNotSuspended(String userName) throws RuntimeErrorException {
        if (isSuspended(userName)) {
            throw new RuntimeException("The user "+userName+ " is suspended!");
        }
    }

    @Override
    public void cleanExpiredSuspensions() {
        List<Suspension> all = suspensionJpaRepository.findAll();
        for (Suspension s : all) {
            if (!s.isSuspended()) {
                suspensionJpaRepository.delete(s);
            }
        }
    }
}