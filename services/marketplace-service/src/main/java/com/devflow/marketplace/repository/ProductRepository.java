package com.devflow.marketplace.repository;

import com.devflow.marketplace.entity.Product;
import com.devflow.marketplace.entity.ProductCategory;
import com.devflow.marketplace.entity.ProductStatus;
// Page: an INTERFACE (one page of results plus totals). Pageable: an INTERFACE (which page, size and sort).
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// [INTERFACE] Spring Data generates the implementation at startup.
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // [METHOD] (declared only) A derived query with paging:
    //   SELECT ... WHERE status = ? ORDER BY <from pageable> LIMIT ? OFFSET ?   plus a COUNT(*) query.
    //   Adding a Pageable PARAMETER is all it takes. Spring Data handles LIMIT, OFFSET and ORDER BY.
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    // [METHOD] The same, plus a category filter: WHERE status = ? AND category = ?
    Page<Product> findByStatusAndCategory(ProductStatus status, ProductCategory category, Pageable pageable);
    // [METHOD] One product, only if it has this status: WHERE id = ? AND status = ?
    //   Used to treat ARCHIVED products as "not found".
    Optional<Product> findByIdAndStatus(UUID id, ProductStatus status);
}
