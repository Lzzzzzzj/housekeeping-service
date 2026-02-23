package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderActionDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 动作：DEPART(出发), ARRIVE(到达), START(开始), FINISH(完成)
     */
    @NotBlank(message = "动作不能为空")
    private String action;

    /**
     * 当前经纬度（可选）
     */
    private Double lng;
    private Double lat;

    /**
     * 现场照片URL列表（可选）
     */
    private List<String> photos;
}

