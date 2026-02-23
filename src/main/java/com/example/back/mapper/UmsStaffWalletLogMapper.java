package com.example.back.mapper;

import com.example.back.entity.ums.UmsStaffWalletLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UmsStaffWalletLogMapper {

    int insert(UmsStaffWalletLog log);
}

