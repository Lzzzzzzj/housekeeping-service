package com.example.back.controller.staff;

import com.example.back.common.result.Result;
import com.example.back.security.UserContext;
import com.example.back.service.StaffOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 服务人员接单/抢单
     */
    @PostMapping("/grab/{orderId}")
    public Result<Void> grabOrder(@PathVariable Long orderId) {
        Long staffUserId = requireStaffUserId();
        staffOrderService.grabOrder(staffUserId, orderId);
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

