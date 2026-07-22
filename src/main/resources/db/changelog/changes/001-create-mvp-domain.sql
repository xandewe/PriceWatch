--liquibase formatted sql

--changeset xandewe:001-create-mvp-domain
CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    description TEXT,
    desired_price NUMERIC(19, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_products_normalized_name UNIQUE (normalized_name),
    CONSTRAINT ck_products_desired_price_positive CHECK (desired_price IS NULL OR desired_price > 0)
);

CREATE TABLE stores (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    website_url VARCHAR(2048),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_stores_normalized_name UNIQUE (normalized_name)
);

CREATE TABLE product_stores (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    store_id UUID NOT NULL,
    url VARCHAR(2048) NOT NULL,
    normalized_url VARCHAR(2048) NOT NULL,
    external_code VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_product_stores_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_stores_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT uk_product_stores_store_normalized_url UNIQUE (store_id, normalized_url)
);

CREATE TABLE prices (
    id UUID PRIMARY KEY,
    product_store_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    availability VARCHAR(20) NOT NULL,
    note TEXT,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prices_product_store FOREIGN KEY (product_store_id) REFERENCES product_stores (id),
    CONSTRAINT ck_prices_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_prices_availability CHECK (availability IN ('AVAILABLE', 'UNAVAILABLE', 'UNKNOWN'))
);

CREATE INDEX idx_products_active ON products (active);
CREATE INDEX idx_stores_active ON stores (active);
CREATE INDEX idx_product_stores_product_active ON product_stores (product_id, active);
CREATE INDEX idx_product_stores_store_active ON product_stores (store_id, active);
CREATE INDEX idx_prices_product_store_recorded_at ON prices (product_store_id, recorded_at DESC);
CREATE INDEX idx_prices_availability ON prices (availability);
CREATE INDEX idx_prices_recorded_at ON prices (recorded_at);
