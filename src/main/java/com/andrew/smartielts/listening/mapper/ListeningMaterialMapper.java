package com.andrew.smartielts.listening.mapper;

import com.andrew.smartielts.listening.domain.pojo.ListeningMaterial;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ListeningMaterialMapper {

    int insertListeningMaterial(ListeningMaterial material);

    ListeningMaterial findActiveById(@Param("id") Long id);

    ListeningMaterial findAnyById(@Param("id") Long id);

    List<ListeningMaterial> findActiveByTestId(@Param("testId") Long testId);

    List<ListeningMaterial> findAnyByTestId(@Param("testId") Long testId);

    List<ListeningMaterial> findActiveByPartGroupId(@Param("partGroupId") Long partGroupId);

    List<ListeningMaterial> findAnyByPartGroupId(@Param("partGroupId") Long partGroupId);

    int updateListeningMaterial(ListeningMaterial material);

    int softDeleteById(@Param("id") Long id);

    int softDeleteByTestId(@Param("testId") Long testId);

    int softDeleteByPartGroupId(@Param("partGroupId") Long partGroupId);

    int restoreById(@Param("id") Long id);

    int restoreByTestId(@Param("testId") Long testId);

    int restoreByPartGroupId(@Param("partGroupId") Long partGroupId);
}