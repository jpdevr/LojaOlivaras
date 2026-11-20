-- Keep user records and revoke sessions that depended on external login.
UPDATE app_user
SET token_version = token_version + 1
WHERE active AND google_subject IS NOT NULL AND password_hash IS NULL;

ALTER TABLE app_user DROP COLUMN google_subject;
