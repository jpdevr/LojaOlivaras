ALTER TABLE store_order
    DROP CONSTRAINT IF EXISTS store_order_status_check,
    DROP CONSTRAINT IF EXISTS store_order_payment_status_check;

ALTER TABLE store_order
    ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'LEGACY',
    ADD COLUMN payment_expires_at TIMESTAMPTZ,
    ADD COLUMN paid_at TIMESTAMPTZ,
    ADD COLUMN estimated_delivery_at TIMESTAMPTZ,
    ADD COLUMN delivered_at TIMESTAMPTZ,
    ADD COLUMN payment_instructions TEXT;

UPDATE store_order
SET paid_at = created_at
WHERE payment_status = 'SIMULATED_APPROVED';

ALTER TABLE store_order
    ADD CONSTRAINT store_order_status_check
        CHECK (status IN ('CONFIRMED', 'PROCESSING', 'DELIVERED')),
    ADD CONSTRAINT store_order_payment_status_check
        CHECK (payment_status IN ('PENDING', 'SIMULATED_APPROVED')),
    ADD CONSTRAINT store_order_payment_method_check
        CHECK (payment_method IN ('PIX', 'BOLETO', 'CREDIT_CARD', 'LEGACY'));

CREATE INDEX store_order_payment_due_idx
    ON store_order (payment_expires_at)
    WHERE payment_status = 'PENDING';

CREATE INDEX store_order_delivery_due_idx
    ON store_order (estimated_delivery_at)
    WHERE status = 'PROCESSING';
