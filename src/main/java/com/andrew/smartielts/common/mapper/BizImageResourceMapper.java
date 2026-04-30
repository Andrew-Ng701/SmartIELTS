package com.andrew.smartielts.common.mapper;

import com.andrew.smartielts.common.domain.pojo.BizImageResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizImageResourceMapper {

    int insert(BizImageResource entity);

    List<BizImageResource> findActiveByTarget(@Param("targetType") String targetType,
                                              @Param("targetId") Long targetId);

    List<BizImageResource> findActiveByTargets(@Param("targetType") String targetType,
                                               @Param("targetIds") List<Long> targetIds);

    int softDeleteByTarget(@Param("targetType") String targetType,
                           @Param("targetId") Long targetId);
}