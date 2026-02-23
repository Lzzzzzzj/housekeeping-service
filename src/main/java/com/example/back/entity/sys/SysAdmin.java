package com.example.back.entity.sys;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员扩展表
 * admin_type: 0-普通管理员, 1-超级管理员
 */
@Data
public class SysAdmin {
    private Long id;
    private Long userId;
    private Integer adminType;
    private BigDecimal balance;
    private Long createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

