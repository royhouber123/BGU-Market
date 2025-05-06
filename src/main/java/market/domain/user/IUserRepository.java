package market.domain.user;

public interface IUserRepository {

    Subscriber findById(String userName);
    void register(String userName, String password);
    void delete(String userName);

    boolean changeUserName(String oldUserName, String newUserName);
    boolean changePassword(String userName, String newPassword);
    boolean verifyPassword(String userName, String plainPassword);

    ShoppingCart getCart(String userName);
}
