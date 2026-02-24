package com.example.back.service.impl;

import com.example.back.common.constant.OrderStatus;
import com.example.back.dto.OrderCreateDTO;
import com.example.back.dto.OrderPayVO;
import com.example.back.entity.oms.OmsOrder;
import com.example.back.entity.oms.OmsOrderStatusLog;
import com.example.back.entity.pms.PmsService;
import com.example.back.entity.sys.SysConfig;
import com.example.back.entity.ums.UmsStaff;
import com.example.back.entity.ums.UmsStaffWalletLog;
import com.example.back.mapper.OmsOrderMapper;
import com.example.back.mapper.OmsOrderStatusLogMapper;
import com.example.back.mapper.OmsOrderExtraMapper;
import com.example.back.mapper.PmsServiceMapper;
import com.example.back.mapper.SysConfigMapper;
import com.example.back.mapper.UmsMemberMapper;
import com.example.back.mapper.UmsStaffMapper;
import com.example.back.mapper.UmsStaffWalletLogMapper;
import com.example.back.service.AutoDispatchService;
import com.example.back.service.UserOrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserOrderServiceImpl implements UserOrderService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderStatusLogMapper omsOrderStatusLogMapper;
    private final OmsOrderExtraMapper omsOrderExtraMapper;
    private final PmsServiceMapper pmsServiceMapper;
    private final UmsMemberMapper umsMemberMapper;
    private final UmsStaffMapper UmsStaffMapper;
    private final UmsStaffWalletLogMapper umsStaffWalletLogMapper;
    private final SysConfigMapper sysConfigMapper;
    private final AutoDispatchService autoDispatchService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OmsOrder createOrder(Long memberId, OrderCreateDTO dto) {
        if (umsMemberMapper.selectById(memberId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        PmsService service = pmsServiceMapper.selectById(dto.getServiceId());
        if (service == null) {
            throw new IllegalArgumentException("服务不存在");
        }
        if (dto.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("预约时间必须为未来时间");
        }

        String addressJson = toJson(dto.getAddressInfo());
        String extJson = dto.getExtInfo() != null ? toJson(dto.getExtInfo()) : null;

        BigDecimal totalAmount = service.getBasePrice();
        String orderSn = "HK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        OmsOrder order = new OmsOrder();
        order.setOrderSn(orderSn);
        order.setMemberId(memberId);
        order.setStaffId(null);
        order.setServiceId(service.getId());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setAppointmentTime(dto.getAppointmentTime());
        order.setAddressInfo(addressJson);
        order.setExtInfo(extJson);

        omsOrderMapper.insert(order);

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(order.getId());
        log.setPreStatus(null);
        log.setPostStatus(OrderStatus.PENDING_PAY);
        log.setOperator("user:" + memberId);
        log.setRemark("用户创建订单");
        omsOrderStatusLogMapper.insert(log);

        return omsOrderMapper.selectById(order.getId());
    }

    @Override
    public OrderPayVO pay(Long memberId, String orderSn) {
        OmsOrder order = omsOrderMapper.selectByOrderSn(orderSn);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new IllegalArgumentException("订单状态不允许支付");
        }

        // TODO: 调用微信支付 SDK 生成预支付单
        OrderPayVO vo = new OrderPayVO();
        vo.setOrderSn(orderSn);
        vo.setPrepayId("prepay_placeholder");
        vo.setPaySign("placeholder");
        vo.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        vo.setNonceStr(UUID.randomUUID().toString().replace("-", ""));
        return vo;
    }

    @Override
    @Transactional
    public void confirmPaySuccess(Long memberId, String orderSn) {
        OmsOrder order = omsOrderMapper.selectByOrderSn(orderSn);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (memberId != null && !order.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw new IllegalArgumentException("订单状态不允许确认支付");
        }

        omsOrderMapper.updateStatus(order.getId(), OrderStatus.PENDING_ACCEPT);
        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(order.getId());
        log.setPreStatus(OrderStatus.PENDING_PAY);
        log.setPostStatus(OrderStatus.PENDING_ACCEPT);
        log.setOperator(memberId != null ? "user:" + memberId : "notify:pay");
        log.setRemark("支付成功，待接单");
        omsOrderStatusLogMapper.insert(log);

        autoDispatchService.tryAutoAssign(order.getId());
    }

    @Override
    public List<OmsOrder> listOrders(Long memberId, Integer status) {
        return omsOrderMapper.selectByMemberId(memberId, status);
    }

    @Override
    @Transactional
    public void cancelOrder(Long memberId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("订单已取消");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("订单已完成，无法取消");
        }

        int preStatus = order.getStatus();
        omsOrderMapper.updateStatus(orderId, OrderStatus.CANCELLED);

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(orderId);
        log.setPreStatus(preStatus);
        log.setPostStatus(OrderStatus.CANCELLED);
        log.setOperator("user:" + memberId);
        log.setRemark("用户取消订单");
        omsOrderStatusLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void confirmOrder(Long memberId, Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("订单已取消，无法确认完成");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("订单已完成");
        }
        if (order.getStatus() != OrderStatus.PENDING_SETTLE) {
            throw new IllegalArgumentException("当前订单状态不允许确认完成");
        }

        int preStatus = order.getStatus();

        // 1. 计算本单总收入（实付 + 已支付增项金额）
        BigDecimal orderIncome = order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO;
        BigDecimal extraPaidAmount = omsOrderExtraMapper.sumPaidAmountByOrderId(orderId);
        if (extraPaidAmount != null) {
            orderIncome = orderIncome.add(extraPaidAmount);
        }

        // 2. 读取结算配置（固定金额 / 百分比），计算平台抽成和师傅收入
        BigDecimal platformAmount = BigDecimal.ZERO;
        BigDecimal staffAmount = BigDecimal.ZERO;

        SysConfig modeCfg = sysConfigMapper.selectByKey("settle.mode");
        String mode = modeCfg != null ? modeCfg.getConfigValue() : "percent";

        if ("fixed".equalsIgnoreCase(mode)) {
            SysConfig fixedCfg = sysConfigMapper.selectByKey("settle.fixed_amount");
            BigDecimal fixedAmount = fixedCfg != null ? new BigDecimal(fixedCfg.getConfigValue()) : BigDecimal.ZERO;
            if (fixedAmount.compareTo(orderIncome) > 0) {
                platformAmount = orderIncome;
            } else {
                platformAmount = fixedAmount;
            }
            staffAmount = orderIncome.subtract(platformAmount);
        } else { // 默认按百分比
            SysConfig percentCfg = sysConfigMapper.selectByKey("settle.percent");
            BigDecimal percent = percentCfg != null ? new BigDecimal(percentCfg.getConfigValue()) : new BigDecimal("0.2");
            if (percent.compareTo(BigDecimal.ZERO) < 0) {
                percent = BigDecimal.ZERO;
            }
            if (percent.compareTo(BigDecimal.ONE) > 0) {
                percent = BigDecimal.ONE;
            }
            platformAmount = orderIncome.multiply(percent);
            staffAmount = orderIncome.subtract(platformAmount);
        }

        if (staffAmount.compareTo(BigDecimal.ZERO) < 0) {
            staffAmount = BigDecimal.ZERO;
        }

        // 3. 更新订单结算字段并标记为已完成
        omsOrderMapper.updateSettleInfo(orderId, platformAmount, staffAmount, LocalDateTime.now(), OrderStatus.COMPLETED);

        // 4. 将师傅收入计入师傅钱包余额，并写入钱包流水
        if (order.getStaffId() != null && staffAmount.compareTo(BigDecimal.ZERO) > 0) {
            UmsStaff staff = UmsStaffMapper.selectById(order.getStaffId());
            if (staff != null) {
                BigDecimal beforeBalance = staff.getBalance() != null ? staff.getBalance() : BigDecimal.ZERO;
                BigDecimal afterBalance = beforeBalance.add(staffAmount);
                staff.setBalance(afterBalance);
                UmsStaffMapper.updateBalance(staff.getId(), afterBalance);

                UmsStaffWalletLog walletLog = new UmsStaffWalletLog();
                walletLog.setStaffId(staff.getId());
                walletLog.setOrderId(orderId);
                walletLog.setChangeType(1);
                walletLog.setChangeAmount(staffAmount);
                walletLog.setBalanceAfter(afterBalance);
                walletLog.setRemark("订单收入结算");
                umsStaffWalletLogMapper.insert(walletLog);
            }
        }

        OmsOrderStatusLog log = new OmsOrderStatusLog();
        log.setOrderId(orderId);
        log.setPreStatus(preStatus);
        log.setPostStatus(OrderStatus.COMPLETED);
        log.setOperator("user:" + memberId);
        log.setRemark("用户确认完成，订单结算完成");
        omsOrderStatusLogMapper.insert(log);
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
}
