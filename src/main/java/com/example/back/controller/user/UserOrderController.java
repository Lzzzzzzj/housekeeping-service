package com.example.back.controller.user;

import com.example.back.common.result.Result;
import com.example.back.dto.*;
import com.example.back.entity.oms.OmsOrder;
import com.example.back.security.UserContext;
import com.example.back.service.UserOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserOrderController {

    private final UserOrderService userOrderService;

    /**
     * 用户提交订单
     */
    @PostMapping("/order/create")
    public Result<OmsOrder> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        Long memberId = requireMemberId();
        OmsOrder order = userOrderService.createOrder(memberId, dto);
        return Result.success(order);
    }

    /**
     * 发起微信支付
     */
    @PostMapping("/order/pay")
    public Result<OrderPayVO> pay(@Valid @RequestBody OrderPayDTO dto) {
        Long memberId = requireMemberId();
        OrderPayVO vo = userOrderService.pay(memberId, dto.getOrderSn());
        return Result.success(vo);
    }

    /**
     * 个人订单中心 (支持按状态过滤)
     */
    @GetMapping("/order/list")
    public Result<List<OmsOrder>> listOrders(@RequestParam(required = false) Integer status) {
        Long memberId = requireMemberId();
        List<OmsOrder> list = userOrderService.listOrders(memberId, status);
        return Result.success(list);
    }

    /**
     * 取消订单
     */
    @PostMapping("/order/cancel")
    public Result<Void> cancelOrder(@Valid @RequestBody OrderCancelDTO dto) {
        Long memberId = requireMemberId();
        userOrderService.cancelOrder(memberId, dto.getOrderId());
        return Result.success();
    }

    private Long requireMemberId() {
        Long memberId = UserContext.getMemberId();
        if (memberId == null) {
            throw new IllegalArgumentException("请登录");
        }
        return memberId;
    }
}
