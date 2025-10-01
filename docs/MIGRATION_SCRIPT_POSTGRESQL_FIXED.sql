-- ========================================
-- MIGRAÇÃO: Constraint Única para Tokens 2FA - CORRIGIDO
-- Database: PostgreSQL
-- Erro corrigido: NOW() não é IMMUTABLE
-- ========================================

-- 1. VERIFICAR ESTADO ATUAL
SELECT 
    user_id,
    COUNT(*) as total_tokens,
    COUNT(CASE WHEN is_valid = true AND expires_at > CURRENT_TIMESTAMP THEN 1 END) as active_tokens
FROM user_token 
GROUP BY user_id
HAVING COUNT(CASE WHEN is_valid = true AND expires_at > CURRENT_TIMESTAMP THEN 1 END) > 1
ORDER BY active_tokens DESC;

-- 2. LIMPEZA DE TOKENS DUPLICADOS
WITH ranked_tokens AS (
    SELECT 
        id, 
        user_id, 
        token,
        created_at,
        is_valid, 
        expires_at,
        ROW_NUMBER() OVER (
            PARTITION BY user_id 
            ORDER BY created_at DESC, id DESC
        ) as rn
    FROM user_token 
    WHERE is_valid = true AND expires_at > CURRENT_TIMESTAMP
)
UPDATE user_token 
SET is_valid = false
WHERE id IN (
    SELECT id FROM ranked_tokens WHERE rn > 1
);

-- 3. VERIFICAR LIMPEZA
SELECT 
    user_id,
    COUNT(*) as active_tokens
FROM user_token 
WHERE is_valid = true AND expires_at > CURRENT_TIMESTAMP
GROUP BY user_id
HAVING COUNT(*) > 1;

-- 4. OPÇÃO 1: ÍNDICE ÚNICO SIMPLES (SEM PREDICATE)
-- Esta é a abordagem mais simples e confiável
CREATE UNIQUE INDEX idx_user_token_unique_active_simple
ON user_token (user_id, is_valid) 
WHERE is_valid = true;

-- 5. OPÇÃO 2: USAR FUNÇÃO IMMUTABLE PERSONALIZADA
-- Criar função immutable para comparação de data
CREATE OR REPLACE FUNCTION is_token_active(expires_at TIMESTAMP)
RETURNS BOOLEAN
LANGUAGE SQL
IMMUTABLE
AS $$
    SELECT expires_at > '1970-01-01'::timestamp;
$$;

-- Depois criar índice com função immutable
-- (Não recomendado para este caso, apenas exemplo)

-- 6. OPÇÃO 3: ÍNDICE PARCIAL COM CONSTRAINT DE APLICAÇÃO
-- Criar índice único apenas para tokens válidos
-- A lógica de expiração fica na aplicação
CREATE UNIQUE INDEX idx_user_token_unique_valid
ON user_token (user_id) 
WHERE is_valid = true;

-- 7. VERIFICAR ÍNDICE CRIADO
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'user_token' 
  AND indexname LIKE 'idx_user_token_unique%';

-- 8. TESTAR CONSTRAINT
-- Tentar inserir token duplicado (deve falhar)
/*
-- Este deve funcionar (primeiro token)
INSERT INTO user_token (user_id, token, created_at, expires_at, is_valid)
VALUES (999, 'TEST01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '5 minutes', true);

-- Este deve falhar (segundo token válido para mesmo usuário)
INSERT INTO user_token (user_id, token, created_at, expires_at, is_valid)
VALUES (999, 'TEST02', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '5 minutes', true);
*/

-- 9. ESTATÍSTICAS FINAIS
SELECT 
    'Total de tokens' as metric,
    COUNT(*) as value
FROM user_token
UNION ALL
SELECT 
    'Tokens válidos' as metric,
    COUNT(*) as value
FROM user_token 
WHERE is_valid = true
UNION ALL
SELECT 
    'Tokens ativos (válidos + não expirados)' as metric,
    COUNT(*) as value
FROM user_token 
WHERE is_valid = true AND expires_at > CURRENT_TIMESTAMP
UNION ALL
SELECT 
    'Usuários com tokens ativos' as metric,
    COUNT(DISTINCT user_id) as value
FROM user_token 
WHERE is_valid = true AND expires_at > CURRENT_TIMESTAMP;