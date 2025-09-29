-- Add column to track if user has ever logged in
ALTER TABLE users ADD COLUMN is_first_login_completed BOOLEAN DEFAULT FALSE;

-- Update existing users: if they have passwordChangedByUser = true, they have completed first login
UPDATE users 
SET is_first_login_completed = TRUE 
WHERE is_password_changed_by_user = TRUE;

-- Create index for performance
CREATE INDEX IF NOT EXISTS idx_users_first_login ON users(is_first_login_completed);