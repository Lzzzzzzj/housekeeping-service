package com.example.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCancelDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
