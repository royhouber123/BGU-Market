package utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class for password hashing and verification using BCrypt.
 */
public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private static final Logger logger = Logger.getInstance();

    /**
     * Hashes a plain text password using BCrypt.
     * 
     * @param plainPassword The plain text password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainPassword) {
        logger.info("Hashing password");
        return encoder.encode(plainPassword);
    }

    /**
     * Verifies if a plain text password matches a hashed password.
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The hashed password to verify against
     * @return True if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        logger.info("Verifying password");
        return encoder.matches(plainPassword, hashedPassword);
    }
}
