# SmartIELTS Backend

SmartIELTS Backend 是 SmartIELTS 系統的 Spring Boot 後端服務，負責身份驗證、權限控制、IELTS Reading / Listening / Writing / Speaking 業務流程、AI 評分與解釋、後台管理、Dashboard 查詢、檔案資源管理與資料持久化。

本倉庫只放後端完整代碼與後端相關文件。前端代碼請放在 `SmartIELTS-frontend`，整體專案總覽、架構說明與前後端連結請放在主倉庫 `SmartIELTS`。

## 技術棧

- Java 17
- Spring Boot 3.3.5
- Spring Security + JWT
- MyBatis
- MySQL
- Redis
- Knife4j / OpenAPI
- Aliyun OSS / OCR / DashScope
- D-ID Speaking avatar integration
- Maven Wrapper

## 主要功能

- Auth：註冊、登入、JWT refresh、登出、修改密碼
- User：個人資料、profile picture、IELTS target score
- Admin：使用者管理、考試內容管理、作答記錄管理
- Reading：試卷、passage、part group、題目、作答、批改與記錄
- Listening：試卷、audio、part group、題目、作答、批改與記錄
- Writing：題目、圖片描述、作答、附件、AI scoring
- Speaking：題目、session、talk、錄音、AI scoring、final evaluation
- Record：統一 record list / detail / review / delete / restore
- Console：固定統計概覽資料
- Dashboard：AI ask、SQL generation、executive summary、learning context
- Storage：透過 `biz_image_resource` 管理業務圖片與上傳資源

## 專案結構

```text
src/main/java/com/andrew/smartielts
  admin/          shared admin support
  auth/           authentication and JWT login flow
  common/         shared constants, result wrapper, storage, security helpers
  console/        deterministic admin/user overview data
  dashboard/      AI dashboard ask/query/answer services
  listening/      listening exam and record module
  reading/        reading exam and record module
  record/         unified user/admin record APIs
  speaking/       speaking exam, session, D-ID, and AI scoring module
  user/           user profile and admin user management
  writing/        writing exam, record, image, and AI scoring module

src/main/resources/mapper
  MyBatis XML mappers

docs/
  api/api-contract.md
  backend/backend-overview.md
  database-overview.md
  database-production-cleanup-outline.md

scripts/sql/
  database migration and seed scripts
```

## 環境需求

- JDK 17+
- MySQL 8+
- Redis 6+
- PowerShell 或相容 shell
- 可連線到需要的 Aliyun / D-ID 服務，若只跑單元測試可使用 mock 或空值設定

## 環境變數

應用設定位於 `src/main/resources/application.yml`，敏感資料請用環境變數提供，不要提交真實 secret。

最小本地啟動範例：

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/smartielts?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Hong_Kong"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET_KEY="replace-with-a-long-random-secret"
$env:REDIS_HOST="127.0.0.1"
$env:REDIS_PORT="6379"
```

如需完整功能，另需設定：

```powershell
$env:ALIYUN_OSS_ENDPOINT=""
$env:ALIYUN_OSS_REGION=""
$env:ALIYUN_OSS_ACCESS_KEY_ID=""
$env:ALIYUN_OSS_ACCESS_KEY_SECRET=""
$env:ALIYUN_OSS_BUCKET_WRITING_QUESTION=""
$env:ALIYUN_OSS_DOMAIN_WRITING_QUESTION=""
$env:ALIYUN_OSS_BUCKET_WRITING_RECORD=""
$env:ALIYUN_OSS_DOMAIN_WRITING_RECORD=""
$env:ALIYUN_OSS_BUCKET_LISTENING_AUDIO=""
$env:ALIYUN_OSS_DOMAIN_LISTENING_AUDIO=""
$env:ALIYUN_OSS_BUCKET_SPEAKING_AUDIO=""
$env:ALIYUN_OSS_DOMAIN_SPEAKING_AUDIO=""
$env:ALIYUN_OSS_BUCKET_QUESTION_GROUP_IMAGE=""
$env:ALIYUN_OSS_DOMAIN_QUESTION_GROUP_IMAGE=""
$env:ALIYUN_OSS_BUCKET_USER_PROFILE_PICTURE=""
$env:ALIYUN_OSS_DOMAIN_USER_PROFILE_PICTURE=""
$env:ALIYUN_AI_API_KEY=""
$env:DID_API_KEY=""
```

## 本地啟動

1. 建立 MySQL database。
2. 套用 `scripts/sql/` 內需要的 schema、migration 與 seed scripts。
3. 啟動 Redis。
4. 設定必要環境變數。
5. 啟動後端：

```powershell
.\mvnw.cmd spring-boot:run
```

預設服務位址：

```text
http://localhost:8080/api
```

Swagger / Knife4j 文件可在啟動後透過 Knife4j UI 查看；實際路徑依 Knife4j 版本與 servlet path 設定為準。

## 測試

執行全部測試：

```powershell
.\mvnw.cmd test
```

目前測試以 service/unit test 為主，涉及登入與權限的測試請依 `AGENTS.md` 的測試策略處理。

## API 文件

- 完整 API contract：`docs/api/api-contract.md`
- 後端模組與 service flow：`docs/backend/backend-overview.md`
- Database schema overview：`docs/database-overview.md`

登入成功後，前端需使用：

```http
Authorization: Bearer <token>
```

主要 base path：

```text
/api/auth/**
/api/user/**
/api/admin/**
```

## Database 與 Migration

資料表與 live schema 說明以 `docs/database-overview.md` 為準。Migration 與 seed scripts 位於：

```text
scripts/sql/
```

新增或調整資料表時，請同步檢查：

- MyBatis mapper XML
- POJO / DTO / VO
- `DbTableNames`
- Dashboard SQL allow-list 與 schema registry
- `docs/database-overview.md`
- 對應 migration script

## 部署

建置 jar：

```powershell
.\mvnw.cmd clean package
```

執行 jar：

```powershell
java -jar target\SmartIELTS-0.0.1-SNAPSHOT.jar
```

部署環境需準備：

- MySQL connection
- Redis connection
- JWT secret
- Aliyun OSS buckets and domains
- Aliyun AI / OCR credentials if AI and OCR features are enabled
- D-ID credentials if speaking talk flow is enabled
- HTTPS webhook URL for production speaking webhook integration

## 相關倉庫

- `SmartIELTS`：主專案總覽，不放實際前後端代碼
- `SmartIELTS-frontend`：前端完整代碼
- `SmartIELTS-backend`：本倉庫，後端完整代碼

## 維護規則

- 專案修改前先閱讀 `AGENTS.md`。
- API contract 變更時同步更新 `docs/api/api-contract.md`。
- 後端 package、service flow、module boundary 變更時同步更新 `docs/backend/backend-overview.md`。
- DB table、column、relationship、soft-delete、dashboard SQL allow-list 或 migration script 變更時同步更新 `docs/database-overview.md`。
- 不要提交真實 token、password、access key 或 production secret。
