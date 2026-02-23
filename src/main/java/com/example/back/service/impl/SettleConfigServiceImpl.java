package com.example.back.service.impl;

import com.example.back.dto.SettleConfigDTO;
import com.example.back.entity.sys.SysConfig;
import com.example.back.mapper.SysConfigMapper;
import com.example.back.service.SettleConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 结算配置服务实现
 */
@Service
@RequiredArgsConstructor
public class SettleConfigServiceImpl implements SettleConfigService {

    private final SysConfigMapper sysConfigMapper;

    private static final String KEY_MODE = "settle.mode";
    private static final String KEY_FIXED_AMOUNT = "settle.fixed_amount";
    private static final String KEY_PERCENT = "settle.percent";

    @Override
    public SettleConfigDTO getConfig() {
        SettleConfigDTO dto = new SettleConfigDTO();

        SysConfig modeCfg = sysConfigMapper.selectByKey(KEY_MODE);
        dto.setMode(modeCfg != null ? modeCfg.getConfigValue() : "percent");

        SysConfig fixedCfg = sysConfigMapper.selectByKey(KEY_FIXED_AMOUNT);
        if (fixedCfg != null) {
            dto.setFixedAmount(new BigDecimal(fixedCfg.getConfigValue()));
        }

        SysConfig percentCfg = sysConfigMapper.selectByKey(KEY_PERCENT);
        if (percentCfg != null) {
            dto.setPercent(new BigDecimal(percentCfg.getConfigValue()));
        }

        return dto;
    }

    @Override
    @Transactional
    public void saveConfig(SettleConfigDTO dto) {
        String mode = dto.getMode() != null ? dto.getMode().toLowerCase() : "percent";
        if (!"fixed".equals(mode) && !"percent".equals(mode)) {
            throw new IllegalArgumentException("结算模式只能是 fixed 或 percent");
        }

        upsertConfig(KEY_MODE, mode, "结算模式：fixed/percent");

        if ("fixed".equals(mode)) {
            BigDecimal fixed = dto.getFixedAmount() != null ? dto.getFixedAmount() : BigDecimal.ZERO;
            if (fixed.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("固定扣费不能为负数");
            }
            upsertConfig(KEY_FIXED_AMOUNT, fixed.toPlainString(), "固定扣费金额");
        } else {
            BigDecimal percent = dto.getPercent() != null ? dto.getPercent() : new BigDecimal("0.2");
            if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("抽成比例必须在 0~1 之间");
            }
            upsertConfig(KEY_PERCENT, percent.toPlainString(), "抽成比例(0~1)");
        }
    }

    private void upsertConfig(String key, String value, String remark) {
        SysConfig existing = sysConfigMapper.selectByKey(key);
        if (existing == null) {
            SysConfig cfg = new SysConfig();
            cfg.setConfigKey(key);
            cfg.setConfigValue(value);
            cfg.setRemark(remark);
            sysConfigMapper.insert(cfg);
        } else {
            sysConfigMapper.updateValue(key, value);
        }
    }
}

