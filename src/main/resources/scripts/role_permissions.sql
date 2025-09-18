CREATE TABLE public.role_permissions (
    id BIGSERIAL PRIMARY KEY, -- Identificador único para a relação
    role_id INT NOT NULL,     -- Chave estrangeira para a tabela roles
    permission_id BIGINT NOT NULL, -- Chave estrangeira para a tabela permissions
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Data de criação do vínculo
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES public.roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_permission FOREIGN KEY (permission_id) REFERENCES public.permissions (id) ON DELETE CASCADE
);
