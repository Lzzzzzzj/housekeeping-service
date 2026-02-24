package com.example.back.controller.user;

import com.example.back.common.result.Result;
import com.example.back.dto.CouponBuyDTO;
import com.example.back.dto.CouponRedeemDTO;
import com.example.back.entity.mkt.MktCoupon;
import com.example.back.entity.mkt.MktUserCoupon;
import com.example.back.security.UserContext;
import com.example.back.service.UserCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/coupon")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponService userCouponService;

    private Long requireMemberId() {
        Long memberId = UserContext.getMemberId();
        if (memberId == null) {
            throw new IllegalArgumentException("请登录");
        }
        return memberId;
    }

    /**
     * 我的优惠券列表
     */
    @GetMapping("/my")
    public Result<List<MktUserCoupon>> myCoupons(@RequestParam(required = false) Integer status) {
        Long memberId = requireMemberId();
        List<MktUserCoupon> list = userCouponService.listMyCoupons(memberId, status);
        return Result.success(list);
    }

    /**
     * 优惠券中心列表
     */
    @GetMapping("/center")
    public Result<List<MktCoupon>> center() {
        requireMemberId(); // 需要登录
        List<MktCoupon> list = userCouponService.listCenterCoupons();
        return Result.success(list);
    }

    /**
     * 余额购买优惠券
     */
    @PostMapping("/buy")
    public Result<MktUserCoupon> buy(@Valid @RequestBody CouponBuyDTO dto) {
        Long memberId = requireMemberId();
        MktUserCoupon userCoupon = userCouponService.buy(memberId, dto);
        return Result.success(userCoupon);
    }

    /**
     * 兑换码领取优惠券
     */
    @PostMapping("/redeem")
    public Result<MktUserCoupon> redeem(@Valid @RequestBody CouponRedeemDTO dto) {
        Long memberId = requireMemberId();
        MktUserCoupon userCoupon = userCouponService.redeem(memberId, dto);
        return Result.success(userCoupon);
    }
}

