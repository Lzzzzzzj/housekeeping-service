package com.example.back.service.impl;

import com.example.back.dto.DispatchConfigDTO;
import com.example.back.entity.sys.SysConfig;
import com.example.back.mapper.SysConfigMapper;
import com.example.back.service.DispatchConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DispatchConfigServiceImpl implements DispatchConfigService {

    private static final String KEY_ENABLE = "dispatch.enable_auto";
    private static final String KEY_WEIGHT_SCORE = "dispatch.weight.score";
    private static final String KEY_WEIGHT_DISTANCE = "dispatch.weight.distance";
    private static final String KEY_WEIGHT_PUNCTUAL = "dispatch.weight.punctual";
    private static final String KEY_RADIUS_KM = "dispatch.radius_km";
    private static final String KEY_MAX_CONCURRENT = "dispatch.max_concurrent_orders";

    private final SysConfigMapper sysConfigMapper;

    @Override
    public DispatchConfigDTO getConfig() {
        DispatchConfigDTO dto = new DispatchConfigDTO();
        dto.setEnableAuto(getBool(KEY_ENABLE, true));
        dto.setWeightScore(getDecimal(KEY_WEIGHT_SCORE, "0.5"));
        dto.setWeightDistance(getDecimal(KEY_WEIGHT_DISTANCE, "0.3"));
        dto.setWeightPunctual(getDecimal(KEY_WEIGHT_PUNCTUAL, "0.2"));
        dto.setRadiusKm(getDecimal(KEY_RADIUS_KM, "5"));
        dto.setMaxConcurrentOrders(getInt(KEY_MAX_CONCURRENT, 3));
        return dto;
    }

    @Override
    @Transactional
    public void saveConfig(DispatchConfigDTO dto) {
        if (dto.getEnableAuto() != null) {
            upsert(KEY_ENABLE, dto.getEnableAuto() ? "true" : "false", "是否启用自动派单");
        }
        if (dto.getWeightScore() != null) {
            upsert(KEY_WEIGHT_SCORE, dto.getWeightScore().toPlainString(), "评分权重");
        }
        if (dto.getWeightDistance() != null) {
            upsert(KEY_WEIGHT_DISTANCE, dto.getWeightDistance().toPlainString(), "距离权重");
        }
        if (dto.getWeightPunctual() != null) {
            upsert(KEY_WEIGHT_PUNCTUAL, dto.getWeightPunctual().toPlainString(), "准时率权重");
        }
        if (dto.getRadiusKm() != null) {
            upsert(KEY_RADIUS_KM, dto.getRadiusKm().toPlainString(), "派单搜索半径(km)");
        }
        if (dto.getMaxConcurrentOrders() != null) {
            if (dto.getMaxConcurrentOrders() < 1 || dto.getMaxConcurrentOrders() > 20) {
                throw new IllegalArgumentException("单师傅最大进行中订单数应在 1~20 之间");
            }
            upsert(KEY_MAX_CONCURRENT, String.valueOf(dto.getMaxConcurrentOrders()), "单师傅最大进行中订单数");
        }
    }

    private boolean getBool(String key, boolean defaultVal) {
        SysConfig c = sysConfigMapper.selectByKey(key);
        if (c == null || c.getConfigValue() == null) return defaultVal;
        return "true".equalsIgnoreCase(c.getConfigValue().trim());
    }

    private BigDecimal getDecimal(String key, String defaultVal) {
        SysConfig c = sysConfigMapper.selectByKey(key);
        if (c == null || c.getConfigValue() == null) return new BigDecimal(defaultVal);
        try {
            return new BigDecimal(c.getConfigValue().trim());
        } catch (NumberFormatException e) {
            return new BigDecimal(defaultVal);
        }
    }

    private int getInt(String key, int defaultVal) {
        SysConfig c = sysConfigMapper.selectByKey(key);
        if (c == null || c.getConfigValue() == null) return defaultVal;
        try {
            return Integer.parseInt(c.getConfigValue().trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private void upsert(String configKey, String configValue, String remark) {
        SysConfig existing = sysConfigMapper.selectByKey(configKey);
        if (existing == null) {
            SysConfig cfg = new SysConfig();
            cfg.setConfigKey(configKey);
            cfg.setConfigValue(configValue);
            cfg.setRemark(remark);
            sysConfigMapper.insert(cfg);
        } else {
            sysConfigMapper.updateValue(configKey, configValue);
        }
    }
}
