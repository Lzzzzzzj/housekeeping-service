package com.example.back.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class OrderCreateDTO {
    @NotNull(message = "服务ID不能为空")
    private Long serviceId;

    @NotNull(message = "预约时间不能为空")
    @Future(message = "预约时间必须为未来时间")
    private LocalDateTime appointmentTime;

    /** 地址快照: {name, phone, lng, lat, address} */
    @NotNull(message = "地址信息不能为空")
    private Map<String, Object> addressInfo;

    /** 动态表单数据 (如几房几厅、有无电梯等) */
    private Map<String, Object> extInfo;

    /**
     * 用户优惠券ID，可为空；一单仅可使用一张券
     */
    private Long userCouponId;
}
