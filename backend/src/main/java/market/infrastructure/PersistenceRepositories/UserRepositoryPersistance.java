package market.infrastructure.PersistenceRepositories;

import market.domain.user.*;
import market.infrastructure.IJpaRepository.IUserJpaRepository;
import utils.Logger;
import utils.PasswordUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("userRepositoryJpa")
@Transactional
public class UserRepositoryPersistance implements IUserRepository {

    private static final Logger logger = Logger.getInstance();

    @Autowired
    private IUserJpaRepository userJpaRepository;

    // We still need to store passwords separately since they shouldn't be in the entity
    private final Map<String, String> passwordMap = new HashMap<>();
    
    // Guest users (not persisted to database)
    private final Map<String, User> guestMap = new HashMap<>();

    public UserRepositoryPersistance() {
        // Initialize with some default users for compatibility
        logger.info("[UserRepositoryJpa] Initializing JPA-based user repository");
    }

    @Override
    public User findById(String userName) {
        logger.info("[UserRepositoryJpa] Attempting to find user by id: " + userName);
        
        // Check guests first
        User guestUser = guestMap.get(userName);
        if (guestUser != null) {
            logger.info("[UserRepositoryJpa] Guest user found: " + userName);
            return guestUser;
        }
        
        // Check database
        Optional<User> userOpt = userJpaRepository.findByUserName(userName);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("[UserRepositoryJpa] Database user found: " + userName);
            return user;
        }
        
        logger.error("[UserRepositoryJpa] User with name '" + userName + "' not found.");
        throw new RuntimeException("[UserRepositoryJpa] User with name '" + userName + "' not found.");
    }

    @Override
    public void register(String name) {
        logger.info("[UserRepositoryJpa] Registering guest: " + name);
        if (guestMap.containsKey(name) || userJpaRepository.existsByUserName(name)) {
            logger.debug("[UserRepositoryJpa] User with name '" + name + "' already exists.");
            throw new RuntimeException("User with name '" + name + "' already exists.");
        }
        User guest = new User(name);
        guestMap.put(name, guest);
        logger.info("[UserRepositoryJpa] Guest registered: " + name);
    }

    @Override
    public void register(String name, String pw) {
        logger.info("[UserRepositoryJpa] Registering user: " + name);
        if (guestMap.containsKey(name) || userJpaRepository.existsByUserName(name)) {
            logger.debug("[UserRepositoryJpa] User with name '" + name + "' already exists.");
            throw new RuntimeException("User with name '" + name + "' already exists.");
        }
        Subscriber subscriber = new Subscriber(name);
        userJpaRepository.save(subscriber);
        passwordMap.put(name, PasswordUtil.hashPassword(pw));
        logger.info("[UserRepositoryJpa] User registered: " + name);
    }

    @Override
    public boolean verifyPassword(String username, String plainPassword) {
        logger.info("[UserRepositoryJpa] Verifying password for user: " + username);
        String hashedPassword = passwordMap.get(username);
        if (hashedPassword == null) {
            logger.debug("[UserRepositoryJpa] User with name '" + username + "' not found or no password set.");
            return false;
        }
        boolean isMatch = PasswordUtil.verifyPassword(plainPassword, hashedPassword);
        if (isMatch) {
            logger.info("[UserRepositoryJpa] Password verification successful for user: " + username);
        } else {
            logger.info("[UserRepositoryJpa] Password verification failed for user: " + username);
        }
        return isMatch;
    }

    @Override
    public void delete(String name) {
        logger.info("[UserRepositoryJpa] Deleting user: " + name);
        boolean isGuest = guestMap.containsKey(name);
        boolean isDbUser = userJpaRepository.existsByUserName(name);
        
        if (!isGuest && !isDbUser) {
            logger.debug("[UserRepositoryJpa] User with name '" + name + "' does not exist.");
            throw new RuntimeException("User with name '" + name + "' does not exist.");
        }
        
        if (isGuest) {
            guestMap.remove(name);
        }
        if (isDbUser) {
            userJpaRepository.deleteById(name);
        }
        passwordMap.remove(name);
        logger.info("[UserRepositoryJpa] User deleted: " + name);
    }

    @Override
    public boolean changeUserName(String oldName, String newName) {
        logger.info("[UserRepositoryJpa] Changing username from '" + oldName + "' to '" + newName + "'.");
        
        // Guest users cannot change usernames
        if (guestMap.containsKey(oldName)) {
            logger.debug("[UserRepositoryJpa] Guest users cannot change usernames: " + oldName);
            throw new RuntimeException("Guest users cannot change usernames.");
        }
        
        // Check if user exists in database
        Optional<User> userOpt = userJpaRepository.findByUserName(oldName);
        if (userOpt.isEmpty()) {
            logger.debug("[UserRepositoryJpa] User with name '" + oldName + "' does not exist.");
            throw new RuntimeException("User with name '" + oldName + "' does not exist.");
        }
        
        // Check if new name already exists
        if (userJpaRepository.existsByUserName(newName) || guestMap.containsKey(newName)) {
            logger.debug("[UserRepositoryJpa] User with name '" + newName + "' already exists.");
            throw new RuntimeException("User with name '" + newName + "' already exists.");
        }
        
        // Update username
        User user = userOpt.get();
        user.setUserName(newName);
        userJpaRepository.save(user);
        
        // Update password mapping
        String password = passwordMap.remove(oldName);
        if (password != null) {
            passwordMap.put(newName, password);
        }
        
        logger.info("[UserRepositoryJpa] Username changed from '" + oldName + "' to '" + newName + "'.");
        return true;
    }

    @Override
    public boolean changePassword(String userName, String newPassword) {
        logger.info("[UserRepositoryJpa] Changing password for user: " + userName);
        
        if (guestMap.containsKey(userName)) {
            logger.debug("[UserRepositoryJpa] Guest users cannot change passwords: " + userName);
            throw new RuntimeException("Guest users cannot change passwords.");
        }
        
        if (!userJpaRepository.existsByUserName(userName)) {
            logger.debug("[UserRepositoryJpa] User with name '" + userName + "' does not exist.");
            throw new RuntimeException("User with name '" + userName + "' does not exist.");
        }
        
        passwordMap.put(userName, PasswordUtil.hashPassword(newPassword));
        logger.info("[UserRepositoryJpa] Password changed for user: " + userName);
        return true;
    }

    @Override
    public ShoppingCart getCart(String userName) {
        logger.info("[UserRepositoryJpa] Getting cart for user: " + userName);
        User user = findById(userName);
        return user.getShoppingCart();
    }

    @Override
    public void saveAdmin(Admin admin, String password) {
        logger.info("[UserRepositoryJpa] Saving admin: " + admin.getUserName());
        userJpaRepository.save(admin);
        passwordMap.put(admin.getUserName(), PasswordUtil.hashPassword(password));
        logger.info("[UserRepositoryJpa] Admin saved: " + admin.getUserName());
    }

    @Override
    public void save(User user) {
        logger.info("[UserRepositoryJpa] Saving user: " + user.getUserName());
        if (user instanceof Subscriber) {
            userJpaRepository.save(user);
        } else {
            // Guest user - save to guest map
            guestMap.put(user.getUserName(), user);
        }
    }

    @Override
    public List<User> getAllUsers() {
        logger.info("[UserRepositoryJpa] Getting all users");
        return userJpaRepository.findAll();
    }
} 