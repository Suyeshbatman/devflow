package com.devflow.marketplace.graphql;

import com.devflow.marketplace.dto.ProductDto;
import com.devflow.marketplace.dto.ProductRequest;
import com.devflow.marketplace.model.ProductCategory;
import com.devflow.marketplace.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;

// Note: @Controller not @RestController for GraphQL
// Spring GraphQL uses @Controller to detect resolvers
@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductResolver {

    private final ProductService productService;

    // @QueryMapping maps to "products" in schema.graphqls
    // Client sends: query { products { id name price } }
    @QueryMapping
    public List<ProductDto> products() {
        return productService.getAllProducts();
    }

    // @Argument maps GraphQL argument to Java parameter
    // Client sends: query { product(id: "1") { name } }
    @QueryMapping
    public ProductDto product(@Argument Long id) {
        return productService.getProductById(id);
    }

    @QueryMapping
    public List<ProductDto> productsByCategory(
            @Argument String category) {
        return productService.getProductsByCategory(
                ProductCategory.valueOf(category));
    }

    // @MutationMapping maps to "createProduct" in schema
    // Client sends: mutation { createProduct(name: "Tool", ...) }
    @MutationMapping
    public ProductDto createProduct(
            @Argument String name,
            @Argument String description,
            @Argument Double price,
            @Argument String category) {

        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setPrice(BigDecimal.valueOf(price));
        request.setCategory(ProductCategory.valueOf(category));

        // GraphQL mutations don't have access to HTTP headers
        // In production we'd use GraphQL context to pass user info
        // For now we use a placeholder
        return productService.createProduct(request, "graphql-user");
    }
}