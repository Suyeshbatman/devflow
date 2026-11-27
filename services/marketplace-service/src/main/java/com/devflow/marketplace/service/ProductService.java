package com.devflow.marketplace.service;

import com.devflow.marketplace.dto.PageResponse;
import com.devflow.marketplace.dto.ProductRequest;
import com.devflow.marketplace.dto.ProductResponse;
import com.devflow.marketplace.entity.Product;
import com.devflow.marketplace.entity.ProductCategory;
import com.devflow.marketplace.entity.ProductStatus;
import com.devflow.marketplace.exception.ForbiddenOperationException;
import com.devflow.marketplace.exception.ProductNotFoundException;
import com.devflow.marketplace.repository.ProductRepository;
import com.devflow.marketplace.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class ProductService {
    // [FIELD] The injected repository.
    private final ProductRepository productRepository;
    // [CONSTRUCTOR] Constructor injection.
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // [ANNOTATION] + [METHOD] Create a product owned by the current user.
    @Transactional
    public ProductResponse create(ProductRequest request, CurrentUser user){
        Product product = new Product(
                user.id(),
                request.name(),
                request.description(),
                request.category(),
                normalizePrice(request.price())
        );
        // [METHOD CALL] save → INSERT on commit; the returned entity has its id and timestamps.
        return ProductResponse.from(productRepository.save(product));
    }

    // [ANNOTATION] + [METHOD] Public: get one ACTIVE product.
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id){
        return ProductResponse.from(findActiveOrThrow(id));
    }

    // [ANNOTATION] + [METHOD] Public: one page of ACTIVE products, optionally filtered by category.
    //   [PARAMETER] category: may be null (meaning "all categories").
    //   [PARAMETER] pageable: built by Spring from ?page=&size=&sort=
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> list(ProductCategory category, Pageable pageable){
        // [LOCAL VARIABLE] The TERNARY operator, "condition ? valueIfTrue : valueIfFalse",
        //   is a one-line if/else that produces a value.
        Page<Product> page = (category == null)
                ? productRepository.findByStatus(ProductStatus.ACTIVE, pageable)
                : productRepository.findByStatusAndCategory(ProductStatus.ACTIVE, category, pageable);
        // [METHOD CALL] page.map(...) converts every Product on the page into a ProductResponse
        //   (keeping the totals). [STATIC METHOD CALL] PageResponse.from(...) wraps it in our stable JSON shape.
        return PageResponse.from(page.map(ProductResponse::from));
    }

    // [ANNOTATION] + [METHOD] Update: only the owner or an ADMIN.
    @Transactional
    public ProductResponse update(UUID id, ProductResponse request, CurrentUser user){
        // [LOCAL VARIABLE] Load the product (404 if it's missing or archived).
        Product product = findActiveOrThrow(id);
        // [METHOD CALL] Ownership check (403 if it fails).
        checkCanModify(product, user);
        // [METHOD CALL] Our domain method on the entity OBJECT.
        product.update(request.name(), request.description(), request.category(), normalizePrice(request.price()));
        // [METHOD CALL] saveAndFlush: send the UPDATE to the database NOW (not at commit),
        //   so @PreUpdate has already set updatedAt when we build the response.
        //   With dirty checking alone, the response would show the OLD updatedAt.
        return ProductResponse.from(productRepository.saveAndFlush(product));
    }

    // [ANNOTATION] + [METHOD] Archive (soft delete): only the owner or an ADMIN. Returns nothing (void).
    @Transactional
    public void archive(UUID id, CurrentUser user){
        Product product = findActiveOrThrow(id);
        checkCanModify(product, user);
        // [METHOD CALL] Change the status. Dirty checking writes the UPDATE at commit (no save needed).
        product.archive();
    }

    // ---------- private helpers ----------

    // [METHOD] Load an ACTIVE product, or throw a 404. Archived products count as "not found".
    private Product findActiveOrThrow(UUID id){
        return productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                // [METHOD CALL] + [LAMBDA] () -> new ...: build the exception only if the Optional is empty.
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
    // [METHOD] The OWNERSHIP rule, in exactly one place.
    private void checkCanModify(Product product, CurrentUser user){
        // Allowed when the user is an admin OR owns the product.
        if (!user.admin() && !product.isOwnedBy(user.id())){
            throw new ForbiddenOperationException("You can only modify your own products");
        }
    }

    // [STATIC METHOD] Always store prices with exactly 2 decimals: 19.9 → 19.90.
    //   RoundingMode.UNNECESSARY ([ENUM CONSTANT]) means "never round". Validation already guarantees
    //   at most 2 decimals, so this only ADDS zeros; it can never silently change an amount.
    private static BigDecimal normalizePrice(BigDecimal price){
        return price.setScale(2, RoundingMode.UNNECESSARY);
    }

}
