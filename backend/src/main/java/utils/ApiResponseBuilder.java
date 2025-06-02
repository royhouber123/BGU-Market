package utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.function.Supplier;

public class ApiResponseBuilder {

    public static <T> ResponseEntity<ApiResponse<T>> build(Supplier<T> supplier) {
        try {
            T result = supplier.get();
            return ResponseEntity.ok(ApiResponse.ok(result));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Unexpected error: " + e.getMessage()));
        }
    }
} 
