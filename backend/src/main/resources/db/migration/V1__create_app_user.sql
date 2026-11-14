CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_name VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100),
    google_subject VARCHAR(255),
    phone VARCHAR(30) NOT NULL,
    birth_date DATE NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'ADMIN')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT app_user_auth_method_ck CHECK (password_hash IS NOT NULL OR google_subject IS NOT NULL)
);

CREATE UNIQUE INDEX app_user_email_lower_uq ON app_user (lower(email));
CREATE UNIQUE INDEX app_user_google_subject_uq ON app_user (google_subject) WHERE google_subject IS NOT NULL;
