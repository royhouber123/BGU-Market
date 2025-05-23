package market.infrastructure;

import market.domain.user.*;
import utils.Logger;
import utils.PasswordUtil;

import java.util.HashMap;
import java.util.Map;

public class UserRepository implements IUserRepository {

    private static final Logger logger = Logger.getInstance();

    private final Map<String, Subscriber> userMap = new HashMap<>();
    private final Map<String, String> passwordMap = new HashMap<>();
    private final Map<String, User> guestMap = new HashMap<>();

    public UserRepository() {
        Subscriber u1 = new Subscriber("username1");
        u1.addProductToCart("111", "productA", 1);
        userMap.put(u1.getUserName(), u1);
        passwordMap.put(u1.getUserName(), PasswordUtil.hashPassword("pw1"));

        Subscriber u2 = new Subscriber("username2");
        u2.addProductToCart("111", "productC", 2);
        userMap.put(u2.getUserName(), u2);
        passwordMap.put(u2.getUserName(), PasswordUtil.hashPassword("pw2"));

        Admin admin = new Admin("adminUser");
        userMap.put(admin.getUserName(), admin);
        passwordMap.put(admin.getUserName(), PasswordUtil.hashPassword("adminPw"));

    }

    // Search for user in both maps - guests and subscribers
    public User findById(String name) {
        logger.info("[UserRepository] Attempting to find user by id: " + name);
        User user = guestMap.get(name) != null ? guestMap.get(name) : userMap.get(name);
        if (user == null) {
            logger.error("[UserRepository] User with name '" + name + "' not found.");
            throw new RuntimeException("[UserRepository] User with name '" + name + "' not found.");
        }
        logger.info("[UserRepository] User found: " + name);
        return user;
    }

    public void register(String name) {
        logger.info("[UserRepository] Registering guest: " + name);
        if (guestMap.containsKey(name)) {
            logger.debug("[UserRepository] Guest with name '" + name + "' already exists.");
            throw new RuntimeException("Guest with name '" + name + "' already exists.");
        }
        User guest = new User(name);
        guestMap.put(name, guest);
        logger.info("[UserRepository] Guest registered: " + name);
    }

    public void register(String name, String pw) {
        logger.info("[UserRepository] Registering user: " + name);
        if (userMap.containsKey(name)) {
            logger.debug("[UserRepository] User with name '" + name + "' already exists.");
            throw new RuntimeException("[UserRepository] User with name '" + name + "' already exists.");
        }
        Subscriber subscriber = new Subscriber(name);
        userMap.put(name, subscriber);
        passwordMap.put(name, PasswordUtil.hashPassword(pw));
        logger.info("[UserRepository] User registered: " + name);
    }


    public boolean verifyPassword(String username, String plainPassword) {
        logger.info("[UserRepository] Verifying password for user: " + username);
        String hashedPassword = passwordMap.get(username);
        if (hashedPassword == null) {
            logger.debug("[UserRepository] User with name '" + username + "' not found.");
            return false;
        }
        boolean isMatch = PasswordUtil.verifyPassword(plainPassword, hashedPassword);
        if (isMatch) {
            logger.info("[UserRepository] Password verification successful for user: " + username);
        } else {
            logger.info("[UserRepository] Password verification failed for user: " + username);
        }
        return isMatch;
    }

    public void delete(String name) {
        logger.info("[UserRepository] Deleting user: " + name);
        boolean isExist = guestMap.containsKey(name) || userMap.containsKey(name);
        if (!isExist) {
            logger.debug("[UserRepository] User with name '" + name + "' does not exist.");
            throw new RuntimeException("User with name '" + name + "' does not exist.");
        }
        guestMap.remove(name);
        userMap.remove(name);
        passwordMap.remove(name);
        logger.info("[UserRepository] User deleted: " + name);
    }

    public boolean changeUserName(String oldName, String newName) {
        logger.info("[UserRepository] Changing username from '" + oldName + "' to '" + newName + "'.");
        if (!userMap.containsKey(oldName)) {
            logger.debug("[UserRepository] User with name '" + oldName + "' does not exist.");
            throw new RuntimeException("User with name '" + oldName + "' does not exist.");
        }
        if (userMap.containsKey(newName)) {
            logger.debug("[UserRepository] User with name '" + newName + "' already exists.");
            throw new RuntimeException("User with name '" + newName + "' already exists.");
        }
        Subscriber u = userMap.remove(oldName);
        u.setUserName(newName);
        userMap.put(newName, u);

        String pw = passwordMap.remove(oldName);
        passwordMap.put(newName, pw);
        logger.info("Username changed from '" + oldName + "' to '" + newName + "'.");
        return true;
    }

    public boolean changePassword(String name, String newPw) {
        logger.info("[UserRepository] Changing password for user: " + name);
        if (!passwordMap.containsKey(name)) {
            logger.debug("[UserRepository] Password for user '" + name + "' does not exist.");
            throw new RuntimeException("Password for user '" + name + "' does not exist.");
        }
        passwordMap.put(name, PasswordUtil.hashPassword(newPw));
        logger.info("[UserRepository] Password changed for user: " + name);
        return true;
    }

    public ShoppingCart getCart(String name) {
        logger.info("[UserRepository] Getting cart for user: " + name);
        User u = guestMap.get(name) != null ? guestMap.get(name) : userMap.get(name);
        if(u == null)
        {
            logger.debug("[UserRepository] User with name '" + name + "' not found.");
            throw new RuntimeException("User with name '" + name + "' not found.");
        }
        logger.info("[UserRepository] Cart retrieved for user: " + name);
        return u.getShoppingCart();
    }

    public void saveAdmin(Admin admin, String password) {
        userMap.put(admin.getUserName(), admin);
        passwordMap.put(admin.getUserName(), PasswordUtil.hashPassword(password));
    }
    
}
