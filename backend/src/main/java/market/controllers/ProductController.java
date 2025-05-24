package market.controllers;

import market.application.ProductService;
import market.domain.store.Listing;
import market.dto.ProductDTO;
import utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for product-related operations.
 * This controller delegates all business logic to the ProductService.
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Search products by name across all stores
     * GET /api/products/search?query={productName}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Listing>>> searchProductsByName(@RequestParam String query) {
        ApiResponse<List<Listing>> response = productService.searchByProductName(query);
        return ResponseEntity.ok(response);
    }

    /**
     * Search products by product ID
     * GET /api/products/id/{productId}
     */
    @GetMapping("/id/{productId}")
    public ResponseEntity<ApiResponse<List<Listing>>> searchProductById(@PathVariable String productId) {
        ApiResponse<List<Listing>> response = productService.searchByProductId(productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all products from a specific store
     * GET /api/products/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<Listing>>> getStoreProducts(@PathVariable String storeId) {
        ApiResponse<List<Listing>> response = productService.getStoreListings(storeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Search products within a specific store by name
     * GET /api/products/store/{storeId}/search?query={productName}
     */
    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<ApiResponse<List<Listing>>> searchInStoreByName(
            @PathVariable String storeId, 
            @RequestParam String query) {
        ApiResponse<List<Listing>> response = productService.searchInStoreByName(storeId, query);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all products sorted by price (ascending)
     * GET /api/products/sorted/price
     */
    @GetMapping("/sorted/price")
    public ResponseEntity<ApiResponse<List<Listing>>> getProductsSortedByPrice() {
        ApiResponse<List<Listing>> response = productService.getAllSortedByPrice();
        return ResponseEntity.ok(response);
    }

    /**
     * Get specific product listing by listing ID
     * GET /api/products/listing/{listingId}
     */
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<ApiResponse<Listing>> getProductListing(@PathVariable String listingId) {
        ApiResponse<Listing> response = productService.getListing(listingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get product information by product ID and store ID
     * POST /api/products/info
     */
    @PostMapping("/info")
    public ResponseEntity<ApiResponse<ProductDTO.ProductInfoResponse>> getProductInfo(
            @RequestBody ProductDTO.ProductInfoRequest request) {
        try {
            ApiResponse<List<Listing>> searchResponse = productService.searchByProductId(request.productId());
            
            if (!searchResponse.isSuccess() || searchResponse.getData().isEmpty()) {
                ProductDTO.ProductInfoResponse response = new ProductDTO.ProductInfoResponse(
                    request.productId(), "", "", "", 0, 0.0, false, "Product not found"
                );
                return ResponseEntity.ok(ApiResponse.ok(response));
            }
            
            Listing listing = searchResponse.getData().get(0);
            ProductDTO.ProductInfoResponse response = new ProductDTO.ProductInfoResponse(
                listing.getProductId(),
                listing.getProductName(),
                listing.getCategory(),
                listing.getProductDescription(),
                listing.getQuantityAvailable(),
                listing.getPrice(),
                true,
                "Product information retrieved successfully"
            );
            
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            ProductDTO.ProductInfoResponse response = new ProductDTO.ProductInfoResponse(
                request.productId(), "", "", "", 0, 0.0, false, "Error retrieving product info: " + e.getMessage()
            );
            return ResponseEntity.ok(ApiResponse.fail("Error retrieving product info: " + e.getMessage()));
        }
    }
} 