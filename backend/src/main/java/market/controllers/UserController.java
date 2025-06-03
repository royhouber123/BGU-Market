package market.controllers;

import market.application.UserService;
import market.domain.user.ShoppingCart;
import market.domain.user.User;
import market.dto.AuthDTO;
import market.dto.UserDTO;
import market.dto.CartDTO;
import utils.ApiResponse;
import utils.ApiResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * REST Controller for user-related operations.
 * This controller delegates all business logic to the UserService.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Register a new guest user
     * POST /api/users/register/guest
     */
    @PostMapping("/register/guest")
    public ResponseEntity<ApiResponse<Void>> registerGuest(@RequestBody AuthDTO.RegisterRequest request) {
        return ApiResponseBuilder.build(() -> 
            userService.register(request.username())
        );
    }

    /**
     * Register a new user with username and password
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@RequestBody AuthDTO.RegisterRequest request) {
        return ApiResponseBuilder.build(() -> 
            userService.register(request.username(), request.password())
        );
    }

    /**
     * Delete a user
     * DELETE /api/users/{username}
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String username) {
        return ApiResponseBuilder.build(() -> 
            userService.deleteUser(username)
        );
    }

    /**
     * Get current user information
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        return ApiResponseBuilder.build(() -> 
            userService.getUser()
        );
    }

    /**
     * Validate if a user exists in the system
     * GET /api/users/validate/{userId}
     */
    @GetMapping("/validate/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateUser(@PathVariable String userId) {
        ApiResponse<Map<String, Boolean>> response = userService.validateUserExists(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Change username
     * PUT /api/users/username
     * Returns new JWT token for the updated username
     */
    @PutMapping("/username")
    public ResponseEntity<ApiResponse<String>> changeUsername(@RequestBody UserDTO.ChangeUsernameRequest request) {
        return ApiResponseBuilder.build(() -> 
            userService.changeUserName(request.newUsername())
        );
    }

    /**
     * Change password
     * PUT /api/users/password
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@RequestBody UserDTO.ChangePasswordRequest request) {
        return ApiResponseBuilder.build(() -> 
            userService.changePassword(request.newPassword())
        );
    }

    /**
     * Add product to cart
     * POST /api/users/cart/add
     */
    @PostMapping("/cart/add")
    public ResponseEntity<ApiResponse<Void>> addProductToCart(@RequestBody CartDTO.AddProductToCartRequest request) {
        return ApiResponseBuilder.build(() -> 
            userService.addProductToCart(
                request.storeId(), 
                request.productName(), 
                request.quantity()
            )
        );
    }

    /**
     * Remove product from cart
     * POST /api/users/cart/remove
     */
    @PostMapping("/cart/remove")
    public ResponseEntity<ApiResponse<Void>> removeProductFromCart(@RequestBody CartDTO.RemoveProductFromCartRequest request) {
        return ApiResponseBuilder.build(() -> 
            userService.removeProductFromCart(
                request.storeId(), 
                request.productName(), 
                request.quantity()
            )
        );
    }

    /**
     * Clear entire cart
     * DELETE /api/users/cart
     */
    @DeleteMapping("/cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        return ApiResponseBuilder.build(() -> 
            userService.clearCart()
        );
    }

    /**
     * Get user's shopping cart
     * GET /api/users/cart
     */
    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<ShoppingCart>> getCart() {
        return ApiResponseBuilder.build(() -> 
            userService.getCart()
        );
    }
} 