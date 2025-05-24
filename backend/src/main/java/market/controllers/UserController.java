package market.controllers;

import market.application.UserService;
import market.domain.user.ShoppingCart;
import market.domain.user.User;
import market.dto.AuthDTO;
import market.dto.UserDTO;
import market.dto.CartDTO;
import utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        ApiResponse<Void> response = userService.register(request.username());
        return ResponseEntity.ok(response);
    }

    /**
     * Register a new user with username and password
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@RequestBody AuthDTO.RegisterRequest request) {
        ApiResponse<Void> response = userService.register(request.username(), request.password());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a user
     * DELETE /api/users/{username}
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String username) {
        ApiResponse<Void> response = userService.deleteUser(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user information
     * GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        ApiResponse<User> response = userService.getUser();
        return ResponseEntity.ok(response);
    }

    /**
     * Change username
     * PUT /api/users/username
     */
    @PutMapping("/username")
    public ResponseEntity<ApiResponse<Boolean>> changeUsername(@RequestBody UserDTO.ChangeUsernameRequest request) {
        ApiResponse<Boolean> response = userService.changeUserName(request.newUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Change password
     * PUT /api/users/password
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@RequestBody UserDTO.ChangePasswordRequest request) {
        ApiResponse<Boolean> response = userService.changePassword(request.newPassword());
        return ResponseEntity.ok(response);
    }

    /**
     * Add product to cart
     * POST /api/users/cart/add
     */
    @PostMapping("/cart/add")
    public ResponseEntity<ApiResponse<Void>> addProductToCart(@RequestBody CartDTO.AddProductToCartRequest request) {
        ApiResponse<Void> response = userService.addProductToCart(
            request.storeId(), 
            request.productName(), 
            request.quantity()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Remove product from cart
     * POST /api/users/cart/remove
     */
    @PostMapping("/cart/remove")
    public ResponseEntity<ApiResponse<Void>> removeProductFromCart(@RequestBody CartDTO.RemoveProductFromCartRequest request) {
        ApiResponse<Void> response = userService.removeProductFromCart(
            request.storeId(), 
            request.productName(), 
            request.quantity()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Clear entire cart
     * DELETE /api/users/cart
     */
    @DeleteMapping("/cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        ApiResponse<Void> response = userService.clearCart();
        return ResponseEntity.ok(response);
    }

    /**
     * Get user's shopping cart
     * GET /api/users/cart
     */
    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<ShoppingCart>> getCart() {
        ApiResponse<ShoppingCart> response = userService.getCart();
        return ResponseEntity.ok(response);
    }
} 