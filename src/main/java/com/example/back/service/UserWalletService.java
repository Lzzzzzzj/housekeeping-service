package com.example.back.service;

import com.example.back.dto.OrderPayVO;
import com.example.back.entity.ums.UmsMemberRecharge;

import java.math.BigDecimal;
import java.util.List;

public interface UserWalletService {

    /**
     * 发起余额充值：生成充值单并创建微信预支付单（占位实现）
     */
    OrderPayVO createRecharge(Long memberId, BigDecimal amount);

    /**
     * 微信支付回调成功：标记充值单成功并将金额累加到用户余额
     */
    void handleRechargeNotify(String rechargeSn, String wechatTransactionId);

    /**
     * 查询用户余额充值记录
     */
    List<UmsMemberRecharge> listRechargeRecords(Long memberId);
}

