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

import javax.swing.JWindow;
import javax.crypto.SecretKey;

public class AuthService {
    public record AuthTokens(String accessToken, String refreshToken) {}
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long accessTokenTtlMs;
    private final long refreshTokenTtlMs;
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    private final IUserRepository userRepository;

    public AuthService(String accessSecret, String refreshSecret , IUserRepository userRepository) {
        this.accessTokenKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlMs = TimeUnit.DAYS.toMillis(1);;
        this.refreshTokenTtlMs = TimeUnit.DAYS.toMillis(1);;
        this.userRepository = userRepository;
    }


    public String generateAccessToken(User user) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenTtlMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(user.getUserId())          
                .setIssuer("com.exmaple")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("user", user);  
                
        if (user instanceof Subscriber sub) {
            Map<String, List<String>> storeRoles = sub.getRoles().keySet()
                .stream()
                .collect(Collectors.groupingBy(
                    StoreRoleKey::storeId,                         
                    Collectors.mapping(StoreRoleKey::roleName,     
                                        Collectors.toList())
                ));
            builder.claim("storeRoles", storeRoles);
        }
        return builder
                .signWith(accessTokenKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenTtlMs);
        String token = Jwts.builder()
            .setSubject(userId)
            .setIssuer("com.example.ecommerce")
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(this.refreshTokenKey, SignatureAlgorithm.HS256)
            .compact();
        refreshTokenStore.put(token, userId);
        return token;
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(accessTokenKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(refreshTokenKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
            return refreshTokenStore.containsKey(token)
                && claims.getExpiration().after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(accessTokenKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public void revokeRefreshToken(String token) {
        refreshTokenStore.remove(token);
    }

    public AuthTokens login(String username, String password) throws Exception {
        
        User u = this.userRepository.isExist(username , password);
        if (u == null) throw new Exception("User not registered");
        String access  = generateAccessToken(u);
        String refresh = generateRefreshToken(u.getUserId());
        return new AuthTokens(access, refresh);
    }

    /** Log out: revoke refresh-token and blacklist the access-token. */
    public void logout(String refreshToken, String accessToken) {
        if (refreshToken != null) revokeRefreshToken(refreshToken);
    }
}
