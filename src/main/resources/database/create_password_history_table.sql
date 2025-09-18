-- Create password_history table
CREATE TABLE IF NOT EXISTS password_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_history_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_password_history_user_id ON password_history(user_id);
CREATE INDEX IF NOT EXISTS idx_password_history_user_created ON password_history(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_password_history_created_at ON password_history(created_at);

-- Add comments
COMMENT ON TABLE password_history IS 'Table to store password history for preventing password reuse';
COMMENT ON COLUMN password_history.id IS 'Primary key';
COMMENT ON COLUMN password_history.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN password_history.password_hash IS 'BCrypt hash of the password';
COMMENT ON COLUMN password_history.created_at IS 'Timestamp when password was created';