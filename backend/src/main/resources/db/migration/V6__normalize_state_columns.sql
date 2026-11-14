ALTER TABLE customer_address
    ALTER COLUMN state TYPE VARCHAR(2) USING btrim(state);

ALTER TABLE store_order
    ALTER COLUMN state TYPE VARCHAR(2) USING btrim(state);
