# SmartIELTS API Contract

Last Updated: 2026-05-22

Source verified against `src/main/java/com/andrew/smartielts/**/controller`, request DTO/query classes, response VO classes, `SecurityConfig`, `JwtAuthenticationFilter`, and `application.yml`.

This is the formal frontend integration contract. It lists only supported endpoints. Removed module-specific record endpoints are explicitly marked in section 14.

## 1. Global Rules

### Base URL

```text
http://localhost:8080/api
```

`spring.mvc.servlet.path = /api`, so controller mapping `/auth/login` is called as `/api/auth/login`.

Docs:

```text
/api/doc.html
/api/v3/api-docs
```

### Result Wrapper

Most JSON APIs return `Result<T>`:

```json
{
  "code": 1,
  "msg": null,
  "data": {}
}
```

| Field | Type | Meaning |
| --- | --- | --- |
| `code` | number | `1` success; `0` business/validation failure. |
| `msg` | string/null | Display this when `code = 0`. |
| `data` | any/null | Endpoint-specific payload. Empty success may be `null`. |

HTTP auth errors:

| Status | Meaning | Frontend behavior |
| --- | --- | --- |
| `401` | Missing/malformed/expired token, deleted user, or invalid token version. | Clear token and redirect login. |
| `403` | Token valid but role mismatch. | Show no-permission state. |

### Auth Header

```http
Authorization: Bearer <data.token>
```

No session/cookie login state is used.

### Roles

| Prefix | Role |
| --- | --- |
| `/api/user/**` | `ROLE_USER` |
| `/api/admin/**` | `ROLE_ADMIN` |
| `/api/smartielts/dashboard/**` | Authenticated; dashboard service handles scope. |

### JSON, Multipart, Time

- JSON requests use `Content-Type: application/json`.
- Multipart requests must use `FormData`; do not manually set boundary.
- `LocalDateTime` format: `2026-05-15T13:30:00`.

### Frontend/Backend Ownership Boundary

- Frontend sends only the key data needed for the backend to complete the action. Backend-owned values include generated IDs, timestamps, ownership, scoring, permission decisions, persistence decisions, and status transitions unless a specific endpoint states otherwise.
- If an integration issue cannot be fixed correctly on the side currently being changed, call out the ownership boundary explicitly. For example, frontend work should flag missing/ambiguous backend fields or API behavior instead of duplicating backend business logic; backend work should flag missing/incorrect frontend payload, state handling, or route usage instead of silently accepting unstable client assumptions.
- When reporting such a finding, include the affected endpoint/DTO/VO or screen flow, why the change belongs to the other side, and the smallest contract/code change needed to reach the goal.

### Pagination

```json
{
  "list": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10
}
```

Common values:

| Name | Values |
| --- | --- |
| module | `READING`, `LISTENING`, `WRITING`, `SPEAKING` |
| recordState | `ACTIVE`, `DELETED` |
| sortDirection | `ASC`, `DESC` |
| record status examples | `COMPLETED`, `DELETED`, `PENDING`, `SUCCESS`, `FAILED`, `RECEIVED`, `PROCESSING`, `SCORED`, `IN_PROGRESS`, `PAUSED`, `SUBMITTED`, `AUTO_SUBMITTED` |

Frontend should render unknown statuses defensively.

## 2. Auth APIs

### Register

```http
POST /api/auth/register
```

Request: `UserDTO`.

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `email` | string | yes | Backend trims and lowercases. |
| `password` | string | yes | Plain password; backend stores BCrypt hash. |

```json
{
  "email": "student@example.com",
  "password": "password123"
}
```

Response data: `AuthResponseVO`.

| Field | Type | Notes |
| --- | --- | --- |
| `token` | string | JWT. |
| `tokenExpiresIn` | number | TTL seconds. |
| `refreshAfterSeconds` | number | Recommended refresh timing. |
| `tokenType` | string | `Bearer`. |
| `userId` | number | Current user id. |
| `role` | string | `USER` or `ADMIN`. |

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "token": "eyJhbGciOi...",
    "tokenExpiresIn": 7200,
    "refreshAfterSeconds": 900,
    "tokenType": "Bearer",
    "userId": 2,
    "role": "USER"
  }
}
```

### Login

```http
POST /api/auth/login
```

Request/response are the same as register.

### Refresh Token

```http
POST /api/auth/refresh
Authorization: Bearer <current-token>
```

Request body: none. Response data: `AuthResponseVO`. Replace the stored token with the returned token.

### Change Password

```http
PUT /api/auth/password
Authorization: Bearer <token>
```

Request: `ChangePasswordDTO`.

| Field | Type | Required |
| --- | --- | --- |
| `oldPassword` | string | yes |
| `newPassword` | string | yes |

```json
{
  "oldPassword": "old-password",
  "newPassword": "new-password"
}
```

Success invalidates old tokens. Frontend should clear auth state and redirect login.

### Logout

```http
POST /api/auth/logout
Authorization: Bearer <token>
```

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "message": "Logout successful.",
    "clearTokenRequired": true,
    "reloginRequired": false
  }
}
```

## 3. User Profile APIs

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/user/profile` | USER | none | `UserProfileVO` |
| `PUT` | `/api/user/profile` | USER | `UserProfileUpdateDTO` | `UserProfileVO` |
| `GET` | `/api/user/profile-picture` | USER | none | `UserProfileVO` |
| `PUT` | `/api/user/profile-picture` | USER | multipart `file` | `UserProfileVO` |

`UserProfileUpdateDTO`:

| Field | Type | Required |
| --- | --- | --- |
| `email` | string | no |
| `username` | string | no |
| `listeningTargetScore` | decimal | no |
| `readingTargetScore` | decimal | no |
| `writingTargetScore` | decimal | no |
| `speakingTargetScore` | decimal | no |

```json
{
  "email": "new-student@example.com",
  "username": "Alice",
  "listeningTargetScore": 7.0,
  "readingTargetScore": 7.0,
  "writingTargetScore": 6.5,
  "speakingTargetScore": 6.5
}
```

`UserProfileVO`:

| Field | Type | Notes |
| --- | --- | --- |
| `id` | number | user id |
| `email` | string | login email |
| `username` | string/null | display name |
| `role` | string | `USER` / `ADMIN` |
| `isDeleted` | number | `0` / `1` |
| `deletedTime` | string/null | soft-delete time |
| `createdTime` | string | created time |
| `lastLoginTime` | string/null | last login |
| `profilePictureUrl` | string/null | avatar URL |
| `profilePictureObjectKey` | string/null | storage object key |
| `listeningTargetScore` | decimal/null | target score |
| `readingTargetScore` | decimal/null | target score |
| `writingTargetScore` | decimal/null | target score |
| `speakingTargetScore` | decimal/null | target score |

Profile picture upload:

```ts
const form = new FormData();
form.append("file", file);
await fetch("/api/user/profile-picture", {
  method: "PUT",
  headers: { Authorization: `Bearer ${token}` },
  body: form
});
```

## 4. Admin User APIs

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/admin/users/list` | ADMIN | query `AdminUserPageQuery` | `AdminUserListVO` |
| `POST` | `/api/admin/users/deleted/overview` | ADMIN | `AdminDeletedUserPageQuery` | `PageResult<UserAdminVO>` |
| `GET` | `/api/admin/users/{userId}` | ADMIN | path | `UserAdminDetailVO` |
| `POST` | `/api/admin/users/{userId}/records` | ADMIN | `AdminUserScopedRecordListQuery` | `PageResult<AdminUserRecordListItemVO>` |
| `GET` | `/api/admin/users/{userId}/records/{moduleType}/{recordId}` | ADMIN | path | `UserRecordDetailVO` |
| `DELETE` | `/api/admin/users/{userId}` | ADMIN | path | empty success |
| `PUT` | `/api/admin/users/{userId}/restore` | ADMIN | path | empty success |

`AdminUserPageQuery` / `AdminDeletedUserPageQuery`:

| Field | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `pageNum` | number | no | `1` | page number |
| `pageSize` | number | no | `10` | page size |
| `keyword` | string | no | null | fuzzy search |
| `email` | string | no | null | email filter |
| `role` | string | no | null | `USER`, `ADMIN` |
| `startTime` | string | no | null | time range start |
| `endTime` | string | no | null | time range end |
| `sortField` | string | no | `createdTime` / `deletedTime` | active/deleted default differs |
| `sortDirection` | string | no | `DESC` | `ASC`, `DESC` |

Deleted user request:

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "keyword": "alice",
  "sortField": "deletedTime",
  "sortDirection": "DESC"
}
```

`AdminUserListVO`:

| Field | Type |
| --- | --- |
| `users` | `PageResult<UserAdminVO>` |
| `totalUsers` | number |
| `activeUsers` | number |
| `deletedUsers` | number |

`UserAdminVO` fields: `id`, `email`, `role`, `isDeleted`, `deletedTime`, `createdTime`, `lastLoginTime`, `consecutiveLoginDays`, `profilePictureUrl`, `profilePictureObjectKey`, `listeningTargetScore`, `readingTargetScore`, `writingTargetScore`, `speakingTargetScore`.

`UserAdminDetailVO` adds:

| Field | Type |
| --- | --- |
| `totalActiveRecordCount` | number |
| `totalDeletedRecordCount` | number |
| `recordCounts` | `UserRecordCountVO[]` |

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "users": {
      "list": [
        {
          "id": 2,
          "email": "student@example.com",
          "role": "USER",
          "isDeleted": 0,
          "createdTime": "2026-05-01T10:00:00",
          "lastLoginTime": "2026-05-15T09:30:00",
          "consecutiveLoginDays": 6,
          "profilePictureUrl": "https://oss.example/avatar.png",
          "listeningTargetScore": 7.0,
          "readingTargetScore": 7.0,
          "writingTargetScore": 6.5,
          "speakingTargetScore": 6.5
        }
      ],
      "total": 1,
      "pageNum": 1,
      "pageSize": 10
    },
    "totalUsers": 120,
    "activeUsers": 118,
    "deletedUsers": 2
  }
}
```
## 5. Unified User Records

The `record` package is the canonical frontend-facing record API across reading, listening, writing, and speaking. Legacy module-specific user record endpoints have been removed.

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/user/records` | USER | query `UserRecordListQuery` | `PageResult<UserRecordListItemVO>` |
| `POST` | `/api/user/records/overview` | USER | `UserRecordPageQuery` | `PageResult<UserRecordItemVO>` |
| `GET` | `/api/user/records/{moduleType}/{recordId}` | USER | path | `UserRecordDetailVO` |
| `GET` | `/api/user/records/listening/{recordId}/sections/{sectionNumber}/script` | USER | path | `ListeningSectionScriptVO` |
| `DELETE` | `/api/user/records/{moduleType}/{recordId}` | USER | path | empty success |
| `PUT` | `/api/user/records/{moduleType}/{recordId}/restore` | USER | path | empty success |
| `GET` | `/api/user/records/speaking/sessions/{sessionId}` | USER | path | `SpeakingSessionSummaryVO` |

### UserRecordDetailVO

`GET /api/user/records/{moduleType}/{recordId}` returns a unified wrapper with `moduleType`, `recordId`, `detailType`, module-specific `detail`, and standardized `review`.

Replay-oriented fields:

| Module | Detail fields | Review fields |
| --- | --- | --- |
| `READING` | `testId`, `testTitle`, `totalScore`, `parts[].groups[].passages[]`, `questions`, `answers` | `examPageReview.parts`, `examPageReview.questions`, `examPageReview.answers`, `examPageReview.questionReviews` |
| `LISTENING` | `testId`, `testTitle`, `testAudio`, `allowAudioSeek`, `parts[].groups[].audios`, `partGroupAudios`, `questions`, `answers` | `examPageReview.testAudio`, `examPageReview.allowAudioSeek`, `examPageReview.parts`, `examPageReview.partGroupAudios`, `examPageReview.questions`, `examPageReview.answers`, `examPageReview.questionReviews` |
| `WRITING` | `questionId`, `questionTitle`, `questionDescription`, `prompt`, `imageDetailDescription`, `questionImageUrl`, `questionImages`, `previewAssets`, `taskType`, `chartType`, `inputType`, `textContent`, `extractedText`, `attachments`, `aiScore`, `aiFeedback`, `aiStatus` | `writingReview.prompt`, `writingReview.imageDetailDescription`, `writingReview.questionImageUrl`, `writingReview.questionImages`, `writingReview.previewAssets`, `writingReview.answerText`, `writingReview.answerSource`, `writingReview.textContent`, `writingReview.extractedText`, `writingReview.attachments`, `writingReview.aiScore`, `writingReview.aiFeedback`, `writingReview.aiStatus` |
| `SPEAKING` | selected speaking record detail with `questionText`, `prompt`, `cueCard` | `speakingSessionReview.conversations[]` with question text, prompt, cue card, audio URL, transcript, scores, feedback, and session status |

Reading/listening instructions are nested under `parts[].groups[]` as `instructionText`, `groupGuideText`, and `groupRequirementText`; question review rows also expose `prompt` as a stable alias of `questionText`.
Reading passage replay data is nested under `parts[].groups[].passages[]` and includes `id`, `passageNo`, `title`, `content`, `materialType`, `displayOrder`, and nested `questions`.
For writing image/PDF submissions, `writingReview.answerText` uses `extractedText` when present and falls back to `textContent`; `attachments` keeps the original uploaded image/PDF URLs and OCR text. `previewAssets` merges question images and answer attachments in display order for a single preview viewer. Each item includes `sourceType` (`QUESTION_IMAGE` or `ANSWER_ATTACHMENT`), `fileType` (`IMAGE` or `PDF`), `fileUrl`, `sortOrder`, and `label`; clients should show answer attachments under the answer box and may open the same viewer used for question images, including mouse-wheel zoom behavior.

### UserRecordListQuery

Use this for the main cross-module record page.

| Field | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `recordState` | string | no | `ACTIVE` | `ACTIVE`, `DELETED` |
| `module` | string | no | null | one module or all |
| `status` | string | no | null | unified display status |
| `sort` | string | no | `UPDATED_DESC` | `UPDATED_DESC`, `UPDATED_ASC`, `SCORE_DESC`, `SCORE_ASC`, `NAME_ASC`, `NAME_DESC`, `MODULE_ASC`, `STATUS_ASC` |
| `pageNum` | number | no | `1` | page number |
| `pageSize` | number | no | `20` | page size |

Example:

```http
GET /api/user/records?recordState=ACTIVE&module=READING&sort=UPDATED_DESC&pageNum=1&pageSize=20
```

### UserRecordPageQuery

Use this only when module-specific filters are needed.

| Field | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `moduleType` | string | yes | null | `READING`, `LISTENING`, `WRITING`, `SPEAKING` |
| `recordState` | string | no | `ACTIVE` | `ACTIVE`, `DELETED` |
| `pageNum` | number | no | `1` | page number |
| `pageSize` | number | no | `10` | page size |
| `testId` | number | no | null | reading/listening active filter |
| `questionId` | number | no | null | writing active filter |
| `sessionId` | string | no | null | speaking active filter |
| `part` | string | no | null | speaking active filter |
| `inputType` | string | no | null | writing: `TEXT`, `IMAGE`, `PDF` |
| `aiStatus` | string | no | null | writing/speaking AI status |
| `answerStatus` | string | no | null | speaking answer status |
| `minScore` / `maxScore` | number | no | null | reading/listening score range |
| `minOverallScore` / `maxOverallScore` | number | no | null | speaking score range |
| `targetScore` | decimal | no | null | writing target score |
| `startTime` / `endTime` | string | no | null | active-list time range |
| `sortDirection` | string | no | `DESC` | `ASC`, `DESC` |

```json
{
  "moduleType": "WRITING",
  "recordState": "ACTIVE",
  "pageNum": 1,
  "pageSize": 10,
  "inputType": "PDF",
  "aiStatus": "SUCCESS",
  "targetScore": 7.0,
  "sortDirection": "DESC"
}
```

### UserRecordListItemVO

| Field | Type | Notes |
| --- | --- | --- |
| `recordId` | number | module record id |
| `name` | string | test/question display name |
| `module` | string | module value |
| `status` | string | deleted rows return `DELETED`; active rows use module status |
| `score` | decimal/null | module score |
| `scoreText` | string/null | display score |
| `updatedTime` | string/null | sort/display time |
| `createdTime` | string/null | creation time |
| `isDeleted` | number | `0`, `1` |
| `deletedTime` | string/null | delete time |

### UserRecordItemVO

| Field | Type | Notes |
| --- | --- | --- |
| `moduleType` | string | module value |
| `recordId` | number | module record id |
| `title` | string/null | display title |
| `subtitle` | string/null | module subtitle |
| `score` | number/null | module score |
| `scoreText` | string/null | display score |
| `status` | string/null | module status |
| `isDeleted` | number | `0`, `1` |
| `deletedTime` | string/null | delete time |
| `createdTime` | string/null | create time |
| `raw` | object | original module VO |

### UserRecordDetailVO

| Field | Type | Notes |
| --- | --- | --- |
| `moduleType` | string | module value |
| `recordId` | number | module record id |
| `detailType` | string | `READING_RECORD_DETAIL`, `LISTENING_RECORD_DETAIL`, `WRITING_RECORD_DETAIL`, `SPEAKING_RECORD_DETAIL` |
| `detail` | object | module-specific detail VO |
| `review` | `RecordReviewVO` | frontend-ready review payload |

### ListeningSectionScriptVO

Used by the listening record review page to display the transcript for one section. The backend verifies the record belongs to the current user before returning script data.

| Field | Type | Notes |
| --- | --- | --- |
| `recordId` | number | listening record id |
| `testId` | number | listening test id |
| `sectionNumber` | number | requested section number |
| `sectionTitle` | string/null | first active part group title in that section |
| `transcriptText` | string | combined transcript text from section part-group audio |
| `audios` | `ListeningAudio[]` | source audio rows for that section |

Detail example:

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "moduleType": "WRITING",
    "recordId": 9101,
    "detailType": "WRITING_RECORD_DETAIL",
    "detail": {
      "recordId": 9101,
      "questionId": 501,
      "questionTitle": "Education Essay",
      "taskType": "TASK_2",
      "inputType": "PDF",
      "targetScore": 7.0,
      "aiScore": 6.5,
      "aiFeedback": "**Cohesion**: Improve paragraph flow.\n\n**Lexical range**: Use more precise topic vocabulary.",
      "aiStatus": "SUCCESS",
      "createdTime": "2026-05-15T10:00:00"
    },
    "review": {
      "moduleType": "WRITING",
      "recordId": 9101,
      "layoutType": "WRITING_REVIEW",
      "title": "Education Essay",
      "score": 6.5,
      "scoreText": "6.5",
      "status": "SUCCESS",
      "writingReview": {
        "questionId": 501,
        "questionTitle": "Education Essay",
        "answerText": "The essay text extracted from PDF...",
        "answerSource": "OCR",
        "aiScore": 6.5,
        "aiFeedback": "**Cohesion**: Improve paragraph flow.\n\n**Lexical range**: Use more precise topic vocabulary."
      }
    }
  }
}
```

## 6. Generic Admin Records

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/admin/records/list` | ADMIN | `AdminUserRecordListQuery` | `PageResult<AdminUserRecordListItemVO>` |
| `GET` | `/api/admin/records/{module}/{recordId}` | ADMIN | path | `UserRecordDetailVO` |
| `DELETE` | `/api/admin/records/{module}/{recordId}` | ADMIN | path | empty success |
| `PUT` | `/api/admin/records/{module}/{recordId}/restore` | ADMIN | path | empty success |

`AdminUserRecordListQuery`:

| Field | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `recordState` | string | no | `ACTIVE` | `ACTIVE`, `DELETED` |
| `module` | string | no | null | module filter |
| `status` | string | no | null | status filter |
| `userId` | number | no | null | owner filter |
| `sort` | string | no | `UPDATED_DESC` | unified sort value |
| `pageNum` | number | no | `1` | page number |
| `pageSize` | number | no | `20` | page size |

`AdminUserScopedRecordListQuery` has the same fields except `userId`.

Admin generic record detail returns the same `UserRecordDetailVO.detail` / `review` replay contract as user record detail. User-scoped admin detail should use `/api/admin/users/{userId}/records/{moduleType}/{recordId}` when the frontend already operates inside a selected user page.

```json
{
  "recordState": "DELETED",
  "module": "SPEAKING",
  "status": "DELETED",
  "userId": 2,
  "sort": "UPDATED_DESC",
  "pageNum": 1,
  "pageSize": 20
}
```

`AdminUserRecordListItemVO`:

| Field | Type | Notes |
| --- | --- | --- |
| `recordId` | number | module record id |
| `userId` | number | owner id |
| `name` | string | display name |
| `module` | string | module value |
| `status` | string | status |
| `score` | decimal/null | score |
| `scoreText` | string/null | display score |
| `updatedTime` | string/null | sort/display time |
| `createdTime` | string/null | create time |
| `isDeleted` | number | `0`, `1` |
| `deletedTime` | string/null | delete time |

## 7. Reading APIs

### User Reading

| Method | Path | Purpose | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/user/reading/tests` | list active tests | none | test list |
| `POST` | `/api/user/reading/tests/{testId}/start` | start session | path | `ReadingSessionVO` |
| `GET` | `/api/user/reading/sessions/{sessionId}` | get session | path | `ReadingSessionVO` |
| `POST` | `/api/user/reading/sessions/{sessionId}/pause` | pause | optional `ReadingSessionActionDTO` | session result |
| `POST` | `/api/user/reading/sessions/{sessionId}/resume` | resume | none | session result |
| `POST` | `/api/user/reading/tests/{testId}/submit` | submit answers | `ReadingSubmitDTO` | submit result |

User reading records use `/api/user/records`.

`ReadingSubmitDTO`:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `sessionId` | string | yes | current session id |
| `startedTime` | string | no | client start time |
| `timeSpentSeconds` | number | no | elapsed seconds |
| `autoSubmitted` | number | no | `1` timeout submit, `0` manual |
| `answers` | `ReadingAnswerDTO[]` | yes | answers |

`ReadingAnswerDTO`: `questionId`, `answer`, `answers`.

```json
{
  "sessionId": "reading-2-7001",
  "startedTime": "2026-05-15T10:00:00",
  "timeSpentSeconds": 3600,
  "autoSubmitted": 0,
  "answers": [
    { "questionId": 3001, "answer": "TRUE" },
    { "questionId": 3002, "answers": ["A", "C"] }
  ]
}
```

`ReadingSessionActionDTO`: `clientTimeSpentSeconds`.

### Admin Reading

| Method | Path | Purpose | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/admin/reading/tests` | create test shell | `ReadingTestDTO` | test object/detail |
| `GET` | `/api/admin/reading/tests` | list tests | query | test list with `tasks` and `questions` counts |
| `GET` | `/api/admin/reading/tests/{testId}` | test detail | path | `ReadingTestDetailVO` |
| `PUT` | `/api/admin/reading/tests/{id}` | update test shell | `ReadingTestDTO` | updated test |
| `PUT` | `/api/admin/reading/tests/{testId}/full` | save full content | `AdminReadingTestFullSaveDTO` | `ReadingTestDetailVO` |
| `DELETE` | `/api/admin/reading/tests/{id}` | delete test | path | empty success |
| `PUT` | `/api/admin/reading/tests/{id}/restore` | restore test | path | empty success |
| `POST` | `/api/admin/reading/part-groups/{partGroupId}/images` | replace group images | multipart `images` | image list |
| `POST` | `/api/admin/reading/questions/{questionId}/images` | replace question images | multipart `images` | image list |

`ReadingTestDTO`: `title`, `totalScore`, `timerMode`, `prepSeconds`, `totalMinutes`, `autoSubmit`, `allowPause`, `partGroups`. `prepMinutes` remains accepted as a legacy fallback when `prepSeconds` is omitted.

Admin reading test list returns `ReadingTest` fields plus `tasks` and `questions`. `tasks` is the active `reading_passage` count; one passage equals one task. `questions` is the active question count under active passages.

`AdminReadingTestFullSaveDTO`: `test`, `partGroups`, `passages`, `questions`.

`ReadingTestDetailVO`: `id`, `title`, `totalScore`, timer fields, `partGroups`, top-level `passages`, top-level `questions`, and nested `parts[].groups[]`. `passages[]` always includes persisted passage `id` values and each passage's nested `questions`.

`TestPartGroup` fields used by reading: `id`, `testId`, `partNumber`, `groupNumber`, `title`, `instructionText`, `groupGuideText`, `groupRequirementText`, `questionType`, `answerMode`, `optionsJson`, `acceptedAnswersJson`, `answerRulesJson`, `caseInsensitive`, `ignoreWhitespace`, `ignorePunctuation`, `questionNoStart`, `questionNoEnd`, `displayOrder`, `timeLimitSeconds`, `isDeleted`, `images`.

Reading `questionType` uses `MATCHING` for all matching-style questions, including heading matching. Legacy input values `HEADING_MATCHING` and `MATCHING_HEADINGS` are accepted by admin reading writes and normalized to `MATCHING`. Reading completion-style groups accept `SUMMARY_COMPLETION`, `SENTENCE_COMPLETION`, `SHORT_ANSWER`, `TABLE_COMPLETION`, `FLOW_CHART_COMPLETION`, `DIAGRAM_LABEL_COMPLETION`, `FORM_COMPLETION`, and `NOTE_COMPLETION`; these default to `answerMode = "TEXT"` when the admin request omits `answerMode`.

Reading `MATCHING` answer banks use `optionsJson` as an array of `{ "label": "A", "text": "..." }` objects. Admin writes normalize labels from the array order (`A`, `B`, `C`, ...), so clients should treat `label` as backend-owned and submit matching answers as the selected label only. Legacy string arrays such as `["Dr Peter", "Dr Merritt"]` are accepted and normalized to the same object shape.

`ReadingPassageDTO` fields: `id`, `clientKey`, `testId`, `partGroupId`, `passageNo`, `title`, `content`, `materialType`, `displayOrder`.

`ReadingQuestionDTO` fields: `id`, `passageId`, `clientPassageKey`, `partGroupId`, `questionNumber`, `questionType`, `answerMode`, `questionText`, `correctAnswer`, `optionsJson`, `acceptedAnswersJson`, `groupLabel`, `caseInsensitive`, `ignoreWhitespace`, `ignorePunctuation`, `displayOrder`, `score`, `answerRules`, `groupGuideText`, `groupRequirementText`, `questionNoStart`, `questionNoEnd`, `groupImages`, `images`.

Reading `answerRules[].answerText` accepts comma-separated answer variants for the same blank. For example, `About 4 months,4 months,4` is saved as three accepted answers for the same `blankNo`. During grading, `caseInsensitive = 1` ignores letter case and `ignoreWhitespace = 1` removes whitespace before comparison.

Reading/listening multiple-choice grading uses `answerMode` as the selection mode. `SINGLE` treats the item as one question: `correctAnswer = "B,D"` or one accepted answer group requires the submitted selected options to match the full set, and earns the question `score` only when all selected answers are correct. `MULTI` treats each correct option as an independently scored answer: `correctAnswer = "B,C,E"` can earn up to 3 points, one point per matched correct option. Admin writes also accept `SINGLE_ANSWER`, `MULTIPLE_ANSWER`, and `MULTIPLE_ANSWERS` as aliases, normalized to `SINGLE`/`MULTI`.

For full save, existing questions should send a real `passageId`. When creating a new passage and its questions in the same request, set `passages[].clientKey` and reference it from `questions[].clientPassageKey`; the response returns the generated real `passages[].id` and `questions[].passageId`.

Reading image multipart fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `images` | file[] | no | Existing images for that target are replaced by the uploaded files. Send an empty/missing array to clear images. |

Full save example:

```json
{
  "test": {
    "title": "Cambridge Reading Test 1",
    "totalScore": 40,
    "timerMode": "COUNTDOWN",
    "prepSeconds": 0,
    "totalMinutes": 60,
    "autoSubmit": 1,
    "allowPause": 1
  },
  "partGroups": [
    {
      "id": 101,
      "partNumber": 1,
      "groupNumber": 1,
      "title": "Passage 1",
      "instructionText": "Read the passage and answer questions 1-13.",
      "questionType": "TRUE_FALSE_NOT_GIVEN",
      "answerMode": "SINGLE",
      "questionNoStart": 1,
      "questionNoEnd": 13,
      "displayOrder": 1,
      "images": []
    }
  ],
  "passages": [
    {
      "id": 201,
      "partGroupId": 101,
      "passageNo": 1,
      "title": "The History of Tea",
      "content": "Passage content...",
      "materialType": "TEXT",
      "displayOrder": 1
    }
  ],
  "questions": [
    {
      "id": 3001,
      "passageId": 201,
      "partGroupId": 101,
      "questionNumber": 1,
      "questionType": "TRUE_FALSE_NOT_GIVEN",
      "answerMode": "SINGLE",
      "questionText": "Tea was first used as medicine.",
      "correctAnswer": "TRUE",
      "score": 1,
      "displayOrder": 1,
      "caseInsensitive": 1,
      "ignoreWhitespace": 1,
      "ignorePunctuation": 1,
      "answerRules": []
    }
  ]
}
```
## 8. Listening APIs

### User Listening

| Method | Path | Purpose | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/user/listening/tests` | list active tests | none | test list |
| `POST` | `/api/user/listening/tests/{testId}/start` | start session | path | `ListeningSessionVO` |
| `GET` | `/api/user/listening/sessions/{sessionId}` | get session | path | `ListeningSessionVO` |
| `POST` | `/api/user/listening/sessions/{sessionId}/pause` | pause | optional `ListeningSessionActionDTO` | session result |
| `POST` | `/api/user/listening/sessions/{sessionId}/resume` | resume | none | session result |
| `POST` | `/api/user/listening/tests/{testId}/submit` | submit answers | `ListeningSubmitDTO` | submit result |

User listening records use `/api/user/records`.

`ListeningSubmitDTO`:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `sessionId` | string | yes | current session id |
| `startedTime` | string | no | client start time |
| `timeSpentSeconds` | number | no | elapsed seconds |
| `autoSubmitted` | number | no | `1` timeout submit, `0` manual |
| `answers` | `ListeningAnswerDTO[]` | yes | answers |

`ListeningAnswerDTO`: `questionId`, `answer`, `answers`.

```json
{
  "sessionId": "listening-2-8001",
  "startedTime": "2026-05-15T10:00:00",
  "timeSpentSeconds": 2400,
  "autoSubmitted": 0,
  "answers": [
    { "questionId": 4001, "answer": "B" },
    { "questionId": 4002, "answers": ["12", "March"] }
  ]
}
```

`ListeningSessionActionDTO`: `clientTimeSpentSeconds`.

### Admin Listening

| Method | Path | Purpose | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/admin/listening/tests` | create test shell | `ListeningTestDTO` | test object/detail |
| `PUT` | `/api/admin/listening/tests/{id}` | update test shell | `ListeningTestDTO` | updated test |
| `PUT` | `/api/admin/listening/tests/{testId}/full` | save full content | `AdminListeningTestFullSaveDTO` | `ListeningTestDetailVO` |
| `GET` | `/api/admin/listening/tests` | list tests | query | test list |
| `GET` | `/api/admin/listening/tests/{testId}` | test detail | path | `ListeningTestDetailVO` |
| `DELETE` | `/api/admin/listening/tests/{id}` | delete test | path | empty success |
| `PUT` | `/api/admin/listening/tests/{id}/restore` | restore test | path | empty success |
| `POST` | `/api/admin/listening/tests/{testId}/audio` | upload test-level audio | multipart `file`, optional `title`, optional `transcriptText` | audio object |
| `GET` | `/api/admin/listening/tests/{testId}/audio` | get test audio | path | audio object/list |
| `PUT` | `/api/admin/listening/tests/{testId}/audio/{audioId}` | replace test-level audio | multipart `file`, optional `title`, optional `transcriptText` | audio object |
| `DELETE` | `/api/admin/listening/tests/{testId}/audio/{audioId}` | delete test audio | path | empty success |
| `POST` | `/api/admin/listening/tests/{testId}/part-groups/{partGroupId}/audio` | upload task/group audio | multipart `file`, optional `title`, optional `transcriptText` | audio object |
| `GET` | `/api/admin/listening/part-groups/{partGroupId}/audio` | list group audio | path | audio list |
| `PUT` | `/api/admin/listening/tests/{testId}/part-groups/{partGroupId}/audio/{audioId}` | replace task/group audio | multipart `file`, optional `title`, optional `transcriptText` | audio object |
| `DELETE` | `/api/admin/listening/tests/{testId}/part-groups/{partGroupId}/audio/{audioId}` | delete group audio | path | empty success |
| `POST` | `/api/admin/listening/part-groups/{partGroupId}/images` | replace group images | multipart `images` | image list |
| `POST` | `/api/admin/listening/questions/{questionId}/images` | replace question images | multipart `images` | image list |

`ListeningTestDTO`: `title`, `totalScore`, `timerMode`, `prepSeconds`, `totalMinutes`, `autoSubmit`, `allowPause`, `allowAudioSeek`. `prepMinutes` remains accepted as a legacy fallback when `prepSeconds` is omitted.

`AdminListeningTestFullSaveDTO`: `test`, `partGroups`, `questions`, `audios`.

Tape assignment:

- Use `/api/admin/listening/tests/{testId}/audio` when one tape applies to the whole listening test. The saved row uses `audioScope = "test"` and `partGroupId = null`.
- Use `/api/admin/listening/tests/{testId}/part-groups/{partGroupId}/audio` when one tape applies to one task/group. The saved row uses `audioScope = "part_group"` and stores the selected `partGroupId`.

Listening part groups use the same `TestPartGroup` shape as reading.

`ListeningQuestionDTO` fields: `id`, `testId`, `partGroupId`, `audio`, `sectionNumber`, `questionNumber`, `questionType`, `answerMode`, `questionText`, `correctAnswer`, `optionsJson`, `acceptedAnswersJson`, `caseInsensitive`, `ignoreWhitespace`, `ignorePunctuation`, `displayOrder`, `score`, `groupImages`, `images`. When `partGroupId` is provided, admin writes derive the persisted `sectionNumber` from that part group's `partNumber`; clients should not use `sectionNumber` to move a question between listening sections.

Listening/reading `acceptedAnswersJson` grading accepts either a JSON string array or a comma-separated text fallback for answer variants. During grading, `caseInsensitive = 1` ignores letter case and `ignoreWhitespace = 1` removes whitespace before comparison. For multiple-choice `SINGLE`, a flat array such as `["B","D"]` is treated as the required selected set; nested arrays can represent alternative required sets.

`ListeningAudioUpsertDTO`: `id`, `title`, `transcriptText`. In full save, `title` and `transcriptText` update the existing audio row only when the field is present/non-null; omitted fields keep the existing value.

Audio multipart fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `file` | file | yes | audio file |
| `title` | string | no | display title |
| `transcriptText` | string | no | manual transcript. If provided and non-blank, backend stores it directly and skips ASR generation; otherwise backend stores the audio first and queues ASR transcript generation after commit when available. |

Listening image multipart fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `images` | file[] | no | Existing images for that target are replaced by the uploaded files. Send an empty/missing array to clear images. |

Admin listening records use `/api/admin/records`.

## 9. Writing APIs

### User Writing

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/user/writing/questions` | USER | optional query `taskType` | `WritingQuestionVO[]` |
| `POST` | `/api/user/writing/questions/{questionId}/submit` | USER | multipart fields | `WritingRecordDetailVO` or submit result |

User writing records use `/api/user/records`.

Question list query:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `taskType` | string | no | Filter by writing task. Accepts `task1`/`TASK1`/`TASK_1` or `task2`/`TASK2`/`TASK_2`. |

Submit multipart fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `targetScore` | decimal | no | target score |
| `textContent` | string | no | typed answer |
| `images` | file[] | no | one or more images |
| `pdf` | file | no | single PDF |

Send at least one answer source: `textContent`, `images`, or `pdf`.

```ts
const form = new FormData();
form.append("targetScore", "7.0");
form.append("textContent", essayText);
await fetch(`/api/user/writing/questions/${questionId}/submit`, {
  method: "POST",
  headers: { Authorization: `Bearer ${token}` },
  body: form
});
```

### Admin Writing

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/admin/writing/questions` | ADMIN | `WritingQuestionDTO` | `WritingQuestionVO` |
| `GET` | `/api/admin/writing/questions` | ADMIN | query | list/page |
| `GET` | `/api/admin/writing/questions/{id}` | ADMIN | path | `WritingQuestionVO` |
| `PUT` | `/api/admin/writing/questions/{id}` | ADMIN | `WritingQuestionDTO` | `WritingQuestionVO` |
| `POST` | `/api/admin/writing/questions/{id}/images` | ADMIN | multipart `images` | image list |
| `DELETE` | `/api/admin/writing/questions/{id}` | ADMIN | path | empty success |
| `PUT` | `/api/admin/writing/questions/{id}/restore` | ADMIN | path | empty success |

`WritingQuestionDTO`:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `taskType` | string | yes | e.g. `TASK_1`, `TASK_2` |
| `chartType` | string | no | Task 1 chart type, e.g. `Line graph`, `Bar chart`, `Pie chart`, `Table`, `Map`, `Process diagram`, `Mixed charts`; `null` for Task 2 |
| `title` | string | yes | question title |
| `description` | string | no | prompt; blank input is accepted and stored as `null` |
| `imageDetailDescription` | string | no | AI-generated or manually supplied detailed image/task context used for writing scoring; image upload/replacement regenerates this from question images |
| `prepSeconds` | number | no | preparation time in seconds; preferred for admin create/update |
| `totalMinutes` | number | no | formal writing time in minutes |
| `prepMinutes` | number | no | legacy preparation minutes fallback when `prepSeconds` is omitted |
| `totalSeconds` | number | no | optional formal writing seconds override when provided |
| `images` | `BizImageResourceDTO[]` | no | question images |

```json
{
  "taskType": "TASK_1",
  "chartType": "Line graph",
  "title": "Monthly Library Visits",
  "description": "The line graph shows monthly visits to three city libraries in 2025. Summarise the information by selecting and reporting the main features.",
  "prepSeconds": 300,
  "totalMinutes": 20,
  "images": []
}
```

`WritingQuestionVO` returns the same question metadata plus `id`, `imageDetailDescription`, `imageUrl`, `imageObjectKey`, `images`, `prepSeconds`, `totalSeconds`, `prepMinutes`, `totalMinutes`, `createdTime`. `imageUrl` and `imageObjectKey` are derived from the primary `images` item, not legacy `writing_question` columns.
Client countdowns must use `prepSeconds` and `totalSeconds` as the source of truth. `prepMinutes` and `totalMinutes` are compatibility/display fields and can lose second-level precision.
`WritingRecordVO` and `WritingRecordDetailVO` include `chartType` beside `taskType` when they embed writing question metadata. `WritingRecordDetailVO` also includes `imageDetailDescription` and `previewAssets`.

Writing question image multipart fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `images` | file[] | no | Existing images for that writing question are replaced by the uploaded files. Send an empty/missing array to clear images. When images are present, backend regenerates `imageDetailDescription` for scoring. |

Admin writing records use `/api/admin/records`.

## 10. Speaking APIs

### User Speaking

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `GET` | `/api/user/speaking/questions` | USER | none | `SpeakingQuestion[]` |
| `POST` | `/api/user/speaking/start-exam` | USER | optional `StartExamRequestDTO` | `StartExamVO` |
| `POST` | `/api/user/speaking/next-question` | USER | `NextQuestionRequestDTO` | `NextQuestionVO` |
| `POST` | `/api/user/speaking/submit-answer` | USER | multipart fields | `SubmitAnswerVO` |
| `GET` | `/api/user/speaking/sessions/{sessionId}/summary` | USER | path | `SpeakingSessionSummaryVO` |
| `GET` | `/api/user/speaking/talks/{talkId}` | USER | path | `SpeakingTalkStatusVO` |
| `POST` | `/api/user/speaking/upload-audio` | USER | multipart fields | `UploadSpeakingAudioVO` |

User speaking records use `/api/user/records`.

`POST /api/user/speaking/next-question` synchronously creates a D-ID talk through `POST {did.baseUrl}/talks`. A 403 from this step usually indicates D-ID API key, presenter, voice, account permission, or external resource policy configuration rather than a frontend payload issue.

`StartExamRequestDTO`:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `examType` | string | no | defaults to full exam if omitted |
| `totalQuestions` | number | no | frontend can omit |

```json
{
  "examType": "FULL"
}
```

`StartExamVO` fields: `sessionId`, `examStatus`, `totalQuestions`, `openingCount`, `part1Count`, `part3Count`, `topicKeyForPart2And3`, `message`.

`NextQuestionRequestDTO`:

```json
{
  "sessionId": "sess-000123"
}
```

`NextQuestionVO` fields: `sessionId`, `questionId`, `part`, `stepType`, `topicKey`, `questionText`, `cueCard`, `displayScript`, `spokenScript`, `prepSeconds`, `answerSeconds`, `currentIndex`, `hasNext`, `talkId`, `examStatus`.

Submit answer multipart fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `sessionId` | string | yes | speaking session id |
| `questionId` | number | yes | current question id |
| `file` | file | yes | audio answer file |

`SubmitAnswerVO` fields: `recordId`, `sessionId`, `questionId`, `audioUrl`, `answerStatus`, `status`, `aiStatus`, `aiProvider`, `aiModel`, `message`.

Upload audio only multipart fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `file` | file | yes | audio file |
| `sessionId` | string | no | optional session id |
| `questionId` | number | no | optional question id |

### Admin Speaking

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/admin/speaking/questions` | ADMIN | speaking question object | question object |
| `GET` | `/api/admin/speaking/questions` | ADMIN | query | question list |
| `GET` | `/api/admin/speaking/questions/{id}` | ADMIN | path | question object |
| `PUT` | `/api/admin/speaking/questions/{id}` | ADMIN | speaking question object | question object |
| `DELETE` | `/api/admin/speaking/questions/{id}` | ADMIN | path | empty success |
| `PUT` | `/api/admin/speaking/questions/{id}/restore` | ADMIN | path | empty success |

Admin speaking records use `/api/admin/records`.
## 11. Review DTOs

### RecordReviewVO

| Field | Type | Notes |
| --- | --- | --- |
| `moduleType` | string | module value |
| `recordId` | number | record id |
| `userId` | number/null | owner id when available |
| `layoutType` | string | `EXAM_PAGE`, `WRITING_REVIEW`, `SPEAKING_SESSION_REVIEW` |
| `title` | string | review title |
| `score` | decimal/null | score |
| `scoreText` | string/null | display score |
| `status` | string/null | display status |
| `createdTime` | string/null | create time |
| `examPageReview` | object/null | reading/listening review |
| `writingReview` | object/null | writing review |
| `speakingSessionReview` | object/null | speaking review |

### ExamPageReviewVO

Fields: `testId`, `testTitle`, `totalScore`, `testAudio`, `allowAudioSeek`, `parts`, `partGroupAudios`, `questions`, `answers`, `questionReviews`.

`QuestionReviewVO` fields: `questionId`, `questionNumber`, `questionType`, `answerMode`, `questionText`, `optionsJson`, `userAnswer`, `correctAnswer`, `isCorrect`, `score`.

### WritingReviewVO

Fields: `questionId`, `questionTitle`, `questionDescription`, `imageDetailDescription`, `questionImageUrl`, `questionImages`, `previewAssets`, `taskType`, `inputType`, `answerText`, `answerSource`, `textContent`, `extractedText`, `answerPreview`, `attachmentCount`, `attachments`, `targetScore`, `aiScore`, `aiFeedback`, `aiStatus`, `aiProvider`, `aiModel`. Writing `aiFeedback` uses double-asterisk Markdown bold for key points, for example `**Cohesion**`, and separates paragraphs with a blank line.

### SpeakingSessionReviewVO

Fields: `sessionId`, `examStatus`, `totalQuestions`, `answeredCount`, `processingCount`, `scoredCount`, `failedCount`, `fluencyAndCoherence`, `lexicalResource`, `grammaticalRangeAndAccuracy`, `pronunciation`, `overallScore`, `feedback`, `conversations`.

`SpeakingConversationReviewVO` fields: `recordId`, `questionId`, `part`, `questionText`, `cueCard`, `audioUrl`, `transcript`, `fluencyAndCoherence`, `lexicalResource`, `grammaticalRangeAndAccuracy`, `pronunciation`, `overallScore`, `feedback`, `relevanceComment`, `qualityComment`, `answerStatus`, `aiStatus`, `aiProvider`, `aiModel`, `aiErrorMessage`, `isDeleted`, `deletedTime`, `createdTime`.

## 12. Console APIs

Console endpoints return deterministic dashboard data and use `snake_case` response fields.

| Method | Path | Role | Response |
| --- | --- | --- | --- |
| `GET` | `/api/admin/console` | ADMIN | `AdminConsoleVO` |
| `GET` | `/api/user/console` | USER | `UserConsoleVO` |

`AdminConsoleVO` top-level fields: `snapshot_id`, `snapshot_time`, `kpis`, `module_stats`, `user_stats`, `recent_issues`, `quick_links`, `leaderboards`, `charts`.

`UserConsoleVO` top-level fields: `snapshot_id`, `snapshot_time`, `profile`, `kpis`, `module_stats`, `recent_records`, `insights`, `charts`.

`UserConsoleVO.profile` fields include `user_id`, `email`, `username`, `last_login_time`, `consecutive_login_days`, `listening_target_score`, `reading_target_score`, `writing_target_score`, and `speaking_target_score`.

`UserConsoleVO.kpis` includes `overall_average_score` and the compatibility alias `overall_average`; both carry the same value.

`scoreRadar` chart includes both direct radar arrays (`indicators`, `values`) and generic chart fields (`dimension_key = module`, `y_key = average_score`, `rows`, `series`) so frontends can render it with either chart adapter.

## 13. Dashboard AI APIs

| Method | Path | Role | Request | Response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/smartielts/dashboard/user/ask` | USER | `DashboardAskRequest` | `DashboardAssistantResponse` |
| `POST` | `/api/smartielts/dashboard/admin/ask` | ADMIN | `DashboardAskRequest` | `DashboardAssistantResponse` |
| `GET` | `/api/smartielts/dashboard/user/executive_summary?time_range=30d&summary_cache_key=<page-session-id>` | USER | query | executive summary |
| `GET` | `/api/smartielts/dashboard/admin/executive_summary?time_range=30d&summary_cache_key=<page-session-id>` | ADMIN | query | executive summary |
| `GET` | `/api/smartielts/dashboard/user/preload?timeRange=30d` | USER | query | preload payload |
| `GET` | `/api/smartielts/dashboard/admin/preload?targetUserId=2&timeRange=30d` | ADMIN | query | preload payload |
| `POST` | `/api/smartielts/dashboard/user/ask-sse` | USER | `DashboardAskRequest` | `text/event-stream` |
| `POST` | `/api/smartielts/dashboard/admin/ask-sse` | ADMIN | `DashboardAskRequest` | `text/event-stream` |

Executive summary query params:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `time_range` | string | no | Defaults to `last30days`. |
| `target_user_id` | number | no | Not used by admin executive summary; the endpoint summarizes the global admin dashboard/admin console, not an admin account or target user's exam records. |
| `summary_cache_key` | string | no | Optional frontend page-session cache key. Executive summary cache is enabled by default; when this value is present it is included in the Redis cache scope so the frontend can keep a page-session-specific summary. Response `meta.summary_cache_enabled`, `meta.summary_cache_hit`, `meta.summary_cache_key`, and `meta.summary_cache_scope_key` indicate cache usage. |

`DashboardAskRequest`:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `query` | string | yes | user/admin question |
| `targetUserId` | number | admin optional | target user for admin |
| `context` | object | no | filters, e.g. `{ "timeRange": "30d" }` |
| `conversationHistory` | array | no | previous turns from the same frontend assistant conversation. The backend sends this to the AI for follow-up questions; each item uses `role`, `content`, optional `createdAt`, and optional `meta`. For second-round question follow-ups, keep the previous assistant response `data.objectRef` under `conversationHistory[].meta.data.objectRef` when the current request does not include `objectRef`. |
| `askScene` | string | no | frontend scene |
| `responseMode` | string | no | display mode hint |
| `objectRef` | `DashboardAskObjectRef` | no | object context |
| `preloadedPayload` | `DashboardAskPreloadedPayload` | no | preload data |
| `clientContext` | `DashboardAskClientContext` | no | frontend metadata |

`DashboardAskObjectRef`: `module`, `objectType`, `testId`, `passageId`, `questionId`, `recordId`, `questionNumber`, `sessionId`.

`DashboardAskConversationMessage`: `role` (`user` or `assistant`), `content`, optional `createdAt`, optional `meta`. `meta.objectRef`, `meta.data.objectRef`, or `meta.response.data.objectRef` can be used to carry the previous question/record context into the next turn.

`DashboardAskClientContext`: `pageName`, `route`, `tab`, `locale`, `clientTime`, `ext`.

`DashboardAskPreloadedPayload`: `snapshotId`, `snapshotTime`, `overview`, `progressSummary`, `recentRecords`, `moduleStats`, `recentQuestions`, `recentPassages`, `aggregates`, `learningContext`, `questionContext`, `availableScopes`, `preloadSource`.

```json
{
  "query": "請分析我最近 30 天 reading 表現",
  "targetUserId": 2,
  "context": { "timeRange": "30d" },
  "conversationHistory": [
    { "role": "user", "content": "Explain question 3 first." },
    { "role": "assistant", "content": "Question 3 is mainly about sentence positioning." }
  ],
  "askScene": "overview",
  "responseMode": "normal",
  "objectRef": {
    "module": "reading",
    "objectType": "record",
    "testId": 12,
    "passageId": 201,
    "questionId": 3001,
    "recordId": 7001,
    "questionNumber": 1,
    "sessionId": "reading-session-1"
  },
  "clientContext": {
    "pageName": "userOverview",
    "route": "/dashboard",
    "tab": "overview",
    "locale": "zh-Hant",
    "clientTime": "2026-05-15T10:00:00+08:00",
    "ext": {}
  }
}
```

`DashboardAssistantResponse`: `answer`, `data`, `suggestions`, `meta`. `suggestions` are returned in English so follow-up buttons remain consistent for IELTS practice prompts. When the backend carries context from `conversationHistory`, `meta.contextCarriedFromHistory = true` and `meta.objectRef` contains the recovered context.

SSE event sequence:

```text
event: start
event: loading
event: intentResolved
event: finalStart
event: answerDelta
event: finalEnd
event: result
event: done
```

`intentResolved` is emitted as soon as the agent finishes the first-stage decision. When the final answer is ready,
`finalStart` carries `clearPrevious: true`; frontend should clear the temporary first-stage display and then append
each `answerDelta.delta` until `finalEnd.done = true`. `result` still contains the full `DashboardAssistantResponse`
for state reconciliation and non-stream consumers.

Error event:

```text
event: error
data: {"code":0,"msg":"dashboard request failed","data":null}
```

Frontend must stop loading on either `done` or `error`.

## 14. Shared DTO Notes And Removed Endpoints

### BizImageResourceDTO

Used by reading/listening/writing image flows. Frontend should preserve backend-provided image resource objects when editing existing images. Do not invent storage object keys client-side unless an upload endpoint returned them.

### Removed Endpoints

Do not use these removed legacy routes:

```text
/api/user/reading/records/**
/api/user/listening/records/**
/api/user/writing/records/**
/api/user/speaking/records/**
/api/user/speaking/questions/{id}
/api/user/reading/tests/{testId}
/api/user/listening/tests/{testId}
/api/admin/reading/records/**
/api/admin/listening/records/**
/api/admin/writing/records/**
/api/admin/speaking/records/**
```

Use the unified user/admin record APIs documented above.

## 15. Frontend Implementation Checklist

1. Store `data.token`, `data.userId`, `data.role`, `data.tokenExpiresIn`, and `data.refreshAfterSeconds` after login/register/refresh.
2. Add `Authorization: Bearer <token>` to protected requests.
3. On `401`, clear auth state and route to login.
4. On `403`, show no-permission UI.
5. Use `GET /api/user/records` for the main cross-module record page.
6. Use `POST /api/user/records/overview` only when module-specific filters from `UserRecordPageQuery` are needed.
7. Use unified `/api/user/records/{moduleType}/{recordId}` for user record detail/delete/restore.
8. Do not call removed module-specific record endpoints.
9. Use `FormData` for profile picture, writing submit files, speaking audio, reading/listening images, and listening audio.
10. For dashboard SSE, stop loading on either `done` or `error` event.
11. If frontend implementation needs backend-owned data or behavior that this contract does not expose, report the missing backend contract explicitly instead of recreating business logic client-side.

## 16. Source Lookup Guide

| Concern | Source Path |
| --- | --- |
| Controllers | `src/main/java/com/andrew/smartielts/**/controller` |
| Request DTOs | `src/main/java/com/andrew/smartielts/**/domain/dto` |
| Query objects | `src/main/java/com/andrew/smartielts/**/domain/query` |
| Response VOs | `src/main/java/com/andrew/smartielts/**/domain/vo` |
| Unified user records | `src/main/java/com/andrew/smartielts/record` |
| Console payloads | `src/main/java/com/andrew/smartielts/console` |
| Security | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtUtil` |
| MyBatis XML | `src/main/resources/mapper` |
