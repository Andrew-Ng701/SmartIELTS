package com.andrew.smartielts.record.mapper;

import com.andrew.smartielts.record.domain.vo.admin.AdminUserRecordListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminUserRecordListMapper {

    Long countRecords(@Param("recordState") String recordState,
                      @Param("module") String module,
                      @Param("status") String status,
                      @Param("userId") Long userId);

    List<AdminUserRecordListItemVO> pageRecords(@Param("recordState") String recordState,
                                                @Param("module") String module,
                                                @Param("status") String status,
                                                @Param("userId") Long userId,
                                                @Param("sort") String sort,
                                                @Param("offset") Integer offset,
                                                @Param("limit") Integer limit);
}
