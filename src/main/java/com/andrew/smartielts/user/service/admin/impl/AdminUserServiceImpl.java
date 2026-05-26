package com.andrew.smartielts.user.service.admin.impl;

import com.andrew.smartielts.auth.domain.pojo.User;
import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.common.page.SortDirectionEnum;
import com.andrew.smartielts.record.domain.query.admin.AdminUserScopedRecordListQuery;
import com.andrew.smartielts.record.domain.vo.UserRecordDetailVO;
import com.andrew.smartielts.record.domain.vo.admin.AdminUserRecordListItemVO;
import com.andrew.smartielts.record.service.UserRecordService;
import com.andrew.smartielts.record.service.admin.AdminUserRecordService;
import com.andrew.smartielts.user.domain.query.admin.AdminDeletedUserPageQuery;
import com.andrew.smartielts.user.domain.query.admin.AdminUserPageQuery;
import com.andrew.smartielts.user.domain.vo.AdminUserListVO;
import com.andrew.smartielts.user.domain.vo.UserAdminDetailVO;
import com.andrew.smartielts.user.domain.vo.UserAdminVO;
import com.andrew.smartielts.user.domain.vo.UserRecordCountVO;
import com.andrew.smartielts.user.mapper.UserMapper;
import com.andrew.smartielts.user.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String SORT_FIELD_ID = "id";
    private static final String SORT_FIELD_EMAIL = "email";
    private static final String SORT_FIELD_ROLE = "role";
    private static final String SORT_FIELD_CREATED_TIME = "createdTime";
    private static final String SORT_FIELD_DELETED_TIME = "deletedTime";
    private static final String SORT_FIELD_LAST_LOGIN_TIME = "lastLoginTime";
    private static final int IELTS_TARGET_SCORE_PART_COUNT = 4;

    private final UserMapper userMapper;
    private final UserRecordService userRecordService;
    private final AdminUserRecordService adminUserRecordService;

    @Override
    public AdminUserListVO listUsers(AdminUserPageQuery query) {
        PageResult<UserAdminVO> users = pageActiveUsers(query);
        AdminUserListVO vo = new AdminUserListVO();
        vo.setUsers(users);
        vo.setTotalUsers(userMapper.countAllUsers());
        vo.setActiveUsers(userMapper.countActiveUsers());
        vo.setDeletedUsers(userMapper.countDeletedUsers());
        return vo;
    }

    @Override
    public PageResult<UserAdminVO> pageActiveUsers(AdminUserPageQuery query) {
        AdminUserPageQuery safeQuery = normalizeActiveQuery(query);
        int pageNum = safeQuery.getPageNum();
        int pageSize = safeQuery.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        Long total = userMapper.countActive(safeQuery);
        List<User> users = userMapper.pageActive(safeQuery, offset, pageSize);
        List<UserAdminVO> list = users.stream().map(this::toVO).toList();

        return new PageResult<>(list, total, pageNum, pageSize);
    }

    @Override
    public PageResult<UserAdminVO> pageDeletedUsers(AdminDeletedUserPageQuery query) {
        AdminDeletedUserPageQuery safeQuery = normalizeDeletedQuery(query);
        int pageNum = safeQuery.getPageNum();
        int pageSize = safeQuery.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        Long total = userMapper.countDeleted(safeQuery);
        List<User> users = userMapper.pageDeleted(safeQuery, offset, pageSize);
        List<UserAdminVO> list = users.stream().map(this::toVO).toList();

        return new PageResult<>(list, total, pageNum, pageSize);
    }

    @Override
    public UserAdminDetailVO getUserDetail(Long userId) {
        User user = userMapper.findAnyById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        UserAdminDetailVO vo = new UserAdminDetailVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setIsDeleted(user.getIsDeleted());
        vo.setDeletedTime(user.getDeletedTime());
        vo.setCreatedTime(user.getCreatedTime());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setProfilePictureUrl(user.getProfilePictureUrl());
        vo.setProfilePictureObjectKey(user.getProfilePictureObjectKey());
        applyIeltsTargetScores(vo, user.getIeltsTargetScores());
        attachRecordCounts(vo, userId);
        return vo;
    }

    @Override
    public UserRecordDetailVO getUserRecordDetail(Long userId, String moduleType, Long recordId) {
        User user = userMapper.findAnyById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return userRecordService.getRecord(userId, moduleType, recordId);
    }

    @Override
    public PageResult<AdminUserRecordListItemVO> listUserRecords(Long userId, AdminUserScopedRecordListQuery query) {
        User user = userMapper.findAnyById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return adminUserRecordService.listRecordsForUser(userId, query);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.findActiveById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        userMapper.softDeleteById(userId);
    }

    @Override
    @Transactional
    public void restoreUser(Long userId) {
        User user = userMapper.findAnyById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (user.getIsDeleted() == null || user.getIsDeleted() == 0) {
            throw new RuntimeException("User is not deleted");
        }
        userMapper.restoreById(userId);
    }

    private AdminUserPageQuery normalizeActiveQuery(AdminUserPageQuery query) {
        AdminUserPageQuery safeQuery = query == null ? new AdminUserPageQuery() : query;
        safeQuery.setPageNum(normalizePageNum(safeQuery.getPageNum()));
        safeQuery.setPageSize(normalizePageSize(safeQuery.getPageSize()));
        safeQuery.setKeyword(normalizeText(safeQuery.getKeyword()));
        safeQuery.setEmail(normalizeText(safeQuery.getEmail()));
        safeQuery.setRole(normalizeText(safeQuery.getRole()));
        safeQuery.setSortField(normalizeSortField(safeQuery.getSortField(), SORT_FIELD_CREATED_TIME));
        safeQuery.setSortDirection(normalizeSortDirection(safeQuery.getSortDirection()));
        return safeQuery;
    }

    private AdminDeletedUserPageQuery normalizeDeletedQuery(AdminDeletedUserPageQuery query) {
        AdminDeletedUserPageQuery safeQuery = query == null ? new AdminDeletedUserPageQuery() : query;
        safeQuery.setPageNum(normalizePageNum(safeQuery.getPageNum()));
        safeQuery.setPageSize(normalizePageSize(safeQuery.getPageSize()));
        safeQuery.setKeyword(normalizeText(safeQuery.getKeyword()));
        safeQuery.setEmail(normalizeText(safeQuery.getEmail()));
        safeQuery.setRole(normalizeText(safeQuery.getRole()));
        safeQuery.setSortField(normalizeSortField(safeQuery.getSortField(), SORT_FIELD_DELETED_TIME));
        safeQuery.setSortDirection(normalizeSortDirection(safeQuery.getSortDirection()));
        return safeQuery;
    }

    private UserAdminVO toVO(User user) {
        UserAdminVO vo = new UserAdminVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setIsDeleted(user.getIsDeleted());
        vo.setDeletedTime(user.getDeletedTime());
        vo.setCreatedTime(user.getCreatedTime());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setConsecutiveLoginDays(user.getConsecutiveLoginDays());
        vo.setProfilePictureUrl(user.getProfilePictureUrl());
        vo.setProfilePictureObjectKey(user.getProfilePictureObjectKey());
        applyIeltsTargetScores(vo, user.getIeltsTargetScores());
        return vo;
    }

    private void attachRecordCounts(UserAdminDetailVO user, Long userId) {
        List<UserRecordCountVO> recordCounts = userMapper.selectRecordCountsByUserIds(List.of(userId));
        if (recordCounts == null) {
            recordCounts = Collections.emptyList();
        }
        user.setRecordCounts(recordCounts);
        user.setTotalActiveRecordCount(sumActiveRecordCount(recordCounts));
        user.setTotalDeletedRecordCount(sumDeletedRecordCount(recordCounts));
    }

    private Long sumActiveRecordCount(List<UserRecordCountVO> recordCounts) {
        return recordCounts.stream()
                .map(UserRecordCountVO::getActiveRecordCount)
                .mapToLong(this::safeLong)
                .sum();
    }

    private Long sumDeletedRecordCount(List<UserRecordCountVO> recordCounts) {
        return recordCounts.stream()
                .map(UserRecordCountVO::getDeletedRecordCount)
                .mapToLong(this::safeLong)
                .sum();
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private SortDirectionEnum normalizeSortDirection(SortDirectionEnum sortDirection) {
        return sortDirection == null ? SortDirectionEnum.DESC : sortDirection;
    }

    private String normalizeSortField(String sortField, String defaultSortField) {
        String normalized = normalizeText(sortField);
        if (SORT_FIELD_ID.equals(normalized)
                || SORT_FIELD_EMAIL.equals(normalized)
                || SORT_FIELD_ROLE.equals(normalized)
                || SORT_FIELD_CREATED_TIME.equals(normalized)
                || SORT_FIELD_DELETED_TIME.equals(normalized)
                || SORT_FIELD_LAST_LOGIN_TIME.equals(normalized)) {
            return normalized;
        }
        return defaultSortField;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void applyIeltsTargetScores(UserAdminVO vo, String rawScores) {
        List<BigDecimal> scores = decodeIeltsTargetScores(rawScores);
        vo.setListeningTargetScore(scores.get(0));
        vo.setReadingTargetScore(scores.get(1));
        vo.setWritingTargetScore(scores.get(2));
        vo.setSpeakingTargetScore(scores.get(3));
    }

    private void applyIeltsTargetScores(UserAdminDetailVO vo, String rawScores) {
        List<BigDecimal> scores = decodeIeltsTargetScores(rawScores);
        vo.setListeningTargetScore(scores.get(0));
        vo.setReadingTargetScore(scores.get(1));
        vo.setWritingTargetScore(scores.get(2));
        vo.setSpeakingTargetScore(scores.get(3));
    }

    private List<BigDecimal> decodeIeltsTargetScores(String rawScores) {
        List<BigDecimal> scores = new java.util.ArrayList<>();
        for (int i = 0; i < IELTS_TARGET_SCORE_PART_COUNT; i++) {
            scores.add(null);
        }
        if (rawScores == null || rawScores.isBlank()) {
            return scores;
        }
        String[] parts = rawScores.split(",", -1);
        for (int i = 0; i < Math.min(parts.length, IELTS_TARGET_SCORE_PART_COUNT); i++) {
            String part = parts[i] == null ? "" : parts[i].trim();
            if (!part.isEmpty()) {
                scores.set(i, new BigDecimal(part).stripTrailingZeros());
            }
        }
        return scores;
    }
}


