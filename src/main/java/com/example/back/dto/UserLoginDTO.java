package com.example.back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {
    @NotBlank(message = "用户名或手机号不能为空")
    private String usernameOrPhone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
