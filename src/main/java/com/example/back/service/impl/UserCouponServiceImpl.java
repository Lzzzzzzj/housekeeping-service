package com.example.back.service.impl;

import com.example.back.dto.CouponBuyDTO;
import com.example.back.dto.CouponRedeemDTO;
import com.example.back.entity.mkt.MktCoupon;
import com.example.back.entity.mkt.MktCouponCode;
import com.example.back.entity.mkt.MktUserCoupon;
import com.example.back.entity.ums.UmsMember;
import com.example.back.mapper.MktCouponCodeMapper;
import com.example.back.mapper.MktCouponMapper;
import com.example.back.mapper.MktUserCouponMapper;
import com.example.back.mapper.UmsMemberMapper;
import com.example.back.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final MktCouponMapper mktCouponMapper;
    private final MktCouponCodeMapper mktCouponCodeMapper;
    private final MktUserCouponMapper mktUserCouponMapper;
    private final UmsMemberMapper umsMemberMapper;

    @Override
    public List<MktUserCoupon> listMyCoupons(Long memberId, Integer status) {
        return mktUserCouponMapper.selectByMember(memberId, status);
    }

    @Override
    public List<MktCoupon> listCenterCoupons() {
        return mktCouponMapper.selectAvailableForCenter();
    }

    @Override
    @Transactional
    public MktUserCoupon buy(Long memberId, CouponBuyDTO dto) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        MktCoupon coupon = mktCouponMapper.selectById(dto.getCouponId());
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new IllegalArgumentException("优惠券不存在或已下架");
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getReceiveStartTime() != null && coupon.getReceiveStartTime().isAfter(now)) {
            throw new IllegalArgumentException("当前不在领取时间内");
        }
        if (coupon.getReceiveEndTime() != null && coupon.getReceiveEndTime().isBefore(now)) {
            throw new IllegalArgumentException("当前不在领取时间内");
        }
        if (coupon.getObtainChannel() != null && coupon.getObtainChannel() == 2) {
            throw new IllegalArgumentException("该优惠券不可通过余额购买");
        }
        BigDecimal price = coupon.getPrice() != null ? coupon.getPrice() : BigDecimal.ZERO;
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("该优惠券不可通过余额购买");
        }
        if (coupon.getReceiveLimit() != null && coupon.getReceiveLimit() > 0) {
            int count = mktUserCouponMapper.countByMemberAndCoupon(memberId, coupon.getId());
            if (count >= coupon.getReceiveLimit()) {
                throw new IllegalArgumentException("领取数量已达上限");
            }
        }
        BigDecimal balance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
        if (balance.compareTo(price) < 0) {
            throw new IllegalArgumentException("余额不足");
        }
        // 扣减余额
        BigDecimal newBalance = balance.subtract(price);
        umsMemberMapper.updateBalance(member.getId(), newBalance);

        // 计算到期时间：使用结束时间为准
        LocalDateTime expireTime = coupon.getUseEndTime();

        MktUserCoupon userCoupon = new MktUserCoupon();
        userCoupon.setCouponId(coupon.getId());
        userCoupon.setMemberId(memberId);
        userCoupon.setStatus(0);
        userCoupon.setObtainType(1);
        userCoupon.setObtainTime(LocalDateTime.now());
        userCoupon.setExpireTime(expireTime);
        mktUserCouponMapper.insert(userCoupon);
        return userCoupon;
    }

    @Override
    @Transactional
    public MktUserCoupon redeem(Long memberId, CouponRedeemDTO dto) {
        MktCouponCode code = mktCouponCodeMapper.selectByCode(dto.getCode());
        if (code == null) {
            throw new IllegalArgumentException("兑换码无效");
        }
        if (code.getUsed() != null && code.getUsed() == 1) {
            throw new IllegalArgumentException("兑换码已被使用");
        }
        if (code.getExpireTime() != null && code.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("兑换码已过期");
        }
        MktCoupon coupon = mktCouponMapper.selectById(code.getCouponId());
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new IllegalArgumentException("优惠券不可用");
        }
        if (coupon.getObtainChannel() != null && coupon.getObtainChannel() == 1) {
            throw new IllegalArgumentException("该优惠券不可通过兑换码领取");
        }
        // 标记兑换码已使用并绑定用户
        int updated = mktCouponCodeMapper.markUsed(code.getId(), memberId);
        if (updated <= 0) {
            throw new IllegalArgumentException("兑换码状态异常，请稍后重试");
        }

        LocalDateTime expireTime = coupon.getUseEndTime();

        MktUserCoupon userCoupon = new MktUserCoupon();
        userCoupon.setCouponId(coupon.getId());
        userCoupon.setMemberId(memberId);
        userCoupon.setStatus(0);
        userCoupon.setObtainType(2);
        userCoupon.setObtainTime(LocalDateTime.now());
        userCoupon.setExpireTime(expireTime);
        mktUserCouponMapper.insert(userCoupon);
        return userCoupon;
    }
}

