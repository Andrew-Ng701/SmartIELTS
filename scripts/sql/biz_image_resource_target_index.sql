CREATE INDEX idx_biz_image_resource_target_active_sort
    ON biz_image_resource (target_type, target_id, is_deleted, sort_order, id);

/*
If a local database was created before biz_image_resource had the current
shared image-resource shape, align it manually before applying the index:

ALTER TABLE biz_image_resource
    ADD COLUMN target_type varchar(64) NOT NULL,
    ADD COLUMN target_id bigint NOT NULL,
    ADD COLUMN bucket_type varchar(64) NOT NULL,
    ADD COLUMN biz_path varchar(128) NOT NULL,
    ADD COLUMN file_url varchar(1024) NULL,
    ADD COLUMN object_key varchar(512) NULL,
    ADD COLUMN original_name varchar(255) NULL,
    ADD COLUMN content_type varchar(128) NULL,
    ADD COLUMN file_size bigint NULL,
    ADD COLUMN width int NULL,
    ADD COLUMN height int NULL,
    ADD COLUMN sort_order int NULL,
    ADD COLUMN created_time datetime NULL,
    ADD COLUMN is_deleted tinyint DEFAULT 0;
*/
