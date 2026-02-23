package com.example.back.mapper;

import com.example.back.entity.oms.OmsOrderStatusLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OmsOrderStatusLogMapper {

    int insert(OmsOrderStatusLog log);
}
