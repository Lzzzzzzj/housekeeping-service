package com.example.back.service;

import com.example.back.dto.DispatchConfigDTO;

/**
 * 自动派单配置服务：后台读取/保存派单权重等
 */
public interface DispatchConfigService {

    DispatchConfigDTO getConfig();

    void saveConfig(DispatchConfigDTO dto);
}
