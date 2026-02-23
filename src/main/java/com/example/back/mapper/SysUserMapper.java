package com.example.back.mapper;

import com.example.back.entity.sys.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {

    int insert(SysUser user);

    SysUser selectById(@Param("id") Long id);

    SysUser selectByUsername(@Param("username") String username);

    SysUser selectByPhone(@Param("phone") String phone);

    SysUser selectByOpenid(@Param("openid") String openid);

    int updateUserType(@Param("id") Long id, @Param("userType") Integer userType);
}
