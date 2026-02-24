package com.example.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CouponBuyDTO {

    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;
}

