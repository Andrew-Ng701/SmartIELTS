package com.andrew.smartielts.user.service.admin;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.record.domain.query.admin.AdminUserScopedRecordListQuery;
import com.andrew.smartielts.user.domain.query.admin.AdminDeletedUserPageQuery;
import com.andrew.smartielts.user.domain.query.admin.AdminUserPageQuery;
import com.andrew.smartielts.record.domain.vo.UserRecordDetailVO;
import com.andrew.smartielts.record.domain.vo.admin.AdminUserRecordListItemVO;
import com.andrew.smartielts.user.domain.vo.AdminUserListVO;
import com.andrew.smartielts.user.domain.vo.UserAdminDetailVO;
import com.andrew.smartielts.user.domain.vo.UserAdminVO;

public interface AdminUserService {

    AdminUserListVO listUsers(AdminUserPageQuery query);

    PageResult<UserAdminVO> pageActiveUsers(AdminUserPageQuery query);

    PageResult<UserAdminVO> pageDeletedUsers(AdminDeletedUserPageQuery query);

    UserAdminDetailVO getUserDetail(Long userId);

    UserRecordDetailVO getUserRecordDetail(Long userId, String moduleType, Long recordId);

    PageResult<AdminUserRecordListItemVO> listUserRecords(Long userId, AdminUserScopedRecordListQuery query);

    void deleteUser(Long userId);

    void restoreUser(Long userId);
}


