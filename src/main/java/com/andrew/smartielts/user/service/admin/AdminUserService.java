package com.andrew.smartielts.user.service.admin;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.user.domain.query.admin.AdminDeletedUserPageQuery;
import com.andrew.smartielts.user.domain.query.admin.AdminUserPageQuery;
import com.andrew.smartielts.user.domain.vo.UserAdminDetailVO;
import com.andrew.smartielts.user.domain.vo.UserAdminVO;

public interface AdminUserService {

    PageResult<UserAdminVO> pageActiveUsers(AdminUserPageQuery query);

    PageResult<UserAdminVO> pageDeletedUsers(AdminDeletedUserPageQuery query);

    UserAdminDetailVO getUserDetail(Long userId);

    void deleteUser(Long userId);

    void restoreUser(Long userId);

    Long totalUsers();

    Long activeUsers();

    Long deletedUsers();
}
