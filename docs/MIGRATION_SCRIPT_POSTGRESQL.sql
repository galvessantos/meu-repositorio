-- ========================================
-- MIGRAÇÃO: Constraint Única para Tokens 2FA
-- Database: PostgreSQL
-- Executar via DBeaver ou psql
-- ========================================

-- 1. VERIFICAR ESTADO ATUAL
-- Verificar se há tokens duplicados antes da migração
SELECT 
    user_id,
    COUNT(*) as total_tokens,
    COUNT(CASE WHEN is_valid = true AND expires_at > NOW() THEN 1 END) as active_tokens
FROM user_token 
GROUP BY user_id
HAVING COUNT(CASE WHEN is_valid = true AND expires_at > NOW() THEN 1 END) > 1
ORDER BY active_tokens DESC;

-- 2. BACKUP (OPCIONAL MAS RECOMENDADO)
-- Criar backup da tabela antes da migração
CREATE TABLE user_token_backup_$(date +%Y%m%d) AS 
SELECT * FROM user_token;

-- 3. LIMPEZA DE TOKENS DUPLICADOS
-- Manter apenas o token mais recente por usuário
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
    WHERE is_valid = true AND expires_at > NOW()
),
tokens_to_invalidate AS (
    SELECT id, user_id, token
    FROM ranked_tokens 
    WHERE rn > 1
)
UPDATE user_token 
SET 
    is_valid = false,
    updated_at = NOW() -- Se você tiver esse campo
WHERE id IN (
    SELECT id FROM tokens_to_invalidate
);

-- 4. VERIFICAR LIMPEZA
-- Confirmar que não há mais tokens duplicados
SELECT 
    user_id,
    COUNT(*) as total_tokens,
    COUNT(CASE WHEN is_valid = true AND expires_at > NOW() THEN 1 END) as active_tokens
FROM user_token 
GROUP BY user_id
HAVING COUNT(CASE WHEN is_valid = true AND expires_at > NOW() THEN 1 END) > 1;

-- Se a query acima retornar 0 linhas, a limpeza foi bem-sucedida!

-- 5. CRIAR ÍNDICE ÚNICO PARCIAL
-- ATENÇÃO: Remover CONCURRENTLY se estiver executando em transação
CREATE UNIQUE INDEX idx_user_token_unique_active 
ON user_token (user_id) 
WHERE is_valid = true AND expires_at > NOW();

-- 6. VERIFICAR ÍNDICE CRIADO
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'user_token' 
  AND indexname = 'idx_user_token_unique_active';

-- 7. TESTAR CONSTRAINT
-- Tentar inserir token duplicado (deve falhar)
/*
INSERT INTO user_token (user_id, token, created_at, expires_at, is_valid)
VALUES (1, 'TEST01', NOW(), NOW() + INTERVAL '5 minutes', true);

INSERT INTO user_token (user_id, token, created_at, expires_at, is_valid)
VALUES (1, 'TEST02', NOW(), NOW() + INTERVAL '5 minutes', true);
-- Esta segunda inserção deve falhar com erro de constraint única
*/

-- 8. ESTATÍSTICAS FINAIS
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
WHERE is_valid = true AND expires_at > NOW()
UNION ALL
SELECT 
    'Usuários com tokens ativos' as metric,
    COUNT(DISTINCT user_id) as value
FROM user_token 
WHERE is_valid = true AND expires_at > NOW();