package com.example.back.mapper;

import com.example.back.entity.mkt.MktCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MktCouponMapper {

    int insert(MktCoupon coupon);

    int update(MktCoupon coupon);

    MktCoupon selectById(@Param("id") Long id);

    List<MktCoupon> selectAvailableForCenter();

    List<MktCoupon> pageQuery(@Param("status") Integer status,
                              @Param("name") String name,
                              @Param("offset") int offset,
                              @Param("limit") int limit);
}

