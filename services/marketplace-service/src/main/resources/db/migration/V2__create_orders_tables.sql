CREATE TABLE orders (
    id UUID NOT NULL,
    buyer_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT ck_order_status CHECK ( status IN ('PLACED', 'CANCELLED') ),
    CONSTRAINT ck_orders_total CHECK ( total_amount >= 0 )
);
-- For "my orders, newest first": WHERE buyer_id = ? ORDER BY created_at DESC
CREATE INDEX idx_orders_buyer_id_created_at ON orders (buyer_id, created_at DESC);

CREATE TABLE order_items(
    id UUID NOT NULL,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    seller_id UUID NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,

    CONSTRAINT pk_order_items PRIMARY KEY (id),

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),

    CONSTRAINT ck_order_items_quantity CHECK ( quantity BETWEEN 1 AND 100),
    CONSTRAINT ck_order_items_unit_price CHECK ( unit_price >= 0 ),

    CONSTRAINT uk_order_items_order_product UNIQUE (order_id, product_id)
);