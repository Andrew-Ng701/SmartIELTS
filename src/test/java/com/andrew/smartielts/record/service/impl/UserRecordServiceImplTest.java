package com.andrew.smartielts.record.service.impl;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.listening.domain.vo.ListeningSectionScriptVO;
import com.andrew.smartielts.listening.service.user.UserListeningService;
import com.andrew.smartielts.record.domain.query.UserRecordListQuery;
import com.andrew.smartielts.record.domain.vo.UserRecordListItemVO;
import com.andrew.smartielts.record.mapper.UserRecordListMapper;
import com.andrew.smartielts.speaking.service.user.UserSpeakingService;
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
class UserRecordServiceImplTest {

    @Mock
    private UserListeningService userListeningService;

    @Mock
    private UserSpeakingService userSpeakingService;

    @Mock
    private UserRecordListMapper userRecordListMapper;

    @Test
    void listRecords_whenQueryNull_shouldUseDefaultActiveAllModules() {
        UserRecordServiceImpl service = newService();
        UserRecordListItemVO item = item(1L, "Reading Mock", "READING", "COMPLETED");
        when(userRecordListMapper.countUserRecords(2L, "ACTIVE", null, null)).thenReturn(1L);
        when(userRecordListMapper.pageUserRecords(2L, "ACTIVE", null, null, "UPDATED_DESC", 0, 20))
                .thenReturn(List.of(item));

        PageResult<UserRecordListItemVO> result = service.listRecords(2L, null);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals(List.of(item), result.getList());
        verify(userRecordListMapper).pageUserRecords(2L, "ACTIVE", null, null, "UPDATED_DESC", 0, 20);
    }

    @Test
    void listRecords_shouldNormalizeDeletedModuleStatusSortAndPaging() {
        UserRecordServiceImpl service = newService();
        UserRecordListQuery query = new UserRecordListQuery();
        query.setRecordState("deleted");
        query.setModule("writing");
        query.setStatus("deleted");
        query.setSort("name_asc");
        query.setPageNum(3);
        query.setPageSize(5);
        when(userRecordListMapper.countUserRecords(2L, "DELETED", "WRITING", "DELETED")).thenReturn(0L);
        when(userRecordListMapper.pageUserRecords(2L, "DELETED", "WRITING", "DELETED", "NAME_ASC", 10, 5))
                .thenReturn(List.of());

        PageResult<UserRecordListItemVO> result = service.listRecords(2L, query);

        assertEquals(0L, result.getTotal());
        assertEquals(3, result.getPageNum());
        assertEquals(5, result.getPageSize());
        verify(userRecordListMapper).pageUserRecords(2L, "DELETED", "WRITING", "DELETED", "NAME_ASC", 10, 5);
    }

    @Test
    void listRecords_whenStatusFailed_shouldPassUnifiedStatusFilter() {
        UserRecordServiceImpl service = newService();
        UserRecordListQuery query = new UserRecordListQuery();
        query.setStatus("failed");
        when(userRecordListMapper.countUserRecords(2L, "ACTIVE", null, "FAILED")).thenReturn(0L);
        when(userRecordListMapper.pageUserRecords(2L, "ACTIVE", null, "FAILED", "UPDATED_DESC", 0, 20))
                .thenReturn(List.of());

        service.listRecords(2L, query);

        verify(userRecordListMapper).pageUserRecords(2L, "ACTIVE", null, "FAILED", "UPDATED_DESC", 0, 20);
    }

    @Test
    void listRecords_whenScoreSort_shouldNormalizeAndPassSort() {
        UserRecordServiceImpl service = newService();
        UserRecordListQuery query = new UserRecordListQuery();
        query.setSort("score_desc");
        when(userRecordListMapper.countUserRecords(2L, "ACTIVE", null, null)).thenReturn(0L);
        when(userRecordListMapper.pageUserRecords(2L, "ACTIVE", null, null, "SCORE_DESC", 0, 20))
                .thenReturn(List.of());

        service.listRecords(2L, query);

        verify(userRecordListMapper).pageUserRecords(2L, "ACTIVE", null, null, "SCORE_DESC", 0, 20);
    }

    @Test
    void listRecords_whenInvalidParams_shouldThrowClearErrors() {
        UserRecordServiceImpl service = newService();
        UserRecordListQuery invalidModule = new UserRecordListQuery();
        invalidModule.setModule("grammar");
        assertEquals("Unsupported moduleType: grammar",
                assertThrows(IllegalArgumentException.class, () -> service.listRecords(2L, invalidModule)).getMessage());

        UserRecordListQuery invalidStatus = new UserRecordListQuery();
        invalidStatus.setStatus("done");
        assertEquals("Unsupported status: done",
                assertThrows(IllegalArgumentException.class, () -> service.listRecords(2L, invalidStatus)).getMessage());

        UserRecordListQuery invalidSort = new UserRecordListQuery();
        invalidSort.setSort("created_desc");
        assertEquals("Unsupported sort: created_desc",
                assertThrows(IllegalArgumentException.class, () -> service.listRecords(2L, invalidSort)).getMessage());

        UserRecordListQuery invalidPage = new UserRecordListQuery();
        invalidPage.setPageNum(0);
        assertEquals("pageNum must be greater than or equal to 1",
                assertThrows(IllegalArgumentException.class, () -> service.listRecords(2L, invalidPage)).getMessage());
    }

    @Test
    void getListeningSectionScript_shouldDelegateToListeningService() {
        UserRecordServiceImpl service = newService();
        ListeningSectionScriptVO expected = new ListeningSectionScriptVO();
        expected.setRecordId(10L);
        expected.setSectionNumber(2);
        when(userListeningService.getRecordSectionScript(10L, 2L, 2)).thenReturn(expected);

        ListeningSectionScriptVO result = service.getListeningSectionScript(2L, 10L, 2);

        assertEquals(expected, result);
        verify(userListeningService).getRecordSectionScript(10L, 2L, 2);
    }

    private UserRecordServiceImpl newService() {
        return new UserRecordServiceImpl(List.of(), userListeningService, userSpeakingService, userRecordListMapper);
    }

    private UserRecordListItemVO item(Long id, String name, String module, String status) {
        UserRecordListItemVO vo = new UserRecordListItemVO();
        vo.setRecordId(id);
        vo.setName(name);
        vo.setModule(module);
        vo.setStatus(status);
        vo.setUpdatedTime(LocalDateTime.now());
        vo.setCreatedTime(vo.getUpdatedTime());
        vo.setIsDeleted(0);
        return vo;
    }
}
