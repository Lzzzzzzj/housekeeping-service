package com.example.back.entity.ums;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务人员申请表
 * status: 0-待审核, 1-已通过, 2-驳回
 */
@Data
public class UmsStaffApply {
    private Long id;
    private Long userId;
    private String realName;
    private String idCard;
    private String phone;
    private String healthCertUrl;
    private String skillCertUrls;  // JSON 数组
    private String applyReason;
    private Integer status;        // 0-待审核, 1-已通过, 2-驳回
    private String rejectReason;
    private LocalDateTime auditTime;
    private Long auditUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
