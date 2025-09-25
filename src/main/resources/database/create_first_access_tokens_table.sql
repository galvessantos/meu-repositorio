-- Create first_access_tokens table
CREATE TABLE IF NOT EXISTS first_access_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    is_used BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_first_access_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_first_access_tokens_token ON first_access_tokens(token);
CREATE INDEX IF NOT EXISTS idx_first_access_tokens_user_id ON first_access_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_first_access_tokens_expires_at ON first_access_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_first_access_tokens_is_used ON first_access_tokens(is_used);

