CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,           -- Identificador único da permissão
    action VARCHAR(50) NOT NULL,        -- Ação permitida (ex: read, write, delete, etc.)
    subject VARCHAR(100) NOT NULL,      -- Entidade ou recurso (ex: User, Vehicle, etc.)
    fields VARCHAR(255),                -- Campos específicos permitidos (opcional, ex: email, name)
    description VARCHAR(255),           -- Descrição da permissão
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Data de criação
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- Última atualização
);

-- Trigger para atualizar automaticamente o campo updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at
BEFORE UPDATE ON permissions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
