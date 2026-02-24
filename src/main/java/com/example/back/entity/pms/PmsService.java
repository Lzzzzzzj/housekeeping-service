package com.example.back.entity.pms;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务产品表
 */
@Data
public class PmsService {
    private Long id;
    private Integer categoryId;
    private String title;
    private BigDecimal basePrice;
    private String unit;
    private Integer depositType;  // 0-全额支付, 1-只付定金, 2-线下报价
    private String description;
    /**
     * 是否参与优惠券活动: 1-参与, 0-不参与
     */
    private Integer allowCoupon;
}
