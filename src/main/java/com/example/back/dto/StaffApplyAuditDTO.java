package com.example.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员审核服务人员申请 DTO
 */
@Data
public class StaffApplyAuditDTO {
    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    /** true-通过, false-驳回 */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    /** 驳回原因（驳回时必填） */
    private String rejectReason;
}
