-- public.refresh_tokens definition

CREATE TABLE public.refresh_tokens (
                                       id BIGSERIAL PRIMARY KEY,
                                       expiry_date TIMESTAMPTZ(6),
                                       "token" VARCHAR(255),
                                       user_id BIGINT,
                                       CONSTRAINT uk_user_id UNIQUE (user_id)
);

-- public.refresh_tokens foreign keys

ALTER TABLE public.refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES public.users(id);


-- public.roles definition

CREATE TABLE public.roles (
                              id SERIAL PRIMARY KEY,
                              "name" VARCHAR(20) NOT NULL,
                              CONSTRAINT roles_name_check CHECK ("name" IN ('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN'))
);


-- public.system_parameter definition

CREATE TABLE public.system_parameter (
                                         id BIGSERIAL PRIMARY KEY,
                                         "system" VARCHAR(255),
                                         "parameter" VARCHAR(255),
                                         value VARCHAR(255)
);


-- public.users definition

CREATE TABLE public.users (
                              id BIGSERIAL PRIMARY KEY,
                              email VARCHAR(50) NOT NULL,
                              fullname VARCHAR(120),
                              is_enabled BOOLEAN NOT NULL,
                              is_reset BOOLEAN NOT NULL,
                              link VARCHAR(1000),
                              "password" VARCHAR(120) NOT NULL,
                              reset_at TIMESTAMP(6),
                              username VARCHAR(20) NOT NULL,
                              CONSTRAINT uk_email UNIQUE (email),
                              CONSTRAINT uk_username UNIQUE (username)
);


-- public.users_roles definition

CREATE TABLE public.users_roles (
                                    user_info_id BIGINT NOT NULL,
                                    roles_id INT NOT NULL,
                                    PRIMARY KEY (user_info_id, roles_id)
);

-- public.users_roles foreign keys

ALTER TABLE public.users_roles
    ADD CONSTRAINT fk_users_roles_role FOREIGN KEY (roles_id) REFERENCES public.roles(id);

ALTER TABLE public.users_roles
    ADD CONSTRAINT fk_users_roles_user FOREIGN KEY (user_info_id) REFERENCES public.users(id);
