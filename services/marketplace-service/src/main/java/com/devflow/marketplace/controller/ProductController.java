package com.devflow.marketplace.controller;

import com.devflow.marketplace.dto.PageResponse;
import com.devflow.marketplace.dto.ProductRequest;
import com.devflow.marketplace.dto.ProductResponse;
import com.devflow.marketplace.entity.ProductCategory;
import com.devflow.marketplace.security.CurrentUser;
import com.devflow.marketplace.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

// A REST controller; every route starts with /api/products.
@RestController
@RequestMapping("/api/products")
public class ProductController {
    // FIELD
    private final ProductService productService;
    // [CONSTRUCTOR] Constructor injection.
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // POST /api/products → 201. Only DEVELOPER or ADMIN (enforced in SecurityConfig).
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        // [LOCAL VARIABLE] [METHOD CALL] [STATIC METHOD CALL] CurrentUser.from(jwt): the token → id + admin flag.
        ProductResponse created = productService.create(request, CurrentUser.from(jwt));
        // [STATIC METHOD CALL] 201 + a Location header for the new product.
        return ResponseEntity.created(URI.create("/api/products/" + created.id())).body(created);
    }

    // GET /api/products?category=&page=&size=&sort= → 200. PUBLIC.
    @GetMapping
    public PageResponse<ProductResponse> list(
            @RequestParam(required = false) ProductCategory category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return productService.list(category, pageable);
    }

    // GET /api/products/{id} → 200 (or 404). PUBLIC.
    @GetMapping("/{id}")
    // [METHOD] [PARAMETER] id: taken from the URL path.
    public ProductResponse getById(@PathVariable UUID id){
        return productService.getById(id);
    }

    // PUT /api/products/{id} → 200. PUT = replace all the editable fields.
    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductResponse request,
            @AuthenticationPrincipal Jwt jwt
    ){
        return productService.update(id, request, CurrentUser.from(jwt));
    }

    // DELETE /api/products/{id} → 204 No Content (success, nothing to send back).
    @DeleteMapping("/{id}")
    // Sets the status code for a method that returns nothing.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // [METHOD] "void": no response body.
    public void archive(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt){
        productService.archive(id, CurrentUser.from(jwt));
    }
}
