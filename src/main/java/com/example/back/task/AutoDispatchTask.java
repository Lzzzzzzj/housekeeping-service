package com.example.back.task;

import com.example.back.common.constant.OrderStatus;
import com.example.back.entity.oms.OmsOrder;
import com.example.back.mapper.OmsOrderMapper;
import com.example.back.service.AutoDispatchService;
import com.example.back.service.OrderGrabPoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时扫描待接单且未分配师傅的订单，尝试自动派单（兜底）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoDispatchTask {

    private final OmsOrderMapper omsOrderMapper;
    private final AutoDispatchService autoDispatchService;
    private final OrderGrabPoolService orderGrabPoolService;

    @Scheduled(fixedDelayString = "${app.auto-dispatch.interval-ms:60000}")
    public void run() {
        // 兜底：数据库为准，扫描仍处于待接单状态的订单
        List<OmsOrder> pool = omsOrderMapper.selectGrabPool(OrderStatus.PENDING_ACCEPT);
        if (pool.isEmpty()) return;
        for (OmsOrder order : pool) {
            try {
                // 确保在兜底扫描时，订单也在 Redis 抢单池中
                double score = order.getAppointmentTime() != null
                        ? order.getAppointmentTime().atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
                        : System.currentTimeMillis() / 1000.0;
                orderGrabPoolService.addToPool(order.getId(), score);
                autoDispatchService.tryAutoAssign(order.getId());
            } catch (Exception e) {
                log.warn("自动派单兜底失败 orderId={}", order.getId(), e);
            }
        }
    }
}
