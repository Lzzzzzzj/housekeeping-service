package com.example.back.mapper;

import com.example.back.entity.sys.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysAdminMapper {

    int insert(SysAdmin admin);

    SysAdmin selectById(@Param("id") Long id);

    SysAdmin selectByUserId(@Param("userId") Long userId);
}

