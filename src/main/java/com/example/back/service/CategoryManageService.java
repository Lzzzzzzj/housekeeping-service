package com.example.back.service;

import com.example.back.dto.CategorySaveDTO;
import com.example.back.entity.pms.PmsCategory;

import java.util.List;

/**
 * 服务类目管理（增删改查）
 */
public interface CategoryManageService {

    void create(CategorySaveDTO dto);

    void update(Integer id, CategorySaveDTO dto);

    void delete(Integer id);

    PmsCategory getById(Integer id);

    /**
     * 分页查询类目列表（按名称模糊查询）
     */
    List<PmsCategory> page(String name, Integer page, Integer pageSize);
}

