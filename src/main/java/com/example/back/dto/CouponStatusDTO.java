package com.example.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CouponStatusDTO {

    /**
     * 状态：0-下架，1-上架
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}

