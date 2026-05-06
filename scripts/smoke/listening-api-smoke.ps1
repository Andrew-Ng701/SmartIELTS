param(
    [int]$Port = 18080,
    [switch]$SkipCompile
)

$ErrorActionPreference = "Stop"

$BaseUrl = "http://127.0.0.1:$Port/api"
$Script:Passed = 0
$Script:Skipped = 0
$Script:CreatedRecordId = $null
$Script:CreatedTestId = $null
$Script:CreatedTempPauseRecordId = $null

function Write-Pass {
    param([string]$Message)
    $Script:Passed++
    Write-Output "PASS $Message"
}

function Write-Skip {
    param([string]$Message)
    $Script:Skipped++
    Write-Output "SKIP $Message"
}

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )
    if (-not $Condition) {
        throw $Message
    }
}

function Ensure-Env {
    param(
        [string]$Name,
        [string]$Value
    )
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($Name))) {
        [Environment]::SetEnvironmentVariable($Name, $Value, "Process")
    }
}

function Import-DotEnv {
    param([string]$Path = ".env")
    if (-not (Test-Path $Path)) {
        throw ".env file not found at $Path"
    }

    foreach ($line in Get-Content -Path $Path) {
        $trimmed = $line.Trim()
        if ($trimmed.Length -eq 0 -or $trimmed.StartsWith("#") -or $trimmed -notmatch "=") {
            continue
        }
        $parts = $trimmed -split "=", 2
        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

function Map-DatasourceEnv {
    if ([string]::IsNullOrWhiteSpace($env:DB_URL) -and -not [string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_URL)) {
        [Environment]::SetEnvironmentVariable("DB_URL", $env:SPRING_DATASOURCE_URL, "Process")
    }
    if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME) -and -not [string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_USERNAME)) {
        [Environment]::SetEnvironmentVariable("DB_USERNAME", $env:SPRING_DATASOURCE_USERNAME, "Process")
    }
    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD) -and -not [string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_PASSWORD)) {
        [Environment]::SetEnvironmentVariable("DB_PASSWORD", $env:SPRING_DATASOURCE_PASSWORD, "Process")
    }
}

function Require-Env {
    param([string[]]$Names)
    $missing = @()
    foreach ($name in $Names) {
        if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
            $missing += $name
        }
    }
    if ($missing.Count -gt 0) {
        throw "Missing required env vars: $($missing -join ', ')"
    }
}

function Invoke-Mysql {
    param([string]$Sql)
    $mysqlArgs = @(
        "--host=127.0.0.1",
        "--port=3306",
        "--user=$env:DB_USERNAME",
        "--password=$env:DB_PASSWORD",
        "--database=smartielts",
        "--execute=$Sql"
    )
    & mysql @mysqlArgs | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "mysql command failed"
    }
}

function Invoke-MysqlScalar {
    param([string]$Sql)
    $mysqlArgs = @(
        "--host=127.0.0.1",
        "--port=3306",
        "--user=$env:DB_USERNAME",
        "--password=$env:DB_PASSWORD",
        "--database=smartielts",
        "--batch",
        "--raw",
        "--skip-column-names",
        "--execute=$Sql"
    )
    $value = & mysql @mysqlArgs
    if ($LASTEXITCODE -ne 0) {
        throw "mysql scalar command failed"
    }
    return (($value | Select-Object -First 1) | Out-String).Trim()
}

function Clear-SmokeData {
    if ($Script:CreatedRecordId) {
        Invoke-Mysql "DELETE FROM listening_answer_record WHERE record_id = $Script:CreatedRecordId; DELETE FROM listening_record WHERE id = $Script:CreatedRecordId;"
    }
    if ($Script:CreatedTempPauseRecordId) {
        Invoke-Mysql "DELETE FROM listening_answer_record WHERE record_id = $Script:CreatedTempPauseRecordId; DELETE FROM listening_record WHERE id = $Script:CreatedTempPauseRecordId;"
    }
    if ($Script:CreatedTestId) {
        Invoke-Mysql "DELETE ar FROM listening_answer_record ar JOIN listening_record r ON ar.record_id = r.id WHERE r.test_id = $Script:CreatedTestId; DELETE FROM listening_record WHERE test_id = $Script:CreatedTestId; DELETE FROM listening_question WHERE test_id = $Script:CreatedTestId; DELETE FROM listening_audio WHERE test_id = $Script:CreatedTestId; DELETE FROM listening_part_group WHERE test_id = $Script:CreatedTestId; DELETE FROM listening_test WHERE id = $Script:CreatedTestId;"
    }
    Invoke-Mysql "DELETE ar FROM listening_answer_record ar JOIN listening_record r ON ar.record_id = r.id WHERE r.user_id = 2 AND r.test_id = 1002 AND r.record_status = 'IN_PROGRESS'; DELETE FROM listening_record WHERE user_id = 2 AND test_id = 1002 AND record_status = 'IN_PROGRESS';"
}

function Invoke-MultipartApi {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token,
        [string]$FilePath,
        [string]$Title,
        [bool]$ExpectSuccess = $true
    )
    $uri = "$BaseUrl$Path"
    $responseText = & curl.exe -sS -X $Method `
        -H "Authorization: Bearer $Token" `
        -F "file=@$FilePath;type=audio/mpeg;filename=$(Split-Path -Leaf $FilePath)" `
        -F "title=$Title" `
        $uri
    if ($LASTEXITCODE -ne 0) {
        throw "curl multipart request failed: $Method $Path"
    }
    $response = $responseText | ConvertFrom-Json
    if ($ExpectSuccess -and $response.code -ne 1) {
        throw "$Method $Path failed: $($response.msg)"
    }
    if (-not $ExpectSuccess -and $response.code -eq 1) {
        throw "$Method $Path unexpectedly succeeded"
    }
    return $response
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [string]$Token,
        [object]$Body = $null,
        [bool]$ExpectSuccess = $true
    )
    $headers = @{ Authorization = "Bearer $Token" }
    $uri = "$BaseUrl$Path"
    if ($Body -ne $null) {
        $json = $Body | ConvertTo-Json -Depth 30
        $response = Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -ContentType "application/json;charset=utf-8" -Body $json -TimeoutSec 20
    } else {
        $response = Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -TimeoutSec 20
    }

    if ($ExpectSuccess -and $response.code -ne 1) {
        throw "$Method $Path failed: $($response.msg)"
    }
    if (-not $ExpectSuccess -and $response.code -eq 1) {
        throw "$Method $Path unexpectedly succeeded"
    }
    return $response
}

function Get-Groups {
    param([object]$Detail)
    $groups = @()
    foreach ($part in @($Detail.parts)) {
        foreach ($group in @($part.groups)) {
            $groups += $group
        }
    }
    return $groups
}

function Get-QuestionsFromGroups {
    param([object]$Detail)
    $questions = @()
    foreach ($group in Get-Groups $Detail) {
        foreach ($question in @($group.questions)) {
            $questions += $question
        }
    }
    return $questions
}

function Assert-FrontendReady {
    param([object]$Detail)
    $parts = @($Detail.parts)
    $groups = @(Get-Groups $Detail)
    $questions = @($Detail.questions)
    Assert-True ($parts.Count -ge 1) "detail has no parts"
    Assert-True ($groups.Count -ge 1) "detail has no groups"
    Assert-True ($questions.Count -ge 1) "detail has no questions"
    Assert-True (($Detail.testAudio -ne $null) -or (@($Detail.partGroupAudios).Count -ge 1)) "detail has no playable audio"

    foreach ($part in $parts) {
        Assert-True (@($part.groups).Count -ge 1) "part $($part.partNumber) has no groups"
    }

    foreach ($group in $groups) {
        $groupQuestions = @($group.questions)
        Assert-True ($groupQuestions.Count -ge 1) "group $($group.id) has no questions"
        if ($null -ne $group.questionNoStart) {
            $min = ($groupQuestions | Measure-Object -Property questionNumber -Minimum).Minimum
            Assert-True ([int]$group.questionNoStart -eq [int]$min) "group $($group.id) questionNoStart mismatch"
        }
        if ($null -ne $group.questionNoEnd) {
            $max = ($groupQuestions | Measure-Object -Property questionNumber -Maximum).Maximum
            Assert-True ([int]$group.questionNoEnd -eq [int]$max) "group $($group.id) questionNoEnd mismatch"
        }
        Assert-True (($group.PSObject.Properties.Name -notcontains "answerRulesJson") -or ($null -eq $group.answerRulesJson)) "user detail leaked group answerRulesJson"
        Assert-True (($group.PSObject.Properties.Name -notcontains "acceptedAnswersJson") -or ($null -eq $group.acceptedAnswersJson)) "user detail leaked group acceptedAnswersJson"
    }

    $numbers = @($questions | ForEach-Object { [int]$_.questionNumber })
    $sorted = @($numbers | Sort-Object)
    Assert-True (($numbers -join ",") -eq ($sorted -join ",")) "question numbers are not sorted"
    foreach ($question in $questions) {
        Assert-True (($question.PSObject.Properties.Name -notcontains "correctAnswer") -or ($null -eq $question.correctAnswer)) "user detail leaked correctAnswer"
        Assert-True (($question.PSObject.Properties.Name -notcontains "acceptedAnswersJson") -or ($null -eq $question.acceptedAnswersJson)) "user detail leaked acceptedAnswersJson"
    }
}

function New-Token {
    param(
        [long]$UserId,
        [string]$Role,
        [long]$TokenVersion,
        [string]$Classpath
    )
    $tokenSource = "target/SmokeToken.java"
    $tokenClasses = "target/smoke-classes"
    if (-not (Test-Path $tokenClasses)) {
        New-Item -ItemType Directory -Path $tokenClasses | Out-Null
    }
$tokenSourceContent = @"
import com.andrew.smartielts.utils.JwtUtil;

public class SmokeToken {
    public static void main(String[] args) {
        System.out.print(JwtUtil.createToken(Long.valueOf(args[0]), args[1], Long.valueOf(args[2]), args[3], 7200000L));
    }
}
"@
    [System.IO.File]::WriteAllText((Join-Path (Get-Location) $tokenSource), $tokenSourceContent, [System.Text.UTF8Encoding]::new($false))
    & javac -cp "target/classes;$Classpath" -d $tokenClasses $tokenSource
    if ($LASTEXITCODE -ne 0) {
        throw "javac SmokeToken failed"
    }
    $token = & java -cp "$tokenClasses;target/classes;$Classpath" SmokeToken $UserId $Role $TokenVersion $env:JWT_SECRET_KEY
    if ($LASTEXITCODE -ne 0) {
        throw "java SmokeToken failed"
    }
    return ($token | Out-String).Trim()
}

Import-DotEnv ".env"
Map-DatasourceEnv
Ensure-Env "SERVER_PORT" "$Port"
Require-Env @(
    "DB_URL",
    "DB_USERNAME",
    "DB_PASSWORD",
    "JWT_SECRET_KEY",
    "ALIYUN_OSS_ENDPOINT",
    "ALIYUN_OSS_REGION",
    "ALIYUN_OSS_ACCESS_KEY_ID",
    "ALIYUN_OSS_ACCESS_KEY_SECRET",
    "ALIYUN_OSS_BUCKET_WRITING_QUESTION",
    "ALIYUN_OSS_DOMAIN_WRITING_QUESTION",
    "ALIYUN_OSS_BUCKET_WRITING_RECORD",
    "ALIYUN_OSS_DOMAIN_WRITING_RECORD",
    "ALIYUN_OSS_BUCKET_LISTENING_AUDIO",
    "ALIYUN_OSS_DOMAIN_LISTENING_AUDIO",
    "ALIYUN_OSS_BUCKET_SPEAKING_AUDIO",
    "ALIYUN_OSS_DOMAIN_SPEAKING_AUDIO",
    "ALIYUN_OSS_BUCKET_QUESTION_GROUP_IMAGE",
    "ALIYUN_OSS_DOMAIN_QUESTION_GROUP_IMAGE"
)

$server = $null
try {
    Clear-SmokeData

    if (-not $SkipCompile) {
        & mvn -q -DskipTests compile
        if ($LASTEXITCODE -ne 0) {
            throw "mvn compile failed"
        }
        Write-Pass "mvn compile"
    }
    & mvn -q dependency:build-classpath "-Dmdep.outputFile=target/smoke-classpath.txt"
    if ($LASTEXITCODE -ne 0) {
        throw "mvn dependency build-classpath failed"
    }
    $classpath = (Get-Content -Path "target/smoke-classpath.txt" -Raw).Trim()
    $adminToken = New-Token 1 "ADMIN" 1 $classpath
    $userToken = New-Token 2 "USER" 1 $classpath

    $outLog = "target/listening-api-smoke.out.log"
    $errLog = "target/listening-api-smoke.err.log"
    $server = Start-Process -FilePath "mvn" -ArgumentList @("spring-boot:run", "-Dspring-boot.run.arguments=--server.port=$Port") -PassThru -WindowStyle Hidden -RedirectStandardOutput $outLog -RedirectStandardError $errLog

    $ready = $false
    for ($i = 0; $i -lt 90; $i++) {
        try {
            Invoke-Api "GET" "/user/listening/tests" $userToken | Out-Null
            $ready = $true
            break
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    if (-not $ready) {
        throw "Spring Boot did not become ready. See $outLog and $errLog"
    }
    Write-Pass "Spring Boot startup"

    $userList = Invoke-Api "GET" "/user/listening/tests" $userToken
    $tests = @($userList.data)
    Assert-True ($tests.Count -eq 5) "user list should expose 5 playable tests"
    Assert-True (-not ($tests | Where-Object { [long]$_.id -eq 1006 })) "user list exposed soft-deleted/empty test 1006"
    Write-Pass "USER list tests exposes playable tests only"

    foreach ($test in $tests) {
        $detail = Invoke-Api "GET" "/user/listening/tests/$($test.id)" $userToken
        Assert-FrontendReady $detail.data
        Assert-True (@($detail.data.parts).Count -ge 4) "test $($test.id) should have 4 seed parts"
    }
    Write-Pass "USER detail parts/groups/questions/audio and no answer leakage"

    Invoke-Api "GET" "/user/listening/tests/1006" $userToken $null $false | Out-Null
    Write-Pass "USER direct open unavailable test returns clear failure"

    Invoke-Api "POST" "/user/listening/records/overview" $userToken @{ pageNum = 1; pageSize = 5 } | Out-Null
    Write-Pass "USER record overview"

    $examDetail = Invoke-Api "GET" "/user/listening/tests/1002" $userToken
    $questions = @($examDetail.data.questions)
    $start = Invoke-Api "POST" "/user/listening/tests/1002/start" $userToken
    $Script:CreatedRecordId = [long]$start.data.recordId
    $sessionId = [string]$start.data.sessionId
    Invoke-Api "GET" "/user/listening/sessions/$sessionId" $userToken | Out-Null
    Assert-True ([int]$start.data.allowPause -eq 0) "seed test 1002 should keep real IELTS allowPause=0"
    $answers = @()
    foreach ($question in $questions) {
        $answers += @{ questionId = [long]$question.id; answer = "smoke" }
    }
    $submit = Invoke-Api "POST" "/user/listening/tests/1002/submit" $userToken @{ sessionId = $sessionId; timeSpentSeconds = 60; answers = $answers }
    Assert-True (@($submit.data.answers).Count -eq $questions.Count) "submitted answer count mismatch"
    Assert-True (@($submit.data.answers | Where-Object { $null -ne $_.correctAnswer }).Count -eq $questions.Count) "correct answers should appear after submit"
    $record = Invoke-Api "GET" "/user/listening/records/$Script:CreatedRecordId" $userToken
    Assert-True (@($record.data.answers).Count -eq $questions.Count) "record detail answer count mismatch"
    Invoke-Api "DELETE" "/user/listening/records/$Script:CreatedRecordId" $userToken | Out-Null
    Invoke-Api "POST" "/user/listening/records/deleted/overview" $userToken @{ pageNum = 1; pageSize = 10 } | Out-Null
    Invoke-Api "PUT" "/user/listening/records/$Script:CreatedRecordId/restore" $userToken | Out-Null
    Write-Pass "USER start/session/submit/record delete/restore flow"

    Invoke-Api "GET" "/admin/listening/tests" $adminToken | Out-Null
    $adminDetail = Invoke-Api "GET" "/admin/listening/tests/1001" $adminToken
    Assert-True (@($adminDetail.data.partGroups | Where-Object { $null -ne $_.answerRulesJson }).Count -ge 1) "admin detail should include group answer rules"
    Invoke-Api "GET" "/admin/listening/tests/1001/part-groups" $adminToken | Out-Null
    Invoke-Api "GET" "/admin/listening/tests/1002/audio" $adminToken | Out-Null
    Invoke-Api "GET" "/admin/listening/part-groups/1104/audio" $adminToken | Out-Null
    Invoke-Api "POST" "/admin/listening/records/overview" $adminToken @{ pageNum = 1; pageSize = 5 } | Out-Null
    Write-Pass "ADMIN read flow"

    $tempTitle = "SMOKE Listening Disposable $(Get-Date -Format yyyyMMddHHmmss)"
    $createdTest = Invoke-Api "POST" "/admin/listening/tests" $adminToken @{ title = $tempTitle; totalScore = 1; timerMode = "TEST_LEVEL"; totalSeconds = 600; autoSubmit = 1; allowPause = 1 }
    $Script:CreatedTestId = [long]$createdTest.data.id

    Invoke-Api "POST" "/admin/listening/tests/$Script:CreatedTestId/part-groups" $adminToken @{ partNumber = 0; groupNumber = 1; title = "Invalid"; questionType = "SHORT_ANSWER"; answerMode = "TEXT" } $false | Out-Null
    $group = Invoke-Api "POST" "/admin/listening/tests/$Script:CreatedTestId/part-groups" $adminToken @{ partNumber = 5; groupNumber = 1; title = "Task 5"; instructionText = "Smoke instruction"; groupGuideText = "Answer the question."; groupRequirementText = "ONE WORD ONLY"; questionType = "SHORT_ANSWER"; answerMode = "TEXT"; answerRulesJson = "[]"; questionNoStart = 1; questionNoEnd = 1; displayOrder = 5; timeLimitSeconds = 0 }
    $groupId = [long]$group.data.id

    Invoke-Api "POST" "/admin/listening/tests/$Script:CreatedTestId/questions" $adminToken @{ partGroupId = $groupId; sectionNumber = 5; questionNumber = 1; questionType = "FORMCOMPLETION"; answerMode = "TEXT"; questionText = "Unsupported type should fail"; displayOrder = 1; score = 1 } $false | Out-Null
    Invoke-Api "POST" "/admin/listening/tests/$Script:CreatedTestId/questions" $adminToken @{ partGroupId = $groupId; sectionNumber = 5; questionNumber = 1; questionType = "SHORT_ANSWER"; answerMode = "TEXT"; questionText = "Smoke question updated later"; displayOrder = 1; score = 1 } | Out-Null
    $tempDetail = Invoke-Api "GET" "/admin/listening/tests/$Script:CreatedTestId" $adminToken
    $createdQuestion = @($tempDetail.data.questions | Where-Object { [long]$_.partGroupId -eq $groupId -and [int]$_.questionNumber -eq 1 })[0]
    $questionId = [long]$createdQuestion.id
    $ruleJson = "[{""questionId"":$questionId,""questionNumber"":1,""answers"":[""alpha""]}]"
    Invoke-Api "PUT" "/admin/listening/part-groups/$groupId" $adminToken @{ partNumber = 5; groupNumber = 1; title = "Task 5 Updated"; instructionText = "Smoke instruction"; groupGuideText = "Answer the question."; groupRequirementText = "ONE WORD ONLY"; questionType = "SHORT_ANSWER"; answerMode = "TEXT"; answerRulesJson = $ruleJson; questionNoStart = 1; questionNoEnd = 1; displayOrder = 5; timeLimitSeconds = 0 } | Out-Null
    Invoke-Api "PUT" "/admin/listening/questions/$questionId" $adminToken @{ partGroupId = $groupId; sectionNumber = 5; questionNumber = 1; questionType = "SHORT_ANSWER"; answerMode = "TEXT"; questionText = "Smoke question after update"; displayOrder = 1; score = 1 } | Out-Null

    $pauseStart = Invoke-Api "POST" "/user/listening/tests/$Script:CreatedTestId/start" $userToken
    $Script:CreatedTempPauseRecordId = [long]$pauseStart.data.recordId
    Assert-True ([int]$pauseStart.data.allowPause -eq 1) "temporary allowPause test did not expose allowPause=1"
    $pauseSessionId = [string]$pauseStart.data.sessionId
    Invoke-Api "POST" "/user/listening/sessions/$pauseSessionId/pause" $userToken @{ clientTimeSpentSeconds = 3 } | Out-Null
    Invoke-Api "POST" "/user/listening/sessions/$pauseSessionId/resume" $userToken | Out-Null
    Write-Pass "USER pause/resume on temporary allowPause test"

    $fixturePath = "target/smoke-listening.mp3"
    $fixtureBytes = [byte[]](0x49,0x44,0x33,0x03,0x00,0x00,0x00,0x00,0x00,0x21,0x54,0x49,0x54,0x32,0x00,0x00,0x00,0x0B,0x00,0x00,0x03,0x53,0x6D,0x6F,0x6B,0x65,0x20,0x41,0x75,0x64,0x69,0x6F,0xFF,0xFB,0x90,0x64,0x00,0x00,0x00,0x00)
    [System.IO.File]::WriteAllBytes((Join-Path (Get-Location) $fixturePath), $fixtureBytes)

    $testAudio = Invoke-MultipartApi "POST" "/admin/listening/tests/$Script:CreatedTestId/audio" $adminToken $fixturePath "Smoke Test Audio"
    Assert-True ([long]$testAudio.data.testId -eq $Script:CreatedTestId) "test audio testId mismatch"
    Assert-True ($testAudio.data.partGroupId -eq $null) "test audio should not have partGroupId"
    Assert-True ([string]$testAudio.data.audioScope -eq "test") "test audio scope mismatch"
    Assert-True (-not [string]::IsNullOrWhiteSpace($testAudio.data.audioUrl)) "test audioUrl missing"
    Assert-True (-not [string]::IsNullOrWhiteSpace($testAudio.data.audioObjectKey)) "test audioObjectKey missing"
    $testAudioId = [long]$testAudio.data.id
    $updatedTestAudio = Invoke-MultipartApi "PUT" "/admin/listening/tests/$Script:CreatedTestId/audio/$testAudioId" $adminToken $fixturePath "Smoke Test Audio Updated"
    Assert-True ([long]$updatedTestAudio.data.id -eq $testAudioId) "updated test audio id mismatch"
    Invoke-Api "DELETE" "/admin/listening/tests/$Script:CreatedTestId/audio/$testAudioId" $adminToken | Out-Null

    $groupAudio = Invoke-MultipartApi "POST" "/admin/listening/tests/$Script:CreatedTestId/part-groups/$groupId/audio" $adminToken $fixturePath "Smoke Group Audio"
    Assert-True ([long]$groupAudio.data.testId -eq $Script:CreatedTestId) "group audio testId mismatch"
    Assert-True ([long]$groupAudio.data.partGroupId -eq $groupId) "group audio partGroupId mismatch"
    Assert-True ([string]$groupAudio.data.audioScope -eq "part_group") "group audio scope mismatch"
    Assert-True (-not [string]::IsNullOrWhiteSpace($groupAudio.data.audioUrl)) "group audioUrl missing"
    Assert-True (-not [string]::IsNullOrWhiteSpace($groupAudio.data.audioObjectKey)) "group audioObjectKey missing"
    $groupAudioId = [long]$groupAudio.data.id
    $updatedGroupAudio = Invoke-MultipartApi "PUT" "/admin/listening/tests/$Script:CreatedTestId/part-groups/$groupId/audio/$groupAudioId" $adminToken $fixturePath "Smoke Group Audio Updated"
    Assert-True ([long]$updatedGroupAudio.data.id -eq $groupAudioId) "updated group audio id mismatch"
    Invoke-Api "DELETE" "/admin/listening/tests/$Script:CreatedTestId/part-groups/$groupId/audio/$groupAudioId" $adminToken | Out-Null
    $activeTempAudioCount = Invoke-MysqlScalar "SELECT COUNT(*) FROM listening_audio WHERE test_id = $Script:CreatedTestId;"
    Assert-True ([int]$activeTempAudioCount -eq 0) "temporary audio rows should be deleted"
    Write-Pass "ADMIN real OSS audio upload/update/delete flow"

    Invoke-Api "DELETE" "/admin/listening/questions/$questionId" $adminToken | Out-Null
    Invoke-Api "PUT" "/admin/listening/questions/$questionId/restore" $adminToken | Out-Null
    Invoke-Api "DELETE" "/admin/listening/part-groups/$groupId" $adminToken | Out-Null
    Invoke-Api "PUT" "/admin/listening/part-groups/$groupId/restore" $adminToken | Out-Null
    Invoke-Api "DELETE" "/admin/listening/tests/$Script:CreatedTestId" $adminToken | Out-Null
    Write-Pass "ADMIN create/update/delete/restore and negative validation flow"

    Write-Output "Listening API smoke completed: $Script:Passed passed, $Script:Skipped skipped."
} finally {
    if ($server -and -not $server.HasExited) {
        & taskkill /PID $server.Id /T /F | Out-Null
    }
    Clear-SmokeData
}
