-- Migração para adicionar constraint única para tokens válidos por usuário
-- Isso previne múltiplos tokens válidos para o mesmo usuário

-- Primeiro, limpar tokens duplicados existentes (manter apenas o mais recente por usuário)
WITH ranked_tokens AS (
    SELECT id, user_id, is_valid, expires_at,
           ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY created_at DESC) as rn
    FROM user_token
    WHERE is_valid = true AND expires_at > CURRENT_TIMESTAMP
)
UPDATE user_token
SET is_valid = false
WHERE id IN (
    SELECT id FROM ranked_tokens WHERE rn > 1
);

-- Criar índice único parcial para garantir apenas um token válido por usuário
-- A lógica de expiração é tratada na aplicação
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_user_token_unique_active
ON user_token (user_id)
WHERE is_valid = true;

-- Para H2 (usado nos testes), a sintaxe é diferente
-- O H2 não suporta índices parciais, então vamos usar uma abordagem alternativa nos testes