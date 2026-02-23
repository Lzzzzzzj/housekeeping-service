package com.example.back.service;

import com.example.back.dto.LoginVO;
import com.example.back.dto.UserLoginDTO;
import com.example.back.dto.UserRegisterDTO;

public interface UserAuthService {

    /**
     * 用户注册
     */
    LoginVO register(UserRegisterDTO dto);

    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO dto);
}
