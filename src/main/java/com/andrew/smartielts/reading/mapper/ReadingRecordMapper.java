package com.andrew.smartielts.reading.mapper;

import com.andrew.smartielts.reading.domain.pojo.ReadingRecord;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.user.UserReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.user.UserReadingRecordPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ReadingRecordMapper {

    void insertReadingRecord(ReadingRecord record);

    ReadingRecord findById(@Param("id") Long id);

    ReadingRecord findAnyById(@Param("id") Long id);

    ReadingRecord findActiveById(@Param("id") Long id);

    ReadingRecord findByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    ReadingRecord findAnyByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    ReadingRecord findActiveByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    List<ReadingRecord> findByUserId(@Param("userId") Long userId);

    List<ReadingRecord> findActiveByUserId(@Param("userId") Long userId);

    List<ReadingRecord> findDeletedByUserId(@Param("userId") Long userId);

    void updateTotalScore(@Param("id") Long id, @Param("totalScore") Integer totalScore);

    Long countUserActive(@Param("userId") Long userId,
                         @Param("query") UserReadingRecordPageQuery query);

    List<ReadingRecord> pageUserActive(@Param("userId") Long userId,
                                       @Param("query") UserReadingRecordPageQuery query,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    Long countUserDeleted(@Param("userId") Long userId,
                          @Param("query") UserReadingDeletedRecordPageQuery query);

    List<ReadingRecord> pageUserDeleted(@Param("userId") Long userId,
                                        @Param("query") UserReadingDeletedRecordPageQuery query,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    Long countAdminActive(@Param("query") AdminReadingRecordPageQuery query);

    List<ReadingRecord> pageAdminActive(@Param("query") AdminReadingRecordPageQuery query,
                                        @Param("offset") Integer offset,
                                        @Param("limit") Integer limit);

    Long countAdminDeleted(@Param("query") AdminReadingDeletedRecordPageQuery query);

    List<ReadingRecord> pageAdminDeleted(@Param("query") AdminReadingDeletedRecordPageQuery query,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    void softDeleteById(@Param("id") Long id);

    void restoreById(@Param("id") Long id);

    void softDeleteByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    void restoreByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    List<ReadingRecord> findRecentActiveByUserId(@Param("userId") Long userId,
                                                 @Param("limit") Integer limit);

    BigDecimal selectUserAverageScore(@Param("userId") Long userId);

    ReadingRecord findBySessionIdForUser(@Param("sessionId") String sessionId, @Param("userId") Long userId);

    ReadingRecord findInProgressByTestIdForUser(@Param("testId") Long testId, @Param("userId") Long userId);

    int updateSessionState(ReadingRecord record);
}