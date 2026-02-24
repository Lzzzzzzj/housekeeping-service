package com.example.back.service.impl;

import com.example.back.dto.CategorySaveDTO;
import com.example.back.entity.pms.PmsCategory;
import com.example.back.mapper.PmsCategoryMapper;
import com.example.back.service.CategoryManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryManageServiceImpl implements CategoryManageService {

    private final PmsCategoryMapper pmsCategoryMapper;

    @Override
    @Transactional
    public void create(CategorySaveDTO dto) {
        PmsCategory category = new PmsCategory();
        BeanUtils.copyProperties(dto, category);
        if (category.getIsShow() == null) {
            category.setIsShow(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        pmsCategoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void update(Integer id, CategorySaveDTO dto) {
        PmsCategory exist = pmsCategoryMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("类目不存在");
        }
        BeanUtils.copyProperties(dto, exist);
        exist.setId(id);
        if (exist.getIsShow() == null) {
            exist.setIsShow(1);
        }
        if (exist.getSort() == null) {
            exist.setSort(0);
        }
        pmsCategoryMapper.update(exist);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        PmsCategory exist = pmsCategoryMapper.selectById(id);
        if (exist == null) {
            throw new IllegalArgumentException("类目不存在");
        }
        pmsCategoryMapper.deleteById(id);
    }

    @Override
    public PmsCategory getById(Integer id) {
        return pmsCategoryMapper.selectById(id);
    }

    @Override
    public List<PmsCategory> page(String name, Integer page, Integer pageSize) {
        int pageNo = (page != null && page > 0) ? page : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 20;
        int offset = (pageNo - 1) * size;
        return pmsCategoryMapper.pageQuery(name, offset, size);
    }
}

