-- V1: the product catalog.
-- Must match Product.java exactly (Hibernate runs with ddl-auto: validate).

CREATE TABLE products (
    id UUID NOT NULL,
    seller_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(30) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT ck_products_price CHECK ( price >= 0 ),
    CONSTRAINT ck_products_category CHECK ( category IN ('CLI_TOOL', 'IDE_EXTENSION', 'LIBRARY', 'TEMPLATE', 'API', 'OTHER') ),
    CONSTRAINT ck_products_status CHECK ( status IN ('ACTIVE', 'ARCHIVED') )
);
-- [INDEX] Speeds up the default catalog query: "WHERE status = 'ACTIVE' ORDER BY created_at DESC".
-- A two-column index: Postgres jumps to the ACTIVE rows, which are already stored newest-first.
CREATE INDEX idx_products_status_created_at ON products (status, created_at DESC);
-- [INDEX] Speeds up "all products by this seller" (used for seller dashboards later).
CREATE INDEX idx_products_seller_id ON products (seller_id);

