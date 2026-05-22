package com.devflow.marketplace.controller;

import com.devflow.common.dto.ApiResponse;
import com.devflow.marketplace.dto.ProductDto;
import com.devflow.marketplace.dto.ProductRequest;
import com.devflow.marketplace.model.ProductCategory;
import com.devflow.marketplace.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    // @RequestHeader reads a specific HTTP header
    // Gateway adds X-User-Email to every authenticated request
    // So we don't need JWT here — gateway already validated it
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        ProductDto product = productService.createProduct(
                request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", product));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts(
            @RequestParam(required = false) ProductCategory category) {

        List<ProductDto> products = category != null
                ? productService.getProductsByCategory(category)
                : productService.getAllProducts();

        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Product retrieved", productService.getProductById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        return ResponseEntity.ok(ApiResponse.success("Product updated",
                productService.updateProduct(id, request, userEmail)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Email") String userEmail) {

        productService.deleteProduct(id, userEmail);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted"));
    }
}