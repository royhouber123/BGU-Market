package market.domain.user;

import java.util.List;

public interface IUserRepository {

    User findById(String userName);
    void register(String userName);
    void register(String userName, String password);
    void delete(String userName);
    boolean changeUserName(String oldUserName, String newUserName);
    boolean changePassword(String userName, String newPassword);
    boolean verifyPassword(String userName, String plainPassword);

    ShoppingCart getCart(String userName);
    
    void saveAdmin(Admin admin, String password);
    
    void save(User user);
    
    List<User> getAllUsers();
}
