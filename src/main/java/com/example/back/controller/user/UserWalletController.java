package com.example.back.controller.user;

import com.example.back.common.result.Result;
import com.example.back.dto.OrderPayVO;
import com.example.back.entity.ums.UmsMemberRecharge;
import com.example.back.security.UserContext;
import com.example.back.service.UserWalletService;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserWalletController {

    private final UserWalletService userWalletService;

    /**
     * 发起余额充值（微信预下单）
     */
    @PostMapping("/wallet/recharge/create")
    public Result<OrderPayVO> createRecharge(@RequestParam @DecimalMin("0.01") BigDecimal amount) {
        Long memberId = requireMemberId();
        OrderPayVO vo = userWalletService.createRecharge(memberId, amount);
        return Result.success(vo);
    }

    /**
     * 微信充值支付成功回调（业务入口，具体签名校验在网关层处理）
     */
    @PostMapping("/wallet/recharge/notify")
    public Result<Void> rechargeNotify(@RequestParam String rechargeSn,
                                       @RequestParam String transactionId) {
        userWalletService.handleRechargeNotify(rechargeSn, transactionId);
        return Result.success();
    }

    /**
     * 查询余额充值记录
     */
    @GetMapping("/wallet/recharge/list")
    public Result<List<UmsMemberRecharge>> listRecharge() {
        Long memberId = requireMemberId();
        List<UmsMemberRecharge> list = userWalletService.listRechargeRecords(memberId);
        return Result.success(list);
    }

    private Long requireMemberId() {
        Long memberId = UserContext.getMemberId();
        if (memberId == null) {
            throw new IllegalArgumentException("请登录");
        }
        return memberId;
    }
}

