package com.andrew.smartielts.speaking.mapper;

import com.andrew.smartielts.speaking.domain.pojo.SpeakingRecord;
import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingDeletedRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.admin.AdminSpeakingRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.user.UserSpeakingDeletedRecordPageQuery;
import com.andrew.smartielts.speaking.domain.query.user.UserSpeakingRecordPageQuery;
import com.andrew.smartielts.speaking.domain.vo.SpeakingRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SpeakingRecordMapper {

    void insertSpeakingRecord(SpeakingRecord record);

    SpeakingRecord findById(@Param("id") Long id);

    SpeakingRecord findAnyById(@Param("id") Long id);

    SpeakingRecord findActiveById(@Param("id") Long id);

    SpeakingRecord findByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    SpeakingRecord findAnyByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    SpeakingRecord findActiveByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    List<SpeakingRecord> findByUserId(@Param("userId") Long userId);

    List<SpeakingRecord> findActiveByUserId(@Param("userId") Long userId);

    List<SpeakingRecord> findDeletedByUserId(@Param("userId") Long userId);

    List<SpeakingRecord> findBySessionId(@Param("sessionId") String sessionId);

    SpeakingRecord findBySessionIdAndQuestionId(@Param("sessionId") String sessionId,
                                                @Param("questionId") Long questionId);

    Long countUserActive(@Param("userId") Long userId,
                         @Param("query") UserSpeakingRecordPageQuery query);

    List<SpeakingRecordVO> pageUserActive(@Param("userId") Long userId,
                                          @Param("query") UserSpeakingRecordPageQuery query,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);

    Long countUserDeleted(@Param("userId") Long userId,
                          @Param("query") UserSpeakingDeletedRecordPageQuery query);

    List<SpeakingRecordVO> pageUserDeleted(@Param("userId") Long userId,
                                           @Param("query") UserSpeakingDeletedRecordPageQuery query,
                                           @Param("offset") Integer offset,
                                           @Param("limit") Integer limit);

    Long countAdminActive(@Param("query") AdminSpeakingRecordPageQuery query);

    Long countAdminDeleted(@Param("query") AdminSpeakingDeletedRecordPageQuery query);

    List<SpeakingRecord> findRecentAiFailures(@Param("limit") Integer limit);

    void updateSpeakingRecord(SpeakingRecord record);

    void softDeleteById(@Param("id") Long id);

    void restoreById(@Param("id") Long id);

    void softDeleteByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    void restoreByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    List<SpeakingRecord> findRecentActiveByUserId(@Param("userId") Long userId,
                                                  @Param("limit") Integer limit);

    BigDecimal selectUserAverageScore(@Param("userId") Long userId);

    Long countAdminAiFailed();
}
