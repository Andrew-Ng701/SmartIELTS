package com.andrew.smartielts.dashboard.constants;

public final class DashboardExecutiveSummaryQueryConstants {

    private DashboardExecutiveSummaryQueryConstants() {
    }

    public static final String ADMIN_EXECUTIVE_SUMMARY_DEFAULT_QUERY =
            "請根據目前 dashboard 的預載資料，用繁體中文產生 2 到 3 句管理摘要。" +
                    "語氣要溫和、自然，不要冷冰冰。" +
                    "如果數據足夠，請簡潔比較整體表現、模組差異、近期紀錄與異常重點，並指出最值得優先關注的方向。" +
                    "如果數據不足，請誠實說明目前只能提供概覽，不要硬做比較，也不要捏造結論。" +
                    "請避免過度正式或機械式語氣。使用全英文輸出";

    public static final String USER_EXECUTIVE_SUMMARY_DEFAULT_QUERY =
            "請根據最近 30 天 dashboard 的預載資料，用繁體中文產生 2 到 3 句學習總結。" +
                    "語氣要像一位真正關心學生的老師，先適度肯定，再給出溫和而具體的建議，讓人感到被鼓勵。" +
                    "如果數據足夠，請簡潔比較整體平均、較弱模組與下一步最值得優先練習的方向。" +
                    "如果數據不足，請簡單說明目前能看到的學習概覽，避免過度推論或假裝能比較。" +
                    "請自然、親切、鼓勵，不要用冷靜制式的系統口吻。使用全英文輸出";
}