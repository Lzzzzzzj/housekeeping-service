package com.example.back.entity.sys;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统统一用户表
 * user_type: 1-用户, 2-服务人员, 3-管理员
 */
@Data
public class SysUser {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer userType;  // 1-用户, 2-服务人员, 3-管理员
    private String openid;      // 微信小程序OpenID
    private Integer status;     // 1-启用, 0-禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
