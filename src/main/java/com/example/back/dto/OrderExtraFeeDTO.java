package com.example.back.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderExtraFeeDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "加价原因不能为空")
    private String title;

    @NotNull(message = "加价金额不能为空")
    @DecimalMin(value = "0.01", message = "加价金额必须大于0")
    private BigDecimal amount;

    /**
     * 现场照片URL列表（可选）
     */
    private List<String> photos;
}

