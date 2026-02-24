package com.example.back.service.impl;

import com.example.back.dto.AdminLoginVO;
import com.example.back.dto.UserLoginDTO;
import com.example.back.entity.sys.SysAdmin;
import com.example.back.entity.sys.SysUser;
import com.example.back.mapper.SysAdminMapper;
import com.example.back.mapper.SysUserMapper;
import com.example.back.security.JwtUtil;
import com.example.back.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final int USER_TYPE_ADMIN = 3;

    private final SysUserMapper sysUserMapper;
    private final SysAdminMapper sysAdminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AdminLoginVO login(UserLoginDTO dto) {
        // 根据用户名或手机号查询用户
        SysUser user = sysUserMapper.selectByUsername(dto.getUsernameOrPhone());
        if (user == null) {
            user = sysUserMapper.selectByPhone(dto.getUsernameOrPhone());
        }

        if (user == null) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        if (user.getUserType() == null || user.getUserType() != USER_TYPE_ADMIN) {
            throw new IllegalArgumentException("需要管理员账号");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        // 查询管理员扩展信息
        SysAdmin admin = sysAdminMapper.selectByUserId(user.getId());
        if (admin == null) {
            throw new IllegalArgumentException("管理员信息缺失，请联系系统管理员");
        }

        // 管理员没有 memberId，这里传 null
        String token = jwtUtil.generateToken(user.getId(), null, user.getUserType());

        // 组装返回对象
        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setUserType(user.getUserType());

        AdminLoginVO.SysAdminInfo adminInfo = new AdminLoginVO.SysAdminInfo();
        adminInfo.setId(admin.getId());
        adminInfo.setUserId(admin.getUserId());
        adminInfo.setAdminType(admin.getAdminType());
        adminInfo.setBalance(admin.getBalance());
        adminInfo.setCreatedBy(admin.getCreatedBy());
        vo.setSysAdmin(adminInfo);

        return vo;
    }
}

