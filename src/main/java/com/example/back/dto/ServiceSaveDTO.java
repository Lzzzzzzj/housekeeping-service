package com.example.back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务类型创建/修改 DTO
 */
@Data
public class ServiceSaveDTO {

    @NotNull(message = "类目ID不能为空")
    private Integer categoryId;

    @NotBlank(message = "服务标题不能为空")
    private String title;

    @NotNull(message = "基础价格不能为空")
    @Min(value = 0, message = "基础价格不能为负数")
    private BigDecimal basePrice;

    @NotBlank(message = "计费单位不能为空")
    private String unit;

    /**
     * 支付模式: 0-全额支付, 1-只付定金, 2-线下报价
     */
    @NotNull(message = "支付模式不能为空")
    private Integer depositType;

    private String description;

    /**
     * 是否参与优惠券活动: 1-参与, 0-不参与；默认参与
     */
    private Integer allowCoupon;
}

