package com.andrew.smartielts.reading.service.user;

import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.reading.domain.dto.ReadingSubmitDTO;
import com.andrew.smartielts.reading.domain.pojo.ReadingTest;
import com.andrew.smartielts.reading.domain.query.user.UserReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.user.UserReadingRecordPageQuery;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordDetailVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordVO;
import com.andrew.smartielts.reading.domain.vo.ReadingTestDetailVO;

import java.util.List;

public interface UserReadingService {
    List<ReadingTest> listTests();

    ReadingTestDetailVO getTestDetail(Long testId);

    ReadingRecordDetailVO submit(Long testId, ReadingSubmitDTO dto);

    PageResult<ReadingRecordVO> pageActiveRecords(Long userId, UserReadingRecordPageQuery query);

    PageResult<ReadingRecordVO> pageDeletedRecords(Long userId, UserReadingDeletedRecordPageQuery query);

    ReadingRecordDetailVO getRecord(Long recordId, Long userId);

    void deleteRecord(Long recordId, Long userId);

    void restoreRecord(Long recordId, Long userId);
}
