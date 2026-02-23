package com.example.back.mapper;

import com.example.back.entity.ums.UmsMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UmsMemberMapper {

    int insert(UmsMember member);

    UmsMember selectById(@Param("id") Long id);

    UmsMember selectByUserId(@Param("userId") Long userId);
}
