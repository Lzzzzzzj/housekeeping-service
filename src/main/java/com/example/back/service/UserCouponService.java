package com.example.back.service;

import com.example.back.dto.CouponBuyDTO;
import com.example.back.dto.CouponRedeemDTO;
import com.example.back.entity.mkt.MktCoupon;
import com.example.back.entity.mkt.MktUserCoupon;

import java.util.List;

public interface UserCouponService {

    /**
     * 我的优惠券列表
     */
    List<MktUserCoupon> listMyCoupons(Long memberId, Integer status);

    /**
     * 优惠券中心列表
     */
    List<MktCoupon> listCenterCoupons();

    /**
     * 余额购买优惠券
     */
    MktUserCoupon buy(Long memberId, CouponBuyDTO dto);

    /**
     * 兑换码领取优惠券
     */
    MktUserCoupon redeem(Long memberId, CouponRedeemDTO dto);
}

