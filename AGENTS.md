# Project Memory

## 語言與命名

- 優先跟隨現有專案風格。若某個 module 已有明確慣例，新程式碼應保持一致，除非該慣例明顯違反 Java 常見規範或會影響 API 相容性。
- Java class、interface、enum、annotation 使用 `UpperCamelCase`。
- Java method、parameter、local variable、一般 field、注入依賴、DTO/VO property 使用 `lowerCamelCase`。
- 共用的 Java 字串常量使用 `UPPER_SNAKE_CASE`，特別是 literal key、code、status、request name、cache name、SQL alias、API 相關 label。
- 非字串值依照標準 Java 判斷：
  - 真正的共用常量使用 `UPPER_SNAKE_CASE`，例如 `static final int MAX_RETRY_COUNT`。
  - runtime value、注入 collaborator、可變狀態、builder、collection、cache、由設定載入的 field，即使是 `final`，通常仍使用 `lowerCamelCase`。
- 不要只為了命名風格而修改公開 DTO/VO 欄位、request parameter、response field、資料庫 column 或 mapper alias。除非任務明確包含 migration，否則要保留外部 contract。

## 前後端職責

- 假設前端只會傳送後端完成動作所需的關鍵資料。
- 前端主要負責顯示、輸入收集、本地互動狀態與使用者體驗。業務規則、權限檢查、評分、持久化決策、跨 entity 推導應放在後端。
- 後端負責驗證輸入、執行權限規則、推導 server-owned value、維持 transaction consistency，並回傳足夠清晰的資料，讓前端不需要複製核心邏輯也能渲染。
- 編寫後端 API 時，目標是令前端容易接入：欄位明確、命名穩定、狀態值清楚，不要求前端推斷隱藏業務狀態。
- 編寫前端時，優先保證核心流程可用。不要為了少打一個 API 或掩蓋後端缺口，把後端應負責的邏輯搬到 client。

## 工程規則

- 優先使用專案既有 pattern、helper class、mapper style、service boundary、response shape，再考慮新增抽象。
- 修改範圍應貼近需求。避免大範圍 rename、無關格式化或順手重構。
- 行為改動應補上或更新聚焦的測試，特別是 service logic、API contract、permission check、mapper/query behavior。
- generated ID、timestamp、ownership、scoring、status transition 預設視為 backend-owned，除非現有設計明確不是如此。

## 文件安全

- 禁止批量刪除文件或目錄。
- 不要使用 `del /s`、`rd /s`、`rmdir /s`、`Remove-Item -Recurse` 或 `rm -rf`。
- 如果必須刪除文件，只能一次刪除一個明確路徑的文件，例如：

```powershell
Remove-Item "C:\path\to\file.txt"
```

- 如果看起來需要批量刪除，必須停止操作，詢問使用者並讓使用者手動處理或明確批准清理方案。
