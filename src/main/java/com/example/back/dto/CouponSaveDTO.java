package com.example.back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponSaveDTO {

    @NotBlank(message = "优惠券名称不能为空")
    private String name;

    /**
     * 优惠券类型：1-立减券，2-满减券
     */
    @NotNull(message = "优惠券类型不能为空")
    private Integer couponType;

    @NotNull(message = "优惠金额不能为空")
    @Min(value = 0, message = "优惠金额不能为负数")
    private BigDecimal amount;

    /**
     * 满减门槛，立减券可为 0
     */
    @NotNull(message = "门槛金额不能为空")
    @Min(value = 0, message = "门槛金额不能为负数")
    private BigDecimal minAmount;

    /**
     * 购买价格(余额支付)，0 表示不可购买
     */
    @NotNull(message = "购买价格不能为空")
    @Min(value = 0, message = "购买价格不能为负数")
    private BigDecimal price;

    private Integer totalCount;

    private Integer receiveLimit;

    /**
     * 领取渠道：0-余额购买+兑换码，1-仅余额购买，2-仅兑换码
     */
    @NotNull(message = "领取渠道不能为空")
    private Integer obtainChannel;

    private Integer status;

    private LocalDateTime receiveStartTime;
    private LocalDateTime receiveEndTime;
    private LocalDateTime useStartTime;
    private LocalDateTime useEndTime;
    private String remark;
}

