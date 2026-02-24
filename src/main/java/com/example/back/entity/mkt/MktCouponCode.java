package com.example.back.entity.mkt;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优惠券兑换码表
 */
@Data
public class MktCouponCode {
    private Long id;
    private Long couponId;
    private String code;
    private Long boundMemberId;
    private Integer used;
    private LocalDateTime usedTime;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}

