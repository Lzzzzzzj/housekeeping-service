package com.example.back.dto;

import com.example.back.entity.pms.PmsCategory;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryVO {
    private Integer id;
    private Integer parentId;
    private String name;
    private String icon;
    private String formConfig;
    private Integer sort;
    private List<CategoryVO> children = new ArrayList<>();

    public static CategoryVO from(PmsCategory entity) {
        CategoryVO vo = new CategoryVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setName(entity.getName());
        vo.setIcon(entity.getIcon());
        vo.setFormConfig(entity.getFormConfig());
        vo.setSort(entity.getSort());
        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            vo.setChildren(entity.getChildren().stream().map(CategoryVO::from).collect(Collectors.toList()));
        }
        return vo;
    }
}
