package com.example.back.mapper;

import com.example.back.entity.pms.PmsCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmsCategoryMapper {
    List<PmsCategory> selectByParentId(@Param("parentId") Integer parentId);

    List<PmsCategory> selectAllShow();

    PmsCategory selectById(@Param("id") Integer id);

    int insert(PmsCategory category);

    int update(PmsCategory category);

    int deleteById(@Param("id") Integer id);

    /**
     * 分页查询类目列表，支持按名称模糊查询
     */
    List<PmsCategory> pageQuery(@Param("name") String name,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);
}
