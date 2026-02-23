package com.example.back.common.constant;

/**
 * 订单状态
 */
public final class OrderStatus {
    public static final int PENDING_PAY = 10;      // 待支付定金
    public static final int PENDING_ACCEPT = 20;   // 待接单/派单
    public static final int PENDING_SERVICE = 30;  // 待服务
    public static final int IN_SERVICE = 40;       // 服务中
    public static final int PENDING_SETTLE = 50;   // 待结算
    public static final int COMPLETED = 60;        // 已完成
    public static final int CANCELLED = 70;        // 已取消
}
