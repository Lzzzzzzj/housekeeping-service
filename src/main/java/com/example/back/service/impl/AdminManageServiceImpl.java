package com.example.back.service.impl;

import com.example.back.dto.AdminCreateDTO;
import com.example.back.entity.sys.SysAdmin;
import com.example.back.entity.sys.SysUser;
import com.example.back.mapper.SysAdminMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.service.AdminManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminManageServiceImpl implements AdminManageService {

    private static final int USER_TYPE_ADMIN = 3;
    private static final int ADMIN_TYPE_NORMAL = 0;
    private static final int ADMIN_TYPE_SUPER = 1;

    private final SysUserMapper sysUserMapper;
    private final SysAdminMapper sysAdminMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void createAdmin(Long superAdminUserId, AdminCreateDTO dto) {
        // 校验当前操作者是否为超级管理员
        SysAdmin operator = sysAdminMapper.selectByUserId(superAdminUserId);
        if (operator == null || operator.getAdminType() == null || operator.getAdminType() != ADMIN_TYPE_SUPER) {
            throw new IllegalArgumentException("仅超级管理员可以创建普通管理员");
        }

        // 检查用户名是否已存在
        if (sysUserMapper.selectByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (sysUserMapper.selectByPhone(dto.getPhone()) != null) {
            throw new IllegalArgumentException("手机号已被使用");
        }

        // 创建 sys_user 记录
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setUserType(USER_TYPE_ADMIN);
        user.setStatus(1);
        sysUserMapper.insert(user);

        // 创建 sys_admin 记录（普通管理员）
        SysAdmin admin = new SysAdmin();
        admin.setUserId(user.getId());
        admin.setAdminType(ADMIN_TYPE_NORMAL);
        admin.setBalance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO);
        admin.setCreatedBy(superAdminUserId);
        sysAdminMapper.insert(admin);
    }
}

