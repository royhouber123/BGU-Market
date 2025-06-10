package market.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.security.Keys;
import market.domain.user.IUserRepository;
import market.domain.user.User;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import utils.Logger;

public class AuthService {
    public record AuthToken(String token) {}
    private static final Logger logger = Logger.getInstance();
    private final SecretKey tokenKey;
    private final long tokenTtlMs;
    private final IUserRepository userRepository;

    public AuthService(IUserRepository userRepository) {
        String tokenSecret = "JMvzGmTQtUL4OWwh-JAiawZXbxKKrFssCXZtkC_ZUKc";

        this.tokenKey = Keys.hmacShaKeyFor(
            io.jsonwebtoken.io.Decoders.BASE64URL.decode(tokenSecret));

        this.tokenTtlMs = TimeUnit.DAYS.toMillis(7);   // 7-day tokens
        this.userRepository = userRepository;
        logger.info("AuthService initialized");
    }

    public String generateToken(User user) {
        logger.info("Generating token for user: " + user.getUserName());
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenTtlMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getUserName())          
                .setIssuer("com.exmaple")
                .setIssuedAt(now)
                .setExpiration(expiry);  
                
        String token = builder
                .signWith(tokenKey, SignatureAlgorithm.HS256)
                .compact();
        logger.info("Token generated for user: " + user.getUserName());
        return token;
    }

    public Void validateToken(String token) {
        logger.info("Validating token");
        Jwts.parserBuilder()
            .setSigningKey(tokenKey)
            .build()
            .parseClaimsJws(token);
        logger.info("Token valid");
        return null;
    }

    public Claims parseToken(String token) {
        logger.info("Parsing token");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(tokenKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        logger.info("Token parsed successfully");
        return claims;
    }

    public AuthToken login(String username, String password) {
        logger.info("Logging in user: " + username);
        //DB Check 
        User u = this.userRepository.findById(username);
        // Verify the provided password matches the stored hashed password
        if (!this.userRepository.verifyPassword(username, password)) {
            logger.error("Invalid password for user: " + username);
            throw new IllegalArgumentException("Invalid username or password");
        }
        String token = generateToken(u);
        logger.info("User logged in successfully");
        return new AuthToken(token);
    }

    /** Generate token for guest user (no password required) */
    public AuthToken loginGuest(String guestUsername) {
        logger.info("Generating token for guest user: " + guestUsername);
        try {
            User u = this.userRepository.findById(guestUsername);
            // Check if this user has a password (exists in password map)
            // If verifyPassword returns true, it means the user has a password (not a guest)
            // If verifyPassword returns false, it could mean either:
            // 1. User doesn't exist (will be caught by findById above)
            // 2. User exists but has no password (guest user)
            
            // Try to verify with a dummy password - if it returns true, user has a password
            if (this.userRepository.verifyPassword(guestUsername, "dummy")) {
                // This user has a password, so it's not a guest
                throw new IllegalArgumentException("User has password, use regular login");
            }
            
            // If we reach here, verifyPassword returned false, which means:
            // - User exists (confirmed by findById above)
            // - User has no password (guest user)
            String token = generateToken(u);
            logger.info("Guest token generated successfully");
            return new AuthToken(token);
            
        } catch (RuntimeException e) {
            // If findById throws exception, user doesn't exist
            if (e.getMessage().contains("not found")) {
                throw new IllegalArgumentException("Guest user not found");
            }
            // Re-throw other runtime exceptions
            throw e;
        }
    }

    /** Log out: revoke the user's token */
    public Void logout(String token) {
        logger.info("Logging out user");
        
        // Parse the token, extract the username (subject)
        Claims claims = parseToken(token);
        String username = claims.getSubject();
        if (username == null || username.isBlank()) {
            logger.error("Invalid token: subject is missing");
            throw new IllegalArgumentException("Invalid token: subject is missing");
        }
    
        // Check if user is a guest (username starts with "guest-")
        if (username.startsWith("guest-")) {
            // For guest users, don't delete them - they should persist until checkout completion
            logger.info("Guest user logout - preserving user data: " + username);
        } else {
            // For regular users, in a stateless JWT system, we typically just let the token expire
            // However, if the business logic requires immediate user deletion on logout, 
            // this should be handled at the application level, not authentication level
            logger.info("Regular user logout - token will expire naturally: " + username);
        }
        
        // Note: In a stateless JWT system, we don't typically need to do anything on logout
        // except remove the token from the client side. The token will expire naturally.
        // If we needed proper token blacklisting, we'd maintain a blacklist of invalidated tokens.
        
        logger.info("User logged out successfully: " + username);
        return null;
    }
}
