CREATE TABLE functionalities (
    id BIGSERIAL PRIMARY KEY,              -- Identificador único da funcionalidade
    name VARCHAR(255) NOT NULL,            -- Nome da funcionalidade
    description TEXT NOT NULL,             -- Descrição da funcionalidade
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Data de criação
);


CREATE TABLE role_functionalities (
    id BIGSERIAL PRIMARY KEY,              -- Identificador único da associação
    role_id INT NOT NULL,                  -- Chave estrangeira para a tabela roles
    functionality_id BIGINT NOT NULL,      -- Chave estrangeira para a tabela functionalities
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Data de criação
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_functionality FOREIGN KEY (functionality_id) REFERENCES functionalities (id) ON DELETE CASCADE
);


-- Inserir funcionalidades na tabela functionalities
INSERT INTO functionalities (name, description)
VALUES 
('viewUserDetails', 'Permite visualizar detalhes de usuários.'),
('updateUserContact', 'Permite atualizar informações de contato dos usuários.'),
('viewAddressDetails', 'Permite visualizar detalhes de endereços associados.'),
('updateAddress', 'Permite atualizar informações de endereço.'),
('viewVehicleDetails', 'Permite visualizar detalhes de veículos.'),
('updateVehicleStatus', 'Permite atualizar o status dos veículos.'),
('generateVehicleReport', 'Permite gerar relatórios sobre veículos.');


-- Obter o ID da role ROLE_ESCOBS na tabela roles
-- Supondo que o ID da role ROLE_ESCOBS seja 4
INSERT INTO role_functionalities (role_id, functionality_id)
VALUES 
(4, 1), -- ROLE_ESCOBS -> viewUserDetails
(4, 2), -- ROLE_ESCOBS -> updateUserContact
(4, 3), -- ROLE_ESCOBS -> viewAddressDetails
(4, 4), -- ROLE_ESCOBS -> updateAddress
(4, 5), -- ROLE_ESCOBS -> viewVehicleDetails
(4, 6), -- ROLE_ESCOBS -> updateVehicleStatus
(4, 7); -- ROLE_ESCOBS -> generateVehicleReport

-- Obter todas as funcionalidades existentes e associar à ROLE_ADMIN (role_id = 1)
DO $$
DECLARE
    functionality RECORD;
BEGIN
    -- Iterar sobre todas as funcionalidades na tabela functionalities
    FOR functionality IN 
        SELECT id FROM functionalities
    LOOP
        -- Inserir cada funcionalidade para ROLE_ADMIN
        INSERT INTO role_functionalities (role_id, functionality_id)
        VALUES (1, functionality.id);
    END LOOP;
END $$;

