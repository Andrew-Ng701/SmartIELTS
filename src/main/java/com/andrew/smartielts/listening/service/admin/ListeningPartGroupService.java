package com.andrew.smartielts.listening.service.admin;

import com.andrew.smartielts.common.domain.pojo.TestPartGroup;

import java.util.List;

public interface ListeningPartGroupService {

    TestPartGroup createPartGroup(TestPartGroup partGroup);

    TestPartGroup updatePartGroup(Long id, TestPartGroup partGroup);

    TestPartGroup getActiveById(Long id);

    TestPartGroup getAnyById(Long id);

    List<TestPartGroup> listActiveByTestId(Long testId);

    List<TestPartGroup> listAnyByTestId(Long testId);

    void deleteById(Long id);

    void restoreById(Long id);

    void deleteByTestId(Long testId);

    void restoreByTestId(Long testId);
}