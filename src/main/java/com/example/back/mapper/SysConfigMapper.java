package com.example.back.mapper;

import com.example.back.entity.sys.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysConfigMapper {

    SysConfig selectByKey(@Param("configKey") String configKey);

    int insert(SysConfig config);

    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);
}

