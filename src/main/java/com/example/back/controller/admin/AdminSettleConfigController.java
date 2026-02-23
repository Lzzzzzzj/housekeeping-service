package com.example.back.controller.admin;

import com.example.back.common.result.Result;
import com.example.back.dto.SettleConfigDTO;
import com.example.back.security.UserContext;
import com.example.back.service.SettleConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 结算配置管理（平台抽成模式与比例/固定金额）
 */
@RestController
@RequestMapping("/api/v1/admin/settle")
@RequiredArgsConstructor
public class AdminSettleConfigController {

    private static final int USER_TYPE_ADMIN = 3;

    private final SettleConfigService settleConfigService;

    private Long requireAdmin() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_ADMIN) {
            throw new IllegalArgumentException("需要管理员权限");
        }
        return userId;
    }

    /**
     * 获取当前结算配置
     */
    @GetMapping("/config")
    public Result<SettleConfigDTO> getConfig() {
        requireAdmin();
        SettleConfigDTO dto = settleConfigService.getConfig();
        return Result.success(dto);
    }

    /**
     * 保存结算配置（mode + fixed/percent）
     */
    @PostMapping("/config")
    public Result<Void> saveConfig(@Valid @RequestBody SettleConfigDTO dto) {
        requireAdmin();
        settleConfigService.saveConfig(dto);
        return Result.success();
    }
}

