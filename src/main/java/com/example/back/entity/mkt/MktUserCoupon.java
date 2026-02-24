package com.example.back.entity.mkt;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户优惠券表
 */
@Data
public class MktUserCoupon {
    private Long id;
    private Long couponId;
    private Long memberId;
    /**
     * 状态：0-未使用，1-已使用，2-已过期
     */
    private Integer status;
    /**
     * 获取方式：1-余额购买，2-兑换码
     */
    private Integer obtainType;
    private LocalDateTime obtainTime;
    private LocalDateTime useTime;
    private Long orderId;
    private LocalDateTime expireTime;
}

