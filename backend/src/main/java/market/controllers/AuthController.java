package market.controllers;

import market.application.AuthService;
import market.dto.AuthDTO;
import utils.ApiResponse;
import utils.ApiResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * User login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthService.AuthToken>> login(@RequestBody AuthDTO.LoginRequest request) {
        return ApiResponseBuilder.build(() ->
            authService.login(request.username(), request.password())
        );
    }

    /**
     * Guest login - generate token for guest user
     * POST /api/auth/guest-login
     */
    @PostMapping("/guest-login")
    public ResponseEntity<ApiResponse<AuthService.AuthToken>> guestLogin(@RequestBody AuthDTO.GuestLoginRequest request) {
        return ApiResponseBuilder.build(() ->
            authService.loginGuest(request.guestId())
        );
    }

    /**
     * Validate token
     * POST /api/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Void>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ApiResponseBuilder.build(() ->
            authService.validateToken(token)    
        );
    }

    /**
     * User logout
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ApiResponseBuilder.build(() ->
            authService.logout(token)    
        );
    }
} 