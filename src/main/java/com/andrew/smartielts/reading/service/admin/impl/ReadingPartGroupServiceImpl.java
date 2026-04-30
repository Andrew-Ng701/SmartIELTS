package com.andrew.smartielts.reading.service.admin.impl;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;
import com.andrew.smartielts.reading.mapper.ReadingPartGroupMapper;
import com.andrew.smartielts.reading.service.admin.ReadingPartGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReadingPartGroupServiceImpl implements ReadingPartGroupService {

    private final ReadingPartGroupMapper readingPartGroupMapper;

    public ReadingPartGroupServiceImpl(ReadingPartGroupMapper readingPartGroupMapper) {
        this.readingPartGroupMapper = readingPartGroupMapper;
    }

    @Override
    @Transactional
    public TestPartGroup createPartGroup(TestPartGroup partGroup) {
        if (partGroup == null) {
            throw new RuntimeException("Part group is required");
        }
        if (partGroup.getTestId() == null) {
            throw new RuntimeException("Test id is required");
        }
        normalize(partGroup);
        partGroup.setIsDeleted(0);
        readingPartGroupMapper.insertReadingPartGroup(partGroup);
        return partGroup;
    }

    @Override
    @Transactional
    public TestPartGroup updatePartGroup(Long id, TestPartGroup partGroup) {
        TestPartGroup existing = readingPartGroupMapper.findActiveById(id);
        if (existing == null) {
            throw new RuntimeException("Reading part group not found");
        }
        if (partGroup == null) {
            throw new RuntimeException("Request body is required");
        }

        existing.setPartNumber(partGroup.getPartNumber());
        existing.setGroupNumber(partGroup.getGroupNumber());
        existing.setTitle(trimToNull(partGroup.getTitle()));
        existing.setInstructionText(trimToNull(partGroup.getInstructionText()));
        existing.setGroupGuideText(trimToNull(partGroup.getGroupGuideText()));
        existing.setGroupRequirementText(trimToNull(partGroup.getGroupRequirementText()));
        existing.setQuestionNoStart(partGroup.getQuestionNoStart());
        existing.setQuestionNoEnd(partGroup.getQuestionNoEnd());
        existing.setDisplayOrder(partGroup.getDisplayOrder());
        existing.setTimeLimitSeconds(partGroup.getTimeLimitSeconds());

        normalize(existing);
        readingPartGroupMapper.updateReadingPartGroup(existing);
        return readingPartGroupMapper.findActiveById(id);
    }

    @Override
    public TestPartGroup getActiveById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        return readingPartGroupMapper.findActiveById(id);
    }

    @Override
    public TestPartGroup getAnyById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        return readingPartGroupMapper.findAnyById(id);
    }

    @Override
    public List<TestPartGroup> listActiveByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        return readingPartGroupMapper.findActiveByTestId(testId);
    }

    @Override
    public List<TestPartGroup> listAnyByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        return readingPartGroupMapper.findAnyByTestId(testId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        readingPartGroupMapper.softDeleteById(id);
    }

    @Override
    @Transactional
    public void restoreById(Long id) {
        if (id == null) {
            throw new RuntimeException("Id is required");
        }
        readingPartGroupMapper.restoreById(id);
    }

    @Override
    @Transactional
    public void deleteByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        readingPartGroupMapper.softDeleteByTestId(testId);
    }

    @Override
    @Transactional
    public void restoreByTestId(Long testId) {
        if (testId == null) {
            throw new RuntimeException("Test id is required");
        }
        readingPartGroupMapper.restoreByTestId(testId);
    }

    private void normalize(TestPartGroup partGroup) {
        partGroup.setTitle(trimToNull(partGroup.getTitle()));
        partGroup.setInstructionText(trimToNull(partGroup.getInstructionText()));
        partGroup.setGroupGuideText(trimToNull(partGroup.getGroupGuideText()));
        partGroup.setGroupRequirementText(trimToNull(partGroup.getGroupRequirementText()));

        if (partGroup.getDisplayOrder() == null) {
            partGroup.setDisplayOrder(0);
        }
        if (partGroup.getTimeLimitSeconds() == null) {
            partGroup.setTimeLimitSeconds(0);
        }
        if (partGroup.getQuestionNoStart() != null && partGroup.getQuestionNoStart() < 1) {
            throw new RuntimeException("questionNoStart must be greater than 0");
        }
        if (partGroup.getQuestionNoEnd() != null && partGroup.getQuestionNoEnd() < 1) {
            throw new RuntimeException("questionNoEnd must be greater than 0");
        }
        if (partGroup.getQuestionNoStart() != null
                && partGroup.getQuestionNoEnd() != null
                && partGroup.getQuestionNoStart() > partGroup.getQuestionNoEnd()) {
            throw new RuntimeException("questionNoStart cannot be greater than questionNoEnd");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}