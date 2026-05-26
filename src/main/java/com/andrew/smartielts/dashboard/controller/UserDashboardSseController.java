package com.andrew.smartielts.dashboard.controller;

import com.andrew.smartielts.common.resultDTO.Result;
import com.andrew.smartielts.dashboard.agent.DashboardIntentExecutionFacade;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskClientContext;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAskRequest;
import com.andrew.smartielts.dashboard.controller.dto.DashboardAssistantResponse;
import com.andrew.smartielts.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/smartielts/dashboard/user")
@RequiredArgsConstructor
@Slf4j
public class UserDashboardSseController {

    private static final long SSE_TIMEOUT_MILLIS = 120000L;
    private static final int ANSWER_CHUNK_CODE_POINTS = 24;

    private final DashboardIntentExecutionFacade dashboardIntentExecutionFacade;

    @Qualifier("dashboardSseExecutor")
    private final Executor dashboardSseExecutor;

    @PostMapping(value = "/ask-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askSse(@RequestBody DashboardAskRequest request) {
        DashboardAskRequest safeRequest = request == null ? new DashboardAskRequest() : request;
        long startedAt = System.currentTimeMillis();

        Long operatorUserId = getCurrentUserId();
        log.info("dashboard.ask.sse.start role=USER operatorUserId={} askScene={} query={}",
                operatorUserId, safeString(safeRequest.getAskScene()), safeString(safeRequest.getQuery()));

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);

        emitter.onCompletion(() ->
                log.info("dashboard.ask.sse.complete role=USER operatorUserId={} elapsedMs={}",
                        operatorUserId, System.currentTimeMillis() - startedAt));

        emitter.onTimeout(() -> {
            log.warn("dashboard.ask.sse.timeout role=USER operatorUserId={} elapsedMs={}",
                    operatorUserId, System.currentTimeMillis() - startedAt);
            safeComplete(emitter);
        });

        emitter.onError(ex ->
                log.error("dashboard.ask.sse.error role=USER operatorUserId={} elapsedMs={} message={}",
                        operatorUserId, System.currentTimeMillis() - startedAt,
                        ex == null ? null : ex.getMessage(), ex));

        CompletableFuture.runAsync(
                () -> executeUserSse(emitter, safeRequest, operatorUserId, startedAt),
                dashboardSseExecutor
        );

        return emitter;
    }

    private void executeUserSse(SseEmitter emitter, DashboardAskRequest request, Long operatorUserId, long startedAt) {
        try {
            log.info("dashboard.ask.sse.async.begin role=USER operatorUserId={}", operatorUserId);

            sendEvent(emitter, "start", simplePayload("message", "dashboard request started"));

            Map<String, Object> loadingPayload = new LinkedHashMap<>();
            loadingPayload.put("answer", buildInitialLoadingAnswer(request));
            loadingPayload.put("loading", true);
            loadingPayload.put("stage", "ANALYZING");
            loadingPayload.put("meta", buildUserLoadingMeta(request));
            sendEvent(emitter, "loading", loadingPayload);

            DashboardAssistantResponse response =
                    dashboardIntentExecutionFacade.ask(
                            "USER",
                            operatorUserId,
                            operatorUserId,
                            request,
                            (displayAnswer, meta) -> sendIntentResolvedEvent(emitter, displayAnswer, meta)
                    );

            log.info("dashboard.ask.sse.facade.done role=USER operatorUserId={} elapsedMs={} meta={}",
                    operatorUserId, System.currentTimeMillis() - startedAt,
                    response == null ? null : response.getMeta());

            streamFinalAnswer(emitter, response);
            sendEvent(emitter, "result", Result.success(response));

            Map<String, Object> donePayload = new LinkedHashMap<>();
            donePayload.put("message", "completed");
            donePayload.put("elapsedMs", System.currentTimeMillis() - startedAt);
            sendEvent(emitter, "done", donePayload);

        } catch (Exception e) {
            log.error("dashboard.ask.sse.async.failed role=USER operatorUserId={} elapsedMs={} message={}",
                    operatorUserId, System.currentTimeMillis() - startedAt, e.getMessage(), e);
            safeSendErrorEvent(emitter, e, startedAt);
        } finally {
            safeComplete(emitter);
        }
    }

    private void safeSendErrorEvent(SseEmitter emitter, Exception e, long startedAt) {
        try {
            String message = e == null || e.getMessage() == null || e.getMessage().isBlank()
                    ? "dashboard request failed"
                    : e.getMessage();

            sendEvent(emitter, "error", Result.error(message));

            Map<String, Object> donePayload = new LinkedHashMap<>();
            donePayload.put("message", "failed");
            donePayload.put("elapsedMs", System.currentTimeMillis() - startedAt);
            sendEvent(emitter, "done", donePayload);
        } catch (Exception sendEx) {
            log.warn("dashboard.ask.sse.error.event.failed message={}", sendEx.getMessage(), sendEx);
        }
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(eventName).data(data));
    }

    private void sendIntentResolvedEvent(SseEmitter emitter, String displayAnswer, Map<String, Object> meta) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("message", "intent resolved");
            payload.put("loading", true);
            payload.put("displayAnswer", displayAnswer == null || displayAnswer.isBlank()
                    ? "我已理解你的問題，正在整理最終回覆。"
                    : displayAnswer.trim());
            payload.put("stage", "DECISION_RESOLVED");
            payload.put("meta", safeMeta(meta));
            sendEvent(emitter, "intentResolved", payload);
        } catch (IOException e) {
            throw new IllegalStateException("failed to send dashboard intentResolved event", e);
        }
    }

    private void streamFinalAnswer(SseEmitter emitter, DashboardAssistantResponse response) throws IOException {
        String answer = response == null ? null : response.getAnswer();
        if (answer == null || answer.isBlank()) {
            return;
        }

        Map<String, Object> startPayload = new LinkedHashMap<>();
        startPayload.put("stage", "FINAL_STREAMING");
        startPayload.put("clearPrevious", true);
        startPayload.put("answer", "");
        startPayload.put("meta", safeMeta(response.getMeta()));
        sendEvent(emitter, "finalStart", startPayload);

        int index = 0;
        int offset = 0;
        while (offset < answer.length()) {
            int nextOffset = nextChunkOffset(answer, offset);
            Map<String, Object> deltaPayload = new LinkedHashMap<>();
            deltaPayload.put("index", index++);
            deltaPayload.put("delta", answer.substring(offset, nextOffset));
            deltaPayload.put("done", false);
            sendEvent(emitter, "answerDelta", deltaPayload);
            offset = nextOffset;
        }

        Map<String, Object> endPayload = new LinkedHashMap<>();
        endPayload.put("stage", "FINAL_STREAMING");
        endPayload.put("done", true);
        sendEvent(emitter, "finalEnd", endPayload);
    }

    private int nextChunkOffset(String text, int offset) {
        int remainingCodePoints = text.codePointCount(offset, text.length());
        int chunkCodePoints = Math.min(ANSWER_CHUNK_CODE_POINTS, remainingCodePoints);
        return text.offsetByCodePoints(offset, chunkCodePoints);
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ex) {
            log.debug("dashboard.ask.sse.complete.ignore message={}", ex.getMessage());
        }
    }

    private Map<String, Object> safeMeta(Map<String, Object> meta) {
        return meta == null ? new LinkedHashMap<>() : new LinkedHashMap<>(meta);
    }

    private Map<String, Object> buildUserLoadingMeta(DashboardAskRequest request) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("role", "USER");
        putIfNotBlank(meta, "askScene", safeString(request == null ? null : request.getAskScene()));
        return meta;
    }

    private Map<String, Object> simplePayload(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    private String buildInitialLoadingAnswer(DashboardAskRequest request) {
        String language = resolveLanguage(request);
        if ("en".equals(language)) {
            return "I'm checking your dashboard data and organizing the key points now.";
        }
        if ("zh-Hans".equals(language)) {
            return "我正在查看你的仪表板数据，并先整理关键脉络。";
        }
        return "我正在查看你的儀表板資料，並先整理關鍵脈絡。";
    }

    private String resolveLanguage(DashboardAskRequest request) {
        DashboardAskClientContext clientContext = request == null ? null : request.getClientContext();
        String locale = clientContext == null ? null : clientContext.getLocale();
        if (locale == null || locale.isBlank()) {
            return detectLanguageByQuery(request == null ? null : request.getQuery());
        }
        String normalized = locale.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("zh-hans") || normalized.startsWith("zh-cn") || normalized.startsWith("zh-sg")) {
            return "zh-Hans";
        }
        if (normalized.startsWith("zh")) {
            return "zh-Hant";
        }
        return "en";
    }

    private String detectLanguageByQuery(String query) {
        if (query == null || query.isBlank()) {
            return "zh-Hant";
        }
        for (char ch : query.toCharArray()) {
            if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN) {
                return "zh-Hant";
            }
        }
        return "en";
    }

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    private String safeString(String text) {
        return text == null ? null : text.replaceAll("\\s+", " ").trim();
    }
}
