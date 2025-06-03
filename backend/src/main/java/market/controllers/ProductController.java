package market.controllers;

import market.application.ProductService;
import market.domain.store.Listing;
import market.dto.ProductDTO;
import utils.ApiResponse;
import utils.ApiResponseBuilder;
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
        return ApiResponseBuilder.build(() -> 
            productService.searchByProductName(query)
        );
    }

    /**
     * Search products by product ID
     * GET /api/products/id/{productId}
     */
    @GetMapping("/id/{productId}")
    public ResponseEntity<ApiResponse<List<Listing>>> searchProductById(@PathVariable String productId) {
        return ApiResponseBuilder.build(() -> 
            productService.searchByProductId(productId)
        );
    }

    /**
     * Get all products from a specific store
     * GET /api/products/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<Listing>>> getStoreProducts(@PathVariable String storeId) {
        return ApiResponseBuilder.build(() -> 
            productService.getStoreListings(storeId)
        );
    }

    /**
     * Search products within a specific store by name
     * GET /api/products/store/{storeId}/search?query={productName}
     */
    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<ApiResponse<List<Listing>>> searchInStoreByName(
            @PathVariable String storeId, 
            @RequestParam String query) {
        return ApiResponseBuilder.build(() -> 
            productService.searchInStoreByName(storeId, query)
        );
    }

    /**
     * Get all products sorted by price (ascending)
     * GET /api/products/sorted/price
     */
    @GetMapping("/sorted/price")
    public ResponseEntity<ApiResponse<List<Listing>>> getProductsSortedByPrice() {
        return ApiResponseBuilder.build(() -> 
            productService.getAllSortedByPrice()
        );
    }

    /**
     * Get specific product listing by listing ID
     * GET /api/products/listing/{listingId}
     */
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<ApiResponse<Listing>> getProductListing(@PathVariable String listingId) {
        return ApiResponseBuilder.build(() -> 
            productService.getListing(listingId)
        );
    }

    /**
     * Get product information by product ID and store ID
     * POST /api/products/info
     */
    @PostMapping("/info")
    public ResponseEntity<ApiResponse<ProductDTO.ProductInfoResponse>> getProductInfo(
            @RequestBody ProductDTO.ProductInfoRequest request) {
        List<Listing> searchResponse = productService.searchByProductId(request.productId());
        
        if (searchResponse == null || searchResponse.isEmpty()) {
            return ApiResponseBuilder.build(() -> 
                new ProductDTO.ProductInfoResponse(
                    request.productId(), "", "", "", 0, 0.0, false, "Product not found"
                )
            );
        }
        
        Listing listing = searchResponse.get(0);
        return ApiResponseBuilder.build(() -> 
            new ProductDTO.ProductInfoResponse(
                listing.getProductId(),
                listing.getProductName(),
                listing.getCategory(),
                listing.getProductDescription(),
                listing.getQuantityAvailable(),
                listing.getPrice(),
                true,
                "Product information retrieved successfully"
            )
        );
    }
} 