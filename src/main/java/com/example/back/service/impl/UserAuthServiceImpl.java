package com.example.back.service.impl;

import com.example.back.dto.LoginVO;
import com.example.back.dto.UserLoginDTO;
import com.example.back.dto.UserRegisterDTO;
import com.example.back.entity.sys.SysUser;
import com.example.back.entity.ums.UmsMember;
import com.example.back.mapper.SysUserMapper;
import com.example.back.mapper.UmsMemberMapper;
import com.example.back.security.JwtUtil;
import com.example.back.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final SysUserMapper sysUserMapper;
    private final UmsMemberMapper umsMemberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final int USER_TYPE_MEMBER = 1; // 用户类型：1-用户

    @Override
    @Transactional
    public LoginVO register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        if (sysUserMapper.selectByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (sysUserMapper.selectByPhone(dto.getPhone()) != null) {
            throw new IllegalArgumentException("手机号已被注册");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setUserType(USER_TYPE_MEMBER);
        user.setStatus(1); // 启用

        sysUserMapper.insert(user);

        // 创建会员信息
        UmsMember member = new UmsMember();
        member.setUserId(user.getId());
        member.setBalance(java.math.BigDecimal.ZERO);
        member.setMemberLevel(0);
        umsMemberMapper.insert(member);

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), member.getId(), user.getUserType());

        // 返回登录信息
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setMemberId(member.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setUserType(user.getUserType());

        return vo;
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        // 根据用户名或手机号查询用户
        SysUser user = sysUserMapper.selectByUsername(dto.getUsernameOrPhone());
        if (user == null) {
            user = sysUserMapper.selectByPhone(dto.getUsernameOrPhone());
        }

        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 查询会员信息
        UmsMember member = umsMemberMapper.selectByUserId(user.getId());
        if (member == null) {
            throw new IllegalArgumentException("用户信息异常，请联系管理员");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), member.getId(), user.getUserType());

        // 返回登录信息
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setMemberId(member.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setUserType(user.getUserType());

        return vo;
    }
}
