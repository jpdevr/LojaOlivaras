ALTER TABLE wand_product DROP CONSTRAINT wand_product_canon_tier_ck;

ALTER TABLE wand_product ADD CONSTRAINT wand_product_canon_tier_ck CHECK (
    (customer_owner_id IS NULL AND source_reference IS NOT NULL
        AND canon_tier IN ('BOOKS', 'OFFICIAL_SUPPLEMENTAL', 'FILM_PRODUCTION'))
    OR (customer_owner_id IS NOT NULL AND source_reference IS NULL AND canon_tier = 'PERSONAL' AND stock <= 1)
    OR (customer_owner_id IS NULL AND source_reference IS NULL AND canon_tier = 'STORE_ORIGINAL')
);

CREATE INDEX store_order_created_idx ON store_order (created_at DESC, id DESC);
CREATE INDEX store_order_approved_paid_idx ON store_order (paid_at)
    WHERE payment_status = 'SIMULATED_APPROVED';
