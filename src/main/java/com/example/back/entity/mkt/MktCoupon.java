package com.example.back.entity.mkt;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板表
 */
@Data
public class MktCoupon {
    private Long id;
    private String name;
    /**
     * 优惠券类型：1-立减券(无门槛)，2-满减券(满X减Y)
     */
    private Integer couponType;
    /**
     * 优惠金额
     */
    private BigDecimal amount;
    /**
     * 满减门槛金额，立减券为 0
     */
    private BigDecimal minAmount;
    /**
     * 购买价格(余额支付)，0 表示不可购买只可兑换
     */
    private BigDecimal price;
    /**
     * 发放总数量，0 表示不限制
     */
    private Integer totalCount;
    /**
     * 每个用户可领取数量上限，0 表示不限制
     */
    private Integer receiveLimit;
    /**
     * 领取渠道：0-余额购买+兑换码，1-仅余额购买，2-仅兑换码
     */
    private Integer obtainChannel;
    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
    private LocalDateTime receiveStartTime;
    private LocalDateTime receiveEndTime;
    private LocalDateTime useStartTime;
    private LocalDateTime useEndTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

