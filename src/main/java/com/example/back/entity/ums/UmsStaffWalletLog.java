package com.example.back.entity.ums;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 师傅钱包流水
 */
@Data
public class UmsStaffWalletLog {
    private Long id;
    private Long staffId;
    private Long orderId;
    /**
     * 1-订单收入, 2-提现, 3-后台调账
     */
    private Integer changeType;
    /**
     * 本次变动金额（正数增加，负数减少）
     */
    private BigDecimal changeAmount;
    /**
     * 变动后的余额快照
     */
    private BigDecimal balanceAfter;
    private String remark;
    private LocalDateTime createTime;
}

