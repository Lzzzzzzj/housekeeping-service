package com.example.back.mapper;

import com.example.back.entity.oms.OmsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

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

    /**
     * 抢单池/待接订单列表
     */
    List<OmsOrder> selectGrabPool(@Param("status") Integer status);

    /**
     * 更新订单结算信息并将状态置为已完成
     */
    int updateSettleInfo(@Param("id") Long id,
                         @Param("platformAmount") BigDecimal platformAmount,
                         @Param("staffAmount") BigDecimal staffAmount,
                         @Param("settleTime") java.time.LocalDateTime settleTime,
                         @Param("status") Integer status);

    /**
     * 统计某师傅当前进行中订单数（状态 30 待服务、40 服务中、50 待结算）
     */
    int countInProgressByStaffId(@Param("staffId") Long staffId);
}
