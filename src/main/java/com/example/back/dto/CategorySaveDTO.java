package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 类目保存 DTO（管理员创建/修改）
 */
@Data
public class CategorySaveDTO {

    private Integer parentId;

    @NotBlank(message = "类目名称不能为空")
    private String name;

    private String icon;

    /**
     * 动态表单配置(JSON字符串)
     */
    private String formConfig;

    private Integer sort;

    /**
     * 是否展示：1-展示，0-不展示
     */
    private Integer isShow;
}

