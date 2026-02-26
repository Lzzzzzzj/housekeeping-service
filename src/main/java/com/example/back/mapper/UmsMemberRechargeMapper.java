package com.example.back.mapper;

import com.example.back.entity.ums.UmsMemberRecharge;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UmsMemberRechargeMapper {

    int insert(UmsMemberRecharge record);

    UmsMemberRecharge selectByRechargeSn(@Param("rechargeSn") String rechargeSn);

    int updatePaySuccess(@Param("id") Long id,
                         @Param("wechatTransactionId") String wechatTransactionId);

    List<UmsMemberRecharge> selectByMemberId(@Param("memberId") Long memberId);
}

