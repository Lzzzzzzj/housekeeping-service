package com.example.back.service.impl;

import com.example.back.dto.CategoryVO;
import com.example.back.entity.pms.PmsCategory;
import com.example.back.mapper.PmsCategoryMapper;
import com.example.back.service.UserCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCategoryServiceImpl implements UserCategoryService {

    private final PmsCategoryMapper pmsCategoryMapper;

    @Override
    public List<CategoryVO> listCategoryTree() {
        List<PmsCategory> all = pmsCategoryMapper.selectAllShow();
        List<PmsCategory> roots = all.stream().filter(c -> c.getParentId() == null || c.getParentId() == 0).collect(Collectors.toList());
        List<PmsCategory> children = all.stream().filter(c -> c.getParentId() != null && c.getParentId() > 0).collect(Collectors.toList());

        for (PmsCategory root : roots) {
            root.setChildren(children.stream()
                    .filter(c -> c.getParentId().equals(root.getId()))
                    .collect(Collectors.toList()));
        }
        return roots.stream().map(CategoryVO::from).collect(Collectors.toList());
    }
}
