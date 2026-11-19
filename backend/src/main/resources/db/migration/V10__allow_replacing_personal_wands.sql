ALTER TABLE wand_product DROP CONSTRAINT wand_product_customer_owner_id_key;

CREATE UNIQUE INDEX wand_product_active_personal_owner_idx
    ON wand_product (customer_owner_id)
    WHERE active AND customer_owner_id IS NOT NULL;
