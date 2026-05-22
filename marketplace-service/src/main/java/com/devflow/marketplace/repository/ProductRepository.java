package com.devflow.marketplace.repository;

import com.devflow.marketplace.model.Product;
import com.devflow.marketplace.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all active products
    // Spring Data generates:
    // SELECT * FROM products WHERE active = true
    List<Product> findByActiveTrue();

    // Find products by category
    // SELECT * FROM products WHERE category = ? AND active = true
    List<Product> findByCategoryAndActiveTrue(ProductCategory category);

    // Find products owned by a specific user
    List<Product> findByOwnerEmail(String ownerEmail);
}