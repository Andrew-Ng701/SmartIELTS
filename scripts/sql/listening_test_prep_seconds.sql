ALTER TABLE listening_test
    ADD COLUMN IF NOT EXISTS prep_seconds INT DEFAULT 0 AFTER timer_mode;

UPDATE listening_test
SET prep_seconds = 0
WHERE prep_seconds IS NULL;

UPDATE listening_test
SET total_seconds = 3600
WHERE total_seconds IS NULL OR total_seconds <= 0;
