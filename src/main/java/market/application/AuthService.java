package market.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import market.domain.user.IUserRepository;
import market.domain.user.Subscriber;
import market.domain.user.User;
import utils.ApiResponse;

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

    public ApiResponse<String> generateToken(User user) {
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
        return ApiResponse.ok(token);
    }

    public ApiResponse<Void> validateToken(String token) {
        logger.info("Validating token");
        try {
            Jwts.parserBuilder()
                .setSigningKey(tokenKey)
                .build()
                .parseClaimsJws(token);
            logger.info("Token valid");
            return ApiResponse.ok(null);
        } catch (JwtException e) {
            logger.error("Invalid token: " + e.getMessage());
            return ApiResponse.fail("Invalid token: " + e.getMessage());
        }
    }

    public ApiResponse<Claims> parseToken(String token) {
        logger.info("Parsing token");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(tokenKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.info("Token parsed successfully");
            return ApiResponse.ok(claims);
        } catch (JwtException e) {
            logger.error("Failed to parse token: " + e.getMessage());
            return ApiResponse.fail("Failed to parse token: " + e.getMessage());
        }
    }

    public ApiResponse<AuthToken> login(String username, String password) throws Exception {
        logger.info("Logging in user: " + username);
        //DB Check 
        try {
            User u = this.userRepository.findById(username);
            // Verify the provided password matches the stored hashed password
            if (!this.userRepository.verifyPassword(username, password)) {
                logger.error("Invalid password for user: " + username);
                throw new Exception("Invalid username or password");
            }
            String token = generateToken(u).getData();
            logger.info("User logged in successfully");
            return ApiResponse.ok(new AuthToken(token));
        } catch (RuntimeException e) {
            logger.error("User not registered: " + username);
            return ApiResponse.fail("Invalid username or password");
        }
    }

    /** Log out: revoke the user's token */
    public ApiResponse<Void> logout(String token) {
        logger.info("Logging out user");
        
        // Parse the token, extract the username (subject)
        Claims claims = parseToken(token).getData();
        String username = claims.getSubject();
        if (username == null || username.isBlank()) {
            logger.error("Invalid token: subject is missing");
            return ApiResponse.fail("Invalid token: subject is missing");
        }
    
        // Delete the user via the repository
        userRepository.delete(username);
        logger.info("User logged out successfully");
        return ApiResponse.ok(null);
    }
}
