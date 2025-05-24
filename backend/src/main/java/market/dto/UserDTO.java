package market.dto;

public class UserDTO {
    
    public record ChangePasswordRequest(
        String newPassword
    ) {}
    
    public record ChangeUsernameRequest(
        String newUsername
    ) {}
    
    public record UserProfileRequest(
        String userId
    ) {}
    
    public record UserProfileResponse(
        String userId,
        String username,
        String email,
        boolean success,
        String message
    ) {}
    
    public record ChangePasswordResponse(
        boolean success,
        String message
    ) {}
    
    public record ChangeUsernameResponse(
        boolean success,
        String message
    ) {}
} 