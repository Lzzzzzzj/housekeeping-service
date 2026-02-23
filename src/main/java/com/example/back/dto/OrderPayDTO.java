package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderPayDTO {
    @NotBlank(message = "订单号不能为空")
    private String orderSn;
}
