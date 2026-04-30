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

    private final DashboardIntentExecutionFacade dashboardIntentExecutionFacade;

    @Qualifier("dashboardSseExecutor")
    private final Executor dashboardSseExecutor;

    @PostMapping(value = "/ask-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askSse(@RequestBody DashboardAskRequest request) {
        long startedAt = System.currentTimeMillis();
        Long requestUserId = getCurrentUserId();

        log.info("dashboard.ask.sse.start role=USER operatorUserId={} askScene={} query={}",
                requestUserId, request.getAskScene(), safeString(request.getQuery()));

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);

        emitter.onCompletion(() -> log.info("dashboard.ask.sse.complete role=USER operatorUserId={} elapsedMs={}",
                requestUserId, System.currentTimeMillis() - startedAt));

        emitter.onTimeout(() -> {
            log.warn("dashboard.ask.sse.timeout role=USER operatorUserId={} elapsedMs={}",
                    requestUserId, System.currentTimeMillis() - startedAt);
            emitter.complete();
        });

        emitter.onError(ex -> {
            log.error("dashboard.ask.sse.error role=USER operatorUserId={} elapsedMs={} message={}",
                    requestUserId, System.currentTimeMillis() - startedAt, ex.getMessage(), ex);
            emitter.completeWithError(ex);
        });

        CompletableFuture.runAsync(() -> {
            try {
                Long operatorUserId = getCurrentUserId();

                log.info("dashboard.ask.sse.async.begin role=USER operatorUserId={}", operatorUserId);

                sendEvent(emitter, "start", Map.of(
                        "message", "dashboard request started"
                ));

                sendEvent(emitter, "loading", Map.of(
                        "answer", buildInitialLoadingAnswer(request),
                        "loading", true,
                        "stage", "ANALYZING",
                        "meta", Map.of(
                                "role", "USER",
                                "askScene", safeString(request.getAskScene())
                        )
                ));

                log.info("dashboard.ask.sse.event.sent role=USER operatorUserId={} event=start/loading", operatorUserId);

                DashboardAssistantResponse response = dashboardIntentExecutionFacade
                        .ask("USER", operatorUserId, operatorUserId, request);

                log.info("dashboard.ask.sse.facade.done role=USER operatorUserId={} elapsedMs={} meta={}",
                        operatorUserId, System.currentTimeMillis() - startedAt, response.getMeta());

                sendEvent(emitter, "intentResolved", Map.of(
                        "message", "intent resolved",
                        "loading", true,
                        "displayAnswer", buildIntentResolvedLoadingAnswer(request, response),
                        "meta", safeMeta(response.getMeta())
                ));

                sendEvent(emitter, "result", Result.success(response));

                sendEvent(emitter, "done", Map.of(
                        "message", "completed",
                        "elapsedMs", System.currentTimeMillis() - startedAt
                ));

                log.info("dashboard.ask.sse.event.sent role=USER operatorUserId={} event=done elapsedMs={}",
                        operatorUserId, System.currentTimeMillis() - startedAt);

                emitter.complete();
            } catch (Exception e) {
                log.error("dashboard.ask.sse.async.failed role=USER operatorUserId={} elapsedMs={} message={}",
                        requestUserId, System.currentTimeMillis() - startedAt, e.getMessage(), e);
                try {
                    sendEvent(emitter, "error", Result.error(
                            e.getMessage() == null ? "dashboard request failed" : e.getMessage()
                    ));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(e);
            }
        }, dashboardSseExecutor);

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(eventName).data(data));
    }

    private Map<String, Object> safeMeta(Map<String, Object> meta) {
        return meta == null ? new LinkedHashMap<>() : new LinkedHashMap<>(meta);
    }

    private String buildInitialLoadingAnswer(DashboardAskRequest request) {
        String language = resolveLanguage(request);
        if ("en".equals(language)) {
            return "I’m checking your dashboard data and organizing the key points now.";
        }
        if ("zh-Hans".equals(language)) {
            return "我先帮你查看当前学习数据，正在整理重点，请稍等一下。";
        }
        return "我先幫你查看目前的學習數據，正在整理重點，請稍等一下。";
    }

    private String buildIntentResolvedLoadingAnswer(DashboardAskRequest request, DashboardAssistantResponse response) {
        String language = resolveLanguage(request);
        String answerMode = response != null && response.getMeta() != null
                ? stringValue(response.getMeta().get("answerMode"))
                : null;

        if ("en".equals(language)) {
            if ("FALL_BACK_SQL".equalsIgnoreCase(answerMode) || "AI_SQL_SUCCESS".equalsIgnoreCase(answerMode)) {
                return "I’m querying your recent learning records and preparing a more complete answer.";
            }
            return "I’ve understood your question and I’m putting the final answer together now.";
        }

        if ("zh-Hans".equals(language)) {
            if ("FALL_BACK_SQL".equalsIgnoreCase(answerMode) || "AI_SQL_SUCCESS".equalsIgnoreCase(answerMode)) {
                return "我正在查询你的相关学习记录，并整理更完整的分析。";
            }
            return "我已经理解你的问题，正在整理最终回复。";
        }

        if ("FALL_BACK_SQL".equalsIgnoreCase(answerMode) || "AI_SQL_SUCCESS".equalsIgnoreCase(answerMode)) {
            return "我正在查詢你的相關學習記錄，並整理更完整的分析。";
        }
        return "我已經理解你的問題，正在整理最終回覆。";
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

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    private String safeString(String text) {
        return text == null ? null : text.replaceAll("[\\r\\n]+", " ").trim();
    }
}