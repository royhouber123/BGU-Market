package market.controllers;

import market.application.AdminService;
import utils.ApiResponse;
import utils.ApiResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for admin-related operations.
 * This controller delegates all business logic to the AdminService.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Close a store by admin request
     * POST /api/admin/stores/close
     */
    @PostMapping("/stores/close")
    public ResponseEntity<ApiResponse<Void>> closeStore(@RequestBody Map<String, String> request) {
        return ApiResponseBuilder.build(() -> 
            adminService.closeStoreByAdmin(
                request.get("adminId"), 
                request.get("storeId")
            )
        );
    }

    /**
     * Verify if a user is an admin
     * GET /api/admin/verify/{userId}
     */
    @GetMapping("/verify/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> validateAdmin(@PathVariable String userId) {
        return ApiResponseBuilder.build(() -> adminService.validateAdmin(userId));
    }

    /**
     * Get list of all stores for admin management
     * GET /api/admin/stores
     */
    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllStores() {
        return ApiResponseBuilder.build(() -> adminService.getAllStores());
    }

    /**
     * Get list of all users for admin management
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers() {
        return ApiResponseBuilder.build(() -> adminService.getAllUsers());
    }
    
    /**
     * Suspend a user by admin request for a specified duration
     * POST /api/admin/users/suspend
     */
    @PostMapping("/users/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendUser(@RequestBody Map<String, Object> request) {
        return ApiResponseBuilder.build(() -> {
            String adminId = (String) request.get("adminId");
            String userId = (String) request.get("userId");
            
            // Parse duration with default to 0 (permanent) if not provided
            long durationHours = 0;
            if (request.containsKey("durationHours")) {
                try {
                    durationHours = Long.parseLong(request.get("durationHours").toString());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid duration format, using permanent suspension");
                }
            }
            
            adminService.suspendUser(adminId, userId, durationHours);
            return null;
        });
    }
    
    /**
     * Unsuspend a user by admin request
     * POST /api/admin/users/unsuspend
     */
    @PostMapping("/users/unsuspend")
    public ResponseEntity<ApiResponse<Void>> unsuspendUser(@RequestBody Map<String, String> request) {
        return ApiResponseBuilder.build(() -> {
            String adminId = request.get("adminId");
            String userId = request.get("userId");
            
            adminService.unsuspendUser(adminId, userId);
            return null;
        });
    }
    
    /**
     * Get a list of all suspended users
     * GET /api/admin/users/suspended
     */
    @GetMapping("/users/suspended")
    public ResponseEntity<ApiResponse<List<String>>> getSuspendedUsers(@RequestParam String adminId) {
        return ApiResponseBuilder.build(() -> adminService.getSuspendedUserIds(adminId));
    }
}
