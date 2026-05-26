package com.andrew.smartielts.record.mapper;

import com.andrew.smartielts.record.domain.vo.UserRecordListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRecordListMapper {

    Long countUserRecords(@Param("userId") Long userId,
                          @Param("recordState") String recordState,
                          @Param("module") String module,
                          @Param("status") String status);

    List<UserRecordListItemVO> pageUserRecords(@Param("userId") Long userId,
                                               @Param("recordState") String recordState,
                                               @Param("module") String module,
                                               @Param("status") String status,
                                               @Param("sort") String sort,
                                               @Param("offset") Integer offset,
                                               @Param("limit") Integer limit);
}
