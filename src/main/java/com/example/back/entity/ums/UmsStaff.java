package com.example.back.entity.ums;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务人员(师傅)扩展表
 * audit_status: 0-待审核, 1-已通过, 2-驳回
 * work_status: 0-休息, 1-听单中, 2-服务中
 */
@Data
public class UmsStaff {
    private Long id;
    private Long userId;
    private String realName;
    private String idCard;
    private String healthCertUrl;
    private BigDecimal serviceScore;
    private Integer orderCount;
    private Integer workStatus;
    private Integer auditStatus;
}
