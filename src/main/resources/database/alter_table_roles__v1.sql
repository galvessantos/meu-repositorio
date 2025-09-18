ALTER TABLE public.roles
    ADD COLUMN is_requires_token_first_login BOOLEAN DEFAULT false,
    ADD COLUMN is_biometric_validation BOOLEAN DEFAULT false,
    ADD COLUMN is_token_login BOOLEAN DEFAULT false;
