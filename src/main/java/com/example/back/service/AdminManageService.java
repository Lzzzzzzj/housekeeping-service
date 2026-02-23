package com.example.back.service;

import com.example.back.dto.AdminCreateDTO;

/**
 * 管理员账号管理服务（超级管理员创建普通管理员）
 */
public interface AdminManageService {

    /**
     * 创建普通管理员账号
     *
     * @param superAdminUserId 当前登录的超级管理员 user_id
     * @param dto              创建参数
     */
    void createAdmin(Long superAdminUserId, AdminCreateDTO dto);
}

