package com.example.back.mapper;

import com.example.back.entity.ums.UmsStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface UmsStaffMapper {

    int insert(UmsStaff staff);

    UmsStaff selectById(@Param("id") Long id);

    UmsStaff selectByUserId(@Param("userId") Long userId);

    int updateBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);

    /**
     * 更新自动接单开关
     */
    int updateAutoAccept(@Param("id") Long id, @Param("autoAccept") Integer autoAccept);

    /**
     * 查询开启自动接单的候选师傅（已审核、听单中、auto_accept=1），按评分与接单数排序
     */
    java.util.List<UmsStaff> selectAutoAcceptCandidates(@Param("limit") int limit);
}
