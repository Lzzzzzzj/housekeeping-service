package com.example.back.mapper;

import com.example.back.entity.oms.OmsOrderExtra;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface OmsOrderExtraMapper {

    int insert(OmsOrderExtra extra);

    /**
     * 汇总某订单下所有已支付增项的金额之和
     */
    BigDecimal sumPaidAmountByOrderId(@Param("orderId") Long orderId);
}

