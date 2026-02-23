package com.example.back.mapper;

import com.example.back.entity.ums.UmsStaff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UmsStaffMapper {

    int insert(UmsStaff staff);

    UmsStaff selectById(@Param("id") Long id);

    UmsStaff selectByUserId(@Param("userId") Long userId);
}
