package com.andrew.smartielts.listening.service.admin;

import com.andrew.smartielts.listening.domain.pojo.ListeningMaterial;

import java.util.List;

public interface ListeningMaterialService {

    ListeningMaterial createMaterial(ListeningMaterial material);

    ListeningMaterial updateMaterial(Long id, ListeningMaterial material);

    ListeningMaterial getActiveById(Long id);

    ListeningMaterial getAnyById(Long id);

    List<ListeningMaterial> listActiveByTestId(Long testId);

    List<ListeningMaterial> listAnyByTestId(Long testId);

    List<ListeningMaterial> listActiveByPartGroupId(Long partGroupId);

    List<ListeningMaterial> listAnyByPartGroupId(Long partGroupId);

    void deleteById(Long id);

    void restoreById(Long id);

    void deleteByTestId(Long testId);

    void restoreByTestId(Long testId);

    void deleteByPartGroupId(Long partGroupId);

    void restoreByPartGroupId(Long partGroupId);
}