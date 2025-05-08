package market.infrastructure;

import market.domain.user.*;
import utils.Logger;
import utils.PasswordUtil;

import java.util.HashMap;
import java.util.Map;

public class UserRepository implements IUserRepository {

    private static final Logger logger = Logger.getInstance();

    private final Map<String, Subscriber>   userMap     = new HashMap<>();
    private final Map<String, String> passwordMap = new HashMap<>();

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

    public Subscriber findById(String name) {
        logger.info("Attempting to find user by id: " + name);
        Subscriber user = userMap.get(name);
        if (user == null) {
            logger.error("User with name '" + name + "' not found.");
            throw new RuntimeException("User with name '" + name + "' not found.");
        }
        logger.info("User found: " + name);
        return user;
    }

    public void register(String name, String pw) {
        logger.info("Registering user: " + name);
        if (userMap.containsKey(name)) {
            logger.error("User with name '" + name + "' already exists.");
            throw new RuntimeException("User with name '" + name + "' already exists.");
        }
        Subscriber subscriber = new Subscriber(name);
        userMap.put(name, subscriber);
        passwordMap.put(name, PasswordUtil.hashPassword(pw));
        logger.info("User registered: " + name);
    }

    public boolean verifyPassword(String username, String plainPassword) {
        logger.info("Verifying password for user: " + username);
        String hashedPassword = passwordMap.get(username);
        if (hashedPassword == null) {
            logger.error("User with name '" + username + "' not found.");
            return false;
        }
        boolean isMatch = PasswordUtil.verifyPassword(plainPassword, hashedPassword);
        if (isMatch) {
            logger.info("Password verification successful for user: " + username);
        } else {
            logger.info("Password verification failed for user: " + username);
        }
        return isMatch;
    }

    public void delete(String name) {
        logger.info("Deleting user: " + name);
        if (!userMap.containsKey(name)) {
            logger.error("User with name '" + name + "' does not exist.");
            throw new RuntimeException("User with name '" + name + "' does not exist.");
        }
        userMap.remove(name);
        passwordMap.remove(name);
        logger.info("User deleted: " + name);
    }

    public boolean changeUserName(String oldName, String newName) {
        logger.info("Changing username from '" + oldName + "' to '" + newName + "'.");
        if (!userMap.containsKey(oldName)) {
            logger.error("User with name '" + oldName + "' does not exist.");
            throw new RuntimeException("User with name '" + oldName + "' does not exist.");
        }
        if (userMap.containsKey(newName)) {
            logger.error("User with name '" + newName + "' already exists.");
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
        logger.info("Changing password for user: " + name);
        if (!passwordMap.containsKey(name)) {
            logger.error("Password for user '" + name + "' does not exist.");
            throw new RuntimeException("Password for user '" + name + "' does not exist.");
        }
        passwordMap.put(name, PasswordUtil.hashPassword(newPw));
        logger.info("Password changed for user: " + name);
        return true;
    }

    public ShoppingCart getCart(String name) {
        logger.info("Getting cart for user: " + name);
        Subscriber u = userMap.get(name);
        if (u == null) {
            logger.error("User with name '" + name + "' not found.");
            throw new RuntimeException("User with name '" + name + "' not found.");
        }
        logger.info("Cart retrieved for user: " + name);
        return u.getShoppingCart();
    }

    public void saveAdmin(Admin admin, String password) {
        userMap.put(admin.getUserName(), admin);
        passwordMap.put(admin.getUserName(), PasswordUtil.hashPassword(password));
    }
    
}
