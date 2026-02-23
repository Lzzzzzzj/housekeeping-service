package com.example.back.mapper;

import com.example.back.entity.oms.OmsOrderExtra;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OmsOrderExtraMapper {

    int insert(OmsOrderExtra extra);
}

