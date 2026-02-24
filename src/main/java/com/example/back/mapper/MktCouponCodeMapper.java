package com.example.back.mapper;

import com.example.back.entity.mkt.MktCouponCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MktCouponCodeMapper {

    MktCouponCode selectByCode(@Param("code") String code);

    int markUsed(@Param("id") Long id,
                 @Param("boundMemberId") Long boundMemberId);
}

