package market.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import market.domain.user.Subscriber.StoreRoleKey;
import market.domain.user.IUserRepository;
import market.domain.user.Subscriber;
import market.domain.user.User;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Collections;

import javax.crypto.SecretKey;

import utils.Logger;

public class AuthService {
    public record AuthTokens(String accessToken, String refreshToken) {}
    private static final Logger logger = Logger.getInstance();
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long accessTokenTtlMs;
    private final long refreshTokenTtlMs;
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    private final IUserRepository userRepository;

    public AuthService(IUserRepository userRepository) {
        String accessSecret  = "JMvzGmTQtUL4OWwh-JAiawZXbxKKrFssCXZtkC_ZUKc";
        String refreshSecret = "LPhmk_IdSLlevsRkTccTc0khD4W8gYrhysF0Yo74R7A";

        this.accessTokenKey  = Keys.hmacShaKeyFor(
            io.jsonwebtoken.io.Decoders.BASE64URL.decode(accessSecret));
        this.refreshTokenKey = Keys.hmacShaKeyFor(
            io.jsonwebtoken.io.Decoders.BASE64URL.decode(refreshSecret));

        this.accessTokenTtlMs  = TimeUnit.DAYS.toMillis(1);   // 24 h access tokens
        this.refreshTokenTtlMs = TimeUnit.DAYS.toMillis(7);   // 7 d refresh tokens
        this.userRepository    = userRepository;
        logger.info("AuthService initialized");
    }

    public String generateAccessToken(User user) {
        logger.info("Generating access token for user: " + user.getUserName());
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenTtlMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getUserName())          
                .setIssuer("com.exmaple")
                .setIssuedAt(now)
                .setExpiration(expiry);  
                
        if (user instanceof Subscriber sub) {
            Map<String, List<String>> storeRoles = sub.getRoles().keySet()
                .stream()
                .collect(Collectors.toMap(
                    StoreRoleKey::storeId,
                    k -> Collections.singletonList(sub.getRoles().get(k).getRoleName())
                ));
            builder.claim("storeRoles", storeRoles);
        }
        String token = builder
                .signWith(accessTokenKey, SignatureAlgorithm.HS256)
                .compact();
        logger.info("Access token generated for user: " + user.getUserName());
        return token;
    }

    public String generateRefreshToken(String userName) {
        logger.info("Generating refresh token for user: " + userName);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenTtlMs);
        String token = Jwts.builder()
            .setSubject(userName)
            .setIssuer("com.example.ecommerce")
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(this.refreshTokenKey, SignatureAlgorithm.HS256)
            .compact();
        refreshTokenStore.put(token, userName);
        logger.info("Refresh token generated for user: " + userName);
        return token;
    }

    public boolean validateAccessToken(String token) {
        logger.info("Validating access token");
        try {
            Jwts.parserBuilder()
                .setSigningKey(accessTokenKey)
                .build()
                .parseClaimsJws(token);
            logger.info("Access token valid");
            return true;
        } catch (JwtException e) {
            logger.error("Invalid access token: " + e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        logger.info("Validating refresh token");
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(refreshTokenKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
            return refreshTokenStore.containsKey(token)
                && claims.getExpiration().after(new Date());
        } catch (JwtException e) {
            logger.error("Invalid refresh token: " + e.getMessage());
            return false;
        }
    }

    public Claims parseAccessToken(String token) {
        logger.info("Parsing access token");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(accessTokenKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.info("Access token parsed successfully");
            return claims;
        } catch (JwtException e) {
            logger.error("Failed to parse access token: " + e.getMessage());
            throw e;
        }
    }

    public void revokeRefreshToken(String token) {
        logger.info("Revoking refresh token");
        refreshTokenStore.remove(token);
    }

    public AuthTokens login(String username, String password) throws Exception {
        logger.info("Logging in user: " + username);
        //DB Check 
        Subscriber u = this.userRepository.findById(username);
        if (u == null) {
            logger.error("User not registered");
            throw new Exception("User not registered");
        }
        String access  = generateAccessToken(u);
        String refresh = generateRefreshToken(u.getUserName());
        logger.info("User logged in successfully");
        return new AuthTokens(access, refresh);
    }

    /** Log out: revoke refresh-token and blacklist the access-token. */
    public void logout(String refreshToken, String accessToken) {
        logger.info("Logging out user");
        // 1. Revoke the refresh token (if any)
        if (refreshToken != null && !refreshToken.isBlank()) {
            revokeRefreshToken(refreshToken);
        }
    
        // 2. Parse the access token, extract the username (subject)
        Claims claims = parseAccessToken(accessToken);
        String username = claims.getSubject();
        if (username == null || username.isBlank()) {
            logger.error("Invalid access token: subject is missing");
            throw new IllegalArgumentException("Invalid access token: subject is missing");
        }
    
        // 3. Delete the user via the repository
        userRepository.delete(username);
        logger.info("User logged out successfully");
    }
}
