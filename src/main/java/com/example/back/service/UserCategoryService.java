package com.example.back.service;

import com.example.back.dto.CategoryVO;

import java.util.List;

public interface UserCategoryService {

    /**
     * 获取全类目树形列表
     */
    List<CategoryVO> listCategoryTree();
}
