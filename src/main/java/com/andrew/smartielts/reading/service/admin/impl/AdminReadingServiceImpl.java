package com.andrew.smartielts.reading.service.admin.impl;

import com.andrew.smartielts.common.constants.RecordQueryValidator;
import com.andrew.smartielts.common.page.PageResult;
import com.andrew.smartielts.reading.domain.dto.ReadingPassageDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingQuestionDTO;
import com.andrew.smartielts.reading.domain.dto.ReadingTestDTO;
import com.andrew.smartielts.reading.domain.pojo.ReadingAnswerRecord;
import com.andrew.smartielts.reading.domain.pojo.ReadingPassage;
import com.andrew.smartielts.reading.domain.pojo.ReadingQuestion;
import com.andrew.smartielts.reading.domain.pojo.ReadingRecord;
import com.andrew.smartielts.reading.domain.pojo.ReadingTest;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingDeletedRecordPageQuery;
import com.andrew.smartielts.reading.domain.query.admin.AdminReadingRecordPageQuery;
import com.andrew.smartielts.reading.domain.vo.ReadingAnswerResultVO;
import com.andrew.smartielts.reading.domain.vo.ReadingPassageVO;
import com.andrew.smartielts.reading.domain.vo.ReadingQuestionVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordDetailVO;
import com.andrew.smartielts.reading.domain.vo.ReadingRecordVO;
import com.andrew.smartielts.reading.domain.vo.ReadingTestDetailVO;
import com.andrew.smartielts.reading.mapper.ReadingAnswerRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingPassageMapper;
import com.andrew.smartielts.reading.mapper.ReadingQuestionMapper;
import com.andrew.smartielts.reading.mapper.ReadingRecordMapper;
import com.andrew.smartielts.reading.mapper.ReadingTestMapper;
import com.andrew.smartielts.reading.service.admin.AdminReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class AdminReadingServiceImpl implements AdminReadingService {

    @Autowired
    private ReadingTestMapper readingTestMapper;

    @Autowired
    private ReadingPassageMapper readingPassageMapper;

    @Autowired
    private ReadingQuestionMapper readingQuestionMapper;

    @Autowired
    private ReadingRecordMapper readingRecordMapper;

    @Autowired
    private ReadingAnswerRecordMapper readingAnswerRecordMapper;

    @Override
    @Transactional
    public ReadingTest createTest(ReadingTestDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        ReadingTest test = new ReadingTest();
        test.setTitle(dto.getTitle());
        test.setTotalScore(dto.getTotalScore());
        test.setCreatedTime(LocalDateTime.now());
        test.setIsDeleted(0);

        readingTestMapper.insertReadingTest(test);
        return test;
    }

    @Override
    public List<ReadingTest> listTests() {
        return readingTestMapper.findAllActive();
    }

    @Override
    public ReadingTestDetailVO getTestDetail(Long testId) {
        ReadingTest test = readingTestMapper.findAnyById(testId);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<ReadingPassage> passages = readingPassageMapper.findAnyByTestId(testId);
        List<ReadingPassageVO> passageVOList = new ArrayList<>();

        for (ReadingPassage passage : passages) {
            ReadingPassageVO passageVO = new ReadingPassageVO();
            passageVO.setId(passage.getId());
            passageVO.setTitle(passage.getTitle());
            passageVO.setContent(passage.getContent());

            List<ReadingQuestion> questions = readingQuestionMapper.findAnyByPassageId(passage.getId());
            List<ReadingQuestionVO> questionVOList = new ArrayList<>();

            for (ReadingQuestion question : questions) {
                ReadingQuestionVO questionVO = toQuestionVO(question);
                questionVOList.add(questionVO);
            }

            questionVOList.sort(Comparator
                    .comparing(ReadingQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)));

            passageVO.setQuestions(questionVOList);
            passageVOList.add(passageVO);
        }

        ReadingTestDetailVO detailVO = new ReadingTestDetailVO();
        detailVO.setId(test.getId());
        detailVO.setTitle(test.getTitle());
        detailVO.setTotalScore(test.getTotalScore());
        detailVO.setPassages(passageVOList);
        return detailVO;
    }

    @Override
    @Transactional
    public ReadingTest updateTest(Long id, ReadingTestDTO dto) {
        ReadingTest test = readingTestMapper.findActiveById(id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        test.setTitle(dto.getTitle());
        test.setTotalScore(dto.getTotalScore());
        readingTestMapper.updateReadingTest(test);
        return test;
    }

    @Override
    @Transactional
    public void deleteTest(Long id) {
        ReadingTest test = readingTestMapper.findActiveById(id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<ReadingPassage> passages = readingPassageMapper.findAnyByTestId(id);
        for (ReadingPassage passage : passages) {
            readingQuestionMapper.softDeleteByPassageId(passage.getId());
        }
        readingPassageMapper.softDeleteByTestId(id);
        readingTestMapper.softDeleteById(id);
    }

    @Override
    @Transactional
    public void restoreTest(Long id) {
        ReadingTest test = readingTestMapper.findAnyById(id);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        readingTestMapper.restoreById(id);
        readingPassageMapper.restoreByTestId(id);

        List<ReadingPassage> passages = readingPassageMapper.findAnyByTestId(id);
        for (ReadingPassage passage : passages) {
            readingQuestionMapper.restoreByPassageId(passage.getId());
        }
    }

    @Override
    @Transactional
    public void createPassage(Long testId, ReadingPassageDTO dto) {
        ReadingTest test = readingTestMapper.findActiveById(testId);
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        ReadingPassage passage = new ReadingPassage();
        passage.setTestId(testId);
        passage.setTitle(dto.getTitle());
        passage.setContent(dto.getContent());
        passage.setIsDeleted(0);

        readingPassageMapper.insertReadingPassage(passage);
    }

    @Override
    @Transactional
    public void updatePassage(Long passageId, ReadingPassageDTO dto) {
        ReadingPassage passage = readingPassageMapper.findActiveById(passageId);
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        passage.setTitle(dto.getTitle());
        passage.setContent(dto.getContent());
        readingPassageMapper.updateReadingPassage(passage);
    }

    @Override
    @Transactional
    public void deletePassage(Long passageId) {
        ReadingPassage passage = readingPassageMapper.findActiveById(passageId);
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }

        readingQuestionMapper.softDeleteByPassageId(passageId);
        readingPassageMapper.softDeleteById(passageId);
    }

    @Override
    @Transactional
    public void restorePassage(Long passageId) {
        ReadingPassage passage = readingPassageMapper.findAnyById(passageId);
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }

        ReadingTest test = readingTestMapper.findAnyById(passage.getTestId());
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }
        if (test.getIsDeleted() != null && test.getIsDeleted() == 1) {
            throw new RuntimeException("Cannot restore passage because parent test is deleted");
        }

        readingPassageMapper.restoreById(passageId);
        readingQuestionMapper.restoreByPassageId(passageId);
    }

    @Override
    @Transactional
    public void createQuestion(Long passageId, ReadingQuestionDTO dto) {
        ReadingPassage passage = readingPassageMapper.findActiveById(passageId);
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        ReadingQuestion question = new ReadingQuestion();
        question.setPassageId(passageId);
        question.setQuestionText(dto.getQuestionText());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setScore(dto.getScore() == null ? 1 : dto.getScore());
        question.setQuestionType(dto.getQuestionType());
        question.setAnswerMode(dto.getAnswerMode());
        question.setOptionsJson(dto.getOptionsJson());
        question.setAcceptedAnswersJson(dto.getAcceptedAnswersJson());
        question.setGroupLabel(dto.getGroupLabel());
        question.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        question.setIsDeleted(0);

        readingQuestionMapper.insertReadingQuestion(question);
    }

    @Override
    @Transactional
    public void updateQuestion(Long questionId, ReadingQuestionDTO dto) {
        ReadingQuestion question = readingQuestionMapper.findActiveById(questionId);
        if (question == null) {
            throw new RuntimeException("Reading question not found");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required");
        }

        question.setQuestionText(dto.getQuestionText());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setScore(dto.getScore() == null ? 1 : dto.getScore());
        question.setQuestionType(dto.getQuestionType());
        question.setAnswerMode(dto.getAnswerMode());
        question.setOptionsJson(dto.getOptionsJson());
        question.setAcceptedAnswersJson(dto.getAcceptedAnswersJson());
        question.setGroupLabel(dto.getGroupLabel());
        question.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());

        readingQuestionMapper.updateReadingQuestion(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        ReadingQuestion question = readingQuestionMapper.findActiveById(questionId);
        if (question == null) {
            throw new RuntimeException("Reading question not found");
        }

        readingQuestionMapper.softDeleteById(questionId);
    }

    @Override
    @Transactional
    public void restoreQuestion(Long questionId) {
        ReadingQuestion question = readingQuestionMapper.findAnyById(questionId);
        if (question == null) {
            throw new RuntimeException("Reading question not found");
        }

        ReadingPassage passage = readingPassageMapper.findAnyById(question.getPassageId());
        if (passage == null) {
            throw new RuntimeException("Reading passage not found");
        }
        if (passage.getIsDeleted() != null && passage.getIsDeleted() == 1) {
            throw new RuntimeException("Cannot restore question because parent passage is deleted");
        }

        readingQuestionMapper.restoreById(questionId);
    }

    @Override
    public PageResult<ReadingRecordVO> pageActiveRecords(AdminReadingRecordPageQuery query) {
        AdminReadingRecordPageQuery safeQuery = query == null ? new AdminReadingRecordPageQuery() : query;

        RecordQueryValidator.validate(
                safeQuery.getPageNum(),
                safeQuery.getPageSize(),
                safeQuery.getUserId(),
                safeQuery.getTestId(),
                safeQuery.getMinScore(),
                safeQuery.getMaxScore(),
                safeQuery.getStartTime(),
                safeQuery.getEndTime()
        );

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = readingRecordMapper.countAdminActive(safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<ReadingRecord> records = readingRecordMapper.pageAdminActive(safeQuery, offset, pageSize);
        List<ReadingRecordVO> voList = new ArrayList<>();

        if (records != null) {
            for (ReadingRecord record : records) {
                voList.add(toRecordVO(record));
            }
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<ReadingRecordVO> pageDeletedRecords(AdminReadingDeletedRecordPageQuery query) {
        AdminReadingDeletedRecordPageQuery safeQuery =
                query == null ? new AdminReadingDeletedRecordPageQuery() : query;

        int pageNum = normalizePageNum(safeQuery.getPageNum());
        int pageSize = normalizePageSize(safeQuery.getPageSize());
        int offset = (pageNum - 1) * pageSize;

        Long total = readingRecordMapper.countAdminDeleted(safeQuery);
        if (total == null || total == 0L) {
            return new PageResult<>(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        List<ReadingRecord> records = readingRecordMapper.pageAdminDeleted(safeQuery, offset, pageSize);
        List<ReadingRecordVO> voList = new ArrayList<>();

        if (records != null) {
            for (ReadingRecord record : records) {
                voList.add(toRecordVO(record));
            }
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    @Override
    public ReadingRecordDetailVO getRecord(Long recordId) {
        ReadingRecord record = readingRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }

        ReadingTest test = readingTestMapper.findAnyById(record.getTestId());
        if (test == null) {
            throw new RuntimeException("Reading test not found");
        }

        List<ReadingPassage> passages = readingPassageMapper.findAnyByTestId(test.getId());
        List<ReadingAnswerRecord> answerRecords = readingAnswerRecordMapper.findByRecordId(recordId);

        List<ReadingPassageVO> passageVOList = new ArrayList<>();
        List<ReadingAnswerResultVO> answerVOList = new ArrayList<>();

        for (ReadingPassage passage : passages) {
            ReadingPassageVO passageVO = new ReadingPassageVO();
            passageVO.setId(passage.getId());
            passageVO.setTitle(passage.getTitle());
            passageVO.setContent(passage.getContent());

            List<ReadingQuestion> questions = readingQuestionMapper.findAnyByPassageId(passage.getId());
            List<ReadingQuestionVO> questionVOList = new ArrayList<>();

            for (ReadingQuestion question : questions) {
                questionVOList.add(toQuestionVO(question));

                ReadingAnswerRecord matched = answerRecords.stream()
                        .filter(answer -> Objects.equals(answer.getQuestionId(), question.getId()))
                        .findFirst()
                        .orElse(null);

                ReadingAnswerResultVO answerVO = new ReadingAnswerResultVO();
                answerVO.setQuestionId(question.getId());
                answerVO.setQuestionType(question.getQuestionType());
                answerVO.setAnswerMode(question.getAnswerMode());
                answerVO.setQuestionText(question.getQuestionText());
                answerVO.setOptionsJson(question.getOptionsJson());
                answerVO.setCorrectAnswer(question.getCorrectAnswer());

                if (matched != null) {
                    answerVO.setUserAnswer(matched.getUserAnswer());
                    answerVO.setIsCorrect(matched.getIsCorrect());
                    answerVO.setScore(matched.getScore());
                } else {
                    answerVO.setUserAnswer(null);
                    answerVO.setIsCorrect(0);
                    answerVO.setScore(0);
                }

                answerVOList.add(answerVO);
            }

            questionVOList.sort(Comparator
                    .comparing(ReadingQuestionVO::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)));

            passageVO.setQuestions(questionVOList);
            passageVOList.add(passageVO);
        }

        ReadingRecordDetailVO detailVO = new ReadingRecordDetailVO();
        detailVO.setRecordId(record.getId());
        detailVO.setTestId(test.getId());
        detailVO.setTestTitle(test.getTitle());
        detailVO.setTotalScore(record.getTotalScore());
        detailVO.setCreatedTime(record.getCreatedTime());
        detailVO.setPassages(passageVOList);
        detailVO.setAnswers(answerVOList);
        return detailVO;
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId) {
        ReadingRecord record = readingRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }
        if (record.getIsDeleted() != null && record.getIsDeleted() == 1) {
            throw new RuntimeException("Reading record already deleted");
        }

        readingRecordMapper.softDeleteById(recordId);
    }

    @Override
    @Transactional
    public void restoreRecord(Long recordId) {
        ReadingRecord record = readingRecordMapper.findAnyById(recordId);
        if (record == null) {
            throw new RuntimeException("Reading record not found");
        }
        if (record.getIsDeleted() == null || record.getIsDeleted() == 0) {
            throw new RuntimeException("Reading record is not deleted");
        }

        readingRecordMapper.restoreById(recordId);
    }

    private ReadingQuestionVO toQuestionVO(ReadingQuestion question) {
        ReadingQuestionVO vo = new ReadingQuestionVO();
        vo.setId(question.getId());
        vo.setQuestionType(question.getQuestionType());
        vo.setAnswerMode(question.getAnswerMode());
        vo.setQuestionText(question.getQuestionText());
        vo.setOptionsJson(question.getOptionsJson());
        vo.setGroupLabel(question.getGroupLabel());
        vo.setDisplayOrder(question.getDisplayOrder());
        vo.setScore(question.getScore());
        return vo;
    }

    private ReadingRecordVO toRecordVO(ReadingRecord record) {
        ReadingRecordVO vo = new ReadingRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setTestId(record.getTestId());
        vo.setTotalScore(record.getTotalScore());
        vo.setCreatedTime(record.getCreatedTime());
        vo.setIsDeleted(record.getIsDeleted());

        ReadingTest test = readingTestMapper.findAnyById(record.getTestId());
        vo.setTestTitle(test == null ? null : test.getTitle());
        return vo;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private void validateRecordQuery(AdminReadingRecordPageQuery query) {
        if (query.getPageNum() != null && query.getPageNum() < 1) {
            throw new RuntimeException("pageNum must be greater than or equal to 1");
        }
        if (query.getPageSize() != null && query.getPageSize() < 1) {
            throw new RuntimeException("pageSize must be greater than or equal to 1");
        }
        if (query.getUserId() != null && query.getUserId() < 1) {
            throw new RuntimeException("userId must be greater than 0");
        }
        if (query.getTestId() != null && query.getTestId() < 1) {
            throw new RuntimeException("testId must be greater than 0");
        }
        if (query.getMinScore() != null && query.getMaxScore() != null
                && query.getMinScore() > query.getMaxScore()) {
            throw new RuntimeException("minScore cannot be greater than maxScore");
        }
        if (query.getStartTime() != null && query.getEndTime() != null
                && query.getStartTime().isAfter(query.getEndTime())) {
            throw new RuntimeException("startTime cannot be later than endTime");
        }
    }
}