SET @schema_name = DATABASE();

SET @has_chart_type = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'writing_question'
      AND COLUMN_NAME = 'chart_type'
);

SET @add_chart_type_sql = IF(
    @has_chart_type = 0,
    'ALTER TABLE writing_question ADD COLUMN chart_type VARCHAR(50) DEFAULT NULL AFTER task_type',
    'SELECT ''chart_type already exists'' AS message'
);

PREPARE add_chart_type_stmt FROM @add_chart_type_sql;
EXECUTE add_chart_type_stmt;
DEALLOCATE PREPARE add_chart_type_stmt;
