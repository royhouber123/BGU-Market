package market.infrastructure;

import market.domain.user.*;
import market.domain.subscriber.Subscriber;

import java.util.HashMap;
import java.util.Map;

public class UserRepository implements IUserRepository {

    private final Map<String, Subscriber>   userMap     = new HashMap<>();
    private final Map<String, String> passwordMap = new HashMap<>();

    public UserRepository() {
        User u1 = new User("username1");
        u1.addProductToCart("111", "productA", 1);
        userMap.put(u1.getUserName(), u1);
        passwordMap.put(u1.getUserName(), "pw1");

        User u2 = new User("username2");
        u2.addProductToCart("111", "productC", 2);
        userMap.put(u2.getUserName(), u2);
        passwordMap.put(u2.getUserName(), "pw2");
    }

    public Subscriber findById(String name) throws Exception {
        Subscriber user = userMap.get(name);
        if (user == null) {
            throw new Exception("User with name '" + name + "' not found.");
        }
        return user;
    }

    public void register(String name, String pw) throws Exception {
        if (userMap.containsKey(name)) {
            throw new Exception("User with name '" + name + "' already exists.");
        }
        userMap.put(name, new User(name));
        passwordMap.put(name, pw);
    }

    public void delete(String name) throws Exception {
        if (!userMap.containsKey(name)) {
            throw new Exception("User with name '" + name + "' does not exist.");
        }
        userMap.remove(name);
        passwordMap.remove(name);
    }

    public boolean changeUserName(String oldName, String newName) throws Exception {
        if (!userMap.containsKey(oldName)) {
            throw new Exception("User with name '" + oldName + "' does not exist.");
        }
        if (userMap.containsKey(newName)) {
            throw new Exception("User with name '" + newName + "' already exists.");
        }
        User u = userMap.remove(oldName);
        u.setUserName(newName);
        userMap.put(newName, u);

        String pw = passwordMap.remove(oldName);
        passwordMap.put(newName, pw);
        return true;
    }

    public boolean changePassword(String name, String newPw) throws Exception {
        if (!passwordMap.containsKey(name)) {
            throw new Exception("Password for user '" + name + "' does not exist.");
        }
        passwordMap.put(name, newPw);
        return true;
    }

    public ShoppingCart getCart(String name) throws Exception {
        User u = userMap.get(name);
        if (u == null) {
            throw new Exception("User with name '" + name + "' not found.");
        }
        return u.getShoppingCart();
    }
}
