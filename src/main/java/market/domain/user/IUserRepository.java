package market.domain.user;

/**
 * Repository interface for User aggregates.
 */
public interface IUserRepository {
    /**
     * Finds a User by its unique identifier.
     *
     * @param userId the id of the user
     * @return the User if found, otherwise null
     */
    User findById(String userId);

    /**
     * Persists or updates the given User.
     *
     * @param user the User to save
     */
    public void register(String username , String password);
    /**
     * Deletes the User with the given identifier.
     *
     * @param userId the id of the user to delete
     */
    void delete(String userId);

    User isExist(String userName, String passowrd);
}
