package com.example.back.entity.oms;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表
 * 状态: 10-待支付, 20-待接单, 30-待服务, 40-服务中, 50-待结算, 60-已完成, 70-已取消
 */
@Data
public class OmsOrder {
    private Long id;
    private String orderSn;
    private Long memberId;
    private Long staffId;
    private Long serviceId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private LocalDateTime appointmentTime;
    private String addressInfo;   // JSON 地址快照
    private String extInfo;       // JSON 动态表单数据
    /**
     * 本单平台抽成金额
     */
    private BigDecimal platformAmount;
    /**
     * 本单师傅收入金额
     */
    private BigDecimal staffAmount;
    /**
     * 结算完成时间
     */
    private LocalDateTime settleTime;
    private LocalDateTime createTime;

    /** 关联查询：服务标题 */
    private String serviceTitle;
}
