package com.devflow.marketplace.repository;

import com.devflow.marketplace.model.Order;
import com.devflow.marketplace.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // All orders placed by a specific buyer
    List<Order> findByBuyerEmail(String buyerEmail);

    // All orders for a specific product
    List<Order> findByProductId(Long productId);

    // Orders filtered by status
    List<Order> findByBuyerEmailAndStatus(String buyerEmail,
                                          OrderStatus status);
}