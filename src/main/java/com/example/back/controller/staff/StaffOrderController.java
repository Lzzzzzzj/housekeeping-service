package com.example.back.controller.staff;

import com.example.back.common.result.Result;
import com.example.back.dto.OrderActionDTO;
import com.example.back.dto.OrderExtraFeeDTO;
import com.example.back.entity.oms.OmsOrder;
import com.example.back.security.UserContext;
import com.example.back.service.StaffOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 师傅端 - 订单相关接口
 */
@RestController
@RequestMapping("/api/v1/staff/order")
@RequiredArgsConstructor
public class StaffOrderController {

    private static final int USER_TYPE_STAFF = 2;

    private final StaffOrderService staffOrderService;

    /**
     * 抢单池/待接订单列表
     */
    @GetMapping("/pool")
    public Result<List<OmsOrder>> pool() {
        Long staffUserId = requireStaffUserId();
        List<OmsOrder> list = staffOrderService.listGrabPool(staffUserId);
        return Result.success(list);
    }

    /**
     * 服务人员接单/抢单
     */
    @PostMapping("/grab/{orderId}")
    public Result<Void> grabOrder(@PathVariable Long orderId) {
        Long staffUserId = requireStaffUserId();
        staffOrderService.grabOrder(staffUserId, orderId);
        return Result.success();
    }

    /**
     * 服务过程中的动作（出发/到达/开始/完成）
     */
    @PutMapping("/action")
    public Result<Void> action(@Valid @RequestBody OrderActionDTO dto) {
        Long staffUserId = requireStaffUserId();
        staffOrderService.processAction(staffUserId, dto);
        return Result.success();
    }

    /**
     * 发起现场加价/增项单
     */
    @PostMapping("/extra-fee")
    public Result<Void> extraFee(@Valid @RequestBody OrderExtraFeeDTO dto) {
        Long staffUserId = requireStaffUserId();
        staffOrderService.createExtraFee(staffUserId, dto);
        return Result.success();
    }

    private Long requireStaffUserId() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_STAFF) {
            throw new IllegalArgumentException("需要服务人员权限");
        }
        return userId;
    }
}

