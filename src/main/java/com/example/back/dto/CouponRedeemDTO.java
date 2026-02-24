package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CouponRedeemDTO {

    @NotBlank(message = "兑换码不能为空")
    private String code;
}

