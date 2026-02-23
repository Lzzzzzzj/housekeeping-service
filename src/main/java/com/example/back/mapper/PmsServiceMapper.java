package com.example.back.mapper;

import com.example.back.entity.pms.PmsService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PmsServiceMapper {

    PmsService selectById(@Param("id") Long id);

    List<PmsService> selectByCategoryId(@Param("categoryId") Integer categoryId);

    int insert(PmsService service);

    int update(PmsService service);

    int deleteById(@Param("id") Long id);

    /**
     * 简单分页查询服务列表（可按类目和标题模糊查询）
     */
    List<PmsService> pageQuery(@Param("categoryId") Integer categoryId,
                               @Param("title") String title,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);
}
