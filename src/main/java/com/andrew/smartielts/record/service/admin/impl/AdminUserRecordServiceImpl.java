package com.andrew.smartielts.record.service.admin.impl;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.listening.service.admin.AdminListeningService;
import com.andrew.smartielts.reading.service.admin.AdminReadingService;
import com.andrew.smartielts.record.constants.UserRecordDetailTypeConstants;
import com.andrew.smartielts.record.constants.UserRecordListSortConstants;
import com.andrew.smartielts.record.constants.UserRecordListStatusConstants;
import com.andrew.smartielts.record.constants.UserRecordModuleConstants;
import com.andrew.smartielts.record.constants.UserRecordStateConstants;
import com.andrew.smartielts.record.domain.query.admin.AdminUserRecordListQuery;
import com.andrew.smartielts.record.domain.query.admin.AdminUserScopedRecordListQuery;
import com.andrew.smartielts.record.domain.vo.UserRecordDetailVO;
import com.andrew.smartielts.record.domain.vo.admin.AdminUserRecordListItemVO;
import com.andrew.smartielts.record.mapper.AdminUserRecordListMapper;
import com.andrew.smartielts.record.service.admin.AdminUserRecordService;
import com.andrew.smartielts.record.support.RecordReviewBuilder;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordDetailVO;
import com.andrew.smartielts.listening.domain.vo.ListeningRecordDetailVO;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordDetailVO;
import com.andrew.smartielts.speaking.service.admin.AdminSpeakingService;
import com.andrew.smartielts.writing.domain.vo.WritingRecordDetailVO;
import com.andrew.smartielts.writing.service.admin.AdminWritingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserRecordServiceImpl implements AdminUserRecordService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminUserRecordListMapper adminUserRecordListMapper;
    private final AdminReadingService adminReadingService;
    private final AdminListeningService adminListeningService;
    private final AdminWritingService adminWritingService;
    private final AdminSpeakingService adminSpeakingService;
    private final RecordReviewBuilder recordReviewBuilder;

    public AdminUserRecordServiceImpl(AdminUserRecordListMapper adminUserRecordListMapper,
                                      AdminReadingService adminReadingService,
                                      AdminListeningService adminListeningService,
                                      AdminWritingService adminWritingService,
                                      AdminSpeakingService adminSpeakingService,
                                      RecordReviewBuilder recordReviewBuilder) {
        this.adminUserRecordListMapper = adminUserRecordListMapper;
        this.adminReadingService = adminReadingService;
        this.adminListeningService = adminListeningService;
        this.adminWritingService = adminWritingService;
        this.adminSpeakingService = adminSpeakingService;
        this.recordReviewBuilder = recordReviewBuilder;
    }

    @Override
    public PageResult<AdminUserRecordListItemVO> listRecords(AdminUserRecordListQuery query) {
        AdminUserRecordListQuery safeQuery = query == null ? new AdminUserRecordListQuery() : query;
        return doListRecords(
                safeQuery.getUserId(),
                safeQuery.getRecordState(),
                safeQuery.getModule(),
                safeQuery.getStatus(),
                safeQuery.getSort(),
                safeQuery.getPageNum(),
                safeQuery.getPageSize()
        );
    }

    @Override
    public PageResult<AdminUserRecordListItemVO> listRecordsForUser(Long userId, AdminUserScopedRecordListQuery query) {
        validateUserId(userId);
        AdminUserScopedRecordListQuery safeQuery = query == null ? new AdminUserScopedRecordListQuery() : query;
        return doListRecords(
                userId,
                safeQuery.getRecordState(),
                safeQuery.getModule(),
                safeQuery.getStatus(),
                safeQuery.getSort(),
                safeQuery.getPageNum(),
                safeQuery.getPageSize()
        );
    }

    @Override
    public Object getRecord(String module, Long recordId) {
        validateRecordId(recordId);
        String normalizedModule = UserRecordModuleConstants.normalize(module);
        return switch (normalizedModule) {
            case UserRecordModuleConstants.READING -> wrapReading(recordId, adminReadingService.getRecord(recordId));
            case UserRecordModuleConstants.LISTENING -> wrapListening(recordId, adminListeningService.getRecord(recordId));
            case UserRecordModuleConstants.WRITING -> wrapWriting(recordId, adminWritingService.getRecord(recordId));
            case UserRecordModuleConstants.SPEAKING -> wrapSpeaking(recordId, adminSpeakingService.getRecord(recordId));
            default -> throw new IllegalArgumentException("Unsupported moduleType: " + module);
        };
    }

    private UserRecordDetailVO wrapReading(Long recordId, ReadingRecordDetailVO detail) {
        UserRecordDetailVO vo = baseDetail(UserRecordModuleConstants.READING, recordId, UserRecordDetailTypeConstants.READING_RECORD_DETAIL);
        vo.setDetail(detail);
        vo.setReview(recordReviewBuilder.buildReading(null, detail));
        return vo;
    }

    private UserRecordDetailVO wrapListening(Long recordId, ListeningRecordDetailVO detail) {
        UserRecordDetailVO vo = baseDetail(UserRecordModuleConstants.LISTENING, recordId, UserRecordDetailTypeConstants.LISTENING_RECORD_DETAIL);
        vo.setDetail(detail);
        vo.setReview(recordReviewBuilder.buildListening(null, detail));
        return vo;
    }

    private UserRecordDetailVO wrapWriting(Long recordId, WritingRecordDetailVO detail) {
        UserRecordDetailVO vo = baseDetail(UserRecordModuleConstants.WRITING, recordId, UserRecordDetailTypeConstants.WRITING_RECORD_DETAIL);
        vo.setDetail(detail);
        vo.setReview(recordReviewBuilder.buildWriting(null, detail));
        return vo;
    }

    private UserRecordDetailVO wrapSpeaking(Long recordId, SpeakingRecordDetailVO detail) {
        UserRecordDetailVO vo = baseDetail(UserRecordModuleConstants.SPEAKING, recordId, UserRecordDetailTypeConstants.SPEAKING_RECORD_DETAIL);
        vo.setDetail(detail);
        return vo;
    }

    private UserRecordDetailVO baseDetail(String moduleType, Long recordId, String detailType) {
        UserRecordDetailVO vo = new UserRecordDetailVO();
        vo.setModuleType(moduleType);
        vo.setRecordId(recordId);
        vo.setDetailType(detailType);
        return vo;
    }

    @Override
    public void deleteRecord(String module, Long recordId) {
        validateRecordId(recordId);
        switch (UserRecordModuleConstants.normalize(module)) {
            case UserRecordModuleConstants.READING -> adminReadingService.deleteRecord(recordId);
            case UserRecordModuleConstants.LISTENING -> adminListeningService.deleteRecord(recordId);
            case UserRecordModuleConstants.WRITING -> adminWritingService.deleteRecord(recordId);
            case UserRecordModuleConstants.SPEAKING -> adminSpeakingService.deleteRecord(recordId);
            default -> throw new IllegalArgumentException("Unsupported moduleType: " + module);
        }
    }

    @Override
    public void restoreRecord(String module, Long recordId) {
        validateRecordId(recordId);
        switch (UserRecordModuleConstants.normalize(module)) {
            case UserRecordModuleConstants.READING -> adminReadingService.restoreRecord(recordId);
            case UserRecordModuleConstants.LISTENING -> adminListeningService.restoreRecord(recordId);
            case UserRecordModuleConstants.WRITING -> adminWritingService.restoreRecord(recordId);
            case UserRecordModuleConstants.SPEAKING -> adminSpeakingService.restoreRecord(recordId);
            default -> throw new IllegalArgumentException("Unsupported moduleType: " + module);
        }
    }

    private PageResult<AdminUserRecordListItemVO> doListRecords(Long userId,
                                                                 String recordState,
                                                                 String module,
                                                                 String status,
                                                                 String sort,
                                                                 Integer pageNum,
                                                                 Integer pageSize) {
        if (userId != null) {
            validateUserId(userId);
        }
        String normalizedRecordState = UserRecordStateConstants.normalize(recordState);
        String normalizedModule = normalizeOptionalModule(module);
        String normalizedStatus = UserRecordListStatusConstants.normalize(status);
        String normalizedSort = UserRecordListSortConstants.normalize(sort);
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        int offset = (normalizedPageNum - 1) * normalizedPageSize;

        Long total = adminUserRecordListMapper.countRecords(
                normalizedRecordState,
                normalizedModule,
                normalizedStatus,
                userId
        );
        List<AdminUserRecordListItemVO> list = adminUserRecordListMapper.pageRecords(
                normalizedRecordState,
                normalizedModule,
                normalizedStatus,
                userId,
                normalizedSort,
                offset,
                normalizedPageSize
        );
        return new PageResult<>(
                list == null ? List.of() : list,
                total == null ? 0L : total,
                normalizedPageNum,
                normalizedPageSize
        );
    }

    private String normalizeOptionalModule(String module) {
        if (module == null || module.isBlank()) {
            return null;
        }
        return UserRecordModuleConstants.normalize(module);
    }

    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null) {
            return DEFAULT_PAGE_NUM;
        }
        if (pageNum < 1) {
            throw new IllegalArgumentException("pageNum must be greater than or equal to 1");
        }
        return pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than or equal to 1");
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId < 1) {
            throw new IllegalArgumentException("userId must be greater than or equal to 1");
        }
    }

    private void validateRecordId(Long recordId) {
        if (recordId == null || recordId < 1) {
            throw new IllegalArgumentException("recordId must be greater than or equal to 1");
        }
    }
}
