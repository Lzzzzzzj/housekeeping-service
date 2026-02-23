package com.example.back.dto;

import lombok.Data;

/**
 * 登录返回信息
 */
@Data
public class LoginVO {
    private String token;
    private Long userId;
    private Long memberId;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer userType;
}
