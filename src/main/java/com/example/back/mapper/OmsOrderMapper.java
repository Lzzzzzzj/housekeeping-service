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

    /**
     * 服务人员接单（抢单）
     * 仅当订单当前状态为指定状态且尚未被任何服务人员接单时才会更新成功
     */
    int grabOrder(@Param("id") Long id,
                  @Param("staffId") Long staffId,
                  @Param("expectedStatus") Integer expectedStatus);
}
