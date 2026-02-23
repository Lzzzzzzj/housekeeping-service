package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 服务人员申请 DTO
 */
@Data
public class StaffApplyDTO {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String healthCertUrl;   // 健康证图片地址
    private String skillCertUrls;   // 技能证书图片 JSON 数组，如 ["url1","url2"]
    private String applyReason;    // 申请理由
}
