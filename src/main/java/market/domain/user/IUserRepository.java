package market.domain.user;

public interface IUserRepository {

    Subscriber findById(String userName);
    void register(String userName, String password);
    void delete(String userName);

    boolean changeUserName(String oldUserName, String newUserName);
    boolean changePassword(String userName, String newPassword);

    ShoppingCart getCart(String userName);
}
