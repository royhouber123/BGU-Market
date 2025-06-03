package market.domain.user;
import java.util.List;

public interface ISuspensionRepository {
    public boolean suspendUser(String userName, long duration); // Duration.ZERO or null for permanent    
    public boolean unsuspendUser(String userName);
    public List<String> getSuspendedUsers();
    public boolean isSuspended(String userName);
    public void checkNotSuspended(String userName) throws Exception;
}
