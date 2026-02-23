package com.example.back.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务人员申请 VO（返回给前端）
 */
@Data
public class StaffApplyVO {
    private Long id;
    private Long userId;
    private String realName;
    private String idCard;
    private String phone;
    private String healthCertUrl;
    private String skillCertUrls;
    private String applyReason;
    private Integer status;        // 0-待审核, 1-已通过, 2-驳回
    private String rejectReason;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;

    // 管理员列表用：申请人昵称
    private String applicantNickname;
    private String applicantUsername;
}
