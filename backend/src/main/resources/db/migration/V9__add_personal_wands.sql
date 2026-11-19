ALTER TABLE wand_product
    ADD COLUMN customer_owner_id UUID UNIQUE REFERENCES app_user(id),
    ALTER COLUMN source_reference DROP NOT NULL,
    DROP CONSTRAINT wand_product_canon_tier_ck;

ALTER TABLE wand_product
    ADD CONSTRAINT wand_product_canon_tier_ck CHECK (
        (customer_owner_id IS NULL AND source_reference IS NOT NULL
            AND canon_tier IN ('BOOKS', 'OFFICIAL_SUPPLEMENTAL', 'FILM_PRODUCTION'))
        OR (customer_owner_id IS NOT NULL AND source_reference IS NULL AND canon_tier = 'PERSONAL' AND stock <= 1)
    );
