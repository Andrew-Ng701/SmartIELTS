ALTER TABLE sys_user
    ADD COLUMN IF NOT EXISTS consecutive_login_days INT NOT NULL DEFAULT 0 AFTER last_login_time;
