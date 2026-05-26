SET @schema_name = DATABASE();

SET @has_prep_seconds = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'writing_question'
      AND COLUMN_NAME = 'prep_seconds'
);

SET @add_prep_seconds_sql = IF(
    @has_prep_seconds = 0,
    'ALTER TABLE writing_question ADD COLUMN prep_seconds INT DEFAULT 0 AFTER image_detail_description',
    'SELECT ''prep_seconds already exists'' AS message'
);

PREPARE add_prep_seconds_stmt FROM @add_prep_seconds_sql;
EXECUTE add_prep_seconds_stmt;
DEALLOCATE PREPARE add_prep_seconds_stmt;

SET @has_total_seconds = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'writing_question'
      AND COLUMN_NAME = 'total_seconds'
);

SET @add_total_seconds_sql = IF(
    @has_total_seconds = 0,
    'ALTER TABLE writing_question ADD COLUMN total_seconds INT DEFAULT 3600 AFTER prep_seconds',
    'SELECT ''total_seconds already exists'' AS message'
);

PREPARE add_total_seconds_stmt FROM @add_total_seconds_sql;
EXECUTE add_total_seconds_stmt;
DEALLOCATE PREPARE add_total_seconds_stmt;

UPDATE writing_question
SET prep_seconds = 0
WHERE prep_seconds IS NULL;

UPDATE writing_question
SET total_seconds = 3600
WHERE total_seconds IS NULL OR total_seconds <= 0;
