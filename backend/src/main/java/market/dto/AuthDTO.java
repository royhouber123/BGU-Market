package market.dto;

public class AuthDTO {
    
    public record LoginRequest(
        String username,
        String password
    ) {}
    
    public record RegisterRequest(
        String username,
        String password
    ) {}
    
    public record GuestCheckoutRequest(
        String email
    ) {}
    
    public record GuestLoginRequest(
        String guestId
    ) {}
    
    public record LoginResponse(
        String token,
        String username,
        boolean success,
        String message
    ) {}
    
    public record RegisterResponse(
        boolean success,
        String message
    ) {}
} 