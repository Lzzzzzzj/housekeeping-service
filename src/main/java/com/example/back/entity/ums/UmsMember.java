package com.example.back.entity.ums;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 客户信息表
 */
@Data
public class UmsMember {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private Integer memberLevel;
}
