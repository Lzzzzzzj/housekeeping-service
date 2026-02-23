package com.example.back.controller.user;

import com.example.back.common.result.Result;
import com.example.back.dto.LoginVO;
import com.example.back.dto.UserLoginDTO;
import com.example.back.dto.UserRegisterDTO;
import com.example.back.service.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody UserRegisterDTO dto) {
        LoginVO vo = userAuthService.register(dto);
        return Result.success(vo);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        LoginVO vo = userAuthService.login(dto);
        return Result.success(vo);
    }
}
