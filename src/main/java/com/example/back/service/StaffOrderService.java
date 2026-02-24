package com.example.back.service;

import com.example.back.dto.OrderActionDTO;
import com.example.back.dto.OrderExtraFeeDTO;
import com.example.back.entity.oms.OmsOrder;

import java.util.List;

/**
 * 师傅端 - 订单相关服务
 */
public interface StaffOrderService {

    /**
     * 服务人员接单/抢单
     *
     * @param staffUserId 当前登录服务人员对应的 sys_user.id
     * @param orderId     要接的订单ID
     */
    void grabOrder(Long staffUserId, Long orderId);

    /**
     * 抢单池/待接订单列表（简化版：当前所有待接单）
     */
    List<OmsOrder> listGrabPool(Long staffUserId);

    /**
     * 处理师傅服务过程中的动作（出发/到达/开始/完成）
     */
    void processAction(Long staffUserId, OrderActionDTO dto);

    /**
     * 发起现场加价/增项单
     */
    void createExtraFee(Long staffUserId, OrderExtraFeeDTO dto);

    /**
     * 开启/关闭自动接单
     */
    void updateAutoAccept(Long staffUserId, boolean enable);
}

