CREATE TABLE guest_cart_transfer (
    cart_id UUID NOT NULL REFERENCES shopping_cart(id) ON DELETE CASCADE,
    transfer_id UUID NOT NULL,
    skipped_units INTEGER NOT NULL CHECK (skipped_units >= 0),
    PRIMARY KEY (cart_id, transfer_id)
);
