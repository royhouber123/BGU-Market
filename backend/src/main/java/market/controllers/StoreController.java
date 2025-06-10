package market.controllers;

import market.application.StoreService;
import market.domain.store.StoreDTO;
import market.domain.store.Listing;
import market.domain.store.IListingRepository;
import market.dto.ProductDTO;
import utils.ApiResponse;
import utils.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * REST Controller for store-related operations.
 * This controller delegates all business logic to the StoreService.
 */
@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*")
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * Create a new store
     * POST /api/stores/create
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<market.dto.StoreDTO.CreateStoreResponse>> createStore(@RequestBody market.dto.StoreDTO.CreateStoreRequest request) {       
        return ApiResponseBuilder.build(() ->
            storeService.createStore(request.storeName(), request.founderId())
        );
    }

    /**
     * Close a store
     * POST /api/stores/{storeID}/close
     */
    @PostMapping("/{storeID}/close")
    public ResponseEntity<ApiResponse<String>> closeStore(@PathVariable String storeID, @RequestParam String userName) {
        return ApiResponseBuilder.build(() -> 
            storeService.closeStore(storeID, userName)
        );
    }

    /**
     * Open a store
     * POST /api/stores/{storeID}/open
     */
    @PostMapping("/{storeID}/open")
    public ResponseEntity<ApiResponse<String>> openStore(@PathVariable String storeID, @RequestParam String userName) {
        return ApiResponseBuilder.build(() ->
            storeService.openStore(storeID, userName)    
        );
    }

    /**
     * Get store information by name
     * GET /api/stores/{storeName}
     */
    @GetMapping("/{storeName}")
    public ResponseEntity<ApiResponse<StoreDTO>> getStore(@PathVariable String storeName) {
        return ApiResponseBuilder.build(() ->
            storeService.getStore(storeName)
        );
    }

    /**
     * Add additional store owner
     * POST /api/stores/owners/add
     */
    @PostMapping("/owners/add")
    public ResponseEntity<ApiResponse<Void>> addAdditionalStoreOwner(@RequestBody market.dto.StoreDTO.AddOwnerRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.addAdditionalStoreOwner(request.appointerID(), request.newOwnerID(), request.storeID())
        );
    }

    /**
     * Request owner appointment
     * POST /api/stores/owners/request
     */
    @PostMapping("/owners/request")
    public ResponseEntity<ApiResponse<Void>> ownerAppointmentRequest(@RequestBody market.dto.StoreDTO.AddOwnerRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.OwnerAppointmentRequest(request.appointerID(), request.newOwnerID(), request.storeID())
        );
    }

    /**
     * Remove owner
     * DELETE /api/stores/{storeID}/owners/{toRemove}
     */
    @DeleteMapping("/{storeID}/owners/{toRemove}")
    public ResponseEntity<ApiResponse<List<List<String>>>> removeOwner(
            @PathVariable String storeID,
            @PathVariable String toRemove,
            @RequestParam String requesterId) {
        return ApiResponseBuilder.build(() ->
            storeService.removeOwner(requesterId, toRemove, storeID)
        );
    }

    /**
     * Add new manager
     * POST /api/stores/managers/add
     */
    @PostMapping("/managers/add")
    public ResponseEntity<ApiResponse<Void>> addNewManager(@RequestBody market.dto.StoreDTO.AddManagerRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.addNewManager(request.appointerID(), request.newManagerName(), request.storeID())
        );
    }

    /**
     * Add permission to manager
     * POST /api/stores/managers/permissions/add
     */
    @PostMapping("/managers/permissions/add")
    public ResponseEntity<ApiResponse<Void>> addPermissionToManager(@RequestBody market.dto.StoreDTO.AddPermissionRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.addPermissionToManager(request.managerID(), request.appointerID(), request.permissionID(), request.storeID())
        );
    }

    /**
     * Get manager permissions
     * GET /api/stores/{storeID}/managers/{managerID}/permissions
     */
    @GetMapping("/{storeID}/managers/{managerID}/permissions")
    public ResponseEntity<ApiResponse<Set<Integer>>> getManagersPermissions(
            @PathVariable String storeID,
            @PathVariable String managerID,
            @RequestParam String whoIsAsking) {
        return ApiResponseBuilder.build(() ->
            storeService.getManagersPermissions(managerID, whoIsAsking, storeID)
        );
    }

    /**
     * Remove permission from manager
     * DELETE /api/stores/{storeID}/managers/{managerID}/permissions/{permissionID}
     */
    @DeleteMapping("/{storeID}/managers/{managerID}/permissions/{permissionID}")
    public ResponseEntity<ApiResponse<Void>> removePermissionFromManager(
            @PathVariable String storeID,
            @PathVariable String managerID,
            @PathVariable int permissionID,
            @RequestParam String appointerID) {
        return ApiResponseBuilder.build(() ->
            storeService.removePermissionFromManager(managerID, permissionID, appointerID, storeID)
        );
    }

    /**
     * Add new listing
     * POST /api/stores/listings/add
     */
    @PostMapping("/listings/add")
    public ResponseEntity<ApiResponse<String>> addNewListing(@RequestBody ProductDTO.AddListingRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.addNewListing(
                request.userName(),
                request.storeID(),
                request.productId(),
                request.productName(),
                request.productCategory(),
                request.productDescription(),
                request.quantity(),
                request.price(),
                request.purchaseType() != null ? request.purchaseType() : "REGULAR"  // Default to REGULAR if not provided
            )
        );
    }

    /**
     * Remove listing
     * DELETE /api/stores/{storeID}/listings/{listingId}
     */
    @DeleteMapping("/{storeID}/listings/{listingId}")
    public ResponseEntity<ApiResponse<Void>> removeListing(
            @PathVariable String storeID,
            @PathVariable String listingId,
            @RequestParam String userName) {
        return ApiResponseBuilder.build(() ->
            storeService.removeListing(userName, storeID, listingId)
        );
    }

    /**
     * Edit listing price
     * PUT /api/stores/listings/price
     */
    @PutMapping("/listings/price")
    public ResponseEntity<ApiResponse<Boolean>> editListingPrice(@RequestBody ProductDTO.EditListingPriceRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.editListingPrice(
                request.userName(),
                request.storeID(),
                request.listingId(),
                request.newPrice()
            )
        );
    }

    /**
     * Edit listing product name
     * PUT /api/stores/listings/name
     */
    @PutMapping("/listings/name")
    public ResponseEntity<ApiResponse<Boolean>> editListingProductName(@RequestBody ProductDTO.EditListingRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.editListingProductName(
                request.userName(),
                request.storeID(),
                request.listingId(),
                request.newValue()
            )
        );
    }

    /**
     * Edit listing description
     * PUT /api/stores/listings/description
     */
    @PutMapping("/listings/description")
    public ResponseEntity<ApiResponse<Boolean>> editListingDescription(@RequestBody ProductDTO.EditListingRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.editListingDescription(
                request.userName(),
                request.storeID(),
                request.listingId(),
                request.newValue()
            )
        );
    }

    /**
     * Edit listing quantity
     * PUT /api/stores/listings/quantity
     */
    @PutMapping("/listings/quantity")
    public ResponseEntity<ApiResponse<Boolean>> editListingQuantity(@RequestBody ProductDTO.EditListingQuantityRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.editListingQuantity(
                request.userName(),
                request.storeID(),
                request.listingId(),
                request.newQuantity()
            )   
        );
    }

    /**
     * Edit listing category
     * PUT /api/stores/listings/category
     */
    @PutMapping("/listings/category")
    public ResponseEntity<ApiResponse<Boolean>> editListingCategory(@RequestBody ProductDTO.EditListingRequest request) {
        return ApiResponseBuilder.build(() ->
            storeService.editListingCategory(
                request.userName(),
                request.storeID(),
                request.listingId(),
                request.newValue()
            ) 
        );
    }

    /**
     * Get product price
     * GET /api/stores/{storeID}/products/{productID}/price
     */
    @GetMapping("/{storeID}/products/{productID}/price")
    public ResponseEntity<ApiResponse<Double>> getProductPrice(
            @PathVariable String storeID,
            @PathVariable String productID) {
        return ApiResponseBuilder.build(() ->
            storeService.getProductPrice(storeID, productID)
        );
    }

    /**
     * Get product price with discounts applied
     * GET /api/stores/{storeID}/products/{listingID}/discounted-price
     */
    @GetMapping("/{storeID}/products/{listingID}/discounted-price")
    public ResponseEntity<ApiResponse<Double>> getProductDiscountedPrice(
            @PathVariable String storeID,
            @PathVariable String listingID) {
        ApiResponse<Double> response = storeService.getProductDiscountedPrice(storeID, listingID);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user is owner
     * GET /api/stores/{storeID}/owners/{userID}/check
     */
    @GetMapping("/{storeID}/owners/{userID}/check")
    public ResponseEntity<ApiResponse<Boolean>> isOwner(
            @PathVariable String storeID,
            @PathVariable String userID) {
        return ApiResponseBuilder.build(() ->
            storeService.isOwner(storeID, userID)
        );
    }

    /**
     * Check if user is manager
     * GET /api/stores/{storeID}/managers/{userID}/check
     */
    @GetMapping("/{storeID}/managers/{userID}/check")
    public ResponseEntity<ApiResponse<Boolean>> isManager(
            @PathVariable String storeID,
            @PathVariable String userID) {
        return ApiResponseBuilder.build(() ->
            storeService.isManager(storeID, userID)
        );
    }

    /**
     * Check if user is founder of store
     * GET /api/stores/{storeID}/founders/{userID}/check
     */
    @GetMapping("/{storeID}/founders/{userID}/check")
    public ResponseEntity<ApiResponse<Boolean>> isFounder(
            @PathVariable String storeID,
            @PathVariable String userID) {
        ApiResponse<Boolean> response = storeService.isFounder(storeID, userID);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's permissions and role in store
     * GET /api/stores/{storeID}/user/{userID}/permissions
     */
    @GetMapping("/{storeID}/user/{userID}/permissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserPermissions(
            @PathVariable String storeID,
            @PathVariable String userID) {
        ApiResponse<Map<String, Object>> response = storeService.getCurrentUserPermissions(storeID, userID);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all stores and their products
     * GET /api/stores/info
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getInformationAboutStoresAndProducts() {
        return ApiResponseBuilder.build(() ->
            storeService.getInformationAboutStoresAndProducts()
        );
    }

    /**
     * Get listing repository
     * GET /api/stores/listings/repository
     */
    @GetMapping("/listings/repository")
    public ResponseEntity<ApiResponse<IListingRepository>> getListingRepository() {
        return ApiResponseBuilder.build(() ->
            storeService.getListingRepository()
        );
    }

    /**
     * Remove manager
     * DELETE /api/stores/{storeID}/managers/{managerID}
     */
    @DeleteMapping("/{storeID}/managers/{managerID}")
    public ResponseEntity<ApiResponse<Void>> removeManager(
            @PathVariable String storeID,
            @PathVariable String managerID,
            @RequestParam String appointerID) {
        return ApiResponseBuilder.build(() ->
            storeService.removeManager(appointerID, managerID, storeID)
        );
    }

    /**
     * Get all store users (owners and managers)
     * GET /api/stores/{storeID}/users
     */
    @GetMapping("/{storeID}/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStoreUsers(
            @PathVariable String storeID,
            @RequestParam String requesterId) {
        ApiResponse<Map<String, Object>> response = storeService.getStoreUsers(storeID, requesterId);
        return ResponseEntity.ok(response);
    }

        /**
     * Send a message to a store (all owners will receive a notification)
     * POST /api/stores/{storeID}/message
     */
    @PostMapping("/{storeID}/message")
    public ResponseEntity<ApiResponse<Void>> sendMessageToStore(
            @PathVariable String storeID,
            @RequestParam String senderUserId,
            @RequestParam String message) {
        return ApiResponseBuilder.build(() -> {
            storeService.sendMessageToStore(storeID, senderUserId, message);
            return null;
        });
    }
} 