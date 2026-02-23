package com.example.back.mapper;

import com.example.back.entity.pms.PmsCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmsCategoryMapper {

    List<PmsCategory> selectByParentId(@Param("parentId") Integer parentId);

    List<PmsCategory> selectAllShow();
}
