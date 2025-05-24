package market.controllers;

import market.application.StoreService;
import market.domain.store.StoreDTO;
import market.domain.store.Listing;
import market.domain.store.IListingRepository;
import market.dto.ProductDTO;
import utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
        ApiResponse<market.dto.StoreDTO.CreateStoreResponse> response = storeService.createStore(request.storeName(), request.founderId());
        return ResponseEntity.ok(response);
    }

    /**
     * Close a store
     * POST /api/stores/{storeID}/close
     */
    @PostMapping("/{storeID}/close")
    public ResponseEntity<ApiResponse<String>> closeStore(@PathVariable String storeID, @RequestParam String userName) {
        ApiResponse<String> response = storeService.closeStore(storeID, userName);
        return ResponseEntity.ok(response);
    }

    /**
     * Open a store
     * POST /api/stores/{storeID}/open
     */
    @PostMapping("/{storeID}/open")
    public ResponseEntity<ApiResponse<String>> openStore(@PathVariable String storeID, @RequestParam String userName) {
        ApiResponse<String> response = storeService.openStore(storeID, userName);
        return ResponseEntity.ok(response);
    }

    /**
     * Get store information by name
     * GET /api/stores/{storeName}
     */
    @GetMapping("/{storeName}")
    public ResponseEntity<ApiResponse<StoreDTO>> getStore(@PathVariable String storeName) {
        ApiResponse<StoreDTO> response = storeService.getStore(storeName);
        return ResponseEntity.ok(response);
    }

    /**
     * Add additional store owner
     * POST /api/stores/owners/add
     */
    @PostMapping("/owners/add")
    public ResponseEntity<ApiResponse<Void>> addAdditionalStoreOwner(@RequestBody market.dto.StoreDTO.AddOwnerRequest request) {
        ApiResponse<Void> response = storeService.addAdditionalStoreOwner(
            request.appointerID(), 
            request.newOwnerID(), 
            request.storeID()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Request owner appointment
     * POST /api/stores/owners/request
     */
    @PostMapping("/owners/request")
    public ResponseEntity<ApiResponse<Void>> ownerAppointmentRequest(@RequestBody market.dto.StoreDTO.AddOwnerRequest request) {
        ApiResponse<Void> response = storeService.OwnerAppointmentRequest(
            request.appointerID(), 
            request.newOwnerID(), 
            request.storeID()
        );
        return ResponseEntity.ok(response);
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
        ApiResponse<List<List<String>>> response = storeService.removeOwner(requesterId, toRemove, storeID);
        return ResponseEntity.ok(response);
    }

    /**
     * Add new manager
     * POST /api/stores/managers/add
     */
    @PostMapping("/managers/add")
    public ResponseEntity<ApiResponse<Void>> addNewManager(@RequestBody market.dto.StoreDTO.AddManagerRequest request) {
        ApiResponse<Void> response = storeService.addNewManager(
            request.appointerID(), 
            request.newManagerName(), 
            request.storeID()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Add permission to manager
     * POST /api/stores/managers/permissions/add
     */
    @PostMapping("/managers/permissions/add")
    public ResponseEntity<ApiResponse<Void>> addPermissionToManager(@RequestBody market.dto.StoreDTO.AddPermissionRequest request) {
        ApiResponse<Void> response = storeService.addPermissionToManager(
            request.managerID(), 
            request.appointerID(), 
            request.permissionID(), 
            request.storeID()
        );
        return ResponseEntity.ok(response);
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
        ApiResponse<Set<Integer>> response = storeService.getManagersPermissions(managerID, whoIsAsking, storeID);
        return ResponseEntity.ok(response);
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
        ApiResponse<Void> response = storeService.removePermissionFromManager(managerID, permissionID, appointerID, storeID);
        return ResponseEntity.ok(response);
    }

    /**
     * Add new listing
     * POST /api/stores/listings/add
     */
    @PostMapping("/listings/add")
    public ResponseEntity<ApiResponse<String>> addNewListing(@RequestBody ProductDTO.AddListingRequest request) {
        ApiResponse<String> response = storeService.addNewListing(
            request.userName(),
            request.storeID(),
            request.productId(),
            request.productName(),
            request.productCategory(),
            request.productDescription(),
            request.quantity(),
            request.price()
        );
        return ResponseEntity.ok(response);
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
        ApiResponse<Void> response = storeService.removeListing(userName, storeID, listingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Edit listing price
     * PUT /api/stores/listings/price
     */
    @PutMapping("/listings/price")
    public ResponseEntity<ApiResponse<Boolean>> editListingPrice(@RequestBody ProductDTO.EditListingPriceRequest request) {
        ApiResponse<Boolean> response = storeService.editListingPrice(
            request.userName(),
            request.storeID(),
            request.listingId(),
            request.newPrice()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Edit listing product name
     * PUT /api/stores/listings/name
     */
    @PutMapping("/listings/name")
    public ResponseEntity<ApiResponse<Boolean>> editListingProductName(@RequestBody ProductDTO.EditListingRequest request) {
        ApiResponse<Boolean> response = storeService.editListingProductName(
            request.userName(),
            request.storeID(),
            request.listingId(),
            request.newValue()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Edit listing description
     * PUT /api/stores/listings/description
     */
    @PutMapping("/listings/description")
    public ResponseEntity<ApiResponse<Boolean>> editListingDescription(@RequestBody ProductDTO.EditListingRequest request) {
        ApiResponse<Boolean> response = storeService.editListingDescription(
            request.userName(),
            request.storeID(),
            request.listingId(),
            request.newValue()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Edit listing quantity
     * PUT /api/stores/listings/quantity
     */
    @PutMapping("/listings/quantity")
    public ResponseEntity<ApiResponse<Boolean>> editListingQuantity(@RequestBody ProductDTO.EditListingQuantityRequest request) {
        ApiResponse<Boolean> response = storeService.editListingQuantity(
            request.userName(),
            request.storeID(),
            request.listingId(),
            request.newQuantity()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Edit listing category
     * PUT /api/stores/listings/category
     */
    @PutMapping("/listings/category")
    public ResponseEntity<ApiResponse<Boolean>> editListingCategory(@RequestBody ProductDTO.EditListingRequest request) {
        ApiResponse<Boolean> response = storeService.editListingCategory(
            request.userName(),
            request.storeID(),
            request.listingId(),
            request.newValue()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get product price
     * GET /api/stores/{storeID}/products/{productID}/price
     */
    @GetMapping("/{storeID}/products/{productID}/price")
    public ResponseEntity<ApiResponse<Double>> getProductPrice(
            @PathVariable String storeID,
            @PathVariable String productID) {
        ApiResponse<Double> response = storeService.getProductPrice(storeID, productID);
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
        ApiResponse<Boolean> response = storeService.isOwner(storeID, userID);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user is manager
     * GET /api/stores/{storeID}/managers/{userID}/check
     */
    @GetMapping("/{storeID}/managers/{userID}/check")
    public ResponseEntity<ApiResponse<Boolean>> isManager(
            @PathVariable String storeID,
            @PathVariable String userID) {
        ApiResponse<Boolean> response = storeService.isManager(storeID, userID);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all stores and their products
     * GET /api/stores/info
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<HashMap<StoreDTO, List<Listing>>>> getInformationAboutStoresAndProducts() {
        ApiResponse<HashMap<StoreDTO, List<Listing>>> response = storeService.getInformationAboutStoresAndProducts();
        return ResponseEntity.ok(response);
    }

    /**
     * Get listing repository
     * GET /api/stores/listings/repository
     */
    @GetMapping("/listings/repository")
    public ResponseEntity<ApiResponse<IListingRepository>> getListingRepository() {
        ApiResponse<IListingRepository> response = storeService.getListingRepository();
        return ResponseEntity.ok(response);
    }
} 