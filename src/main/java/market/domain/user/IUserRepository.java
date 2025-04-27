package market.domain.user;

public interface IUserRepository {

    User findById(String userName);
    void register(String userName, String password);
    void delete(String userName);
    User isExist(String userName, String password);

    boolean changeUserName(String oldUserName, String newUserName);
    boolean changePassword(String userName, String newPassword);

    ShoppingCart getCart(String userName);
}
