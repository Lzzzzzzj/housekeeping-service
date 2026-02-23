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
}
