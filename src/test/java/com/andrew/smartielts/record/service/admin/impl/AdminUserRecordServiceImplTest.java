package com.andrew.smartielts.record.service.admin.impl;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.listening.service.admin.AdminListeningService;
import com.andrew.smartielts.reading.service.admin.AdminReadingService;
import com.andrew.smartielts.record.domain.query.admin.AdminUserRecordListQuery;
import com.andrew.smartielts.record.domain.query.admin.AdminUserScopedRecordListQuery;
import com.andrew.smartielts.record.domain.vo.UserRecordDetailVO;
import com.andrew.smartielts.record.domain.vo.review.RecordReviewVO;
import com.andrew.smartielts.record.domain.vo.admin.AdminUserRecordListItemVO;
import com.andrew.smartielts.record.mapper.AdminUserRecordListMapper;
import com.andrew.smartielts.record.support.RecordReviewBuilder;
import com.andrew.smartielts.speaking.service.admin.AdminSpeakingService;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.writing.service.admin.AdminWritingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserRecordServiceImplTest {

    @Mock
    private AdminUserRecordListMapper adminUserRecordListMapper;

    @Mock
    private AdminReadingService adminReadingService;

    @Mock
    private AdminListeningService adminListeningService;

    @Mock
    private AdminWritingService adminWritingService;

    @Mock
    private AdminSpeakingService adminSpeakingService;

    @Test
    void listRecords_whenQueryNull_shouldUseDefaultActiveAllUsersAllModules() {
        AdminUserRecordServiceImpl service = service();
        AdminUserRecordListItemVO item = item(1L, 2L, "Reading Mock", "READING", "SUBMITTED");
        when(adminUserRecordListMapper.countRecords("ACTIVE", null, null, null)).thenReturn(1L);
        when(adminUserRecordListMapper.pageRecords("ACTIVE", null, null, null, "UPDATED_DESC", 0, 20))
                .thenReturn(List.of(item));

        PageResult<AdminUserRecordListItemVO> result = service.listRecords(null);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals(List.of(item), result.getList());
        verify(adminUserRecordListMapper).pageRecords("ACTIVE", null, null, null, "UPDATED_DESC", 0, 20);
    }

    @Test
    void listRecords_shouldNormalizeFiltersSortAndPaging() {
        AdminUserRecordServiceImpl service = service();
        AdminUserRecordListQuery query = new AdminUserRecordListQuery();
        query.setRecordState("deleted");
        query.setModule("writing");
        query.setStatus("failed");
        query.setUserId(9L);
        query.setSort("score_asc");
        query.setPageNum(2);
        query.setPageSize(300);
        when(adminUserRecordListMapper.countRecords("DELETED", "WRITING", "FAILED", 9L)).thenReturn(0L);
        when(adminUserRecordListMapper.pageRecords("DELETED", "WRITING", "FAILED", 9L, "SCORE_ASC", 100, 100))
                .thenReturn(List.of());

        PageResult<AdminUserRecordListItemVO> result = service.listRecords(query);

        assertEquals(0L, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals(100, result.getPageSize());
        verify(adminUserRecordListMapper).pageRecords("DELETED", "WRITING", "FAILED", 9L, "SCORE_ASC", 100, 100);
    }

    @Test
    void listRecordsForUser_shouldUsePathUserId() {
        AdminUserRecordServiceImpl service = service();
        AdminUserScopedRecordListQuery query = new AdminUserScopedRecordListQuery();
        query.setModule("reading");
        query.setSort("score_desc");
        when(adminUserRecordListMapper.countRecords("ACTIVE", "READING", null, 9L)).thenReturn(0L);
        when(adminUserRecordListMapper.pageRecords("ACTIVE", "READING", null, 9L, "SCORE_DESC", 0, 20))
                .thenReturn(List.of());

        PageResult<AdminUserRecordListItemVO> result = service.listRecordsForUser(9L, query);

        assertEquals(0L, result.getTotal());
        verify(adminUserRecordListMapper).pageRecords("ACTIVE", "READING", null, 9L, "SCORE_DESC", 0, 20);
    }

    @Test
    void listRecords_whenInvalidParams_shouldThrowClearErrors() {
        AdminUserRecordServiceImpl service = service();
        AdminUserRecordListQuery invalidModule = new AdminUserRecordListQuery();
        invalidModule.setModule("grammar");
        assertEquals("Unsupported moduleType: grammar",
                assertThrows(IllegalArgumentException.class, () -> service.listRecords(invalidModule)).getMessage());

        AdminUserRecordListQuery invalidStatus = new AdminUserRecordListQuery();
        invalidStatus.setStatus("done");
        assertEquals("Unsupported status: done",
                assertThrows(IllegalArgumentException.class, () -> service.listRecords(invalidStatus)).getMessage());

        AdminUserRecordListQuery invalidSort = new AdminUserRecordListQuery();
        invalidSort.setSort("unsafe_sql");
        assertEquals("Unsupported sort: unsafe_sql",
                assertThrows(IllegalArgumentException.class, () -> service.listRecords(invalidSort)).getMessage());

        AdminUserRecordListQuery invalidPage = new AdminUserRecordListQuery();
        invalidPage.setPageNum(0);
        assertEquals("pageNum must be greater than or equal to 1",
                assertThrows(IllegalArgumentException.class, () -> service.listRecords(invalidPage)).getMessage());
    }

    @Test
    void recordOperations_shouldDelegateByModule() {
        AdminUserRecordServiceImpl service = service();
        WritingRecordDetailVO writingDetail = new WritingRecordDetailVO();
        writingDetail.setRecordId(102L);
        writingDetail.setQuestionId(12L);
        writingDetail.setQuestionTitle("Writing Task");
        writingDetail.setQuestionDescription("Describe the chart.");
        writingDetail.setPrompt("Describe the chart.");
        when(adminWritingService.getRecord(102L)).thenReturn(writingDetail);

        service.deleteRecord("READING", 100L);
        service.restoreRecord("LISTENING", 101L);
        service.getRecord("WRITING", 102L);
        service.getRecord("SPEAKING", 103L);

        verify(adminReadingService).deleteRecord(100L);
        verify(adminListeningService).restoreRecord(101L);
        verify(adminWritingService).getRecord(102L);
        verify(adminSpeakingService).getRecord(103L);
    }

    @Test
    void getRecord_shouldReturnUnifiedDetailWithReviewForAdminReplay() {
        AdminUserRecordServiceImpl service = service();
        WritingRecordDetailVO detail = new WritingRecordDetailVO();
        detail.setRecordId(102L);
        detail.setQuestionId(12L);
        detail.setQuestionTitle("Writing Task");
        detail.setQuestionDescription("Describe the chart.");
        detail.setPrompt("Describe the chart.");
        when(adminWritingService.getRecord(102L)).thenReturn(detail);

        UserRecordDetailVO result = (UserRecordDetailVO) service.getRecord("WRITING", 102L);

        assertEquals("WRITING", result.getModuleType());
        assertEquals("WRITING_RECORD_DETAIL", result.getDetailType());
        assertEquals(detail, result.getDetail());
        RecordReviewVO review = (RecordReviewVO) result.getReview();
        assertEquals("Describe the chart.", review.getWritingReview().getPrompt());
    }

    private AdminUserRecordServiceImpl service() {
        return new AdminUserRecordServiceImpl(
                adminUserRecordListMapper,
                adminReadingService,
                adminListeningService,
                adminWritingService,
                adminSpeakingService,
                new RecordReviewBuilder()
        );
    }

    private AdminUserRecordListItemVO item(Long id, Long userId, String name, String module, String status) {
        AdminUserRecordListItemVO vo = new AdminUserRecordListItemVO();
        vo.setRecordId(id);
        vo.setUserId(userId);
        vo.setName(name);
        vo.setModule(module);
        vo.setStatus(status);
        vo.setUpdatedTime(LocalDateTime.now());
        vo.setCreatedTime(vo.getUpdatedTime());
        vo.setIsDeleted(0);
        return vo;
    }
}

