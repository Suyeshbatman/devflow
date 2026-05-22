package com.devflow.marketplace.service;

import com.devflow.common.enums.ErrorCode;
import com.devflow.common.exception.BaseException;
import com.devflow.marketplace.dto.ProductDto;
import com.devflow.marketplace.dto.ProductRequest;
import com.devflow.marketplace.model.Product;
import com.devflow.marketplace.model.ProductCategory;
import com.devflow.marketplace.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDto createProduct(ProductRequest request,
                                    String ownerEmail) {
        log.info("Creating product: {} by {}", request.getName(), ownerEmail);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .ownerEmail(ownerEmail)
                .active(true)
                .purchaseCount(0)
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getId());
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::mapToDto)
                .toList();
        // .toList() is Java 16+ — creates immutable list
        // replaces .collect(Collectors.toList())
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(
            ProductCategory category) {
        return productRepository
                .findByCategoryAndActiveTrue(category)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional
    public ProductDto updateProduct(Long id,
                                    ProductRequest request,
                                    String ownerEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.PRODUCT_NOT_FOUND));

        // Only the owner can update their product
        if (!product.getOwnerEmail().equals(ownerEmail)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());

        return mapToDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id, String ownerEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getOwnerEmail().equals(ownerEmail)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        // Soft delete — set active=false instead of deleting
        // This preserves order history that references this product
        product.setActive(false);
        productRepository.save(product);
        log.info("Product {} soft-deleted by {}", id, ownerEmail);
    }

    // Converts Product entity to ProductDto
    public ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .ownerEmail(product.getOwnerEmail())
                .active(product.isActive())
                .purchaseCount(product.getPurchaseCount())
                .createdAt(product.getCreatedAt())
                .build();
    }
}