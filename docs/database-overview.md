# SmartIELTS Database Overview

Last Updated: 2026-05-13

Source Verified: MyBatis XML under `src/main/resources/mapper`, POJO classes under `src/main/java/com/andrew/smartielts/**/domain/pojo`, constants under `common/constants` and `dashboard/constants`, SQL snippets under `scripts/sql`.

## 使用方式

這份文件是目前 code 依賴的資料庫結構總覽，不是完整 DDL migration。新增欄位或資料表時，應同時更新：

- MyBatis mapper XML
- 對應 POJO/DTO/VO
- `common/constants/DbTableNames.java`
- `dashboard/constants/DashboardTableNameConstants.java` 和 dashboard schema guard，如果該表允許 AI dashboard 查詢
- `scripts/sql/*.sql` migration snippet
- 本文件

## 命名與共通欄位

- DB table/column 使用 `snake_case`。
- Java property 使用 `lowerCamelCase`，MyBatis result map 或 `map-underscore-to-camel-case` 負責轉換。
- 共用資料表名稱常量使用 `UPPER_SNAKE_CASE` 欄位名，例如 `READING_TEST = "reading_test"`。
- `id` 通常是 auto-increment primary key。
- `is_deleted` 使用 `0/1` 表示 soft delete；有 `deleted_time` 的表需要 restore 時清回 `NULL`。
- `created_time`、`updated_time` 由 service 或 SQL 寫入，依 module 既有 pattern 處理。
- record 類表的 `user_id` 是 ownership boundary，user endpoint 必須以目前 JWT user id 查詢或更新。

## Table Groups

目前 code 使用的核心資料表：

- Common/auth: `sys_user`, `biz_image_resource`
- Reading: `reading_test`, `reading_part_group`, `reading_passage`, `reading_question`, `reading_question_answer_rule`, `reading_record`, `reading_answer_record`
- Listening: `listening_test`, `listening_audio`, `listening_part_group`, `listening_question`, `listening_record`, `listening_answer_record`
- Writing: `writing_question`, `writing_record`, `writing_record_attachment`
- Speaking: `speaking_question`, `speaking_record`, `speaking_session`, `speaking_talk`

Dashboard AI SQL table allow-list currently mirrors these names except `speaking_talk`, which is used by D-ID status flow but is not part of the main learning analytics allow-list.

## Common And Auth

### `sys_user`

Purpose: user identity, auth state, role, profile and IELTS target settings.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | user id; JWT `userId` comes from this |
| `email` | login identifier; login normalizes to `trim().toLowerCase()` |
| `username` | profile display name |
| `password` | BCrypt hash |
| `role` | `USER` or `ADMIN`; Spring Security adds `ROLE_` prefix |
| `is_deleted` | soft-delete flag; deleted users cannot login |
| `deleted_time` | admin user recycle-bin timestamp |
| `created_time` | registration timestamp |
| `last_login_time` | updated on login |
| `consecutive_login_days` | backend-maintained current login streak; same-day login does not increment |
| `token_version` | JWT invalidation version; logout/password change increments it |
| `profile_picture_url` | public profile picture URL |
| `profile_picture_object_key` | OSS object key for profile picture |
| `ielts_target_scores` | compact profile target score storage used by user profile service |

Important behavior:

- Auth mapper does not load profile fields; user mapper does.
- Login updates `last_login_time` and `consecutive_login_days` together.
- Existing token becomes invalid when `token_version` no longer matches JWT claim.
- Admin user delete/restore is soft delete only.

### `biz_image_resource`

Purpose: shared image resource table for reading/listening part groups, reading/listening questions, writing question image migration, and future image targets.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | image resource id |
| `target_type` | stable target enum-like value, e.g. `READING_PART_GROUP`, `LISTENING_QUESTION`, `WRITING_QUESTION` |
| `target_id` | target entity id |
| `bucket_type` | storage bucket type/key |
| `biz_path` | business path under OSS |
| `file_url` | public or signed URL returned to frontend |
| `object_key` | OSS object key |
| `original_name` | uploaded filename |
| `content_type` | MIME type |
| `file_size` | file size in bytes |
| `width`, `height` | image dimensions when available |
| `sort_order` | display order for multiple images |
| `created_time` | upload timestamp |
| `is_deleted` | soft-delete flag, though current `deleteByTarget` physically deletes target images |

Recommended index:

```sql
CREATE INDEX idx_biz_image_resource_target_active_sort
    ON biz_image_resource (target_type, target_id, is_deleted, sort_order, id);
```

Migration source: `scripts/sql/biz_image_resource_target_index.sql`.

## Reading

### `reading_test`

Purpose: reading test paper header and timer policy.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | test id |
| `title` | display title |
| `total_score` | total possible score |
| `created_time`, `updated_time` | lifecycle timestamps |
| `is_deleted` | admin soft-delete flag |
| `timer_mode` | currently `test_level` |
| `prep_seconds` | preparation time stored in seconds |
| `total_seconds` | formal test time stored in seconds |
| `auto_submit` | whether frontend/backend should treat timeout as auto submit |
| `allow_pause` | whether pause/resume is allowed |

API note: admin write contract uses `prepSeconds` for preparation time and `totalMinutes` for formal exam time; service stores seconds. Migration source: `scripts/sql/reading_test_prep_seconds.sql`.

### `reading_part_group`

Purpose: group-level structure and shared answer rule metadata for reading questions.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | part group id |
| `test_id` | parent `reading_test.id` |
| `part_number`, `group_number` | IELTS part/group positioning |
| `title` | group title |
| `instruction_text` | instructions shown with group |
| `group_guide_text` | guide/help text |
| `group_requirement_text` | requirement prompt |
| `question_type` | group question type |
| `answer_mode` | `TEXT`, `SINGLE`, `MULTI` |
| `options_json` | group-level options JSON |
| `accepted_answers_json` | group-level accepted answers JSON |
| `answer_rules_json` | group-level answer rules JSON |
| `case_insensitive`, `ignore_whitespace`, `ignore_punctuation` | answer normalization switches |
| `question_no_start`, `question_no_end` | displayed question number range |
| `display_order` | ordering |
| `time_limit_seconds` | optional group limit |
| `is_deleted` | soft-delete flag |

Reading matching-style groups should store `question_type = 'MATCHING'`. Legacy values `HEADING_MATCHING` and `MATCHING_HEADINGS` can be normalized with `scripts/sql/reading_matching_question_type_merge.sql`.

Images are stored in `biz_image_resource` with target type `READING_PART_GROUP`.

### `reading_passage`

Purpose: reading passage content, optionally linked to a part group.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | passage id |
| `test_id` | parent `reading_test.id` |
| `part_group_id` | optional parent `reading_part_group.id` |
| `passage_no` | passage number |
| `title` | passage title |
| `content` | passage text/content |
| `material_type` | content material type |
| `display_order` | ordering |
| `is_deleted` | soft-delete flag |

### `reading_question`

Purpose: individual reading question.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | question id |
| `passage_id` | parent `reading_passage.id` |
| `part_group_id` | optional parent `reading_part_group.id` |
| `question_number` | displayed number |
| `question_type` | e.g. `MULTIPLE_CHOICE_SINGLE`, `MATCHING`, `SUMMARY_COMPLETION`, `TABLE_COMPLETION`, `NOTE_COMPLETION` |
| `answer_mode` | `TEXT`, `SINGLE`, `MULTI` |
| `question_text` | prompt text |
| `correct_answer` | legacy/simple answer |
| `options_json` | options JSON |
| `accepted_answers_json` | accepted answers JSON |
| `group_label` | display label |
| `case_insensitive`, `ignore_whitespace`, `ignore_punctuation` | answer normalization switches |
| `display_order` | ordering |
| `score` | score per question |
| `is_deleted` | soft-delete flag |

Reading matching-style questions should store `question_type = 'MATCHING'`, including heading matching. Their answer bank is stored in `options_json` as ordered `{ "label": "A", "text": "..." }` objects; admin writes normalize labels from item order and store each prompt's `correct_answer` as the selected label.

Images are stored in `biz_image_resource` with target type `READING_QUESTION`.

### `reading_question_answer_rule`

Purpose: per-question accepted answer variants.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | rule id |
| `question_id` | parent `reading_question.id` |
| `blank_no` | blank number for multi-blank questions |
| `answer_group_no` | accepted answer group number |
| `answer_text` | raw accepted answer |
| `normalized_answer` | normalized answer text |
| `is_primary` | primary answer marker |
| `display_order` | ordering |

### `reading_record`

Purpose: reading user attempt/session record.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | record id |
| `user_id` | owner `sys_user.id` |
| `test_id` | attempted `reading_test.id` |
| `session_id` | frontend/backend session identifier |
| `total_score` | achieved score |
| `started_time`, `submitted_time` | attempt lifecycle |
| `time_limit_seconds`, `time_spent_seconds` | timer snapshot |
| `record_status` | `in_progress`, `paused`, `submitted`, `auto_submitted` |
| `created_time` | record create timestamp |
| `is_deleted` | user/admin soft-delete flag |

### `reading_answer_record`

Purpose: stored answer judgment rows for a reading record.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | answer row id |
| `record_id` | parent `reading_record.id` |
| `question_id` | answered `reading_question.id` |
| `part_group_id` | group id used during judgment |
| `user_answer` | display answer |
| `normalized_answer` | normalized answer |
| `raw_answers_json` | raw multi-answer payload |
| `is_correct` | correctness flag |
| `score` | awarded score |

## Listening

### `listening_test`

Purpose: listening test paper header, timer policy, and audio seek policy.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | test id |
| `title` | display title |
| `total_score` | total possible score |
| `created_time`, `updated_time` | lifecycle timestamps |
| `is_deleted` | admin soft-delete flag |
| `timer_mode` | currently `test_level` |
| `prep_seconds` | preparation time stored in seconds |
| `total_seconds` | formal test time stored in seconds |
| `auto_submit` | timeout behavior |
| `allow_pause` | pause/resume behavior |
| `allow_audio_seek` | whether frontend may seek audio |

Migration sources:

- `scripts/sql/listening_test_prep_seconds.sql`
- `scripts/sql/listening_test_allow_audio_seek.sql`

### `listening_audio`

Purpose: test-level and part-group audio metadata.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | audio id |
| `test_id` | parent `listening_test.id` |
| `part_group_id` | parent `listening_part_group.id` when `audio_scope = part_group` |
| `audio_scope` | `test` or `part_group` |
| `title` | display title |
| `audio_url` | OSS URL |
| `audio_object_key` | OSS object key |
| `transcript_text` | manual or ASR transcript text if available |
| `is_deleted` | soft-delete flag; current mapper delete methods physically delete rows |
| `created_time`, `updated_time` | lifecycle timestamps |

### `listening_part_group`

Same shape as `reading_part_group`, but belongs to `listening_test`. Images use `biz_image_resource` target type `LISTENING_PART_GROUP`.

### `listening_question`

Purpose: individual listening question.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | question id |
| `test_id` | parent `listening_test.id` |
| `part_group_id` | optional parent `listening_part_group.id` |
| `section_number` | listening section number |
| `question_number` | displayed number |
| `question_type` | e.g. `FORM_COMPLETION`, `NOTE_COMPLETION` |
| `answer_mode` | `TEXT`, `SINGLE`, `MULTI` |
| `question_text` | prompt text |
| `correct_answer` | legacy/simple answer |
| `options_json` | options JSON |
| `accepted_answers_json` | accepted answers JSON |
| `case_insensitive`, `ignore_whitespace`, `ignore_punctuation` | answer normalization switches |
| `display_order` | ordering |
| `score` | score per question |
| `is_deleted` | soft-delete flag |

Images use `biz_image_resource` target type `LISTENING_QUESTION`.

### `listening_record`

Same role and shape as `reading_record`, but points to `listening_test`.

Record statuses currently use `in_progress`, `paused`, `submitted`.

### `listening_answer_record`

Same role and shape as `reading_answer_record`, but points to listening questions.

## Writing

### `writing_question`

Purpose: writing prompt/task.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | question id |
| `task_type` | writing task type |
| `chart_type` | optional IELTS Task 1 chart type, e.g. `Line graph`, `Bar chart`, `Pie chart`, `Table`, `Map`, `Process diagram`, `Mixed charts` |
| `title` | display title |
| `description` | prompt description |
| `image_detail_description` | AI-generated detailed description of writing question images, used as scoring context |
| `prep_seconds` | preparation time stored in seconds |
| `total_seconds` | formal writing time stored in seconds |
| `is_deleted` | soft-delete flag |
| `deleted_time` | delete timestamp |
| `created_time` | create timestamp |

Question images are stored in `biz_image_resource` with target type `WRITING_QUESTION`; `writing_question` no longer stores legacy `image_url` or `image_object_key`.

Migration sources:

- `scripts/sql/writing_question_time_settings.sql`
- `scripts/sql/writing_question_chart_type.sql`
- `scripts/sql/writing_question_image_detail_description.sql`
- `scripts/sql/writing_question_drop_image_target_migrated.sql`

### `writing_record`

Purpose: writing submission and AI scoring result.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | record id |
| `user_id` | owner `sys_user.id` |
| `question_id` | parent `writing_question.id` |
| `input_type` | `TEXT`, `IMAGE`, or `PDF` |
| `text_content` | direct text answer |
| `extracted_text` | OCR/PDF extracted text |
| `target_score` | optional target score |
| `ai_score` | AI IELTS score |
| `ai_feedback` | AI feedback |
| `ai_raw_response` | raw model response |
| `ai_status` | `PENDING`, `SUCCESS`, `FAILED` |
| `ai_provider` | AI provider label |
| `ai_model` | AI model label |
| `created_time` | submission time |
| `is_deleted` | soft-delete flag |
| `deleted_time` | delete timestamp |

### `writing_record_attachment`

Purpose: uploaded images/PDFs for a writing record.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | attachment id |
| `record_id` | parent `writing_record.id` |
| `file_type` | `IMAGE` or `PDF` |
| `file_url` | OSS URL |
| `file_key` | OSS object key |
| `sort_order` | display order |
| `created_time` | upload timestamp |
| `ocr_text` | extracted OCR text for image attachments |

## Speaking

### `speaking_question`

Purpose: speaking question bank.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | question id |
| `part` | `OPENING`, `PART1`, `PART2`, `PART3` |
| `sub_type` | planner subtype, e.g. normal/cue-card/follow-up category |
| `topic_key` | topic grouping key |
| `question_text` | prompt |
| `cue_card` | PART2 cue card text |
| `follow_up_questions_json` | follow-up question JSON |
| `prep_seconds` | preparation time |
| `answer_seconds` | answer time |
| `display_order` | ordering |
| `active` | whether planner may use it |
| `is_deleted` | soft-delete flag |
| `deleted_time` | delete timestamp |
| `created_time` | create timestamp |

### `speaking_record`

Purpose: one submitted speaking answer and its scoring result.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | record id |
| `user_id` | owner `sys_user.id` |
| `session_id` | parent speaking session identifier |
| `question_id` | answered `speaking_question.id` |
| `audio_url` | uploaded answer audio URL |
| `transcript` | ASR transcript |
| `fluency_and_coherence` | IELTS criterion score |
| `lexical_resource` | IELTS criterion score |
| `grammatical_range_and_accuracy` | IELTS criterion score |
| `pronunciation` | IELTS criterion score |
| `overall_score` | overall score |
| `feedback` | AI feedback |
| `answer_status` | `RECEIVED`, `PROCESSING`, `SCORED`, `FAILED` |
| `is_deleted` | soft-delete flag |
| `deleted_time` | delete timestamp |
| `ai_status` | AI processing status |
| `ai_provider` | provider label |
| `ai_model` | model label |
| `ai_error_message` | failure message |
| `relevance_comment`, `quality_comment` | extra evaluation comments |
| `created_time`, `updated_time` | lifecycle timestamps |

### `speaking_session`

Purpose: full speaking exam session state and final evaluation.

Columns used by code:

| Column | Meaning |
| --- | --- |
| `id` | row id |
| `session_id` | public session identifier |
| `user_id` | owner `sys_user.id` |
| `exam_type` | currently `FULL` |
| `total_questions` | planned question count |
| `current_index` | current progress index |
| `exam_status` | `PENDING`, `STARTED`, `IN_PROGRESS`, `WAITING_FINAL_EVALUATION`, `COMPLETED`, `FAILED` |
| `exam_plan_json` | planned questions JSON |
| `fluency_and_coherence`, `lexical_resource`, `grammatical_range_and_accuracy`, `pronunciation`, `overall_score` | final scores |
| `final_feedback` | final AI feedback |
| `started_time`, `completed_time` | session lifecycle |
| `created_time`, `updated_time` | row lifecycle |

### `speaking_talk`

Purpose: D-ID talk tracking for generated speaking video/status.

Columns from migration:

| Column | Meaning |
| --- | --- |
| `id` | row id |
| `talk_id` | D-ID talk id; unique |
| `user_id` | owner `sys_user.id` |
| `session_id` | speaking session id |
| `question_id` | speaking question id |
| `talk_status` | `CREATED` or `FAILED` plus provider statuses |
| `video_url` | generated video URL |
| `error_message` | failure message |
| `created_time`, `updated_time` | lifecycle timestamps |

Migration source: `scripts/sql/speaking_talk.sql`.

Recommended keys from migration:

- `UNIQUE KEY uk_speaking_talk_talk_id (talk_id)`
- `KEY idx_speaking_talk_user_id (user_id)`
- `KEY idx_speaking_talk_session_question (session_id, question_id)`

## Dashboard SQL Awareness

Dashboard SQL generation and guard logic use explicit schema awareness. When adding/removing tables or columns used by AI dashboard:

- Update `DashboardTableNameConstants`.
- Update `DashboardTableSchemaRegistry`.
- Update prompt/schema constants in `dashboard/query`.
- Keep read-only and permission guards aligned with user/admin scope.

Do not expose tables containing secrets or auth-sensitive fields beyond what dashboard services already guard. `sys_user.password` and token internals should not be surfaced to AI answers.

## Production Cleanup Notes

Known structural cleanup note:

- `listening_question_answer_rule_backup_before_group_rules` is documented as a removal candidate in `docs/database-production-cleanup-outline.md`; no mapper/service reference was found.
- `speaking_talk` is required if D-ID speaking talk flow is enabled.

Before production schema cleanup:

1. Back up schema and data.
2. Verify target branch references with `rg`.
3. Apply one focused migration at a time.
4. Compile and run affected service/controller tests.

