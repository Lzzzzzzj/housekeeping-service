package com.example.back.mapper;

import com.example.back.entity.mkt.MktUserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MktUserCouponMapper {

    int insert(MktUserCoupon userCoupon);

    MktUserCoupon selectById(@Param("id") Long id);

    /**
     * 用户领取数量统计
     */
    int countByMemberAndCoupon(@Param("memberId") Long memberId,
                               @Param("couponId") Long couponId);

    /**
     * 我的优惠券列表
     */
    List<MktUserCoupon> selectByMember(@Param("memberId") Long memberId,
                                       @Param("status") Integer status);
}

