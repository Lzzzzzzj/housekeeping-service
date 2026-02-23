package com.example.back.service;

import com.example.back.dto.SettleConfigDTO;

/**
 * 结算配置服务：封装读取/保存 settle.mode 等逻辑
 */
public interface SettleConfigService {

    /**
     * 查询当前结算配置
     */
    SettleConfigDTO getConfig();

    /**
     * 保存结算配置（不存在则插入，存在则更新）
     */
    void saveConfig(SettleConfigDTO dto);
}

