package com.andrew.smartielts.record.service.admin;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.record.domain.query.admin.AdminUserRecordListQuery;
import com.andrew.smartielts.record.domain.query.admin.AdminUserScopedRecordListQuery;
import com.andrew.smartielts.record.domain.vo.admin.AdminUserRecordListItemVO;

public interface AdminUserRecordService {

    PageResult<AdminUserRecordListItemVO> listRecords(AdminUserRecordListQuery query);

    PageResult<AdminUserRecordListItemVO> listRecordsForUser(Long userId, AdminUserScopedRecordListQuery query);

    Object getRecord(String module, Long recordId);

    void deleteRecord(String module, Long recordId);

    void restoreRecord(String module, Long recordId);
}
