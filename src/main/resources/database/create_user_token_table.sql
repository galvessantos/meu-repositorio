-- Create user_token table and indexes
CREATE TABLE IF NOT EXISTS user_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_user_token_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_user_token_user ON user_token(user_id);
CREATE INDEX IF NOT EXISTS idx_user_token_valid ON user_token(user_id, is_valid);
