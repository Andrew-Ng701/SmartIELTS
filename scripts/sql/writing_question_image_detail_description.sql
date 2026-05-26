SET @schema_name = DATABASE();

SET @has_image_detail_description = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'writing_question'
      AND COLUMN_NAME = 'image_detail_description'
);

SET @add_image_detail_description_sql = IF(
    @has_image_detail_description = 0,
    'ALTER TABLE writing_question ADD COLUMN image_detail_description TEXT DEFAULT NULL AFTER description',
    'SELECT ''image_detail_description already exists'' AS message'
);

PREPARE add_image_detail_description_stmt FROM @add_image_detail_description_sql;
EXECUTE add_image_detail_description_stmt;
DEALLOCATE PREPARE add_image_detail_description_stmt;

SET @has_image_url = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'writing_question'
      AND COLUMN_NAME = 'image_url'
);

SET @drop_image_url_sql = IF(
    @has_image_url > 0,
    'ALTER TABLE writing_question DROP COLUMN image_url',
    'SELECT ''image_url already removed'' AS message'
);

PREPARE drop_image_url_stmt FROM @drop_image_url_sql;
EXECUTE drop_image_url_stmt;
DEALLOCATE PREPARE drop_image_url_stmt;

SET @has_image_object_key = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'writing_question'
      AND COLUMN_NAME = 'image_object_key'
);

SET @drop_image_object_key_sql = IF(
    @has_image_object_key > 0,
    'ALTER TABLE writing_question DROP COLUMN image_object_key',
    'SELECT ''image_object_key already removed'' AS message'
);

PREPARE drop_image_object_key_stmt FROM @drop_image_object_key_sql;
EXECUTE drop_image_object_key_stmt;
DEALLOCATE PREPARE drop_image_object_key_stmt;
