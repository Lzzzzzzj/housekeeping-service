package com.example.back.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 师傅端上报实时经纬度请求体。
 */
@Data
public class StaffLocationUpdateDTO {

    /**
     * 当前经度
     */
    @NotNull(message = "经度不能为空")
    private Double lng;

    /**
     * 当前纬度
     */
    @NotNull(message = "纬度不能为空")
    private Double lat;
}

