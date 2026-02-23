package com.example.back.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结算配置 DTO：用于后台设置平台抽成模式及参数
 */
@Data
public class SettleConfigDTO {

    /**
     * 模式：fixed / percent
     */
    @NotBlank(message = "结算模式不能为空")
    private String mode;

    /**
     * 固定扣费金额（mode = fixed 时生效）
     */
    @DecimalMin(value = "0.00", message = "固定扣费不能为负数")
    private BigDecimal fixedAmount;

    /**
     * 抽成比例（0-1 之间的小数，mode = percent 时生效）
     */
    @DecimalMin(value = "0.00", message = "抽成比例不能小于0")
    @DecimalMax(value = "1.00", message = "抽成比例不能大于1")
    private BigDecimal percent;
}

