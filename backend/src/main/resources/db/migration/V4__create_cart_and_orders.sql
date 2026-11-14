CREATE TABLE shopping_cart (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES app_user(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES shopping_cart(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES wand_product(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT cart_item_one_product_per_cart_uq UNIQUE (cart_id, product_id)
);

CREATE INDEX cart_item_cart_idx ON cart_item (cart_id);

CREATE TABLE store_order (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES app_user(id),
    status VARCHAR(30) NOT NULL CHECK (status IN ('CONFIRMED')),
    payment_status VARCHAR(40) NOT NULL CHECK (payment_status IN ('SIMULATED_APPROVED')),
    payment_reference VARCHAR(50) NOT NULL UNIQUE,
    total_galleons NUMERIC(12, 2) NOT NULL CHECK (total_galleons >= 0),
    recipient VARCHAR(120) NOT NULL,
    zip_code VARCHAR(8) NOT NULL,
    street VARCHAR(160) NOT NULL,
    street_number VARCHAR(30) NOT NULL,
    complement VARCHAR(120),
    district VARCHAR(120) NOT NULL,
    city VARCHAR(120) NOT NULL,
    state CHAR(2) NOT NULL,
    delivery_reference VARCHAR(160),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX store_order_customer_created_idx ON store_order (customer_id, created_at DESC);

CREATE TABLE store_order_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES store_order(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES wand_product(id),
    product_name VARCHAR(160) NOT NULL,
    wood VARCHAR(80),
    core VARCHAR(120),
    length_inches NUMERIC(5, 2),
    unit_price_galleons NUMERIC(12, 2) NOT NULL CHECK (unit_price_galleons >= 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    line_total_galleons NUMERIC(12, 2) NOT NULL CHECK (line_total_galleons >= 0)
);

CREATE INDEX store_order_item_order_idx ON store_order_item (order_id);
