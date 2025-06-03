public interface ISuspensionRepository {
    public boolean suspendUser(String userName, Duration duration); // Duration.ZERO or null for permanent    
    public boolean unsuspendUser(String userName);
    public List<User> getSuspendedUsers();
    public boolean isSuspended(String userName);
    public void checkNotSuspended(String userName) throws Exception;
}
