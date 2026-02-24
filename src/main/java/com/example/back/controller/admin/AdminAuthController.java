package com.example.back.controller.admin;

import com.example.back.common.result.Result;
import com.example.back.dto.AdminLoginVO;
import com.example.back.dto.UserLoginDTO;
import com.example.back.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员认证（登录）
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        AdminLoginVO vo = adminAuthService.login(dto);
        return Result.success(vo);
    }
}

