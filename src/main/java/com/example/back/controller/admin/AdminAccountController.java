package com.example.back.controller.admin;

import com.example.back.common.result.Result;
import com.example.back.dto.AdminCreateDTO;
import com.example.back.security.UserContext;
import com.example.back.service.AdminManageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员账号管理（超级管理员创建普通管理员）
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAccountController {

    private static final int USER_TYPE_ADMIN = 3;

    private final AdminManageService adminManageService;

    /**
     * 超级管理员创建普通管理员账号
     */
    @PostMapping("/admin/create")
    public Result<Void> createAdmin(@Valid @RequestBody AdminCreateDTO dto) {
        Long userId = requireAdmin();
        adminManageService.createAdmin(userId, dto);
        return Result.success();
    }

    private Long requireAdmin() {
        Long userId = UserContext.getUserId();
        Integer userType = UserContext.getUserType();
        if (userId == null || userType == null || userType != USER_TYPE_ADMIN) {
            throw new IllegalArgumentException("需要管理员权限");
        }
        return userId;
    }
}

