CREATE TABLE wishlist_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id),
    product_id UUID NOT NULL REFERENCES wand_product(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT wishlist_item_user_product_uq UNIQUE (user_id, product_id)
);

CREATE INDEX wishlist_item_user_created_idx ON wishlist_item (user_id, created_at DESC);
