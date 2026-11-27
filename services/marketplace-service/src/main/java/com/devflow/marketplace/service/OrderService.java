package com.devflow.marketplace.service;

import com.devflow.marketplace.dto.CreateOrderRequest;
import com.devflow.marketplace.dto.OrderLineRequest;
import com.devflow.marketplace.dto.OrderResponse;
import com.devflow.marketplace.dto.PageResponse;
import com.devflow.marketplace.entity.Order;
import com.devflow.marketplace.entity.Product;
import com.devflow.marketplace.entity.ProductStatus;
import com.devflow.marketplace.exception.InvalidOrderException;
import com.devflow.marketplace.exception.OrderNotFoundException;
import com.devflow.marketplace.repository.OrderRepository;
import com.devflow.marketplace.repository.ProductRepository;
import com.devflow.marketplace.security.CurrentUser;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository){
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request, CurrentUser user){
        // ---- 1. Reject duplicate products in the request ----
        // [LOCAL VARIABLE] + [CONSTRUCTOR CALL] A Set keeps only UNIQUE values.
        Set<UUID> productIds = new HashSet<>();
        // for-each over the requested lines. [METHOD CALL] request.items() is the record accessor.
        for (OrderLineRequest line : request.items()){
            if(!productIds.add(line.productId())){
                throw new InvalidOrderException("Product " + line.productId() + " appears more than once; use quantity instead");
            }
        }
        // ---- 2. Load ALL the requested products in ONE query (instead of one query per product) ----
        // [LOCAL VARIABLE] products: a Map from productId → Product, for quick lookups by id.
        // [METHOD CALL] findByIdInAndStatus(...) returns a List<Product> (only the ACTIVE ones)
        // .stream().collect(Collectors.toMap(key, value)) turns the List into a Map:
        // Product::getId     → the KEY for each entry
        // Function.identity() → the VALUE is the product itself
        Map<UUID, Product> products = productRepository
                .findByIdInAndStatus(productIds, ProductStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // ---- 3. Build the order, checking each line ----
        // [LOCAL VARIABLE] + [CONSTRUCTOR CALL] A new, empty order for this buyer.
        Order order = new Order(user.id());

        for (OrderLineRequest line : request.items()){
            // [LOCAL VARIABLE] [METHOD CALL] Map.get(key) returns the Product, or null if it isn't in the map
            //   (meaning it doesn't exist, or it's ARCHIVED).
            Product product = products.get(line.productId());

            if (product == null) {
                throw new InvalidOrderException("Product " + line.productId() + " is not available");
            }

            // Business rule: sellers can't buy their own products.
            if(product.isOwnedBy(user.id())){
                throw new InvalidOrderException("You cannot buy your own product" + product.getName());
            }
            // [METHOD CALL] addItem copies the current name/price (the SNAPSHOT) and updates the total.
            //   line.quantity() is an Integer; Java "unboxes" it into an int automatically.
            order.addItem(product, line.quantity());
        }
        // ---- 4. Save ----
        // [METHOD CALL] save(order): CASCADE ALL means the items are saved too.
        //   One INSERT for the order plus one per item, all at commit.
        return OrderResponse.from(orderRepository.save(order));
    }

    // [ANNOTATION] + [METHOD] One order: visible to its buyer or an ADMIN, and to nobody else.
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id, CurrentUser user){
        // [LOCAL VARIABLE] Load it, or throw a 404.
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // Someone else's order → the SAME 404 as a missing one (don't reveal that it exists).
        if(!user.admin() && !order.isPlacedBy(user.id())){
            throw new OrderNotFoundException(id);
        }
        // [STATIC METHOD CALL] Converted INSIDE the transaction, so the lazy items can still load.
        return OrderResponse.from(order);
    }

    // [ANNOTATION] + [METHOD] A page of the current user's own orders.
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listMine(CurrentUser user, Pageable pageable){
        // [METHOD CALL] findByBuyerId(...) → Page<Order>; .map(OrderResponse::from) converts each one
        //   (touching each order's lazy items; batch fetching loads them all in ONE query).
        return PageResponse.from(orderRepository.findByBuyerId(user.id(), pageable).map(OrderResponse::from));
    }
}
