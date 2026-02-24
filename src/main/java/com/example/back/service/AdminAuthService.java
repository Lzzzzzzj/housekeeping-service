package com.example.back.service;

import com.example.back.dto.AdminLoginVO;
import com.example.back.dto.UserLoginDTO;

/**
 * 管理员认证服务（登录）
 */
public interface AdminAuthService {

    /**
     * 管理员登录
     */
    AdminLoginVO login(UserLoginDTO dto);
}

