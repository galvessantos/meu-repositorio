ALTER TABLE public.users
    ADD COLUMN token_temporary VARCHAR(255),
ADD COLUMN token_expired_at TIMESTAMP(6);