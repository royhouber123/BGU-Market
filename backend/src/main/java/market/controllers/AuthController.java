package market.controllers;

import market.application.AuthService;
import market.dto.AuthDTO;
import utils.ApiResponse;
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
        ApiResponse<AuthService.AuthToken> response = authService.login(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    /**
     * Validate token
     * POST /api/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Void>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        ApiResponse<Void> response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * User logout
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        ApiResponse<Void> response = authService.logout(token);
        return ResponseEntity.ok(response);
    }
} 