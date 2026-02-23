package com.example.back.entity.sys;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置表
 */
@Data
public class SysConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String remark;
    private LocalDateTime updateTime;
}

