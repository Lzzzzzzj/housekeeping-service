package com.example.back.mapper;

import com.example.back.entity.oms.OmsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OmsOrderMapper {

    int insert(OmsOrder order);

    OmsOrder selectById(@Param("id") Long id);

    OmsOrder selectByOrderSn(@Param("orderSn") String orderSn);

    List<OmsOrder> selectByMemberId(@Param("memberId") Long memberId, @Param("status") Integer status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
