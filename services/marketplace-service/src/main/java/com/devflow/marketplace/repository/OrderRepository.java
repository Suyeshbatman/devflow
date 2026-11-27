package com.devflow.marketplace.repository;

import com.devflow.marketplace.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// [INTERFACE] Spring Data implements it. Inherited: save, findById, and more.
public interface OrderRepository extends JpaRepository<Order, UUID>{
    // [METHOD] (declared) One page of a buyer's orders: WHERE buyer_id = ? ORDER BY ... LIMIT/OFFSET
    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);
}
