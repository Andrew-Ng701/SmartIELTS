# SmartIELTS Backend Overview

Last Updated: 2026-05-15

Source Verified: `pom.xml`, `application.yml`, `src/main/java/com/andrew/smartielts`, `src/main/resources/mapper`, `scripts/sql`, current tests.

## �ت�

�o����󴣨ѫ�ݵ��c�`���A������}�o�P AI agent ��ֳt�w�� module�Bservice boundary�B��ƪ�P API contract�CAPI �Ӹ`�H `docs/api/api-contract.md` ���ǡF��Ʈw���c�H `docs/database-overview.md` �ɥR�C

## �޳N��

- Spring Boot `3.3.5`
- Java `17`
- Spring Security stateless JWT
- MyBatis XML mapper with MySQL
- Knife4j / OpenAPI 3
- Redis for dashboard/preload cache
- Aliyun OSS for file storage
- Aliyun-compatible AI services for writing�Bspeaking�Bdashboard
- D-ID integration for speaking talk flow

Entrypoint:

- `src/main/java/com/andrew/smartielts/SmartIeltsApplication.java`

Key config:

- `src/main/resources/application.yml`
- `security/config/SecurityConfig.java`
- `security/config/SwaggerConfig.java`
- `config/WebMvcConfig.java`
- `config/MyBatisConfig.java`

## Runtime

- Server port default: `8080`
- Servlet path default: `/api`
- Controller mapping ���]�t `/api`�FHTTP client �ݭn�[�W servlet path�C
- MyBatis XML: `src/main/resources/mapper/**/*.xml`
- MyBatis `map-underscore-to-camel-case`: `true`
- Multipart defaults: max file size `20MB`, max request size `100MB`
- D-ID smoke page root path `/did-agent-smoke.html` redirects to `/api/did-agent-smoke.html`

## Security And Auth

Auth is stateless JWT.

Public:

- `/api/auth/register`
- `/api/auth/login`
- Swagger / Knife4j docs

Protected:

- `/api/user/**` requires `ROLE_USER`
- `/api/admin/**` requires `ROLE_ADMIN`
- `/api/smartielts/dashboard/**` requires authentication; role and target-user scope are checked inside dashboard services.

Login/register/refresh response returns:

- `token`
- `tokenExpiresIn`
- `refreshAfterSeconds`
- `tokenType`
- `userId`
- `role`

JWT claims include `userId`, `role`, and `tokenVersion`. `JwtAuthenticationFilter` parses `Authorization: Bearer <token>`, loads active `sys_user`, and rejects the request if token `tokenVersion` differs from `sys_user.token_version`.

Successful login updates `sys_user.last_login_time` and backend-owned `sys_user.consecutive_login_days`. Same-day logins keep the current streak, login after yesterday increments it, and login after a gap resets it to `1`.

`POST /api/auth/logout` and `PUT /api/auth/password` increment `token_version`, so old tokens become invalid immediately.

## Common Contracts

- `Result<T>`: `code`, `msg`, `data`
- `PageResult<T>`: `list`, `total`, `pageNum`, `pageSize`
- Backend-owned values stay on server: ids, ownership, timestamps, status transitions, scoring, generated file object keys, AI result, `tokenVersion`.
- Database uses `snake_case`; Java properties use `lowerCamelCase`. Do not rename mapper aliases or public DTO/VO fields only for style.

## Module Map

- `auth`: register, login, refresh, password change, logout.
- `security`: JWT filter, security config, password config, login principal model, JWT properties.
- `user`: current user profile, profile picture, personal overview/stats, admin user management.
- `admin`: compatibility/admin entrypoints, generic exam and record wrappers.
- `console`: deterministic dashboard/console display data without AI chat orchestration.
- `record`: unified USER record API across reading, listening, writing, and speaking.
- `reading`: reading tests, passages, part groups, questions, sessions, submissions, records.
- `listening`: listening tests, audio, part groups, questions, sessions, submissions, records.
- `writing`: writing questions, submissions, OCR/PDF extraction, attachments, AI scoring, records.
- `speaking`: speaking questions, exam sessions, answer audio upload, D-ID talk status, ASR/scoring, records.
- `dashboard`: AI dashboard ask/SSE/preload, intent handling, answer composition, SQL/query safety.
- `common`: response/page wrappers, validators, storage, image resources, constants, exception handling.
- `utils`: JWT and security helper utilities.

Layer pattern:

- `controller`: HTTP contract and request binding.
- `service`: business rules, ownership checks, orchestration, transaction boundaries.
- `mapper`: MyBatis mapper interfaces.
- `resources/mapper`: SQL XML.
- `domain/dto`: request payloads.
- `domain/query`: filter/page queries.
- `domain/vo`: response payloads.
- `domain/pojo`: persistence objects.

## Generic Admin Wrappers

`admin/exam` provides generic admin test-paper endpoints:

- Base path: `/api/admin/exams/{module}/tests`
- Supported module values are handled by `AdminExamServiceImpl`.
- Current intent is to normalize admin test management while module-specific services still own business rules.

`record/controller/admin` and `record/service/admin` provide generic admin record endpoints:

- Base path: `/api/admin/records`
- `POST /api/admin/records/list` returns a cross-user, cross-module paged record list for admin management pages.
- `AdminUserRecordListMapper` uses SQL `union all` across reading, listening, writing and speaking records so filtering and sorting are globally correct.
- List filters support active/deleted state, optional module, optional status, optional owner `userId`, score/time/name/module/status sorting and pagination.
- Detail/delete/restore behavior delegates to module admin services, similar to the user-facing `record` package.
- Module-specific admin record endpoints have been retired. Admin record list/detail/delete/restore uses `/api/admin/records` and delegates to module admin services internally.

Admin user control also exposes user-scoped record detail:

- `POST /api/admin/users/{userId}/records`
- `GET /api/admin/users/{userId}/records/{moduleType}/{recordId}`
- The list endpoint verifies the admin-selected user exists, then reuses `AdminUserRecordService` with the path `userId` as the fixed owner filter.
- The detail endpoint verifies the admin-selected user exists, then reuses the unified record detail flow with that `userId` so module ownership checks remain consistent.

Admin speaking record detail returns the selected recording plus `sessionRecords`, allowing admin user control pages to show every recording in the same speaking session.

Generic admin record detail `GET /api/admin/records/{module}/{recordId}` also returns `UserRecordDetailVO`, so admin and user replay screens share the same `detail`/`review` contract.

Module-specific admin endpoints remain the source of complete functionality for test/question management, nested reading/listening content management and upload flows.

## Console Flow

Main controllers:

- `console/controller/AdminConsoleController`
- `console/controller/UserConsoleController`

Purpose:

- Keep deterministic full console data available without going through the AI assistant route.
- Use console-owned VO shapes for the latest backend dashboard contract instead of preserving older overview visual fields.
- Provide one full-page GET surface per role:
  - `GET /api/admin/console`
  - `GET /api/user/console`
- Admin console is a global management overview and does not accept `targetUserId`.

Core services:

- `console/service/AdminConsoleService`
- `console/service/UserConsoleService`
- `console/service/LearningConsoleQueryService`

`AdminConsoleService` and `UserConsoleService` assemble the public console payloads. Dashboard handlers and profile stats use `LearningConsoleQueryService` directly for smaller deterministic query slices.

## Unified User Record Flow

Main controller:

- `record/controller/UserRecordController`

Purpose:

- Give frontend one record listing/detail/delete/restore API for all IELTS modules.
- Keep module-specific ownership and business rules in original services.
- Provide `GET /api/user/records` for a cross-module basic list with global sorting, filtering and pagination.
- The basic list includes score fields and supports score sorting in addition to time/name/module/status sorting.
- Normalize list display into `UserRecordItemVO`.
- Preserve module-specific raw list item under `raw`.
- Normalize detail display into `UserRecordDetailVO` with `detailType`, module-specific `detail`, and standardized `review`.
- Detail payloads carry replay data for the frontend: reading keeps passages and group instructions under `parts[].groups[]`, listening keeps group instructions plus test/part-group audio, writing keeps `prompt`, text/OCR, attachments, question images, and a merged `previewAssets` list for question/answer media preview, and speaking keeps `prompt`/cue-card fields.

Supported module types:

- `READING`
- `LISTENING`
- `WRITING`
- `SPEAKING`

Supported record states:

- `ACTIVE`
- `DELETED`

Key classes:

- `record/service/impl/UserRecordServiceImpl`
- `record/mapper/UserRecordListMapper`
- `record/support/UserRecordAdapter`
- `record/support/ReadingUserRecordAdapter`
- `record/support/ListeningUserRecordAdapter`
- `record/support/WritingUserRecordAdapter`
- `record/support/SpeakingUserRecordAdapter`
- `record/support/RecordReviewBuilder`

Flow:

1. Frontend calls `GET /api/user/records` for a cross-module basic list. `UserRecordListMapper` uses SQL `union all` across four record tables so module/status filters and sorting are globally correct.
2. Frontend can still call `/api/user/records/overview` with `moduleType`, `recordState`, paging and optional module-specific filters.
3. `UserRecordServiceImpl` normalizes module/state and selects a `UserRecordAdapter` for the module-specific overview/detail/delete/restore paths.
4. Adapter converts unified query into module-specific query and calls the module user service.
5. Detail/delete/restore are delegated to the same adapter so ownership checks remain module-owned.
6. `RecordReviewBuilder` converts module details into review payloads. Reading/listening review preserves the original exam page structure, including reading passage titles/content and listening audio, then adds merged question answer rows; writing review carries question images, uploaded answer attachments, a merged `previewAssets` list, and uses final OCR text for image/PDF submissions when present; speaking review expands a single record into the whole session with final scores and per-question recordings/comments.
7. Speaking whole-session review is also available through `/api/user/records/speaking/sessions/{sessionId}`.

## User And Admin User Flow

Current user:

- `user/controller/user/UserController`
- `user/service/user/impl/UserServiceImpl`

Main capabilities:

- profile read/update
- profile picture upload/read
- IELTS target score fields
- profile picture OSS object management

Admin user:

- `user/controller/admin/AdminUserController`
- `user/service/admin/impl/AdminUserServiceImpl`

Main capabilities:

- active user list via `GET /api/admin/users/list`, returning user profile basics and IELTS target scores without concrete user record rows
- deleted user page
- user detail with profile and aggregate record counts
- user-scoped record detail via `GET /api/admin/users/{userId}/records/{moduleType}/{recordId}`, returning the same `UserRecordDetailVO.detail` and `review` contract as the user record API
- soft delete / restore

## Reading Flow

Main controllers:

- `reading/controller/user/UserReadingController`
- `reading/controller/admin/AdminReadingController`

User flow:

1. List tests.
2. Start a session and receive `ReadingSessionVO`.
3. Fetch/pause/resume session.
4. Submit `ReadingSubmitDTO`.
5. Backend validates ownership/session status, judges answers, writes record and answer rows.
6. User record detail/delete is available through the unified record API; admin record list/detail/delete/restore is available through `/api/admin/records`.

Admin flow:

- Manage test shell/list/delete/restore, full test content, images, and other reading setup flows.
- Save full test content with `/api/admin/reading/tests/{testId}/full`; this is the official admin entrypoint for passages, part groups, questions, sorting, soft delete, and restore.
- Part group/question image upload stays as multipart resource endpoints and uses shared `BizImageResourceService`.
- Accepted answer rules and judging behavior stay backend-owned.
- Admin create/update uses `prepSeconds` for preparation time and `totalMinutes` for formal exam time; legacy `prepMinutes` remains accepted as a fallback.
- User test list and session responses return both seconds and minutes.
- Starting a reading session immediately creates an `IN_PROGRESS` record.

## Listening Flow

Main controllers:

- `listening/controller/user/UserListeningController`
- `listening/controller/admin/AdminListeningController`

User flow mirrors reading:

1. List listening tests.
2. Start/fetch/pause/resume session.
3. Submit `ListeningSubmitDTO`.
4. Backend judges answers and persists record/answer rows.
5. Detail response includes test-level and part-group audio data used by frontend playback/review.

Admin flow:

- Manage test shell/list/detail/delete/restore, full test content, audio, images, and other listening setup flows.
- Save full test content with `/api/admin/listening/tests/{testId}/full`; this is the official admin entrypoint for part groups, questions, sorting, soft delete, restore, audio references, and image references.
- Test audio, part-group audio, and image upload stay as multipart resource endpoints.
- Audio upload uses multipart `file` with optional `title` and optional `transcriptText`. A test-level audio row represents one tape for the whole test; a part-group audio row represents one tape for one task/group.
- `allowAudioSeek` is part of the current listening test/session contract.
- Admin create/update uses `prepSeconds` for preparation time and `totalMinutes` for formal exam time; legacy `prepMinutes` remains accepted as a fallback.
- User test list and session responses return both seconds and minutes.
- Starting a listening session immediately creates an `IN_PROGRESS` record.

## Writing Flow

Main controllers:

- `writing/controller/user/UserWritingController`
- `writing/controller/admin/AdminWritingController`

User submission:

1. User selects a writing question.
2. User submits multipart content to `/api/user/writing/questions/{questionId}/submit`.
3. Supported inputs: `textContent`, multiple `images`, single `pdf`, optional `targetScore`.
4. `WritingSubmissionValidator` validates input combinations.
5. Backend uploads files to OSS, extracts text when needed, creates record/attachments.
6. PDF submission first uses PDF text extraction; when that returns no text, the backend renders PDF pages to images, uploads the rendered pages, and runs OCR as fallback.
7. Admin writing question image replacement generates `imageDetailDescription` from the uploaded question images using the writing AI image flow. The description focuses on chart/table/map/process data needed for scoring and excludes irrelevant visual-quality comments such as colour or clarity.
8. AI scoring updates status, score, feedback, provider, and model. For writing questions with images, scoring includes both `description` and `imageDetailDescription`.

Backend-owned values:

- `inputType`: `TEXT`, `IMAGE`, `PDF`
- file type
- OCR/PDF extracted text
- Writing question `imageDetailDescription`
- AI status/result/provider/model
- Writing AI feedback is requested in English, uses double-asterisk Markdown bold for key points, and separates paragraphs with a blank line.
- Writing question create/update prefers `prepSeconds` for preparation time and `totalMinutes` for formal writing time; legacy `prepMinutes` remains accepted as a fallback, and optional `totalSeconds` can override `totalMinutes`.
- Writing question `chartType` is optional metadata for IELTS Task 1 chart categories and remains `null` for prompts that do not use a chart category.
- Writing has no start/session API; frontend handles prep countdown and formal exam timing before submit.

## Speaking Flow

Main controllers:

- `speaking/controller/user/UserSpeakingController`
- `speaking/controller/admin/AdminSpeakingController`

User exam flow:

1. Start exam with optional `StartExamRequestDTO`.
2. Backend plans questions and session state.
3. Frontend asks for next question by `sessionId`.
4. User submits answer audio with `sessionId`, `questionId`, multipart `file`.
5. Backend uploads audio, performs ASR/scoring, stores speaking record, and updates session summary.
6. User can fetch session summary, D-ID talk status, or unified record detail.

Audio-only upload:

- `/api/user/speaking/upload-audio`
- Required part: `file`
- Optional params: `sessionId`, `questionId`

## Dashboard And AI Query Flow

Main controllers:

- `dashboard/controller/UserDashboardController`
- `dashboard/controller/AdminDashboardController`
- `dashboard/controller/UserDashboardSseController`
- `dashboard/controller/AdminDashboardSseController`
- `dashboard/controller/DashboardPreloadController`

Capabilities:

- JSON ask endpoints
- SSE ask endpoints with staged events
- User/admin overview visual payloads
- User/admin executive summaries
- Preload payloads for frontend assistant calls

Executive summary cache:

- `UserDashboardServiceImpl` and `AdminDashboardServiceImpl` cache executive summary VO payloads through `DashboardExecutiveSummaryCacheService`.
- Redis keys are scoped by role, operator user, target/global scope, time range, and optional `summary_cache_key`.
- Cache is enabled by default; `summary_cache_key` only narrows the frontend page-session cache scope.
- Cache failures are non-blocking: services fall back to composing a fresh executive summary.

Ask flow:

1. Controller receives `DashboardAskRequest`.
2. `DashboardIntentExecutionFacade` resolves role/operator/target context.
3. Context resolver combines request context, preload payload, learning context, and question context.
4. For reading/listening record-level asks, learning context also includes `record_questions` so range prompts such as "Question 1-5" can reach AI with question text, saved answer, correct answer, accepted answers, correctness, and score.
5. If `conversationHistory` is present, the facade passes prior user/assistant turns into ask decision, direct answer composition data, and structured-query fallback context so second-round follow-up questions can reference first-round content. If the current follow-up request omits `objectRef`, the facade also attempts to recover it from `conversationHistory[].meta.objectRef`, `conversationHistory[].meta.data.objectRef`, or `conversationHistory[].meta.response.data.objectRef`.
6. Intent parsing resolves capability/action/filter plan.
7. Router dispatches to structured handlers or guarded SQL/query path.
8. SSE ask endpoints receive first-stage progress through `DashboardAskProgressListener` immediately after decision resolution.
9. Answer compose/rewrite/review services produce final answer/data/suggestions/meta. Dashboard suggestions are emitted in English and should stay focused on useful IELTS follow-up actions for the current user context.
10. SSE ask endpoints emit `finalStart(clearPrevious=true)`, stream `answerDelta` chunks, then send the full `result`.

SSE events:

- `start`
- `loading`
- `intentResolved`
- `finalStart`
- `answerDelta`
- `finalEnd`
- `result`
- `error`
- `done`

High-risk dashboard areas:

- `dashboard/query/ReadOnlySqlGuard`
- `dashboard/query/DashboardSqlSchemaGuard`
- `dashboard/query/DashboardAiSqlPolicyGuard`
- `dashboard/query/DashboardQueryPermissionGuard`
- `dashboard/query/DashboardTableSchemaRegistry`
- `dashboard/detail/**`
- `dashboard/agent/**`

## Storage, OSS, Multipart

Shared storage:

- `common/storage/OssProperties`
- `common/storage/BucketType`
- `common/storage/service/OssStorageService`
- `common/storage/service/impl/OssStorageServiceImpl`
- `common/storage/UploadResult`

Bucket types currently cover:

- writing question images
- writing record files
- listening audio
- speaking audio
- question group images
- user profile pictures

Image resources:

- `common/image/service/BizImageResourceService`
- `common/image/domain/dto/BizImageResourceDTO`
- table: `biz_image_resource`

Multipart endpoints:

- Writing submit: `targetScore`, `textContent`, `images`, `pdf`
- Reading admin images: part group/question `file`
- Listening admin audio: `file`, optional `title`
- Listening admin images: part group/question `file`
- Speaking answer: `sessionId`, `questionId`, `file`
- Speaking upload audio: `file`, optional `sessionId`, `questionId`
- User profile picture: `file`

## Mapper And Database Access

Mapper interfaces live in module `mapper` packages. SQL XML lives under `src/main/resources/mapper/<module>`.

Important mapper groups:

- `auth/mapper/AuthMapper`
- `user/mapper/UserMapper`
- `reading/mapper/*`
- `listening/mapper/*`
- `writing/mapper/*`
- `speaking/mapper/*`
- `dashboard/learning/mapper/LearningObjectMapper`
- `common/image/mapper/BizImageResourceMapper`

SQL changes should preserve:

- snake_case DB columns
- lowerCamelCase Java properties
- existing aliases used by VO/DTO mapper result maps
- soft-delete filtering rules
- ownership checks

See `docs/database-overview.md` before adding or renaming a table.

## Constants Naming

Stable literal values shared across modules should live in a `*Constants` class and use `UPPER_SNAKE_CASE` field names, especially:

- DB table names: `DbTableNames`, `DashboardTableNameConstants`
- API message keys/text: `ApiMessageConstants`
- storage target/bucket/path values: `StorageBizConstants`
- module/status/detail values: `UserRecordModuleConstants`, `UserRecordStateConstants`, `SpeakingStatusConstants`

Runtime values and injected collaborators keep `lowerCamelCase` even when `final`. Existing public DTO/VO fields, request parameters, response fields, DB columns, and mapper aliases should not be renamed just for style.

## Testing Notes

General rules:

- Service/unit tests that need current user id should mock `SecurityUtils.getCurrentUserId()`.
- Ordinary service tests should not call HTTP login.
- Security filter/controller auth tests may use MockMvc with Bearer token.
- HTTP integration tests should login once and reuse `data.token`.
- Do not logout/change password before reusing the same token.

Useful commands:

```powershell
mvn test
mvn -q -DskipTests compile
```

## Source Lookup Guide

API mapping:

```powershell
rg -n --glob '*Controller.java' "@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@DeleteMapping|@PatchMapping" src/main/java/com/andrew/smartielts
```

DTO/query/VO fields:

```powershell
rg -n "class .*DTO|class .*Query|class .*VO|private .*;" src/main/java/com/andrew/smartielts
```

Module entrypoints:

- Auth: `auth/controller/AuthController.java`
- Current user: `user/controller/user/UserController.java`
- Admin users: `user/controller/admin/AdminUserController.java`
- Console: `console/controller/*`
- Generic admin exams: `admin/exam/controller/AdminExamController.java`
- Generic admin records: `record/controller/admin/AdminUserRecordController.java`
- User records: `record/controller/UserRecordController.java`
- Reading: `reading/controller/*`
- Listening: `listening/controller/*`
- Writing: `writing/controller/*`
- Speaking: `speaking/controller/*`
- Dashboard: `dashboard/controller/*`

High-risk source areas that justify direct lookup:

- JWT/security: `security/**`, `utils/JwtUtil.java`, `utils/SecurityUtils.java`
- Dashboard AI/query: `dashboard/agent/**`, `dashboard/query/**`, `dashboard/detail/**`
- File upload/storage: `common/storage/**`, `common/image/**`, module upload services
- Scoring/judging: reading/listening support classes, writing/speaking AI services
- Mapper/query behavior: XML under `src/main/resources/mapper`

## Documentation Update Checklist

When API, backend structure, or DB schema changes:

- Update `docs/api/api-contract.md` for role, path, method, request fields, response shape, multipart and SSE details.
- Update this overview for package/module/service flow changes.
- Update `docs/database-overview.md` for table/column/relationship/migration changes.
- Update `AGENTS.md` only for stable project memory, workflow rules, test/login facts, or project-level conventions that future agents must follow.
