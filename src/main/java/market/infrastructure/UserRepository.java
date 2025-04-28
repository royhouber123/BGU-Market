package market.infrastructure;

import market.domain.user.IUserRepository;
import market.domain.user.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of IUserRepository for development and testing.
 * Initializes with sample users and allows in-memory CRUD operations.
 */
public class UserRepository implements IUserRepository {

    private final Map<String, User> userMap = new HashMap<>();
    
    public UserRepository() {
        // Initialize with mock data
        User user1 = new User("username1");
        user1.addProductToCart("store1", "productA" , 1);
        user1.addProductToCart("store2", "productB" , 1);

        User user2 = new User("username2");
        user2.addProductToCart("store1", "productC", 2);
    }

    @Override
    public User findById(String userName) {
        return userMap.get(userName);
    }

    @Override
    public void register(String username , String password) {
        userMap.put(username , new User(username));
    }

    @Override
    public void delete(String userName) {
        userMap.remove(userName);
    }

    @Override
    public User isExist(String userName ,String password)
    {
        //TODO - DB LOGIN INFO CHECK
        return this.userMap.get(userName);
    }
}