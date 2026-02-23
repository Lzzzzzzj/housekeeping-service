package com.example.back.entity.pms;

import lombok.Data;

import java.util.List;

/**
 * 服务类目表
 */
@Data
public class PmsCategory {
    private Integer id;
    private Integer parentId;
    private String name;
    private String icon;
    private String formConfig;  // JSON
    private Integer sort;
    private Integer isShow;

    /** 子类目（树形结构） */
    private List<PmsCategory> children;
}
