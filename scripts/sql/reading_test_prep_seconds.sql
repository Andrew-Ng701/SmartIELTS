ALTER TABLE reading_test
    ADD COLUMN IF NOT EXISTS prep_seconds INT DEFAULT 0 AFTER timer_mode;

UPDATE reading_test
SET prep_seconds = 0
WHERE prep_seconds IS NULL;

UPDATE reading_test
SET total_seconds = 3600
WHERE total_seconds IS NULL OR total_seconds <= 0;
