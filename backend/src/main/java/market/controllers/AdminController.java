package market.controllers;

import market.application.AdminService;
import utils.ApiResponse;
import utils.ApiResponseBuilder;
import utils.Logger;

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
    
    private static final Logger logger = Logger.getInstance();

    /**
     * Close a store by admin request
     * POST /api/admin/stores/close
     */
    @PostMapping("/stores/close")
    public ResponseEntity<ApiResponse<Boolean>> closeStore(@RequestBody Map<String, String> request) {
        return ApiResponseBuilder.build(() -> {
            try {
                adminService.closeStoreByAdmin(
                    request.get("adminId"), 
                    request.get("storeId")
                );
                return true;
            } catch (Exception e) {
                logger.error("Error closing store: " + e.getMessage());
                throw new RuntimeException("Failed to close store", e);
            }
        });
    }

    /**
     * Verify if a user is an admin
     * GET /api/admin/verify/{userId}
     */
    @GetMapping("/verify/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> validateAdmin(@PathVariable String userId) {
        return ApiResponseBuilder.build(() -> {
            try {
                return adminService.validateAdmin(userId);
            } catch (Exception e) {
                logger.error("Error validating admin: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Get list of all stores for admin management
     * GET /api/admin/stores
     */
    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllStores() {
        return ApiResponseBuilder.build(() -> {
            try {
                return adminService.getAllStores();
            } catch (Exception e) {
                logger.error("Error getting stores: " + e.getMessage());
                throw new RuntimeException("Failed to retrieve stores", e);
            }
        });
    }

    /**
     * Get list of all users for admin management
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers() {
        return ApiResponseBuilder.build(() -> {
            try {
                return adminService.getAllUsers();
            } catch (Exception e) {
                logger.error("Error getting users: " + e.getMessage());
                throw new RuntimeException("Failed to retrieve users", e);
            }
        });
    }
    
    /**
     * Suspend a user by admin request for a specified duration
     * POST /api/admin/users/suspend
     */
    @PostMapping("/users/suspend")
    public ResponseEntity<ApiResponse<Boolean>> suspendUser(@RequestBody Map<String, Object> request) {
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
            
            try {
                System.out.println("Suspension user: " + userId + " for " + durationHours + " hours");
                adminService.suspendUser(adminId, userId, durationHours);
                return true;
            } catch (Exception e) {
                logger.error("Error suspending user: " + e.getMessage());
                throw new RuntimeException("Failed to suspend user", e);
            }
        });
    }
    
    /**
     * Unsuspend a user by admin request
     * POST /api/admin/users/unsuspend
     */
    @PostMapping("/users/unsuspend")
    public ResponseEntity<ApiResponse<Boolean>> unsuspendUser(@RequestBody Map<String, String> request) {
        return ApiResponseBuilder.build(() -> {
            String adminId = request.get("adminId");
            String userId = request.get("userId");
            
            try {
                System.out.println("Unsuspending user: " + userId);
                adminService.unsuspendUser(adminId, userId);
                return true;
            } catch (Exception e) {
                logger.error("Error unsuspending user: " + e.getMessage());
                throw new RuntimeException("Failed to unsuspend user", e);
            }
        });
    }
    
    /**
     * Get a list of all suspended users
     * GET /api/admin/users/suspended
     */
    @GetMapping("/users/suspended")
    public ResponseEntity<ApiResponse<List<String>>> getSuspendedUsers(@RequestParam String adminId) {
        return ApiResponseBuilder.build(() -> {
            try {
                System.out.println("Get all suspended users Admin ID: " + adminId);
                return adminService.getSuspendedUserIds(adminId);
            } catch (Exception e) {
                logger.error("Error getting suspended users: " + e.getMessage());
                throw new RuntimeException("Failed to retrieve suspended users", e);
            }
        });
    }
}
