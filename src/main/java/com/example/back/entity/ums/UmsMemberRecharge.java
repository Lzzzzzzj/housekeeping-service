package com.example.back.entity.ums;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户余额充值记录
 */
@Data
public class UmsMemberRecharge {
    private Long id;
    private Long memberId;
    private String rechargeSn;
    private BigDecimal amount;
    private Integer payChannel;
    private Integer payStatus;
    private String wechatPrepayId;
    private String wechatTransactionId;
    private LocalDateTime notifyTime;
    private LocalDateTime createTime;
}

