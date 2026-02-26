package com.example.back.service.impl;

import com.example.back.dto.OrderPayVO;
import com.example.back.entity.ums.UmsMember;
import com.example.back.entity.ums.UmsMemberRecharge;
import com.example.back.mapper.UmsMemberMapper;
import com.example.back.mapper.UmsMemberRechargeMapper;
import com.example.back.service.UserWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserWalletServiceImpl implements UserWalletService {

    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberRechargeMapper umsMemberRechargeMapper;

    @Override
    @Transactional
    public OrderPayVO createRecharge(Long memberId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        String rechargeSn = "RC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        UmsMemberRecharge record = new UmsMemberRecharge();
        record.setMemberId(memberId);
        record.setRechargeSn(rechargeSn);
        record.setAmount(amount);
        record.setPayChannel(1); // 微信小程序
        record.setPayStatus(0);  // 待支付

        // TODO: 这里应调用微信支付 SDK 创建预支付单，当前为占位实现
        String prepayId = "recharge_prepay_placeholder";

        record.setWechatPrepayId(prepayId);
        umsMemberRechargeMapper.insert(record);

        OrderPayVO vo = new OrderPayVO();
        vo.setOrderSn(rechargeSn);
        vo.setPrepayId(prepayId);
        vo.setPaySign("placeholder");
        vo.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        vo.setNonceStr(UUID.randomUUID().toString().replace("-", ""));
        return vo;
    }

    @Override
    @Transactional
    public void handleRechargeNotify(String rechargeSn, String wechatTransactionId) {
        UmsMemberRecharge record = umsMemberRechargeMapper.selectByRechargeSn(rechargeSn);
        if (record == null) {
            throw new IllegalArgumentException("充值单不存在");
        }
        if (record.getPayStatus() != null && record.getPayStatus() == 1) {
            // 已经处理过，直接返回
            return;
        }

        int updated = umsMemberRechargeMapper.updatePaySuccess(record.getId(), wechatTransactionId);
        if (updated <= 0) {
            // 状态已被其他回调更新，直接返回
            return;
        }

        UmsMember member = umsMemberMapper.selectById(record.getMemberId());
        if (member == null) {
            throw new IllegalStateException("充值用户不存在");
        }
        BigDecimal balance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = balance.add(record.getAmount());
        umsMemberMapper.updateBalance(member.getId(), newBalance);
    }

    @Override
    public List<UmsMemberRecharge> listRechargeRecords(Long memberId) {
        return umsMemberRechargeMapper.selectByMemberId(memberId);
    }
}

