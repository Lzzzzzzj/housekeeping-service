package com.example.back.controller.admin;

import com.example.back.common.result.Result;
import com.example.back.dto.DispatchConfigDTO;
import com.example.back.security.UserContext;
import com.example.back.service.DispatchConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 自动派单权重配置
 */
@RestController
@RequestMapping("/api/v1/admin/dispatch")
@RequiredArgsConstructor
public class AdminDispatchConfigController {

    private static final int USER_TYPE_ADMIN = 3;

    private final DispatchConfigService dispatchConfigService;

    private Long requireAdmin() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_ADMIN) {
            throw new IllegalArgumentException("需要管理员权限");
        }
        return userId;
    }

    @GetMapping("/config")
    public Result<DispatchConfigDTO> getConfig() {
        requireAdmin();
        return Result.success(dispatchConfigService.getConfig());
    }

    @PostMapping("/config")
    public Result<Void> saveConfig(@Valid @RequestBody DispatchConfigDTO dto) {
        requireAdmin();
        dispatchConfigService.saveConfig(dto);
        return Result.success();
    }
}
