-- Funções personalizadas para compatibilidade H2 com PostgreSQL
-- Apenas criamos ALIAS para funções que NÃO existem nativamente no H2

-- Função ENCODE para simular encode() do PostgreSQL
CREATE ALIAS IF NOT EXISTS ENCODE FOR "com.montreal.oauth.config.H2Functions.encode";

-- Função CRIPTOGRAFAR personalizada
CREATE ALIAS IF NOT EXISTS CRIPTOGRAFAR FOR "com.montreal.oauth.config.H2Functions.criptografar";

-- Função DESCRIPTOGRAFAR se necessária
CREATE ALIAS IF NOT EXISTS DESCRIPTOGRAFAR FOR "com.montreal.oauth.config.H2Functions.descriptografar";

-- Função para gerar UUID se necessária (apenas se não usar a nativa)
CREATE ALIAS IF NOT EXISTS UUID_GENERATE_V4 FOR "com.montreal.oauth.config.H2Functions.generateUUID";

-- Removemos LOWER, UPPER e NOW pois já existem nativamente no H2