ALTER TABLE app_user
    ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE app_user
    DROP CONSTRAINT app_user_auth_method_ck,
    ALTER COLUMN phone DROP NOT NULL,
    ALTER COLUMN birth_date DROP NOT NULL;

CREATE TABLE customer_address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES app_user(id),
    label VARCHAR(40),
    recipient VARCHAR(120) NOT NULL,
    zip_code VARCHAR(8) NOT NULL,
    street VARCHAR(160) NOT NULL,
    street_number VARCHAR(30) NOT NULL,
    complement VARCHAR(120),
    district VARCHAR(120) NOT NULL,
    city VARCHAR(120) NOT NULL,
    state CHAR(2) NOT NULL,
    delivery_reference VARCHAR(160),
    default_address BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX customer_address_user_created_idx ON customer_address (user_id, created_at DESC);
CREATE UNIQUE INDEX customer_address_one_default_per_user_uq
    ON customer_address (user_id) WHERE default_address = TRUE;
